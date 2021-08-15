/**
 *
 */
package http;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import util.Text;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bchristiansen
 */
@Slf4j
public class HttpUtils
{
    private static final int BUFFER_SIZE = 4096;

    public static class HttpResponse
    {
        @Getter @Setter private int responseCode;
        @Getter @Setter private String responseBody;
        @Getter @Setter private Map<String, List<String>> responseHeaders;
    }

    /**
     * Creates a GET request to send a URL and returns the response code
     *
     * @param url     The REST URL to be sent.
     * @param headers Any headers that may be needed. Such as<br><br>
     *                <b><tt>("Accept", "application/json")</tt></b>,<br>
     *                <b><tt>("Authorization", "Basic QVBJdXNlcjpUYWNvVGltZSMx")</tt></b>,<br>
     *                <b><tt>("Content-Type", "application/json; charset=UTF-8")</tt></b><br><br>
     *                This method does NOT add any headers.
     * @return
     * @throws IOException
     */
    public static String createGetRequest(URL url, HashMap<String, String> headers) throws IOException
    {
        URLConnection conn = null;
        String response = null;
        log.debug("Request: GET " + url.toString());

        try
        {
            conn = url.openConnection();
            // Parse out the headers if there are any.
            for (String key : headers.keySet())
                conn.setRequestProperty(key, headers.get(key));

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null)
            {
                sb.append(line);
            }
            rd.close();
// Commented out because it removes escaped quotes in a JSON object           response = StringEscapeUtils.unescapeJava(sb.toString());
            response = sb.toString();
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        if (conn instanceof HttpURLConnection)
        {
            HttpURLConnection httpConnection = (HttpURLConnection) conn;

            log.debug("Response: {} - {}", httpConnection.getResponseCode(), response);
            if (httpConnection.getResponseCode() >= HttpStatus.SC_PARTIAL_CONTENT)
            {
                try
                {
                    JSONObject error = new JSONObject();
                    error.put("http_status", httpConnection.getResponseCode());
                    error.put("response", response);
                    throw new IOException(error.toString());
                } catch (JSONException ex)
                {
                    ex.printStackTrace();
                }
                //throw new IOException("HTTP Status: " + httpConnection.getResponseCode() + ", Response: " + response);
            }
        } else
            log.debug("Response: {}", response);

        return response;
    }

    /**
     * Creates an HTTP request to send a URL, specific method (e.g. PUT, GET, POST, etc.), extra headers
     * and returns the response code
     *
     * @param url    The URL of the request.
     * @param method GET, POST, PUT, etc.
     * @param json   The request to be made in a json format.
     * @return response {@link String}
     * @throws IOException Throws the exception on certification or connection failures.
     */
    public static String createHttpRequest(URL url, String method, JSONObject json) throws IOException
    {
        String response;
        HttpURLConnection connection;
        BufferedReader rd;
        StringBuilder sb;
        String line;

        // start forming the request string for display (THIS IS NOT USED FOR THE ACTUAL REQUEST)
        String requestString = "Request: " + method + " " + url.toString();

        // instantiate the HttpURLConnection with the URL object - A new
        // connection is opened every time by calling the openConnection
        // method of the protocol handler for this URL.
        connection = (HttpURLConnection) url.openConnection();

        // We're going to send using parameter:method
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestProperty("Accept", "application/json; charset=UTF-8");
        connection.setRequestMethod(method);

        log.debug("The request string: {}", requestString);
        HttpURLConnection.setFollowRedirects(false);
        connection.setInstanceFollowRedirects(false);

        // Write the json string to the output stream.
        OutputStream os = connection.getOutputStream();
        os.write(json.toString().getBytes("UTF-8"));
        os.close();

        // Get the response
        InputStreamReader is;
        try
        {
            is = new InputStreamReader(connection.getInputStream(), "UTF-8");
        } catch (Exception ex)
        {
            try
            {
                is = new InputStreamReader(connection.getErrorStream(), "UTF-8");
            } catch (Exception storeEx)
            {
                log.error("The certification has failed.  Message:" + ex.getMessage());
                throw ex;
            }
        }
        rd = new BufferedReader(is);
        sb = new StringBuilder();

        while ((line = rd.readLine()) != null)
        {
            sb.append(line);
        }
        rd.close();
        response = StringEscapeUtils.unescapeJava(sb.toString());

        log.debug("Response: {} - {}", connection.getResponseCode(), Text.isSet(response) ? response.substring(0, Math.min(response.length(), 255)) : "Empty Response Body.");
        if (connection.getResponseCode() >= HttpStatus.SC_PARTIAL_CONTENT)
        {
            try
            {
                JSONObject error = new JSONObject();
                error.put("http_status", connection.getResponseCode());
                error.put("response", response);
                throw new IOException(error.toString());
            } catch (JSONException ex)
            {
                ex.printStackTrace();
            }
        }

        return response;
    }


