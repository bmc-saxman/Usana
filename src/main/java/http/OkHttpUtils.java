package http;

import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by bchristiansen
 * Class to allow for REST calls using generic methods
 */
public class OkHttpUtils
{
    private OkHttpClient baseClient, client;
    private MediaType intMediaType;
    private Response okHttpResponse;
    private Request request;
    private RequestBody body;
    private int connectTime, readTime, writeTime;

    /**
     * Constructor for the class, creates the client to be used for all requests and sets default timeouts to 65 seconds
     */
    public OkHttpUtils()
    {
        baseClient = new OkHttpClient();
        //Setting this to what we use 90% of the time
        intMediaType = MediaType.parse("application/json");
        connectTime = 65000;
        readTime = 65000;
        writeTime = 65000;

        setTimeouts(connectTime, readTime, writeTime);
    }

    /**
     * Allows you to set the connect, read, and write timeouts for a request in milliseconds.
     * By default timeouts are all set to 10k milliseconds or 10 seconds
     *
     * @param connectTimeInMillis {@link int} value of milliseconds you want before timeout
     * @param readTimeInMillis    {@link int} value of milliseconds you want before timeout
     * @param writeTimeInMillis   {@link int} value of milliseconds you want before timeout
     * @return {@link OkHttpUtils}
     */
    public OkHttpUtils setTimeouts(int connectTimeInMillis, int readTimeInMillis, int writeTimeInMillis)
    {
        connectTime = connectTimeInMillis;
        readTime = readTimeInMillis;
        writeTime = writeTimeInMillis;

        client = baseClient.newBuilder()
            .connectTimeout(connectTime, TimeUnit.MILLISECONDS)
            .readTimeout(readTime, TimeUnit.MILLISECONDS)
            .writeTimeout(writeTime, TimeUnit.MILLISECONDS)
            .build();

        return this;
    }

    /**
     * Makes a post call with no authorization to the URL specified with the payload as the body
     *
     * @param url     full path of the endpoint to post to
     * @param payload String value that is formatted appropriately for the given media type of application/json
     * @return {@link Response}
     * @throws IOException throws if you pass in invalid data
     */
    public Response postRequest(String url, String payload) throws IOException
    {
        body = RequestBody.create(intMediaType, payload);

        request = new Request.Builder()
            .url(url)
            .post(body)
            .build();

        okHttpResponse = client.newCall(request).execute();

        return okHttpResponse;
    }

    /**
     * Private class used for post when no auth is needed, see documentation of method calling this one.
     */
    private Response postRequest(String url, String payload, String mediaType) throws IOException
    {
        intMediaType = MediaType.parse(mediaType);

        body = RequestBody.create(intMediaType, payload);

        request = new Request.Builder()
            .url(url)
            .post(body)
            .build();

        okHttpResponse = client.newCall(request).execute();

        return okHttpResponse;
    }

    /**
     * Post request that allows the user to input the URL, Payload, mediaType, and an authorization. If no authorization is needed
     * simply pass in {@link String} "null"
     *
     * @param url       full path of the endpoint to post to
     * @param payload   String value that is formatted appropriately for the given media type
     * @param mediaType Set the media type, or pass in {@link String} "null" to use default of application/json
     * @param authToken this will be set as a header with the key being authorization, and the value the string passed in
     * @return {@link Response}
     * @throws IOException throws if you pass in invalid data
     */
    public Response postRequest(String url, String payload, String mediaType, String authToken) throws IOException
    {
        if (authToken.equals("null"))
        {
            return postRequest(url, payload, mediaType);
        }
        if (!mediaType.equals("null"))
        {
            intMediaType = MediaType.parse(mediaType);
        }

        body = RequestBody.create(intMediaType, payload);

        request = new Request.Builder()
            .url(url)
            .addHeader("authorization", authToken)
            .post(body)
            .build();

        okHttpResponse = client.newCall(request).execute();

        return okHttpResponse;
    }

