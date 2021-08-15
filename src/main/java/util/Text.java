package util;

import java.awt.*;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Utilities for handling text and random numbers.
 *
 * @author bchristiansen
 */
public class Text
{
    /**
     * Random number generator
     */
    public static final Random ranGen = new Random();

    /**
     * Simple date format that returns the date in MM/dd/yyyy.
     */
    public static final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

    /**
     * A list of the names of the common ratings in the LMP.
     */
    private static final String[] ratings = {"", "Hot", "Warm", "Cold"};

    /**
     * Simple list of all 50 states. The first entry is needed because we
     * chose not to return a 0 in our integer randomizer routine.
     */
    private static final String[] allStates =
        {
            "", "Alabama", "Alaska", "Arizona", "Arkansas", "California",
            "Colorado", "Connecticut", "Delaware", "Florida", "Georgia",
            "Hawaii", "Idaho", "Illinois", "Indiana", "Iowa",
            "Kansas", "Kentucky", "Louisiana", "Maine", "Maryland",
            "Massachusetts", "Michigan", "Minnesota", "Mississippi", "Missouri",
            "Montana", "Nebraska", "Nevada", "New Hampshire", "New Jersey",
            "New Mexico", "New York", "North Carolina", "North Dakota", "Ohio",
            "Oklahoma", "Oregon", "Pennsylvania", "Rhode Island", "South Carolina",
            "South Dakota", "Tennessee", "Texas", "Utah", "Vermont",
            "Virginia", "Washington", "West Virginia", "Wisconsin", "Wyoming"
        };

    private static final int[] validAreaCodes =
        {
            201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 212, 213, 214, 215, 216, 217,
            218, 219, 224, 225, 228, 229, 231, 234, 236, 239, 240, 242, 246, 248, 250, 251,
            252, 253, 254, 256, 260, 262, 264, 267, 268, 269, 270, 276, 278, 281, 283, 284,
            289, 301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 312, 313, 314, 315, 316,
            317, 318, 319, 320, 321, 323, 325, 330, 331, 334, 336, 337, 339, 340, 341, 345,
            347, 351, 352, 360, 361, 369, 380, 385, 386, 401, 402, 403, 404, 405, 406, 407,
            408, 409, 410, 412, 413, 414, 415, 416, 417, 418, 419, 423, 424, 425, 430, 432,
            434, 435, 440, 441, 442, 443, 450, 456, 464, 469, 470, 473, 475, 478, 479, 480,
            484, 501, 502, 503, 504, 505, 506, 507, 508, 509, 510, 512, 513, 514, 515, 516,
            517, 518, 519, 520, 530, 540, 541, 551, 557, 559, 561, 562, 563, 564, 567, 570,
            571, 573, 574, 575, 580, 581, 585, 586, 601, 602, 603, 604, 605, 606, 607, 608,
            609, 610, 612, 613, 614, 615, 616, 617, 618, 619, 620, 623, 626, 627, 628, 630,
            631, 636, 641, 646, 647, 649, 650, 651, 660, 661, 662, 664, 669, 670, 671, 678,
            679, 682, 689, 701, 702, 703, 704, 705, 706, 707, 708, 709, 710, 712, 713, 714,
            715, 716, 717, 718, 719, 720, 724, 727, 731, 732, 734, 737, 740, 747, 754, 757,
            758, 760, 762, 736, 763, 764, 765, 767, 769, 770, 772, 773, 774, 775, 778, 779,
            780, 781, 784, 785, 786, 787, 801, 802, 803, 804, 805, 806, 807, 808, 809, 810,
            812, 813, 814, 815, 816, 817, 818, 819, 828, 830, 831, 832, 835, 841, 843, 845,
            847, 848, 850, 855, 856, 857, 858, 859, 860, 862, 863, 864, 865, 867, 868, 869,
            870, 872, 876, 878, 880, 881, 882, 901, 902, 903, 904, 905, 906, 907, 908, 909,
            910, 912, 913, 914, 915, 916, 917, 918, 919, 920, 925, 927, 928, 931, 935, 936,
            937, 939, 940, 941, 947, 949, 951, 952, 954, 956, 957, 959, 970, 971, 972, 973,
            975, 978, 979, 980, 984, 985, 989
        };

