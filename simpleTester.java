//package stateCapitals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.InputMismatchException;
import java.util.Collections;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class simpleTester {

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
        String repeat;
        String errorMsg;

        // Choose the properties file to use for the test
        File propsFile = null;
        String propsFileName;
        String propsFileDirName = "/home/cory/git/stateCapitals";
        FilenameFilter propsFileNameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) { return name.matches(".*\\.props"); }
        };
        //propsFileName = "/home/cory/git/stateCapitals/stateCapitals.props";
        //propsFileName = "/home/cory/git/stateCapitals/countryCapitals.props";

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
                System.out.println("  " + (i+1) + ": " + filename);
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
        Properties stateCapitalsProps = new Properties();
        stateCapitalsProps.load(new FileInputStream(propsFile));
        final ArrayList<String> originalFullTestList = new ArrayList<>(stateCapitalsProps.stringPropertyNames());
        ArrayList<String> fullTestList;
        ArrayList<String> successList = new ArrayList<>();
        ArrayList<String> failureList = new ArrayList<>(originalFullTestList);
        final ArrayList<String> trimmedFailureList;
        ArrayList<String> statesThisRound;
        
        // Deciding on test size
        final int DEFAULT_TEST_SIZE_MAX = 50;
        final int originalFullTestSize = originalFullTestList.size();
        int testSize = originalFullTestSize;
        if (DEFAULT_TEST_SIZE_MAX < testSize) {
            System.out.println("There are " + testSize + " prompts to test.");
            System.out.println("Do you want to choose a smaller number for this test?");
            System.out.print("  yes or no (will default to no): ");
            String choice = reader.nextLine();
            if (choice.toLowerCase().equals("yes")) {
                System.out.print("How large a list do you want (will default to " + DEFAULT_TEST_SIZE_MAX + " on invalid choice): ");
                try {
                    testSize = reader.nextInt();
                    reader.nextLine(); // skip the remainder of the current input line (including the newline char)
                    if ( (testSize < 1) || (testSize > originalFullTestSize) )
                        testSize = DEFAULT_TEST_SIZE_MAX;
                } catch (InputMismatchException e) {
                    testSize = DEFAULT_TEST_SIZE_MAX;
                }
            }
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

        // The main loop routine
        do {
        failureList = new ArrayList<String>(trimmedFailureList);
        while ( (failureList != null) && (size = failureList.size()) > 0) {
            System.out.println("Remaining answers set:");
            HelperFunctions.printValues(failureList, stateCapitalsProps);
                
            statesThisRound = new ArrayList<>(failureList);
            while (size>0) {
                randIndex = rand.nextInt(size);
                state = statesThisRound.remove(randIndex);
                capital = stateCapitalsProps.getProperty(state);
    
                System.out.println("State:     " + state);
                System.out.print("Capital:   ");
                String guess = reader.nextLine();
                if (!guess.toLowerCase().equals(capital.toLowerCase()))
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
                System.out.println("You have " + size + " prompts left to get correct. Wanna play again?");
                System.out.print("  yes or no (will default to yes): ");
                repeat = reader.nextLine();
                if (repeat.toLowerCase().equals("no"))
                    failureList = null;
            }
        }
            System.out.println("Do you want to repeat this exam?");
            System.out.print("  yes or no (will default to yes): ");
            repeat = reader.nextLine();
        } while (!repeat.toLowerCase().equals("no"));
        
        reader.close();
    }

}

class HelperFunctions {
    public static void printValues(ArrayList<String> keyList, Properties props) {
      /**
       * Given a list of keys and a Properties object cover AT LEAST all the keys
       * from the other object (possibly more than that), print out the values
       * from the Properties object corresponding the keys contained within the key
       * list. No test against duplication of values or keys is performed.
       **/
        int MAX_LINE_WIDTH = 110;
        int MIN_BUFFER = 2;
        String MIN_BUFFER_STR = repeatString(" ", MIN_BUFFER);
        int numValues = keyList.size();
        int numValuesPerLine;
        int maxValueWidth=0;
        int posInLine = 0;
        ArrayList<String> valueList = new ArrayList<>();

        for ( String key : keyList )
            valueList.add(props.getProperty(key));

        for ( String value : valueList )
            maxValueWidth = max(maxValueWidth, value.length());

        numValuesPerLine = (MAX_LINE_WIDTH-MIN_BUFFER) / (maxValueWidth+2*MIN_BUFFER);
        numValuesPerLine = max(1, numValuesPerLine);

        System.out.println("Remaining values are...");
        for ( String value : valueList ) {
            int bufferLen = maxValueWidth + MIN_BUFFER - value.length();
            System.out.print(MIN_BUFFER_STR);
            System.out.print(value);
            System.out.print(repeatString(" ", bufferLen));
            posInLine = (posInLine+1) % numValuesPerLine;
            if (posInLine == 0)
                System.out.println();
        }
        if (posInLine != 0) // Add an extra linebreak unless the last line already added it
            System.out.println();
        System.out.println(); // An an extra, extra linebreak
    }

    public static String repeatString(String str, int n) {
        StringBuilder ret = new StringBuilder(n);
        for (int i=0; i<n; i++)
            ret.append(str);
        return ret.toString();
    }
}
