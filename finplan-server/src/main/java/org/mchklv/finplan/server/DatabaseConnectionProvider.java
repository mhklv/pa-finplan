package org.mchklv.finplan.server;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.dbcp2.BasicDataSource;

public class DatabaseConnectionProvider {
    private static String databaseAddress, databaseName, databaseUsername, databasePassword;
    private static BasicDataSource dataSource;
    
    
    public static Connection createConnection() {
        Connection DBConn = null;
        
        try {
            String url = String.format("jdbc:mysql://%s/%s", databaseAddress, databaseName);
            DBConn = DriverManager.getConnection(url, databaseUsername, databasePassword);
            DBConn.setAutoCommit(false);
        }
        catch (SQLException e) {
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();
        }
        
        return DBConn;
    }

    
    public static BasicDataSource setupDataSource() throws SQLException{
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.mariadb.jdbc.Driver");
        dataSource.setUrl(DatabaseConnectionProvider.getDBURL());
        dataSource.setUsername(DatabaseConnectionProvider.getDBUsername());
        dataSource.setPassword(DatabaseConnectionProvider.getDBUserPassword());
        dataSource.setDefaultCatalog(DatabaseConnectionProvider.getDBName());

        dataSource.setDefaultAutoCommit(false);

        dataSource.setMinIdle(5);
        dataSource.setMaxTotal(130);
        dataSource.setMaxIdle(20);
        dataSource.setMaxWaitMillis(-1);

        dataSource.getConnection().close();

        return dataSource;
    }

    public static DataSource getDataSource() {
        return dataSource;
    }
    
    public static String getDBURL() {
        return String.format("jdbc:mysql://%s/%s", databaseAddress, databaseName);
    }

    public static String getDBUsername() {
        return databaseUsername;
    }

    public static String getDBName() {
        return databaseName;
    }

    public static String getDBUserPassword() {
        return databasePassword;
    }

    
    public static void initDatabase(CommandLine commandLine) {
        databaseAddress = commandLine.getOptionValue("dbaddr");
        databaseName = commandLine.getOptionValue("dbname");
        databaseUsername = commandLine.getOptionValue("dbusername");
        databasePassword = commandLine.getOptionValue("dbpass");
        
        Connection testConn = createConnection();
        if (testConn == null) {
            System.exit(1);
        }

        try {
            initDatabaseTables();
            dataSource = setupDataSource();
        }
        catch (SQLException e) {
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();

            System.exit(1);
        }
    }

    private static void initDatabaseTables() throws SQLException {
        Connection dbConn = createConnection();
        DatabaseMetaData metadata = dbConn.getMetaData();
        ResultSet tablesResSet = metadata.getTables(null, null, null, new String[] {"TABLE"});
        LinkedList<String> presentTableNames = new LinkedList<String>();

        while (tablesResSet.next()) {
            presentTableNames.add(tablesResSet.getString("TABLE_NAME"));
        }

        tablesResSet.close();
        
        createMissingTables(dbConn, presentTableNames);

        if (!presentTableNames.contains("keyValueGroup")) {
            DataStorage.insertDefaultKVGroups(dbConn, true);
        }
        if (!presentTableNames.contains("expenseCategory")) {
            DataStorage.insertDefaultExpCats(dbConn, true);
        }
        
        dbConn.close();
    }

    private static void createMissingTables(Connection dbconn, List<String> presentTables) throws SQLException {
        String[][] tablesNamesAndDefs = DatabaseTables.TABLES_NAMES_AND_DEFINITIONS;
        
        for (int i = 0; i < tablesNamesAndDefs.length; ++i) {
            String curTable = tablesNamesAndDefs[i][0];
            boolean curTablePresent = false;

            for (String curPresentTable : presentTables) {
                if (curTable.toUpperCase().equals(curPresentTable.toUpperCase())) {
                    curTablePresent = true;
                    break;
                }
            }
            
            if (!curTablePresent) {
                DatabaseTables.createTableByIndex(dbconn, i);
            }
        }
    }
}
