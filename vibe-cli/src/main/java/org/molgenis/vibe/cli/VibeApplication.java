package org.molgenis.vibe.cli;

import org.apache.commons.cli.ParseException;
import org.molgenis.vibe.cli.io.options_digestion.CommandLineOptionsParser;
import org.molgenis.vibe.cli.io.options_digestion.VibeOptions;
import org.molgenis.vibe.cli.properties.VibePropertiesLoader;

import java.io.IOException;

/**
 * The main application class.
 */
public class VibeApplication {
    /**
     * The main method for when used as a standalone application.
     * @param args {@link String}{@code []}
     */
    public static void main(String[] args) {
        if ( loadPropertiesFile() ) { // If properties file loading succeeded, continues.
            try {
                // Parses user-input.
                VibeOptions vibeOptions = new VibeOptions();
                CommandLineOptionsParser.parse(args, vibeOptions);

                // If all input correctly parsed, runs app.
                if (vibeOptions.validate()) {
                    executeRunMode(vibeOptions);
                } else { // Errors caused by invalid options configuration.
                    printUnexpectedExceptionOccurred();
                    System.err.println(vibeOptions.toString());
                }
            } catch (ParseException e) { // Errors generated during options parsing.
                System.err.println(e.getLocalizedMessage());
            }
        }
    }

    /**
     * Parses application properties.
     * <b>Should always be ran first (as it sets the values for the VibeProperties enum)!</b>
     * @return {@code true} if property file was loaded, {@code false} if it failed to do so
     */
    private static boolean loadPropertiesFile() {
        try {
            VibePropertiesLoader.loadProperties();
        } catch (IOException e) {
            System.err.println("Failed to load properties file. Please contact the developer.");
            return false;
        }
        return true;
    }

    /**
     * Runs the selected {@link RunMode} and handles any {@link Exception} that is triggered in it.
     * @param vibeOptions the {@link VibeOptions} as generated through {@link CommandLineOptionsParser)}
     */
    private static void executeRunMode(VibeOptions vibeOptions) {
        try {
            vibeOptions.getRunMode().run(vibeOptions);
        } catch (IOException e) {
            System.err.println(e.getLocalizedMessage());
        } catch (Exception e) { // Errors generated while running the app.
            printUnexpectedExceptionOccurred();
            e.printStackTrace();
        }
    }

    public static void printUnexpectedExceptionOccurred() {
        System.err.println("######## ######## ########");
        System.err.println("An unexpected exception occurred. Please notify the developer (see https://github.com/molgenis/vibe) and supply the text as seen below.");
        System.err.println("######## ######## ########");
    }
}
