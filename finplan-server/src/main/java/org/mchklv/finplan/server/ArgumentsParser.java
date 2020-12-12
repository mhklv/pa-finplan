package org.mchklv.finplan.server;

import org.apache.commons.cli.*;

public class ArgumentsParser {
    private static Options createCommandLineOptions() {
        Options resOptions = new Options();
        Option option;

        option = new Option("dbaddr", "DBAddress", true,
                            "Hostname of the machine which DMS runs on.\nCould also be ab IP address.");
        option.setRequired(true);
        resOptions.addOption(option);

        option = new Option("dbname", "DBName", true,
                "Name of the database to which it is the server will connect to.");
        option.setRequired(true);
        resOptions.addOption(option);

        option = new Option("dbusername", "DBUsername", true,
                "User's name that will be used to ocnnect to the database.");
        option.setRequired(true);
        resOptions.addOption(option);

        option = new Option("dbpass", "DBPassword", true,
                            "Password that will be used to connect to the database.");
        option.setRequired(true);
        resOptions.addOption(option);

        option = new Option("port", "ServerPort", true,
                            "Local port on which the server is run.");
        option.setRequired(false);
        resOptions.addOption(option);

        return resOptions;
    }


    public static CommandLine parseArguments(String[] args) {
        Options commandlineOptions = createCommandLineOptions();
        
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine commandLine = null;

        try {
            commandLine = parser.parse(commandlineOptions, args);
        }
        catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", commandlineOptions);

            System.exit(1);
        }

        return commandLine;
    }
}