    public static Map createHttpRequest(URL url, String method, HashMap<String, String> headers, JSONObject json) throws IOException, JSONException
    {
        String response;
        HttpURLConnection connection;
        BufferedReader rd;
        StringBuilder sb;
        String line;

        try
        {
            Field methodsField = HttpURLConnection.class.getDeclaredField("methods");
            methodsField.setAccessible(true);
            // get the methods field modifiers
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            // bypass the "private" modifier
            modifiersField.setAccessible(true);

            // remove the "final" modifier
            modifiersField.setInt(methodsField, methodsField.getModifiers() & ~Modifier.FINAL);

         /* valid HTTP methods */
            String[] methods = {
                "GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE", "PATCH"
            };
            // set the new methods - including patch
            methodsField.set(null, methods);

        } catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException e)
        {
            e.printStackTrace();
        }

        // start forming the request string for display (THIS IS NOT USED FOR THE ACTUAL REQUEST)
        String requestString = "Request: " + method + " " + url.toString();

        // instantiate the HttpURLConnection with the URL object - A new
        // connection is opened every time by calling the openConnection
        // method of the protocol handler for this URL.
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestProperty("Accept", "application/json; charset=UTF-8");
        connection.setRequestMethod(method);

        //add the headers to the request
        if (headers != null)
        {
            for (String key : headers.keySet())
            {
                requestString += " " + headers.get(key);
                connection.setRequestProperty(key, headers.get(key));
            }
        }

        log.debug("The request string: {}", requestString);
        HttpURLConnection.setFollowRedirects(false);
        connection.setInstanceFollowRedirects(false);

        if (json != null)
        {
            // Write the json string to the output stream.
            OutputStream os = connection.getOutputStream();
            os.write(json.toString().getBytes("UTF-8"));
            os.close();
        }


        // Get the response
        InputStreamReader is;
        try
        {
            is = new InputStreamReader(connection.getInputStream(), "UTF-8");
        } catch (Exception ex)
        {
            try
            {
                is = new InputStreamReader(connection.getErrorStream(), "UTF-8");
            } catch (Exception storeEx)
            {
                log.error("The certification has failed.  Message:" + ex.getMessage());
                throw ex;
            }
        }
        rd = new BufferedReader(is);
        sb = new StringBuilder();

        while ((line = rd.readLine()) != null)
        {
            sb.append(line);
        }
        rd.close();
        response = sb.toString();

        log.debug("Response: {} - {}", connection.getResponseCode(), Text.isSet(response) ? response.substring(0, Math.min(response.length(), 255)) : "Empty Response Body.");
        Map m = new HashMap();
        m.put("status", connection.getResponseCode());
        if (response.startsWith("["))
        {
            m.put("data", new JSONArray(response));
        } else if (response.startsWith("{"))
        {
            m.put("data", new JSONObject(response));
        } else
        {
            m.put("data", response);
        }