    /**
     * put request that allows the user to input the URL, Payload, mediaType, and an authorization.
     *
     * @param url       full path of the endpoint to post to
     * @param payload   String value that is formatted appropriately for the given media type
     * @param mediaType Set the media type, or pass in {@link String} "null" to use default of application/json
     * @param authToken this will be set as a header with the key being authorization, and the value the string passed in
     * @return {@link Response}
     * @throws IOException throws if you pass in invalid data
     */
    public Response putRequest(String url, String payload, String mediaType, String authToken) throws IOException
    {
        if (!mediaType.equals("null"))
        {
            intMediaType = MediaType.parse(mediaType);
        }
        body = RequestBody.create(intMediaType, payload);

        request = new Request.Builder()
            .url(url)
            .addHeader("authorization", authToken)
            .put(body)
            .build();

        okHttpResponse = client.newCall(request).execute();

        return okHttpResponse;
    }

    /**
     * Get request sent to given URL, and authorization head set to passed in token
     *
     * @param url   String value of the URL to send GET to
     * @param token String value of the token for authorization header
     * @return {@link Response}
     * @throws IOException throws if you pass in invalid data
     */
    public Response getRequest(String url, String token) throws IOException
    {
        request = new Request.Builder()
            .url(url)
            .get()
            .addHeader("Cache-Control", "no-cache")
            .addHeader("authorization", token)
            .build();

        okHttpResponse = client.newCall(request).execute();

        return okHttpResponse;
    }

    /**
     * @param url     String value of the URL to send GET to
     * @param headers Headers that may not be the default values.
     * @return {@link Response}
     * @throws IOException Throws if the data is invalid.
     */
    public Response getRequest(String url, HashMap<String, String> headers) throws IOException
    {
        Request.Builder builder = new Request.Builder()
            .url(url)
            .get();

        Iterator it = headers.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry mapElement = (Map.Entry) it.next();
            builder.addHeader(mapElement.getKey().toString(), mapElement.getValue().toString());
        }
        request = builder.build();

        okHttpResponse = client.newCall(request).execute();

        return okHttpResponse;
    }

    /**
     * Delete request sent to given URL, and authorization head set to passed in token
     *
     * @param url   String value of the URL to send DELETE to
     * @param token String value of the token for authorization header
     * @return {@link Response}
     * @throws IOException throws if you pass in invalid data
     */
    public Response deleteRequest(String url, String token) throws IOException
    {
        request = new Request.Builder()
            .url(url)
            .delete()
            .addHeader("Cache-Control", "no-cache")
            .addHeader("authorization", token)
            .build();

        okHttpResponse = client.newCall(request).execute();

        return okHttpResponse;
    }


    /**
     * Delete request sent to given URL, and authorization head set to passed in token
     *
     * @param url   String value of the URL to send DELETE to
     * @param token String value of the token for authorization header
     * @return {@link Response}
     * @throws IOException throws if you pass in invalid data
     */
    public Response deleteRequest(String url, String payload, String token) throws IOException
    {
        body = RequestBody.create(intMediaType, payload);

        request = new Request.Builder()
            .url(url)
            .delete(body)
            .addHeader("Cache-Control", "no-cache")
            .addHeader("authorization", token)
            .build();

        okHttpResponse = client.newCall(request).execute();

        return okHttpResponse;
    }


    /**
     * patchRequest request that allows the user to input the URL, Payload, mediaType, and an authorization.
     *
     * @param url       full path of the endpoint to post to
     * @param payload   String value that is formatted appropriately for the given media type
     * @param mediaType Set the media type, or pass in {@link String} "null" to use default of application/json
     * @param authToken this will be set as a header with the key being authorization, and the value the string passed in
     * @return {@link Response}
     * @throws IOException throws if you pass in invalid data
     */
    public Response patchRequest(String url, String payload, String mediaType, String authToken) throws IOException
    {

        if (!mediaType.equals("null"))
        {
            intMediaType = MediaType.parse(mediaType);
        }

        body = RequestBody.create(intMediaType, payload);

        request = new Request.Builder()
            .url(url)
            .addHeader("authorization", authToken)
            .patch(body)
            .build();

        okHttpResponse = client.newCall(request).execute();

        return okHttpResponse;
    }
}