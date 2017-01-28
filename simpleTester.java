//package simpleTester;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner; // find a better I/O interface
import java.util.InputMismatchException;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class simpleTester {
    
    final static String YES_REGEX = "[yY]([eE][sS])?";
    final static String  NO_REGEX = "[nN][oO]?";
    final static String yes_OR_NO_PROMPT = "  yes or No: ";
    final static String YES_OR_no_PROMPT = "  Yes or no: ";
    public static void main(String[] args) throws FileNotFoundException, IOException {
        // Setup a random number generator for later use
        Random rand = new Random();

        // Variables for interacting with the testing list(s)
        int size;
        int index;
        int randIndex;
        int numPropsFiles = 0;
        String state, capital;
        String repeatStr;
        String errorMsg;
        String line;
        String prompt;
        String initialPrompt;
        String failPrompt;
        String failRepeatPrompt;
        boolean repeat;

        // Choose the properties file to use for the test
        File propsFile = null;
        String propsFileName;
        String propsFileDirName = "/home/cory/git/simpleTester/props";
        FilenameFilter propsFileNameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) { return name.matches(".*\\.props"); }
        };

        // Make sure the chosen directory for test property files exists
        File propsFileDir = new File(propsFileDirName);
        if (!(propsFileDir.exists() && propsFileDir.isDirectory())) {
            errorMsg = "ERROR: The chosen properties directory does not exist or is not a directory: " + propsFileDirName;
            System.out.println(errorMsg);
            System.exit(0);
        }
        
        // Choose how to interact with the user (e.g., using java.util.Scanner)
        // This will need to be closed!
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        
        // Grab the list of properties files from the chosen directory
        File[] propsFileList = propsFileDir.listFiles(propsFileNameFilter);
        // If there were no property files, exit. If just one property file, use it. Otherwise, ask user to choose.
        if ( (propsFileList == null) || ((numPropsFiles = propsFileList.length) < 1) ) {
            errorMsg = "ERROR: No property files found in the chosen directory: " + propsFileDirName;
            System.out.println(errorMsg);
            reader.close();
            System.exit(0);
        }

        // Create a sorted version of the props files
        ArrayList<File> propsFileArrayList = new ArrayList<File>(Arrays.asList(propsFileList));
        Collections.sort(propsFileArrayList);

        // If there were no property files, exit. If just one property file, use it. Otherwise, ask user to choose.
        if (numPropsFiles == 1) {
            propsFile = propsFileArrayList.get(0);
        } else {
            // Ask user to choose
            System.out.println("There are multiple property files to choose from...");
            for (int i=0; i<numPropsFiles; i++) {
                File file = propsFileArrayList.get(i);
                String filename = file.getName();
                //System.out.println("  " + (i+1) + ": " + filename);
                System.out.format("  %2d: %s%n", i+1, filename);
            }
            // Get index
            initialPrompt = "Choose the number for the test you want to take: ";
            failRepeatPrompt = "Invalid input. Try again: ";
            index = HelperFunctions.getInt(reader, initialPrompt, failRepeatPrompt);
            while (index < 1 || index > numPropsFiles) {
                index = HelperFunctions.getInt(reader, failRepeatPrompt, failRepeatPrompt);
            }
            
            propsFile = propsFileArrayList.get(index-1);
            System.out.println(); // Add a blank line
        }
        
        // Make sure the chosen test property file exists
        propsFileName = propsFile.getName();
        if (!propsFile.exists()) {
            errorMsg = "ERROR: Could not find the chosen properties file: " + propsFileName;
            System.out.println(errorMsg);
            reader.close();
            System.exit(0);
        }

        // Import the test property file and declare/initialize some useful lists
        Properties simpleTesterProps = new Properties();
        simpleTesterProps.load(new FileInputStream(propsFile));
        final ArrayList<String> originalFullTestList = new ArrayList<>(simpleTesterProps.stringPropertyNames());
        ArrayList<String> fullTestList;
        ArrayList<String> successList = new ArrayList<>();
        ArrayList<String> failureList = new ArrayList<>(originalFullTestList);
        final ArrayList<String> trimmedFailureList;
        ArrayList<String> statesThisRound;
        
        // Deciding on test size
        final int DEFAULT_TEST_SIZE_MAX = 10;
        final int originalFullTestSize = originalFullTestList.size();
        int testSize = originalFullTestSize;
        if (DEFAULT_TEST_SIZE_MAX < testSize) {
            System.out.println("There are " + testSize + " prompts to test.");
            System.out.println("Do you want to choose a smaller number for this test?");
            String choice = HelperFunctions.getTrimmedString(reader, YES_OR_no_PROMPT);
            System.out.println(); // Add a blank line
            if (!choice.toLowerCase().matches(NO_REGEX)) {

                // Get index
                initialPrompt = "How large a list do you want (defaults to " + DEFAULT_TEST_SIZE_MAX + " on invalid choice): ";
                failPrompt = "Invalid choice. Going with default value. ";
                testSize = HelperFunctions.getIntWithDefault(reader, initialPrompt, failPrompt, DEFAULT_TEST_SIZE_MAX);
            }
            System.out.println(); // Add a blank line
        }

        // Make sure testSize is valid
        errorMsg = "Somehow an invalid testSize (" + testSize + ") has been selected. Should be in the range 1.." + originalFullTestSize;
        assert ( (testSize >= 1) && (testSize <= originalFullTestSize) ) : errorMsg;

        // Grab random entries to test if not testing the whole set
        if (testSize == originalFullTestSize)
            fullTestList = originalFullTestList;
        else {
            size = failureList.size();
            while (testSize < size) {
                randIndex = rand.nextInt(size);
                failureList.remove(randIndex);
                size--;
            }
        }
        trimmedFailureList = new ArrayList<String>(failureList);

        // Outer loop
        do {
            failureList = new ArrayList<String>(trimmedFailureList);
            
            // Inner loop
            while ( (failureList != null) && (size = failureList.size()) > 0) {
                HelperFunctions.printStringArrayListRandom(failureList, "prompts");
                HelperFunctions.printValuesFromArrayList(failureList, simpleTesterProps);
                    
                statesThisRound = new ArrayList<>(failureList);
                while (size>0) {
                    randIndex = rand.nextInt(size);
                    state = statesThisRound.remove(randIndex);
                    capital = simpleTesterProps.getProperty(state);
        
                    System.out.println("Prompt:    " + state);
                    prompt = "Guess:     ";
                    String guess = HelperFunctions.getTrimmedString(reader, prompt);
                    if (!HelperFunctions.checkGuessIgnorePunctuationIgnoreCase(guess, capital))
                        System.out.println("Incorrect: " + capital.toUpperCase());
                    else {
                        failureList.remove(state);
                        successList.add(state);
                    }
        
                    System.out.println("");
                    size--;
                }
                size = failureList.size();
                if (size>0) {
                    System.out.format("You have %d prompts left to get correct. Wanna play again?%n", size);
                    prompt = YES_OR_no_PROMPT;
                    repeatStr = HelperFunctions.getTrimmedString(reader, prompt);
                    repeat = !repeatStr.matches(NO_REGEX);
                    if (!repeat) {
                        failureList = null;
                        System.out.println();
                    }
                }
            }
            System.out.println("Do you want to repeat this exam?");
            prompt = YES_OR_no_PROMPT;
            repeatStr = HelperFunctions.getTrimmedString(reader, prompt);
            repeat = !repeatStr.matches(NO_REGEX);

            if (repeat)
                System.out.println();

        } while (repeat);
        
        reader.close();
    }

}

