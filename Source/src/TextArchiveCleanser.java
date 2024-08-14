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
    List<SMSMessage> smsMessageList;


    /*--- Constructor ---*/

    public TextArchiveCleanser() {

        // Initialize Variables
        smsMessageList = new ArrayList<>();

        // Open Files
        openInputFile();
    }


    /*--- Public Methods ---*/

    public void run() {
        parseHeader();
        parseSMSEntries();
        parseMMSEntries();
        parseFooter();

        System.out.println("Total SMS Messages: " + smsMessageList.size());
    }


    /*--- Private Entry Processing Methods ---*/

    private void processSMSEntry(String entry) {
        smsMessageList.add(SMSMessage.fromEntry(entry));
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

                // SMS Entry - Parse
                processSMSEntry(inputLine);

            } else {

                // First MMS Entry - Continue
                isSMSParsingComplete = true;
            }

            // Read Next Line
            if ((inputLine = getNextLineOfInputFile()) == null) {
                System.out.println("End of file reached while parsing sms entries.");
                exit(0);
            }
        }
    }

    private void parseMMSEntries() {
        boolean isMMSParsingComplete = false;
        Pattern eofPattern = Pattern.compile("^</smses>");
        Matcher eofMatcher;

        while (!isMMSParsingComplete) {

            // Parse Line
            eofMatcher = eofPattern.matcher(inputLine);
            if (!eofMatcher.find()) {

                // MMS Entry - Parse
                // TODO - Parse MMS Entry

            } else {

                // End of File Reached - Continue
                isMMSParsingComplete = true;
            }

            // Read Next Line
            if (!isMMSParsingComplete && (inputLine = getNextLineOfInputFile()) == null) {
                System.out.println("End of file reached while parsing mms entries.");
                exit(0);
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