    /**
     * Returns the state abbreviation as long as it is in the map
     *
     * @param state The full name of the state.
     * @return {@link String} The abbreviated value for the the state.
     */
    public static String getStateAbbreviation(String state)
    {
        HashMap<String, String> states = new HashMap<String, String>();
        states.put("Alabama", "AL");
        states.put("Alaska", "AK");
        states.put("Alberta", "AB");
        states.put("American Samoa", "AS");
        states.put("Arizona", "AZ");
        states.put("Arkansas", "AR");
        states.put("Armed Forces (AE)", "AE");
        states.put("Armed Forces Americas", "AA");
        states.put("Armed Forces Pacific", "AP");
        states.put("British Columbia", "BC");
        states.put("California", "CA");
        states.put("Colorado", "CO");
        states.put("Connecticut", "CT");
        states.put("Delaware", "DE");
        states.put("District Of Columbia", "DC");
        states.put("Florida", "FL");
        states.put("Georgia", "GA");
        states.put("Guam", "GU");
        states.put("Hawaii", "HI");
        states.put("Idaho", "ID");
        states.put("Illinois", "IL");
        states.put("Indiana", "IN");
        states.put("Iowa", "IA");
        states.put("Kansas", "KS");
        states.put("Kentucky", "KY");
        states.put("Louisiana", "LA");
        states.put("Maine", "ME");
        states.put("Manitoba", "MB");
        states.put("Maryland", "MD");
        states.put("Massachusetts", "MA");
        states.put("Michigan", "MI");
        states.put("Minnesota", "MN");
        states.put("Mississippi", "MS");
        states.put("Missouri", "MO");
        states.put("Montana", "MT");
        states.put("Nebraska", "NE");
        states.put("Nevada", "NV");
        states.put("New Brunswick", "NB");
        states.put("New Hampshire", "NH");
        states.put("New Jersey", "NJ");
        states.put("New Mexico", "NM");
        states.put("New York", "NY");
        states.put("Newfoundland", "NF");
        states.put("North Carolina", "NC");
        states.put("North Dakota", "ND");
        states.put("Northwest Territories", "NT");
        states.put("Nova Scotia", "NS");
        states.put("Nunavut", "NU");
        states.put("Ohio", "OH");
        states.put("Oklahoma", "OK");
        states.put("Ontario", "ON");
        states.put("Oregon", "OR");
        states.put("Pennsylvania", "PA");
        states.put("Prince Edward Island", "PE");
        states.put("Puerto Rico", "PR");
        states.put("Quebec", "PQ");
        states.put("Rhode Island", "RI");
        states.put("Saskatchewan", "SK");
        states.put("South Carolina", "SC");
        states.put("South Dakota", "SD");
        states.put("Tennessee", "TN");
        states.put("Texas", "TX");
        states.put("Utah", "UT");
        states.put("Vermont", "VT");
        states.put("Virgin Islands", "VI");
        states.put("Virginia", "VA");
        states.put("Washington", "WA");
        states.put("West Virginia", "WV");
        states.put("Wisconsin", "WI");
        states.put("Wyoming", "WY");
        states.put("Yukon Territory", "YT");

        return states.get(state);
    }

    /**
     * Do a case insensitive search for the subString in the body.
     * @param body The primary string to be searched.
     * @param subString The secondary string being looked for in the primary string.
     * @return {@link Boolean}
     */
    public static boolean contains(String body, String subString)
    {
        return Pattern.compile(Pattern.quote(subString), Pattern.CASE_INSENSITIVE).matcher(body).find();
    }

    /**
     * Takes a Java Color object and returns it as a hex color string, i.e. #33537A.
     *
     * @param value - A Color value.
     * @return - The Color converted to a String in hexadecimal, including the pound character
     * at the beginning of the String.
     */
    public static String colorToHex(Color value)
    {
        String color = String.format("#%2s%2s%2s",
            Integer.toHexString(value.getRed()),
            Integer.toHexString(value.getGreen()),
            Integer.toHexString(value.getBlue())).replaceAll(" ", "0");

        return color;
    }

