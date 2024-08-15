package model;


public class SMSMessage {


    /*--- Variables ---*/

    private static final String PHONE_NUMBER_START = "address=\"";
    private static final String PHONE_NUMBER_END = "\" date=\"";
    private static final String TIMESTAMP_START = " date=\"";
    private static final String TIMESTAMP_END = "\" type=\"";
    private static final String CONTENT_START_DOUBLE_QUOTE = "\" body=\"";
    private static final String CONTENT_END_DOUBLE_QUOTE = "\" toa=\"";
    private static final String CONTENT_START_SINGLE_QUOTE = "\" body='";
    private static final String CONTENT_END_SINGLE_QUOTE = "' toa=\"";

    private final String phoneNumber;
    private final String timestamp;
    private final String content;


    /*--- Constructor ---*/

    public SMSMessage(String phoneNumber, String timestamp, String content) {
        this.phoneNumber = phoneNumber;
        this.timestamp = timestamp;
        this.content = content;
    }


    /*--- Public Methods ---*/

    public String getPhoneNumber() {
        return phoneNumber;
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

        if (o.getClass() != MMSMessage.class) return false;

        MMSMessage mmsMessage = (MMSMessage) o;

        return mmsMessage.getPhoneNumberListSize() == 1 &&
                mmsMessage.doesPhoneNumberListContain(phoneNumber) &&
                mmsMessage.getTimestampWithoutMilliseconds().equals(getTimestampWithoutMilliseconds()) &&
                mmsMessage.getContent().equals(content);
    }

    public static SMSMessage fromEntry(String entry) {


        /* Read Phone Number */

        // Read Phone Number
        String phoneNumber = entry.substring(
                entry.indexOf(PHONE_NUMBER_START) + PHONE_NUMBER_START.length(),
                entry.indexOf(PHONE_NUMBER_END)
        );

        // Sanitize Phone Number
        phoneNumber = phoneNumber.replaceAll("\\+1", "");
        phoneNumber = phoneNumber.replaceAll("[-() ]", "");
        if (phoneNumber.length() == 11 && phoneNumber.charAt(0) == '1') {
            phoneNumber = phoneNumber.substring(1);
        }

        /* Print Weird Phone Numbers
         *
         * Note: Pretty much every entry with a number that isn't 10 characters long can
         *       be safely deleted. These are order confirmations, instacart messages,
         *       two-factor authentication codes, billing reminders, and the like. I'll
         *       want to do a final manual check of all records. But, it's most likely
         *       that all of them can be cleared.
         */
        if (false && phoneNumber.length() != 10) System.out.println(phoneNumber);


        /* Read Timestamp */

        /* Notes: All sms timestamps are confirmed to be 13 characters in length. However,
         *        the last three digits (representing millisecond) are zeroed out until
         *        timestamp 1409455938, or Saturday August 30 2014 10:32:18:938pm. Maybe
         *        this is the first text I received on a new smartphone?
         */
        String timestamp = entry.substring(
                entry.indexOf(TIMESTAMP_START) + TIMESTAMP_START.length(),
                entry.indexOf(TIMESTAMP_END)
        );


        /* Read Content */

        // Read Content
        String content;
        if (entry.contains(CONTENT_START_DOUBLE_QUOTE)) {
            content = entry.substring(
                    entry.indexOf(CONTENT_START_DOUBLE_QUOTE) + CONTENT_START_DOUBLE_QUOTE.length(),
                    entry.indexOf(CONTENT_END_DOUBLE_QUOTE)
            );
        } else {
            content = entry.substring(
                    entry.indexOf(CONTENT_START_SINGLE_QUOTE) + CONTENT_START_SINGLE_QUOTE.length(),
                    entry.indexOf(CONTENT_END_SINGLE_QUOTE)
            );
        }

        /* Return Valid Entry */

        return new SMSMessage(phoneNumber, timestamp, content);
    }

    public boolean shouldBeFiltered() {


        /* Filter Spam */

        /* Four-Digit Numbers (Automated Messages)
         *   AT&T Automated Messages
         *   Mint Automated Messages
         */
        if (phoneNumber.length() == 4) return true;

        /* Five-Digit Numbers (Automated Messages)
         *   Basically everything else. I've scrolled through hundreds of entries and
         *   the only thing I considered saving was appointment details for doctor visits.
         *   Otherwise, just years of instacart orders, concert venue offers, Spectrum alerts,
         *   verification codes, takeout orders, Nelnet reminders, retail spam, etc.
         */
        if (phoneNumber.length() == 5) return true;

        /* Six-Digit Numbers (Automated Messages)
         *   AMC
         *   Microsoft
         *   Steam
         *   Stripe
         *   Google
         */
        if (phoneNumber.length() == 6) return true;

        /* Eight-Digit Numbers (Automated Messages)
         *   More AT&T
         *   More Google
         */
        if (phoneNumber.length() == 8) return true;

        /* Alerts
         *   AMBER
         *   National Weather Service
         */
        if (phoneNumber.equals("#CMAS#CMASALL")) return true;

        /* Specific Messages
         */
        if (
                timestamp.equals("1360089340000") || // Shazam Text
                phoneNumber.equals("129")            // Voicemail Pin Reset
        ) {
            return true;
        }

        return false;
    }
}
