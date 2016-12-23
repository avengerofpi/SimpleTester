//package stateCapitals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

public class simpleTester {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        
        String propsFileName = "/home/cory/git/stateCapitals/stateCapitals.props";
        Properties stateCapitalsProps = new Properties();
        File propsFile = new File(propsFileName);
        Scanner reader = new Scanner(System.in);
        
        System.out.println(propsFileName);

        if (!propsFile.exists()) {
            String errorMsg = "ERROR: Could not find the chosen properties file: " + propsFileName;
            System.out.println(errorMsg);
            int ignoreMe = System.in.read();
            System.out.println("" + ignoreMe);
            reader.close();
            return;
        }
        
        stateCapitalsProps.load(new FileInputStream(propsFile));
        final ArrayList<String> states = new ArrayList<>(stateCapitalsProps.stringPropertyNames());
        ArrayList<String> successList = new ArrayList<>();
        ArrayList<String> failureList = new ArrayList<>(states);
        ArrayList<String> statesThisRound;
        
        Random rand = new Random();
        int size;
        int randIndex;
        String state, capital;
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
                System.out.println("You have " + size + " States left to get correct. Wanna play again?");
                System.out.print("yes or no (will default to yes): ");
                String repeat = reader.nextLine();
                if (repeat.toLowerCase().equals("no"))
                    failureList = null;
            }
        }
        
        reader.close();
    }

}