    /**
     * Replaces all occurrences of ampersand with &amp;.<br>
     * Does not re-encode ampersands that have all ready been encoded.
     *
     * @param string The string to encode the ampersands of.
     * @return The string with the ampersands encoded.
     */
    public static String encodeAmpersands(String string)
    {
        return string.replaceAll("&(?!amp;)", "&amp;");
    }

    /**
     * Find a substring in a larger string that is a case-insensitive search.
     * @param source The larger string containing the wanted string
     * @param wanted The smaller string being searched for.
     * @return {@link Boolean}
     */
    public static boolean findSubstring(String source, String wanted)
    {
        return Pattern.compile(
            Pattern.quote(wanted),
            Pattern.CASE_INSENSITIVE).matcher(source).find();
    }

    /**
     * Formats the phone number in the following format: (xxx) xxx-xxxx
     *
     * @param number The un-formated phone number.
     * @return The phone number formated as (xxx) xxx-xxxx.
     */
    public static String formatPhoneNumber(String number)
    {
        return (number.replaceFirst("(\\d{3})(\\d{3})(\\d+)", "($1) $2-$3"));
    }

    /**
     * Formats the phone number in the following format: (xxx)xxx-xxxx<br>
     * (Note the lack of a space between the closing parentheses and the next number.)
     *
     * @param number The un-formated phone number.
     * @return The phone number formated as (xxx)xxx-xxxx.
     */
    public static String formatSFPhoneNumber(String number)
    {
        return (number.replaceFirst("(\\d{3})(\\d{3})(\\d+)", "($1)$2-$3"));
    }

    /**
     * Formats the phone number in the following format: +1 xxx-xxx-xxxx<br>
     * @param number The un-formated phone number.
     * @return The phone number formated as xxx-xxx-xxxx.
     */
    public static String formatPBPhoneNumber(String number)
    {
        String thanksPB = (number.replaceFirst("(\\d{3})(\\d{3})(\\d+)", "$1-$2-$3"));

        return "+1 " + thanksPB;
    }

    /**
     * Takes a number String and formats it to a currency String.
     *
     * @param number The number string to format.
     * @return A String formatted to currency. It doesn't set any locale information
     * so the return value is based in the system's current locale.
     */
    public static String formatToCurrency(String number)
    {
        NumberFormat defaultFormat = NumberFormat.getCurrencyInstance();
        double numericValue = Double.parseDouble(number);
        return (defaultFormat.format(numericValue));
    }

    /**
     * Returns the current date on the system.
     *
     * @return The current date in a MM/dd/yyyy format.
     */
    public static String getCurrentDate()
    {
        Calendar currentDate = Calendar.getInstance();
        String dateNow = dateFormat.format(currentDate.getTime());
        return dateNow;
    }

    public static int getRandomAreaCode()
    {
        return validAreaCodes[ranGen.nextInt(validAreaCodes.length)];
    }

    /**
     * Gives a random date from the <tt>(currentYear - 1)</tt> to <tt>(currentYear + 1)</tt>.
     *
     * @return A random date in MM/dd/yyyy format.
     */
    public static String getRandomDate()
    {
        Calendar currentDate = Calendar.getInstance();
        String dateNow = new SimpleDateFormat("yyyy", Locale.getDefault()).format(currentDate.getTime());

        int year = getRandomInteger(Integer.valueOf(dateNow) - 1, Integer.valueOf(dateNow) + 1);
        int month = getRandomInteger(0, 11);

        GregorianCalendar gc = new GregorianCalendar(year, month, 1);
        int day = getRandomInteger(1, gc.getActualMaximum(Calendar.DAY_OF_MONTH));

        gc.set(year, month, day);

        return dateFormat.format(gc.getTime());
    }