        m.put("raw", response);
        return m;
    }

    public static Map createHttpRequest(URL url, String method, HashMap<String, String> headers, JSONArray json) throws IOException, JSONException
    {
        String response;
        HttpURLConnection connection;
        BufferedReader rd;
        StringBuilder sb;
        String line;

        try
        {
            Field methodsField = HttpURLConnection.class.getDeclaredField("methods");
            methodsField.setAccessible(true);
            // get the methods field modifiers
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            // bypass the "private" modifier
            modifiersField.setAccessible(true);

            // remove the "final" modifier
            modifiersField.setInt(methodsField, methodsField.getModifiers() & ~Modifier.FINAL);

         /* valid HTTP methods */
            String[] methods = {
                "GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE", "PATCH"
            };
            // set the new methods - including patch
            methodsField.set(null, methods);

        } catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException e)
        {
            e.printStackTrace();
        }

        // start forming the request string for display (THIS IS NOT USED FOR THE ACTUAL REQUEST)
        String requestString = "Request: " + method + " " + url.toString();

        // instantiate the HttpURLConnection with the URL object - A new
        // connection is opened every time by calling the openConnection
        // method of the protocol handler for this URL.
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestProperty("Accept", "application/json; charset=UTF-8");
        connection.setRequestMethod(method);
        //add the headers to the request
        for (String key : headers.keySet())
        {
            requestString += " " + headers.get(key);
            connection.setRequestProperty(key, headers.get(key));
        }

        log.debug(requestString);

        log.debug("The request string: {}", requestString);
        HttpURLConnection.setFollowRedirects(false);
        connection.setInstanceFollowRedirects(false);

        // Write the json string to the output stream.
        OutputStream os = connection.getOutputStream();
        os.write(json.toString().getBytes("UTF-8"));
        os.close();

        // Get the response
        InputStreamReader is;
        try
        {
            is = new InputStreamReader(connection.getInputStream(), "UTF-8");
        } catch (Exception ex)
        {
            try
            {
                is = new InputStreamReader(connection.getErrorStream(), "UTF-8");
            } catch (Exception storeEx)
            {
                log.error("The certification has failed.  Message:" + ex.getMessage());
                throw ex;
            }
        }
        rd = new BufferedReader(is);
        sb = new StringBuilder();

        while ((line = rd.readLine()) != null)
        {
            sb.append(line);
        }
        rd.close();
        response = StringEscapeUtils.unescapeJava(sb.toString());

        log.debug("Response: {} - {}", connection.getResponseCode(), Text.isSet(response) ? response.substring(0, Math.min(response.length(), 255)) : "Empty Response Body.");
        Map m = new HashMap();
        m.put("status", connection.getResponseCode());
        if (response.startsWith("["))
        {
            m.put("data", new JSONArray(response));
        } else if (response.startsWith("{"))
        {
            m.put("data", new JSONObject(response));
        } else
        {
            m.put("data", response);
        }

        m.put("raw", response);
        return m;
    }

    /**
     * Creates an HTTP request to send a URL, specific method (e.g. PUT, GET, POST, etc.), extra headers
     * and returns the response code
     *
     * @param url     The URL of the request.
     * @param method  GET, POST, PUT, etc.
     * @param headers Any header (key, value) pairs that are needed, such as<br><br>
     *                <b><tt>("Accept", "application/json")</tt></b>,<br>
     *                <b><tt>("Authorization", "Basic QVBJdXNlcjpUYWNvVGltZSMx")</tt></b>,<br>
     *                <b><tt>("Content-Type", "application/json; charset=UTF-8")</tt></b><br><br>
     *                This method does NOT add any headers.
     * @return response
     * @throws IOException Throws an IOException.
     */
    public static String createHttpRequest(URL url, String method, HashMap<String, String> headers) throws IOException
    {
        String response;
        HttpURLConnection connection;
        BufferedReader rd;
        StringBuilder sb;
        String line;

        // start forming the request string for display (THIS IS NOT USED FOR THE ACTUAL REQUEST)
        String requestString = "Request: " + method + " " + url.toString();

        // instantiate the HttpURLConnection with the URL object - A new
        // connection is opened every time by calling the openConnection
        // method of the protocol handler for this URL.
        connection = (HttpURLConnection) url.openConnection();

        // We're going to send using parameter:method
        connection.setRequestMethod(method);
        //add the headers to the request
        for (String key : headers.keySet())
        {
            requestString += " " + headers.get(key);
            connection.setRequestProperty(key, headers.get(key));
        }
        //connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        log.debug(requestString);
        HttpURLConnection.setFollowRedirects(false);
        connection.setInstanceFollowRedirects(false);

        // Get the response
        InputStreamReader is = null;
        try
        {
            is = new InputStreamReader(connection.getInputStream(), "UTF-8");
        } catch (Exception ex)
        {
            try
            {
                is = new InputStreamReader(connection.getErrorStream(), "UTF-8");
            } catch (Exception storeEx)
            {
                log.error("The certification has failed.  Message:" + ex.getMessage());
                throw ex;
            }
        }
        rd = new BufferedReader(is);
        sb = new StringBuilder();

        while ((line = rd.readLine()) != null)
        {
            sb.append(line);
        }
        rd.close();
        response = sb.toString();

        log.debug("Response: {} - {}", connection.getResponseCode(), Text.isSet(response) ? response.substring(0, Math.min(response.length(), 255)) : "Empty Response Body.");
        if (connection.getResponseCode() >= HttpStatus.SC_PARTIAL_CONTENT)
        {
            try
            {
                JSONObject error = new JSONObject();
                error.put("http_status", connection.getResponseCode());
                error.put("response", response);
                throw new IOException(error.toString());
            } catch (JSONException ex)
            {
                ex.printStackTrace();
            }
        }

        return response;
    }

    /**
     * Creates an HTTP request to send a URL, specific method (e.g. PUT, POST, etc.), extra headers, and url parameters
     * and returns the response code
     *
     * @param url           The URL of the request.
     * @param method        GET, POST, PUT, etc.
     * @param headers       Any header (key, value) pairs that are needed, such as<br><br>
     *                      <b><tt>("Accept", "application/json")</tt></b>,<br>
     *                      <b><tt>("Authorization", "Basic QVBJdXNlcjpUYWNvVGltZSMx")</tt></b>,<br>
     *                      <b><tt>("Content-Type", "application/json; charset=UTF-8")</tt></b><br><br>
     *                      This method does NOT add any headers.
     * @param payload The parameters to be included in the url of the request or post body(e.g. param1=data&param2=data...)
     * @return response
     * @throws IOException Throws an IOException.
     */
    public static HttpResponse createHttpRequest(URL url, String method, HashMap<String, String> headers, String payload) throws IOException
    {
        HttpURLConnection connection;
        BufferedReader rd;
        StringBuilder sb;
        String line;

        // start forming the request string for display (THIS IS NOT USED FOR THE ACTUAL REQUEST)
        String requestString = "Request: " + method + " " + url.toString();

        // instantiate the HttpURLConnection with the URL object - A new
        // connection is opened every time by calling the openConnection
        // method of the protocol handler for this URL.
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setUseCaches(false);

        // We're going to send using parameter:method
        connection.setRequestMethod(method);

        //add the headers to the request
        if (headers != null)
        {
            for (String key : headers.keySet())
            {
                requestString += " " + key + ": " + headers.get(key);
                connection.setRequestProperty(key, headers.get(key));
            }
        }

        log.debug(requestString);

        HttpURLConnection.setFollowRedirects(false);
        connection.setInstanceFollowRedirects(false);

        if (payload != null)
        {
            //get the data from the url parameters
            byte[] postData = payload.getBytes(StandardCharsets.UTF_8);

            //Find and set the content length header
            int postDataLength = postData.length;
            connection.setRequestProperty("Content-Length", Integer.toString(postDataLength));

            //Write the data
            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream()))
            {
                wr.write(postData);
            }
        }

        // Get the responseBody
        InputStreamReader is = null;
        try
        {
            is = new InputStreamReader(connection.getInputStream(), "UTF-8");
        } catch (Exception ex)
        {
            try
            {
                is = new InputStreamReader(connection.getErrorStream(), "UTF-8");
            } catch (Exception storeEx)
            {
                log.error("The certification has failed.  Message:" + ex.getMessage());
                throw ex;
            }
        }
        rd = new BufferedReader(is);
        sb = new StringBuilder();

        while ((line = rd.readLine()) != null)
        {
            sb.append(line);
        }
        rd.close();
        int responseCode = connection.getResponseCode();
        String responseBody = StringEscapeUtils.unescapeJava(sb.toString());

        log.debug("Response: {} - {}", connection.getResponseCode(), Text.isSet(responseBody) ? responseBody.substring(0, Math.min(responseBody.length(), 255)) : "Empty Response Body.");
        if (connection.getResponseCode() >= HttpStatus.SC_PARTIAL_CONTENT)
        {
            HashMap<String, Object > error = new HashMap<>();
            error.put("responseCode", responseCode);
            error.put("responseBody", responseBody);
            throw new IOException(error.toString());
        }
        Map<String, List<String>> responseHeaders = connection.getHeaderFields();

        HttpResponse response = new HttpResponse();
        response.setResponseCode(responseCode);
        response.setResponseBody(responseBody);
        response.setResponseHeaders(responseHeaders);

        return response;
    }

    /**
     * Given a URL string, download a file to the specified location.
     *
     * @param fileURL The URL string pointing to your file.
     * @param saveDir The folder to where the file is to be saved.
     * @param headers Any header (key, value) pairs that are needed, such as<br><br>
     *                <b><tt>("Accept", "application/json")</tt></b>,<br>
     *                <b><tt>("Authorization", "Basic QVBJdXNlcjpUYWNvVGltZSMx")</tt></b>,<br>
     *                <b><tt>("Content-Type", "application/json; charset=UTF-8")</tt></b><br><br>
     *                This method does NOT add any headers.
     * @throws IOException Throw an exception on failure.
     */
    public static void downloadFile(String fileURL, String saveDir, HashMap<String, String> headers) throws IOException
    {
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();

        // Parse out the headers if there are any.
        for (String key : headers.keySet())
            httpConn.setRequestProperty(key, headers.get(key));
        int responseCode = httpConn.getResponseCode();

        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK)
        {
            String fileName = "";
            String disposition = httpConn.getHeaderField("Content-Disposition");
            String contentType = httpConn.getContentType();
            int contentLength = httpConn.getContentLength();

            if (disposition != null)
            {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0)
                {
                    fileName = disposition.substring(index + 10,
                        disposition.length() - 1);
                }
            } else
            {
                // extracts file name from URL
                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                    fileURL.length());
            }

            log.debug("Content-Type = " + contentType);
            log.debug("Content-Disposition = " + disposition);
            log.debug("Content-Length = " + contentLength);
            log.debug("fileName = " + fileName);

            // opens input stream from the HTTP connection
            InputStream raw = httpConn.getInputStream();
            InputStream inputStream = new BufferedInputStream(raw);
            String saveFilePath = saveDir + File.separator + fileName;

            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);

            int bytesRead;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1)
            {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            log.debug("File downloaded");
        } else
        {
            log.error("No file to download. Server replied HTTP code: " + responseCode);
            throw new IOException("The file was not downloaded.");
        }
        httpConn.disconnect();
    }

    /**
     * Used to simply build a REST URL.
     *
     * @param baseUrl    The base URL for the request.
     * @param function   What function to be used, GET, POST, PUT, etc.
     * @param parameters Any additional parameters that might be needed.
     * @return {@link URL}
     */
    public static URL getRestUrl(String baseUrl, String function, String[] parameters)
    {
        URL url = null;
        StringBuilder sb = new StringBuilder();

        // Create the URL for the Rest HTTP request
        sb.append(baseUrl);
        if (Text.isSet(function))
        {
            sb.append("/");
            sb.append(function);
        }

        // It IS valid to have 0 parameters
        if (parameters.length > 0)
        {
            for (String s : parameters)
            {
                if (Text.isSet(s))
                {
                    sb.append("/");
                    sb.append(s);
                }
            }
        }

        try
        {
            url = new URL(sb.toString());
        } catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Use to build the RESTful string with a query string notation, such as the following <br/>
     * <tt>/v1/company/:company_id/scores?page=:page&pagesize=:pagesize</tt>
     *
     * @param baseUrl    The base URL for the request.
     * @param function   What function to be used, GET, POST, PUT, etc.
     * @param parameters Any additional parameters that might be needed.
     * @param resources  Additional headers if needed.
     * @return {@link URL}
     */
    public static URL getRestUrlQuery(String baseUrl, String function, String[] parameters, HashMap<String, String> resources)
    {
        URL url = null;
        StringBuilder sb = new StringBuilder();

        // Create the URL for the Rest HTTP request
        sb.append(baseUrl);
        if (Text.isSet(function))
        {
            sb.append("/");
            sb.append(function);
        }

        // It IS valid to have 0 parameters
        if (parameters.length > 0)
        {
            for (String s : parameters)
            {
                if (Text.isSet(s))
                {
                    sb.append("/");
                    sb.append(s);
                }
            }
        }

        // It IS valid to have 0 parameters
        if (resources.size() > 0)
        {
            int count = 0;
            sb.append("?");
            for (String key : resources.keySet())
            {
                count++;
                if (Text.isSet(resources.get(key)))
                {
                    sb.append(key);
                    sb.append("=");
                    sb.append(resources.get(key));

                    // Make sure that the & is not added at the end of the string.
                    if (count < resources.size())
                        sb.append("&");
                }
            }
        }

        try
        {
            url = new URL(sb.toString().replace(" ", "%20"));
        } catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

        return url;
    }

    public enum RequestMethods
    {
        PUT("PUT"), GET("GET"), POST("POST"), DELETE("DELETE"), OPTIONS("OPTIONS"), PATCH("PATCH");

        private String value;

        private RequestMethods(String value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return this.value;
        }
    }
}
