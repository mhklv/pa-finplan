package org.mchklv.finplan.server;

import java.util.List;

import org.apache.commons.cli.*;


public class Main {
    static int SERVER_PORT = 33334;
    
    
    
    public static void main(String[] args) {
        CommandLine commandLine = ArgumentsParser.parseArguments(args);
        DatabaseConnectionProvider.initDatabase(commandLine);
        
        SERVER_PORT = Integer.parseInt(commandLine.getOptionValue("port", String.valueOf(SERVER_PORT)));

        Server server = new Server(SERVER_PORT);
        server.run();
    }
}