    /**
     * Generates an email for sdk.spc+<tt>&#35;&#35;&#35;</tt>@gmail.com with a random 3 digit number. We can't
     * used bogus email addresses because of the problems we're having with bounced emails.
     * So the address will no longer be random, but will use our old testing emails address.
     *
     * @return A random email address. With the format of sdk.spc+<tt>&#35;&#35;&#35;</tt>@gmail.com
     */
    public static String getRandomEmail()
    {
        // Setup a random email.
        int ranInt = getRandomInteger(1000);
        return "sdk.spc+" + ranInt + "@gmail.com";
//        return "e" + ranGen.nextInt(10000) + "@" + ranGen.nextInt(100000) + ".com";
    }

    /**
     * The nextInt(int n) method is used to get a pseudorandom, uniformly
     * distributed int value between 1 (inclusive) and the specified value
     * (exclusive), drawn from this random number generator's sequence.
     *
     * @param range Determines the values returned: between 1 and range - 1.
     * @return A random number between and including 1 and range - 1.
     */
    public static int getRandomInteger(int range)
    {
        return ranGen.nextInt(range - 1) + 1;
    }

    /**
     * Generate a random number given the minimum and maximum
     *
     * @param min The minimum number for the randomization.
     * @param max The maximum number for the randomization.
     * @return random number
     */
    public static int getRandomInteger(int min, int max)
    {
        int randomNum;

        if (min == max)
            randomNum = min;
        else if (min > max)
            randomNum = -1;
        else
            randomNum = min + (int) (Math.random() * ((max - min) + 1));

        return randomNum;
    }

    /**
     * Generate a random percentage between 0 and 100.
     *
     * @return A random number as a String between 0 and 100 with 1 decimal place of precision.
     */
    public static String getRandomPercent()
    {
        double d = ranGen.nextFloat() * 100;
        String percent = String.format("%3.1f", d);
        return percent;
    }

    /**
     * Generating phone numbers is common enough in the automation that it is useful
     * to have some helper methods. This one just generates the 10 digit number.
     *
     * @return A String with a random 10 digit number that can be formatted into a
     * phone number. Please see the method formatPhoneNumber for setting the 10 digit
     * number into a standard format.
     */
    public static String getRandomPhoneNumber()
    {
        // Select an area code from the list of valid area codes.
        int areaCode = getRandomAreaCode();
        long number = (long) Math.floor(Math.random() * 9000000L) + 1000000L;

        return areaCode + String.valueOf(number);
    }

    /**
     * Create a random sublist of an ArrayList
     *
     * @param fromList   The list to choose values from
     * @param d          The probability any given element will be selected (on the first pass)
     * @param atLeastOne If true, add at least one element if none are selected on the first pass
     * @return {@link ArrayList} An ArrayList with entries chosen randomly from fromList
     */
    public static ArrayList<String> getRandomSubList(ArrayList<String> fromList, double d, boolean atLeastOne)
    {
        ArrayList<String> randomList = new ArrayList<String>();
        String chosenElement;

        if (fromList.isEmpty())
        {
            return randomList; // Empty
        }

        for (String str : fromList)
        {
            if (ranGen.nextFloat() < d)
            {
                randomList.add(str);
            }
        }

        if (atLeastOne && randomList.isEmpty())
        {
            chosenElement = fromList.get(ranGen.nextInt(fromList.size()));
            randomList.add(chosenElement);
        }

        return randomList;
    }

    /**
     * Get a random rating from the standard rating list.
     *
     * @return A random rating. Values are "Hot", "Warm" and "Cold.
     */
    public static String getRandomRating()
    {
        int length = ratings.length;
        String rating = ratings[getRandomInteger(length)];
        return rating;
    }

    /**
     * Get a random U.S. state name.
     *
     * @return A random U.S. state.
     */
    public static String getRandomState()
    {
        int length = allStates.length;
        String state = allStates[getRandomInteger(length)];
        return state;
    }

