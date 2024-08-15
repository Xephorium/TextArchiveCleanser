import model.MMSMessage;
import model.SMSMessage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.exit;


public class TextArchiveCleanser {


    /*--- Variable Declarations ---*/

    // Input Processing
    private BufferedReader inputFileBufferedReader;
    private String inputLine;

    // Message Processing
    List<SMSMessage> preservedSMSMessages;
    List<SMSMessage> filteredSMSMessages;
    List<MMSMessage> preservedMMSMessages;
    int filteredMMSMessages = 0;
    int duplicateMMSMessages = 0;


    /*--- Constructor ---*/

    public TextArchiveCleanser() {

        // Initialize Variables
        preservedSMSMessages = new ArrayList<>();
        filteredSMSMessages = new ArrayList<>();
        preservedMMSMessages = new ArrayList<>();

        // Open Files
        openInputFile();
    }


    /*--- Public Methods ---*/

    public void run() {
        parseHeader();
        parseSMSEntries();
        parseMMSEntries();
        parseFooter();

        System.out.println("-----");
        System.out.println("SMS Preserved:\t" + preservedSMSMessages.size());
        System.out.println("SMS Filtered:\t" + filteredSMSMessages.size());
        System.out.println("-----");
        System.out.println("MMS Duplicates:\t" + duplicateMMSMessages);
        System.out.println("MMS Preserved:\t" + preservedMMSMessages.size());
        System.out.println("MMS Filtered:\t" + filteredMMSMessages);
        System.out.println("-----");
        System.out.println("New Archive:\t" + (preservedSMSMessages.size() + preservedMMSMessages.size()) + " Messages");
    }


    /*--- Private Entry Processing Methods ---*/

    private void processSMSEntry(String entry) {
        SMSMessage smsMessage = SMSMessage.fromEntry(entry);
        if (smsMessage.shouldBeFiltered()) {
            filteredSMSMessages.add(smsMessage);
        } else {
            preservedSMSMessages.add(smsMessage);
        }
    }

    private void processMMSEntry(String entry) {
        MMSMessage mmsMessage = MMSMessage.fromEntry(entry);
        if (preservedSMSMessages.contains(mmsMessage) || filteredSMSMessages.contains(mmsMessage)) {
            duplicateMMSMessages++;
        } else {
            if (mmsMessage.shouldBeFiltered()) {
                filteredMMSMessages++;
            } else {
                preservedMMSMessages.add(mmsMessage);
            }
        }
    }


    /*--- Private Parsing Methods ---*/

    private void parseHeader() {
        boolean isHeaderParsingComplete = false;
        Pattern smsEntryPattern = Pattern.compile("^  <sms ");
        Matcher smsEntryMatcher;

        while (!isHeaderParsingComplete) {

            // Read Next Line
            if ((inputLine = getNextLineOfInputFile()) == null) {
                System.out.println("End of file reached while parsing header.");
                exit(0);
            }

            // Parse Line
            smsEntryMatcher = smsEntryPattern.matcher(inputLine);
            if (!smsEntryMatcher.find()) {

                // Header Line - Add to Output
                // TODO - Add to Output File

            } else {

                // First SMS Entry - Continue
                isHeaderParsingComplete = true;
            }
        }
    }

    private void parseSMSEntries() {
        boolean isSMSParsingComplete = false;
        Pattern mmsEntryPattern = Pattern.compile("^  <mms ");
        Matcher mmsEntryMatcher;

        while (!isSMSParsingComplete) {

            // Parse Line
            mmsEntryMatcher = mmsEntryPattern.matcher(inputLine);
            if (!mmsEntryMatcher.find()) {

                // SMS Entry - Process
                processSMSEntry(inputLine);

            } else {

                // First MMS Entry - Continue
                isSMSParsingComplete = true;
            }

            // Read Next Line
            if (!isSMSParsingComplete && (inputLine = getNextLineOfInputFile()) == null) {
                System.out.println("End of file reached while parsing sms entries.");
                exit(0);
            }
        }
    }

    private void parseMMSEntries() {
        boolean isEndOfFileReached = false;
        Pattern endOfFilePattern = Pattern.compile("^</smses>");
        Pattern endOfMMSPattern = Pattern.compile("^  </mms>");
        Matcher matcher;

        while (!isEndOfFileReached) {
            matcher = endOfFilePattern.matcher(inputLine);
            if (matcher.find()) {

                // End of File Reached - Exit Loop
                isEndOfFileReached = true;

            } else {

                // MMS Entry - Add First Line To String
                StringBuilder mmsEntryStringBuilder = new StringBuilder();
                mmsEntryStringBuilder.append(inputLine);
                mmsEntryStringBuilder.append("\n");

                // MMS Entry - Add Subsequent Lines To String
                boolean isEndOfMMSReached = false;
                while (!isEndOfMMSReached && (inputLine = getNextLineOfInputFile()) != null) {
                    mmsEntryStringBuilder.append(inputLine);
                    mmsEntryStringBuilder.append("\n");

                    matcher = endOfMMSPattern.matcher(inputLine);
                    if (matcher.find()) {
                        isEndOfMMSReached = true;
                    }
                }

                // Whole MMS Entry Read - Process
                processMMSEntry(mmsEntryStringBuilder.toString());
            }

            // Check Next Line
            if (!isEndOfFileReached) {
                if ((inputLine = getNextLineOfInputFile()) == null) {
                    System.out.println("End of file reached while parsing mms entries.");
                    exit(0);
                }
            }
        }
    }

    private void parseFooter() {

        // Copy Footer to Output
        // TODO - Add to Output File
    }


    /*--- Private I/O Methods ---*/

    private void openInputFile() {

        // Build Input File
        File inputFile = new File("input/input.xml");

        // Ensure Input File Exists
        if (!inputFile.exists()) {
            System.out.println("Input file doesn't exist.");
            exit(0);
        }

        // Build Input BufferedReader
        try {
            FileReader inputFileReader = new FileReader(inputFile);
            inputFileBufferedReader = new BufferedReader(inputFileReader);
        } catch(Exception e) {
            System.out.println("Error building BufferedReader for input file.");
            exit(0);
        }
    }

    private String getNextLineOfInputFile() {
        try {
            return inputFileBufferedReader.readLine();
        } catch (IOException e) {
            return null;
        }
    }
}
