package model;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MMSMessage {


    /*--- Variables ---*/

    private static final String PHONE_NUMBER_HEADER_START = "\" address=\"";
    private static final String PHONE_NUMBER_HEADER_END = "\" sub_cs=\"";
    private static final String PHONE_NUMBER_START = "<addr address=\"";
    private static final String PHONE_NUMBER_END = "\" type=\"";
    private static final String TIMESTAMP_START = "<mms date=\"";
    private static final String TIMESTAMP_END = "\" rr=\"";
    private static final String CONTENT_TYPE = " ct=\"text/plain\" ";
    private static final String CONTENT_START_DOUBLE_QUOTE = "\" text=\"";
    private static final String CONTENT_END_DOUBLE_QUOTE = "\" />";
    private static final String CONTENT_START_SINGLE_QUOTE = "\" text='";
    private static final String CONTENT_END_SINGLE_QUOTE = "' />";

    private final List<String> phoneNumberList;
    private final String timestamp;
    private final String content;


    /*--- Constructor ---*/

    public MMSMessage(List<String> phoneNumberList, String timestamp, String content) {
        this.phoneNumberList = phoneNumberList;
        this.timestamp = timestamp;
        this.content = content;
    }


    /*--- Public Methods ---*/

    public static MMSMessage fromEntry(String entry) {


        /* Read Phone Numbers */

        // Initialize Phone Number List
        List<String> phoneNumberList = new ArrayList<>();

        // Read Phone Numbers
        if (entry.contains("<addrs />")) {

            // If Entry Lacks <addr/> Elements, Parse From Header
            String allNumbers = entry.substring(
                    entry.indexOf(PHONE_NUMBER_HEADER_START) + PHONE_NUMBER_HEADER_START.length(),
                    entry.indexOf(PHONE_NUMBER_HEADER_END)
            );

            // Sanitize & Add New Numbers
            List<String> newNumbers = new ArrayList<>(Arrays.asList(allNumbers.split("~")));
            for (String newNumber: newNumbers) {

                // Sanitize New Number
                newNumber = newNumber.replaceAll("\\+1", "");
                if (newNumber.length() == 11 && newNumber.charAt(0) == '1') {
                    newNumber = newNumber.substring(1);
                }

                // Add Number
                phoneNumberList.add(newNumber);
            }

        } else {

            // Read All Available Phone Numbers From <addr/> Elements
            int index = entry.indexOf(PHONE_NUMBER_START);
            while (index >= 0) {

                // Read New Number
                String newNumber = entry.substring(
                        index + PHONE_NUMBER_START.length(),
                        entry.indexOf(PHONE_NUMBER_END, index)
                );

                // Sanitize New Number
                newNumber = newNumber.replaceAll("\\+1", "");
                if (newNumber.length() == 11 && newNumber.charAt(0) == '1') {
                    newNumber = newNumber.substring(1);
                }

                // Add Number
                phoneNumberList.add(newNumber);

                // Iterate Index
                index = entry.indexOf(PHONE_NUMBER_START, index + 1);
            }
        }


        /* Read Timestamp */

        /* Notes: All mms timestamps are confirmed to be 13 characters in length. However,
         *        the last three digits (representing millisecond) are always zeroed out.
         */
        String timestamp = entry.substring(
                entry.indexOf(TIMESTAMP_START) + TIMESTAMP_START.length(),
                entry.indexOf(TIMESTAMP_END)
        );


        /* Read Content */

        // Read Content
        boolean isContentFound = false;
        String content = "";
        List<String> entryLines = new ArrayList<>(Arrays.asList(entry.split("\n")));
        for (String line: entryLines) {
            if (line.contains(CONTENT_TYPE)) {
                if (line.contains(CONTENT_END_DOUBLE_QUOTE)) {
                    isContentFound = true;
                    content = line.substring(
                            line.indexOf(CONTENT_START_DOUBLE_QUOTE) + CONTENT_START_DOUBLE_QUOTE.length(),
                            line.indexOf(CONTENT_END_DOUBLE_QUOTE)
                    );
                } else {
                    isContentFound = true;
                    content = line.substring(
                            line.indexOf(CONTENT_START_SINGLE_QUOTE) + CONTENT_START_SINGLE_QUOTE.length(),
                            line.indexOf(CONTENT_END_SINGLE_QUOTE)
                    );
                }
                break;
            }
        }

        if (isContentFound) {
            System.out.println(content);
        }

        return new MMSMessage(phoneNumberList, timestamp, content);
    }
}