    /**
     * Generates a random name of the text passed in plus the textLength.
     *
     * @param name       The beginning part of the string.
     * @param textLength The number of characters to add onto the end of name.
     * @return A string with random characters added to the end.
     */
    public static String getRandomText(String name, int textLength)
    {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder text = new StringBuilder(name);

        for (int i = 0; i < textLength; i++)
        {
            text.append(characters.charAt(ranGen.nextInt(characters.length())));
        }

        return text.toString();
    }

    /**
     * Generates a simple URL in the form: "http://www.randomText.com"
     *
     * @return A random URL.
     */
    public static String getRandomWebSite()
    {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder url = new StringBuilder("http://www.");

        for (int i = 0; i < 10; i++)
        {
            url.append(characters.charAt(ranGen.nextInt(characters.length())));
        }
        url.append(".com");

        return url.toString();
    }

    /**
     * Generates a random nine digit zip code.
     *
     * @return A random zip code with a hyphen after the first five digits (xxxxx-xxxx).
     */
    public static String getRandomZipCode()
    {
        StringBuilder zip = makeNumber(9);

        zip.insert(5, "-");

        return zip.toString();
    }

    /**
     * Get the current time.</br>
     * If not 0 the &lt;hour&gt; and &lt;minute&gt; passed are added to the time.
     *
     * @param hour   The number of hours to be added to the current time.
     * @param minute The number of minutes to be added to the current time.
     * @return The String representing the current time plus the hour and minute added.
     * In the format of <tt>HH:mm</tt>.
     */
    public static String getTime(int hour, int minute)
    {
        Calendar cal = Calendar.getInstance();
        cal.getTime();

        // Add any hours or minutes to the current time.
        if (hour != 0)
            cal.add(Calendar.HOUR, hour);
        if (minute != 0)
            cal.add(Calendar.MINUTE, minute);

        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        return (timeFormat.format(cal.getTime()));
    }

    /**
     * Takes an array, and returns a string with each objects toString separated by the specified character.</br>
     * Any instance of the separating character will be escaped.
     *
     * @param obj       The object array to implode.
     * @param separator The character to use to separate each instance of the string.
     * @return The Object as an a string.
     */
    public static String implode(Object[] obj, String separator)
    {
        String replaceMe = "\\" + separator;
        StringBuilder imp = new StringBuilder();

        for (int x = 0, l = obj.length - 1; x <= l; x++)
        {
            imp.append(obj[x].toString().replace(separator, replaceMe));
            if (x < l)
                imp.append(separator);
        }

        return imp.toString();
    }

    /**
     * Takes a String array, and returns a String with each String in the array separated by a comma.<br/>
     * Any commas within the String that are not used to separate the Strings will be escaped.
     *
     * @param str The String array to implode.
     * @return A string with the each object in the array's toString result comma separated.
     */
    public static String implode(String[] str)
    {
        return implode(str, ",");
    }

    /**
     * Generates a random dollar amount.
     *
     * @return A float that is at least one million.
     */
    public static String makeDollarAmount()
    {
        float dollar = ranGen.nextFloat();
        dollar *= 1000000;

        BigDecimal big = new BigDecimal(String.valueOf(dollar));
        big = big.setScale(2, BigDecimal.ROUND_HALF_EVEN);
        String value = big.toString();
        return value;

    }

    /**
     * Generates a random number that has the specified number of digits in it.<br>
     * For example, if three is the specified length it will return a random number between 100 and 999 inclusive.
     *
     * @param length The number of digits that need to be generated.
     * @return A StringBuilder that contains the randomly generated digits.
     */
    public static StringBuilder makeNumber(final int length)
    {
        StringBuilder number = new StringBuilder(length);

        // Get the random number
        number.append(
            Long.toString((long) Math.floor(ranGen.nextDouble() * Math.pow(10, length))));

        // If the number is to short, add one digit to it until it is the correct length
        while (number.length() < length)
        {
            number.append(ranGen.nextInt(9));
        }

        return number;
    }

    /**
     * Takes a string and returns a String array.</br>
     * The string is split into the array on every comma.
     *
     * @param input The String to split into an array
     * @return An array of string.
     */
    public static String[] split(String input)
    {
        // This can probably be made more efficient using a regular expression
        return split(input, ',').toArray(new String[0]);
    }