class HelperFunctions {
    public static void printStringArrayList(ArrayList<String> list, String label) {
      /**
       * Give an ArrayList<String> object, print out all entries formatted into columns.
       **/
        int MAX_LINE_WIDTH = 110;
        int MIN_BUFFER = 2;
        String MIN_BUFFER_STR = repeatString(" ", MIN_BUFFER);
        int n = list.size();
        int numValuesPerLine;
        int maxValueWidth=0;
        int posInLine = 0;

        for ( String entry : list )
            maxValueWidth = max(maxValueWidth, entry.length());

        numValuesPerLine = (MAX_LINE_WIDTH-MIN_BUFFER) / (maxValueWidth+2*MIN_BUFFER);
        numValuesPerLine = max(1, numValuesPerLine);

        System.out.println("Remaining " + label + " are...");
        for ( String entry : list ) {
            int bufferLen = maxValueWidth + MIN_BUFFER - entry.length();
            System.out.print(MIN_BUFFER_STR);
            System.out.print(entry);
            System.out.print(repeatString(" ", bufferLen));
            posInLine = (posInLine+1) % numValuesPerLine;
            if (posInLine == 0)
                System.out.println();
        }
        if (posInLine != 0) // Add an extra linebreak unless the last line already added it
            System.out.println();
        System.out.println(); // An an extra, extra linebreak; should be a fully blank line
    }

