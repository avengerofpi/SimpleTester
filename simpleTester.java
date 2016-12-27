//package stateCapitals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.InputMismatchException;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class simpleTester {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        // Setup a random number generator for later use
        Random rand = new Random();

        // Easy access to print and println
        
        
        // Variables for interacting with the testing list(s)
        int size;
        int randIndex;
        String state, capital;
        String repeat;

        // Choose the properties file to use for the test
        String propsFileName;
        propsFileName = "/home/cory/git/stateCapitals/stateCapitals.props";
        propsFileName = "/home/cory/git/stateCapitals/countryCapitals.props";
        
        // Make sure the chosen test property file exists
        File propsFile = new File(propsFileName);
        if (!propsFile.exists()) {
            String errorMsg = "ERROR: Could not find the chosen properties file: " + propsFileName;
            System.out.println(errorMsg);
            int ignoreMe = System.in.read();
            System.out.println("" + ignoreMe);
            System.exit(0);
        }

        // Choose how to interact with the user (e.g., using java.util.Scanner)
        Scanner reader = new Scanner(System.in);
        
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
                    if ( (testSize < 1) || (testSize > originalFullTestSize) )
                        testSize = DEFAULT_TEST_SIZE_MAX;
                } catch (InputMismatchException e) {
                    testSize = DEFAULT_TEST_SIZE_MAX;
                }
            }
        }

        // Make sure testSize is valid
        String errorMsg = "Somehow an invalid testSize (" + testSize + ") has been selected. Should be in the range 1.." + originalFullTestSize;
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
            statesThisRound = new ArrayList<>(failureList);
            while (size>0) {
                randIndex = rand.nextInt(size);
                state = statesThisRound.remove(randIndex);
                capital = stateCapitalsProps.getProperty(state);
    
                System.out.println("State:   " + state);
                System.out.print("Capital: ");
                String guess = reader.nextLine();
                if (!guess.toLowerCase().equals(capital.toLowerCase()))
                    System.out.println("Incorrect: " + capital);
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
            System.out.print("  yes or no (will default to no): ");
            repeat = reader.nextLine();
        } while (repeat.toLowerCase().equals("yes"));
        
        reader.close();
    }

}