    /**
     * Takes a string and returns an ArrayList of String.</br>
     * Any instance of the separator char that is escaped (proceeded by a backslash) will not be split on, and will be unescaped.
     *
     * @param input     The string to split.
     * @param separator The character to split the string on.
     * @return An ArrayList&lt;String&gt; containing the split string.
     */
    public static ArrayList<String> split(String input, char separator)
    {
        ArrayList<String> output = new ArrayList<String>();
        int last = 0, index = 0, tempLast = 0;
        while (true)
        {
            index = input.indexOf(separator, last + tempLast);

            if (index < 0)
                break; // reached the end, no more separators

            if (input.charAt(index - 1) == '\\')
            {
                // This separator is escaped, unescape it
                input = input.substring(0, index - 1) + input.substring(index);
                tempLast = index;
                continue;
            }
            else
            {
                tempLast = 0;
            }

            output.add(input.substring(last, index).trim());
            last = index + 1;
        }
        /* Do the last one until end of string */
        output.add(input.substring(last));
        return output;
    }

    /**
     * Remove the formating from the a phone number so it appears as a simple 10 digit number: xxxxxxxxxx.
     *
     * @param number The phone number to de-format.
     * @return The number without any formatting.
     */
    public static String stripPhoneFormat(String number)
    {
        return (number.replaceAll("[\\s\\-()]", ""));
    }

    /**
     * Indicates if a string is set to a value.
     *
     * @param data The string to test.
     * @return <tt>true<tt> if the string has a value.
     */
    public static boolean isSet(String data)
    {
        boolean textIsSet = (data != null && !data.equals("null") && !data.isEmpty());
        return textIsSet;
    }

    /**
     * Get the calendar with the given calendar value and format
     *
     * @param format        The format to be used with the calendar.
     * @param calendarValue The date to set in the returned Calendar object.
     * @return Calendar with the given calendar value and format; Otherwise, returns null.
     */
    public static Calendar getCalendarFromFormat(String format, String calendarValue)
    {
        try
        {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            cal.setTime(dateFormat.parse(calendarValue));
            return cal;
        } catch (Exception ex)
        {
            return null;
        }
    }

    /**
     * Get a random calendar date
     *
     * @return random calendar date
     */
    public static Calendar getRandomCalendarDate()
    {
        return getCalendarFromFormat("MM/dd/yyyy", getRandomDate());
    }

    /**
     * Get the date in string form with the given calendar object
     *
     * @param format The desired format for the returned date.
     * @param cal    The calendar object.
     * @return date in string form
     */
    public static String getStringFromCalendar(String format, Calendar cal)
    {
        try
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            String returnDate = dateFormat.format(cal.getTime());
            return returnDate;
        } catch (Exception ex)
        {
            return "";
        }
    }

    /**
     * Returns either true or false
     *
     * @return {@link Boolean}
     */
    public static boolean getRandomBoolean()
    {
        int value = getRandomInteger(0, 1);
        return value == 0 ? true : false;
    }

    /**
     * Method to remove all the special characters as they are causing search problems.</br>
     * TODO: Case #311168, when resolved, this method should be removed.
     *
     * @param text The entire text string from which is to be removed all special characters.
     * @return {@link String} The string without special characters.
     */
    public static String removeSpecial(String text)
    {
        //String special = "[\\!\\#\\$\\%\\^&\\*\\(\\)\\[\\]\\?]";
        String special = "[^a-zA-Z0-9 .]";
        String newText = text.replaceAll(special, "");

        return newText;
    }

    /**
     * Generates a random time
     *
     * @return {@link String} A random time in 24 hour form.
     */
    public static String getRandomTime()
    {
        String a = String.valueOf(getRandomInteger(0, 23));
        String delim = ":";
        String b = "";
        return a + delim + b;
    }

    public static String URLEncode(String request)
    {
        request = request.replaceAll("\\s+", "%20");
        request = request.replaceAll("\\*", "%2A");
        request = request.replaceAll("\\#", "%23");

        return request;
    }
}