    public static void printStringArrayListRandom(ArrayList<String> list, String label) {
        ArrayList<String> localList = new ArrayList<>(list);
        ArrayList<String> randomizedList = new ArrayList<>();
        int n = localList.size();
        int index;
        Random rand = new Random();
        for (int i=0; i < n--; /*nothing*/) {
            index = rand.nextInt(n+1);
            randomizedList.add(localList.remove(index));
        }
        printStringArrayList(randomizedList, label);
    }

    public static void printValuesFromArrayList(ArrayList<String> keyList, Properties props) {
      /**
       * Given a list of keys and a Properties object covering AT LEAST all the keys
       * itemized in the list object (possibly more than that), print out the values
       * from the Properties object corresponding the keys contained within the key
       * list. No test against duplication of values or keys is performed.
       **/
        ArrayList<String> valueList = new ArrayList<>();

        for ( String key : keyList )
            valueList.add(props.getProperty(key));

        printStringArrayListRandom(valueList, "values");
    }

    public static String repeatString(String str, int n) {
        StringBuilder ret = new StringBuilder(n);
        for (int i=0; i<n; i++)
            ret.append(str);
        return ret.toString();
    }

    public static String keepOnlyAlphaNumeric(String in) {
        final String UNWANTED_REGEX = "[^ a-zA-Z0-9]";
        Matcher matcher = Pattern.compile(UNWANTED_REGEX).matcher(in);
        ArrayList<Integer> indexList = new ArrayList<>();
        while (matcher.find())
            indexList.add(0, matcher.regionStart()); // creating index list in reverse order
        StringBuilder out = new StringBuilder(in);
        for (int index : indexList)
            out.deleteCharAt(index);
        return out.toString();
    }

    public static boolean checkGuess(String guess, String expected) {
        return expected.equals(guess);
    }

    public static boolean checkGuessIgnoreCase(String guess, String expected) {
        return expected.toLowerCase().equals(guess.toLowerCase());
    }

    public static boolean checkGuessIgnorePunctuation(String guess, String expected) {
        String newGuess = keepOnlyAlphaNumeric(guess);
        String newExpected = keepOnlyAlphaNumeric(expected);
        return newExpected.equals(newGuess);
    }

    public static boolean checkGuessIgnorePunctuationIgnoreCase(String guess, String expected) {
        String newGuess = keepOnlyAlphaNumeric(guess);
        String newExpected = keepOnlyAlphaNumeric(expected);
        return newExpected.toLowerCase().equals(newGuess.toLowerCase());
    }

    public static int getInt(BufferedReader reader, String initialPrompt, String failRepeatPrompt) throws IOException {
        boolean done = false;
        String line;
        int n = 0;
        System.out.print(initialPrompt);
        do {
            try {
                line = reader.readLine().trim();
                n = Integer.decode(line);
                done = true;
            } catch (NumberFormatException e) {
                System.out.print(failRepeatPrompt);
            }
        } while (!done);
        return n;
    }

    public static int getIntWithDefault(BufferedReader reader, String initialPrompt, String failPrompt, int defaultInt) throws IOException {
        String line;
        int n = defaultInt;
        System.out.print(initialPrompt);
        try {
            line = reader.readLine().trim();
            n = Integer.decode(line);
        } catch (NumberFormatException e) {
            System.out.println(failPrompt);
        }
        return n;
    }

    public static String getTrimmedString(BufferedReader reader, String prompt) throws IOException {
        System.out.print(prompt);
        return reader.readLine().trim();
    }
}
