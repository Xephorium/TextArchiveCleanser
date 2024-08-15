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

    public int getPhoneNumberListSize() {
        return phoneNumberList.size();
    }

    public boolean doesPhoneNumberListContain(String phoneNumber) {
        return phoneNumberList.contains(phoneNumber);
    }

    public double getTimestampAsDouble() {
        return Double.parseDouble(timestamp);
    }

    public String getTimestampWithoutMilliseconds() {
        return timestamp.substring(0, timestamp.length() - 3);
    }

    public String getContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;

        if (o == null) return false;

        if(SMSMessage.class != o.getClass()) return false;

        SMSMessage smsMessage = (SMSMessage) o;

        return getPhoneNumberListSize() == 1 &&
                doesPhoneNumberListContain(smsMessage.getPhoneNumber()) &&
                getTimestampWithoutMilliseconds().equals(smsMessage.getTimestampWithoutMilliseconds()) &&
                content.equals(smsMessage.getContent());
    }

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
                if (!shouldPhoneNumberBeSkipped(newNumber)) {
                    phoneNumberList.add(newNumber);
                }

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
        String content = "";
        List<String> entryLines = new ArrayList<>(Arrays.asList(entry.split("\n")));
        for (String line: entryLines) {
            if (line.contains(CONTENT_TYPE)) {
                if (line.contains(CONTENT_END_DOUBLE_QUOTE)) {
                    content = line.substring(
                            line.indexOf(CONTENT_START_DOUBLE_QUOTE) + CONTENT_START_DOUBLE_QUOTE.length(),
                            line.indexOf(CONTENT_END_DOUBLE_QUOTE)
                    );
                } else {
                    content = line.substring(
                            line.indexOf(CONTENT_START_SINGLE_QUOTE) + CONTENT_START_SINGLE_QUOTE.length(),
                            line.indexOf(CONTENT_END_SINGLE_QUOTE)
                    );
                }
                break;
            }
        }

        return new MMSMessage(phoneNumberList, timestamp, content);
    }

    public boolean shouldBeFiltered() {


        /* Filter Spam */

        for (String number: phoneNumberList) {

            /* Four-Digit Numbers (Automated Messages - All Single Phone Number)
             *   AT&T
             *   Mint
             */
            if (number.length() == 4) return true;

            /* Five-Digit Numbers (Automated Messages)
             *   Pasta House
             *   Bank of America
             *   DHL
             *   Webster Dental Care
             *   Walgreens
             *   FSA
             *   UPS
             *   DocASAP
             *   UMSL (Preserved because funny - 67283, 77295)
             *   USPS
             *   Wayfair (Preserved because I like that chair - 65399)
             *   Google
             *   Walmart
             *   Instacart
             *   Mint
             *   Zelle
             *   AirBnB
             *   Political Spam
             *   Expedia
             *   American Airlines
             *   Imos Pizza
             *
             */
            if (number.length() == 5 &&
                    !number.equals("67283") &&
                    !number.equals("77295") &&
                    !number.equals("65399")
            ) return true;

            /* Six-Digit Numbers (Automated Messages)
             *   Steam
             *   Choice Hotels
             *   UMSL Alerts (Preserved because funny - 226787)
             *   ArtStation
             *   AMC
             *   AirBnB
             *   PayPal
             *   Coinbase
             *   eBay
             *   Microsoft
             */
            if (number.length() == 6 && !number.equals("226787")) return true;

            /* Seven-Digit Numbers
             *   AT&T
             *   That one time I got added to a group chat with randos and everyone was confused.
             */
            if (number.length() == 7) return true;

            /* Eight-Digit Numbers (Automated Messages)
             *   AT&T
             */
            if (number.length() == 8) return true;

            // Nine-Digit Numbers (Invalid Numbers)
            if (number.length() == 9) return true;

            // Specific Messages
            if(
                    number.equals("90g@smile.ms") ||                      // Webster Dental Care
                    number.equals("zachariahstantonzle6347@gmail.com") || // Spam
                    number.equals("barbrawhartwellgr0155@hotmail.com") || // Spam
                    number.equals("hishighness1947@gmail.com") ||         // Spam
                    number.equals("+8622759730") ||                       // Spam
                    number.contains("mms.cricketwireless.net")            // Spam
            ) return true;
        }

        return false;
    }

    /*--- Private Methods ---*/

    private static boolean shouldPhoneNumberBeSkipped(String number) {

        /* This text appears as an additional address alongside correct
         * phone numbers in exactly 13 early MMS entries. No idea why.
         */
        if (number.equals("insert-address-token")) return true;

        return false;
    }
}
