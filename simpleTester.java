//package simpleTester;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
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
    public static void main(String[] args) throws FileNotFoundException, IOException {
        // Setup a random number generator for later use
        Random rand = new Random();

        // Easy access to print and println
        
        
        // Variables for interacting with the testing list(s)
        int size;
        int index;
        int randIndex;
        int numPropsFiles = 0;
        String state, capital;
        String repeatStr;
        String errorMsg;
        boolean repeat;

        // Choose the properties file to use for the test
        File propsFile = null;
        String propsFileName;
        String propsFileDirName = "/home/cory/git/simpleTester/props";
        FilenameFilter propsFileNameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) { return name.matches(".*\\.props"); }
        };
        //propsFileName = "/home/cory/git/simpleTester/simpleTester.props";
        //propsFileName = "/home/cory/git/simpleTester/countryCapitals.props";

        // Make sure the chosen directory for test property files exists
        File propsFileDir = new File(propsFileDirName);
        if (!(propsFileDir.exists() && propsFileDir.isDirectory())) {
            errorMsg = "ERROR: The chosen properties directory does not exist or is not a directory: " + propsFileDirName;
            System.out.println(errorMsg);
            System.exit(0);
        }
        
        // Choose how to interact with the user (e.g., using java.util.Scanner)
        // This will need to be closed!
        Scanner reader = new Scanner(System.in);
        
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
            System.out.print("Choose the number for the test you want to take (defaults to 1): ");
            if (reader.hasNextInt()) {
                index = reader.nextInt();
                reader.nextLine(); // skip the remainder of the current input line (including the newline char)
            } else
                index = 1;
            if (index < 1 || index > numPropsFiles)
                index = 1;
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
            System.out.print("  Yes or no: ");
            String choice = reader.nextLine();
            System.out.println(); // Add a blank line
            if (!choice.toLowerCase().matches(NO_REGEX)) {
                System.out.print("How large a list do you want (defaults to " + DEFAULT_TEST_SIZE_MAX + " on invalid choice): ");
                try {
                    testSize = reader.nextInt();
                    reader.nextLine(); // skip the remainder of the current input line (including the newline char)
                    if ( (testSize < 1) || (testSize > originalFullTestSize) )
                        testSize = DEFAULT_TEST_SIZE_MAX;
                } catch (InputMismatchException e) {
                    testSize = DEFAULT_TEST_SIZE_MAX;
                }
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
                    System.out.print(  "Guess:     ");
                    String guess = reader.nextLine();
                    //if (!guess.toLowerCase().matches(capital.toLowerCase()))
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
                    System.out.print("  Yes or no: ");
                    repeatStr = reader.nextLine();
                    repeat = !repeatStr.matches(NO_REGEX);
                    if (!repeat) {
                        failureList = null;
                        System.out.println();
                        System.out.format("Matched '%s': '%s'%n", NO_REGEX, repeatStr);
                        System.out.println();
                    }
                }
            }
            System.out.println("Do you want to repeat this exam?");
            System.out.print("  Yes or no: ");
            repeatStr = reader.nextLine();
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

    public static String keepOnlySpacesAndLetters(String in) {
        final String UNWANTED_REGEX = "[^ a-zA-Z]";
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
        String newGuess = keepOnlySpacesAndLetters(guess);
        String newExpected = keepOnlySpacesAndLetters(expected);
        return newExpected.equals(newGuess);
    }

    public static boolean checkGuessIgnorePunctuationIgnoreCase(String guess, String expected) {
        String newGuess = keepOnlySpacesAndLetters(guess);
        String newExpected = keepOnlySpacesAndLetters(expected);
        return newExpected.toLowerCase().equals(newGuess.toLowerCase());
    }
}
