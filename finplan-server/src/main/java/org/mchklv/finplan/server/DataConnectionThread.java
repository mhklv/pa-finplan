package org.mchklv.finplan.server;

import java.io.*;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;

import javax.net.ssl.SSLSocket;
import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.mchklv.finplan.common.*;
import org.mchklv.finplan.common.messageBundles.*;


public class DataConnectionThread implements Runnable {
    private final int SO_TIMEOUT = 120000;
    
    private SSLSocket connSocket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private Connection dbConn;
    private ProtectedArea authedProtectedArea;

    private boolean isInitSuccessful; 
    private boolean clientAuthorized = false;
    private int invalidAuthTriesLeft = 100000; // tmp
    private boolean clientBrokeProtocol = false;

    
    public DataConnectionThread(SSLSocket socket) {
        connSocket = socket;
        isInitSuccessful = init();
    }

    
    public void run() {
        if (!isInitSuccessful) {
            try {
                if (connSocket != null) connSocket.close();
                if (dbConn != null) dbConn.close();
            }
            catch (Throwable e) {
                System.out.println("Error " + e.getMessage());
                e.printStackTrace();
            }
            
            return;
        }
        
        try {
            work();
        }
        catch (Throwable e) {
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();
        }
        finally {
            if (connSocket != null) {
                try {
                    connSocket.close();
                }
                catch (IOException e) {
                    System.out.println("Error " + e.getMessage());
                    e.printStackTrace();
                }
            }

            if (dbConn != null) {
                try {
                    dbConn.close();
                }
                catch (SQLException e) {
                    System.out.println("Error " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("Connection closed with " + connSocket.getInetAddress());
        }
    }

    // Returns true if initialization is sucessfull
    private boolean init() {
        if (connSocket == null || !connSocket.isConnected()) {
            return false;
        }
        
        try {
            outputStream = new ObjectOutputStream(new BufferedOutputStream(connSocket.getOutputStream()));
            outputStream.flush();
            inputStream = new ObjectInputStream(new BufferedInputStream(connSocket.getInputStream()));

            DataSource ds = DatabaseConnectionProvider.getDataSource();

            try {
                dbConn = ds.getConnection();
            }
            catch (SQLException e) {
                System.out.println("Error " + e.getMessage());
                e.printStackTrace();

                return false;
            }

            if (dbConn == null) {
                return false;
            }
        }
        catch (IOException e) {
            System.out.println("Error during establishing connection with " + connSocket.getInetAddress());
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();

            return false;
        }

        try {
            connSocket.setSoTimeout(SO_TIMEOUT);
            connSocket.setKeepAlive(true);
        }
        catch (SocketException e) {
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();

            return false;
        }
        
        System.out.println("Connection established with " + connSocket.getInetAddress());
        return true;
    }
    
    private void work() throws IOException, ClassNotFoundException {
        Message message;
        
        while (true) {
            if (clientBrokeProtocol) {
                System.out.println("Protocol has been broken by client " + connSocket.getInetAddress());
                break;
            }

            try {
                message = (Message) inputStream.readObject();
            }
            catch (SocketTimeoutException e) {
                System.out.println("Connection timed out for "+ connSocket.getInetAddress());
                break;
            }

            System.out.println("Got message from " + connSocket.getInetAddress() + " : " + message.toString());
            processInitialMessage(message);
        }
    }


    private void processInitialMessage(Message message) throws IOException {
        try {
            switch (message.getCommand()) {
                case MessageCommands.REGISTER:
                    processRegister(message);
                    break;

                case MessageCommands.AUTHORIZE:
                    processAuthorize(message);
                    break;

                case MessageCommands.INSERT_RECORD:
                    if (!clientAuthorized) {
                        clientBrokeProtocol = true;
                    }
                    else {
                        processInsertRecord(message);
                    }
                    break;

                case MessageCommands.UPDATE_RECORD:
                    if (!clientAuthorized) {
                        clientBrokeProtocol = true;
                    }
                    else {
                        processUpdateRecord(message);
                    }
                    break;

                case MessageCommands.DELETE_RECORD:
                    if (!clientAuthorized) {
                        clientBrokeProtocol = true;
                    }
                    else {
                        processDeleteRecord(message);
                    }
                    break;

                case MessageCommands.RETRIEVE:
                    if (!clientAuthorized && message.getSubCommand() != MessageCommands.RetrieveSubCommands.PROTECTED_AREA) {
                        clientBrokeProtocol = true;
                    }
                    else {
                        processRetrieveRecord(message);
                    }
                    break;

                default:
                    clientBrokeProtocol = true;
            }
        }
        catch (SQLException e) {
            outputStream.writeObject(new Message(MessageCommands.IO_ERROR, 0, null));
            outputStream.flush();
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processRegister(Message message) throws IOException, SQLException {
        RegistrationBundle regBundle = (RegistrationBundle) message.getPayload();
        boolean isQuotaAvailable = DataStorage.regQuotaAvailable(dbConn, regBundle.getRegQuotaKey());
        
        if (isQuotaAvailable) {
            ProtectedArea newPA = regBundle.getProtectedArea();
            newPA.generateHash(regBundle.getPlainPassword());
            DataStorage.insertProtectedArea(dbConn, newPA, false);
            DataStorage.insertDefaultAdmEconTasks(dbConn, newPA, true);
            DataStorage.spendOutRegQuota(dbConn, regBundle.getRegQuotaKey(), true);
                
            regBundle.setAnswer("OK");
            outputStream.writeObject(message);
            outputStream.flush();
        }
        else {
            message.setCommand(MessageCommands.ERROR);
            regBundle.setAnswer("Invalid quota key");
            outputStream.writeObject(message);
            outputStream.flush();
                
            clientBrokeProtocol = true;
        }
    }

    private void processAuthorize(Message message) throws IOException, SQLException {
        AuthorizationBundle authBundle = (AuthorizationBundle) message.getPayload();
        ProtectedArea authArea = authBundle.getProtectedArea();
        DataStorage.getPAPassHash(dbConn, authArea);
        boolean isPassValid = authArea.isPasswordValid(authBundle.getPlainPassword());

        if (isPassValid) {
            authBundle.setAnswer("OK");
            outputStream.writeObject(message);
            outputStream.flush();
            clientAuthorized = true;
            authedProtectedArea = authBundle.getProtectedArea();
        }
        else {
            message.setCommand(MessageCommands.ERROR);
            authBundle.setAnswer("Invalid password");
            outputStream.writeObject(message);
            outputStream.flush();
                    
            if (invalidAuthTriesLeft == 0) {
                clientBrokeProtocol = true;
            }
            else {
                --invalidAuthTriesLeft;
            }
        }
    }

    private void processInsertRecord(Message message) throws IOException, SQLException {
        InsertRecordBundle insBundle = (InsertRecordBundle) message.getPayload();
        
        switch (message.getSubCommand()) {
            case MessageCommands.RecordTypes.KEY_VALUE: {
                KeyValue newKeyValue = (KeyValue) insBundle.getRecordObj();
                KeyValueGroup parentGroup = (KeyValueGroup) insBundle.getParentObj();
                DataStorage.insertKeyValue(dbConn, newKeyValue, authedProtectedArea, parentGroup, true);
                outputStream.writeObject(new Message(message.getCommand(), message.getSubCommand(), newKeyValue));
                break;
            }
                
            case MessageCommands.RecordTypes.RATIONALE_UNIT: {
                RationaleUnit newRationaleUnit = (RationaleUnit) insBundle.getRecordObj();
                KeyValue parentKV = (KeyValue) insBundle.getParentObj();
                DataStorage.insertRationaleUnit(dbConn, newRationaleUnit, parentKV, true);
                outputStream.writeObject(new Message(message.getCommand(), message.getSubCommand(), newRationaleUnit));
                break;
            }
                
            case MessageCommands.RecordTypes.COND_INDICATOR: {
                ConditionIndicator newCondIndicator = (ConditionIndicator) insBundle.getRecordObj();
                RationaleUnit parentRationaleUnit = (RationaleUnit) insBundle.getParentObj();
                DataStorage.insertCondIndicator(dbConn, newCondIndicator, parentRationaleUnit, true);
                outputStream.writeObject(new Message(message.getCommand(), message.getSubCommand(), newCondIndicator));
                break;
            }

            case MessageCommands.RecordTypes.TASK: {
                Task newTask = (Task) insBundle.getRecordObj();
                RationaleUnit parentRationaleUnit = (RationaleUnit) insBundle.getParentObj();
                DataStorage.insertTask(dbConn, newTask, parentRationaleUnit, true);
                outputStream.writeObject(new Message(message.getCommand(), message.getSubCommand(), newTask));
                break;
            }

            case MessageCommands.RecordTypes.EVENT: {
                Event newEvent = (Event) insBundle.getRecordObj();
                Task parentTask = (Task) insBundle.getParentObj();
                DataStorage.insertEvent(dbConn, newEvent, parentTask, true);
                outputStream.writeObject(new Message(message.getCommand(), message.getSubCommand(), newEvent));
                break;
            }
                
            case MessageCommands.RecordTypes.PARTNER_ORG: {
                PartnerOrg newPartnerOrg = (PartnerOrg) insBundle.getRecordObj();
                Event parentEvent = (Event) insBundle.getParentObj();
                DataStorage.insertPartnerOrg(dbConn, newPartnerOrg, parentEvent, true);
                outputStream.writeObject(new Message(message.getCommand(), message.getSubCommand(), newPartnerOrg));
                break;
            }

            case MessageCommands.RecordTypes.EXP_CATEGORY: {
                ExpenseCategory newExpCat = (ExpenseCategory) insBundle.getRecordObj();
                DataStorage.insertExpCategory(dbConn, newExpCat, true);
                outputStream.writeObject(new Message(message.getCommand(), message.getSubCommand(), newExpCat));
                break;
            }
                
            case MessageCommands.RecordTypes.EXPENSE: {
                Expense newExp = (Expense) insBundle.getRecordObj();
                Event parentEvent = (Event) insBundle.getParentObj();
                DataStorage.insertExpense(dbConn, newExp, parentEvent, true);
                outputStream.writeObject(new Message(message.getCommand(), message.getSubCommand(), newExp));
                break;
            }

            case MessageCommands.RecordTypes.EVENT_DATE_INTERVAL: {
                LocalDateInterval newDateInterval = (LocalDateInterval) insBundle.getRecordObj();
                Event parentEvent = (Event) insBundle.getParentObj();
                DataStorage.insertEventDateInterval(dbConn, newDateInterval, parentEvent, true);
                outputStream.writeObject(new Message(message.getCommand(), message.getSubCommand(), newDateInterval));
                break;
            }

            case MessageCommands.RecordTypes.ADM_ECON_TASK: {
                AdmEconTask newTask = (AdmEconTask) insBundle.getRecordObj();
                DataStorage.insertAdmEconTask(dbConn, newTask, authedProtectedArea, true);
                outputStream.writeObject(new Message(message.getCommand(), message.getSubCommand(), newTask));
                break;
            }

            case MessageCommands.RecordTypes.ADM_ECON_CI: {
                ConditionIndicator newCI = (ConditionIndicator) insBundle.getRecordObj();
                AdmEconTask parentTask = (AdmEconTask) insBundle.getParentObj();
                DataStorage.insertAdmEconCI(dbConn, newCI, parentTask, true);
                outputStream.writeObject(new Message(message.getCommand(), message.getSubCommand(), newCI));
                break;
            }

            case MessageCommands.RecordTypes.ADM_ECON_EVENT: {
                Event newEvent = (Event) insBundle.getRecordObj();
                AdmEconTask parentTask = (AdmEconTask) insBundle.getParentObj();
                DataStorage.insertAdmEconEvent(dbConn, newEvent, parentTask, true);
                outputStream.writeObject(new Message(message.getCommand(), message.getSubCommand(), newEvent));
                break;
            }

            case MessageCommands.RecordTypes.ADM_ECON_EXPENSE: {
                Expense newExpense = (Expense) insBundle.getRecordObj();
                Event parentEvent = (Event) insBundle.getParentObj();
                DataStorage.insertAdmEconExpense(dbConn, newExpense, parentEvent, true);
                outputStream.writeObject(new Message(message.getCommand(), message.getSubCommand(), newExpense));
                break;
            }

            case MessageCommands.RecordTypes.ADM_ECON_EVENT_DATE_INTERVAL: {
                LocalDateInterval newDateInterval = (LocalDateInterval) insBundle.getRecordObj();
                Event parentEvent = (Event) insBundle.getParentObj();
                DataStorage.insertAdmEconDateInterval(dbConn, newDateInterval, parentEvent, true);
                outputStream.writeObject(new Message(message.getCommand(), message.getSubCommand(), newDateInterval));
                break;
            }
                
            default:
                clientBrokeProtocol = true;
                return;
        }

        outputStream.flush();                
    }

    private void processUpdateRecord(Message message) throws IOException, SQLException {
        switch (message.getSubCommand()) {
            case MessageCommands.RecordTypes.KEY_VALUE: {
                KeyValue updatedKeyValue = (KeyValue) message.getPayload();
                DataStorage.updateKeyValue(dbConn, updatedKeyValue, true);
                break;
            }
                
            case MessageCommands.RecordTypes.RATIONALE_UNIT: {
                RationaleUnit updatedRU = (RationaleUnit) message.getPayload();
                DataStorage.updateRationaleUnit(dbConn, updatedRU, true);
                break;
            }
                
            case MessageCommands.RecordTypes.COND_INDICATOR: {
                ConditionIndicator updatedCI = (ConditionIndicator) message.getPayload();
                DataStorage.updateCondIndicator(dbConn, updatedCI, true);
                break;
            }

            case MessageCommands.RecordTypes.TASK: {
                Task updatedTask = (Task) message.getPayload();
                DataStorage.updateTask(dbConn, updatedTask, true);
                break;
            }

            case MessageCommands.RecordTypes.EVENT: {
                Event updatedEvent = (Event) message.getPayload();
                DataStorage.updateEvent(dbConn, updatedEvent, true);
                break;
            }
                
            case MessageCommands.RecordTypes.PARTNER_ORG: {
                PartnerOrg updatedPartnerOrg = (PartnerOrg) message.getPayload();
                DataStorage.updatePartnerOrg(dbConn, updatedPartnerOrg, true);
                break;
            }

            case MessageCommands.RecordTypes.EXP_CATEGORY: {
                ExpenseCategory updatedExpenseCategory = (ExpenseCategory) message.getPayload();
                DataStorage.updateExpCategory(dbConn, updatedExpenseCategory, true);
                break;
            }
                
            case MessageCommands.RecordTypes.EXPENSE: {
                Expense updatedExpense = (Expense) message.getPayload();
                DataStorage.updateExpense(dbConn, updatedExpense, true);
                break;
            }

            case MessageCommands.RecordTypes.EVENT_DATE_INTERVAL: {
                LocalDateInterval updatedDateInterval = (LocalDateInterval) message.getPayload();
                DataStorage.updateDateInterval(dbConn, updatedDateInterval, true);
                break;
            }
                
            case MessageCommands.RecordTypes.ADM_ECON_TASK: {
                AdmEconTask updatedTask = (AdmEconTask) message.getPayload();
                DataStorage.updateAdmEconTask(dbConn, updatedTask, true);
                break;
            }
                
            case MessageCommands.RecordTypes.ADM_ECON_CI: {
                ConditionIndicator updatedCI = (ConditionIndicator) message.getPayload();
                DataStorage.updateAdmEconCI(dbConn, updatedCI, true);
                break;
            }

            case MessageCommands.RecordTypes.ADM_ECON_EVENT: {
                Event updatedEvent = (Event) message.getPayload();
                DataStorage.updateAdmEconEvent(dbConn, updatedEvent, true);
                break;
            }

            case MessageCommands.RecordTypes.ADM_ECON_EXPENSE: {
                Expense updatedExpense = (Expense) message.getPayload();
                DataStorage.updateAdmEconExpense(dbConn, updatedExpense, true);
                break;
            }

            case MessageCommands.RecordTypes.ADM_ECON_EVENT_DATE_INTERVAL: {
                LocalDateInterval updatedDateInterval = (LocalDateInterval) message.getPayload();
                DataStorage.updateAdmEconDateInterval(dbConn, updatedDateInterval, true);
                break;
            }
                
            default:
                clientBrokeProtocol = true;
                return;
        }

        message.setPayload("OK");
        outputStream.writeObject(message);
        outputStream.flush();
    }

    private void processDeleteRecord(Message message) throws IOException, SQLException {
        switch (message.getSubCommand()) {
            case MessageCommands.RecordTypes.KEY_VALUE: {
                KeyValue updatedKeyValue = (KeyValue) message.getPayload();
                DataStorage.deleteKeyValue(dbConn, updatedKeyValue, true);
                break;
            }
                
            case MessageCommands.RecordTypes.RATIONALE_UNIT: {
                RationaleUnit updatedRU = (RationaleUnit) message.getPayload();
                DataStorage.deleteRationaleUnit(dbConn, updatedRU, true);
                break;
            }
                
            case MessageCommands.RecordTypes.COND_INDICATOR: {
                ConditionIndicator updatedCI = (ConditionIndicator) message.getPayload();
                DataStorage.deleteCondIndicator(dbConn, updatedCI, true);
                break;
            }

            case MessageCommands.RecordTypes.TASK: {
                Task updatedTask = (Task) message.getPayload();
                DataStorage.deleteTask(dbConn, updatedTask, true);
                break;
            }

            case MessageCommands.RecordTypes.EVENT: {
                Event updatedEvent = (Event) message.getPayload();
                DataStorage.deleteEvent(dbConn, updatedEvent, true);
                break;
            }
                
            case MessageCommands.RecordTypes.PARTNER_ORG: {
                PartnerOrg updatedPartnerOrg = (PartnerOrg) message.getPayload();
                DataStorage.deletePartnerOrg(dbConn, updatedPartnerOrg, true);
                break;
            }

            case MessageCommands.RecordTypes.EXP_CATEGORY: {
                ExpenseCategory updatedExpenseCategory = (ExpenseCategory) message.getPayload();
                DataStorage.deleteExpCategory(dbConn, updatedExpenseCategory, true);
                break;
            }
                
            case MessageCommands.RecordTypes.EXPENSE: {
                Expense updatedExpense = (Expense) message.getPayload();
                DataStorage.deleteExpense(dbConn, updatedExpense, true);
                break;
            }

            case MessageCommands.RecordTypes.EVENT_DATE_INTERVAL: {
                LocalDateInterval updatedDateInterval = (LocalDateInterval) message.getPayload();
                DataStorage.deleteDateInterval(dbConn, updatedDateInterval, true);
                break;
            }

            case MessageCommands.RecordTypes.ADM_ECON_TASK: {
                AdmEconTask updatedTask = (AdmEconTask) message.getPayload();
                DataStorage.deleteAdmEconTask(dbConn, updatedTask, true);
                break;
            }
                
            case MessageCommands.RecordTypes.ADM_ECON_CI: {
                ConditionIndicator updatedCI = (ConditionIndicator) message.getPayload();
                DataStorage.deleteAdmEconCI(dbConn, updatedCI, true);
                break;
            }

            case MessageCommands.RecordTypes.ADM_ECON_EVENT: {
                Event updatedEvent = (Event) message.getPayload();
                DataStorage.deleteAdmEconEvent(dbConn, updatedEvent, true);
                break;
            }

            case MessageCommands.RecordTypes.ADM_ECON_EXPENSE: {
                Expense updatedExpense = (Expense) message.getPayload();
                DataStorage.deleteAdmEconExpense(dbConn, updatedExpense, true);
                break;
            }

            case MessageCommands.RecordTypes.ADM_ECON_EVENT_DATE_INTERVAL: {
                LocalDateInterval updatedDateInterval = (LocalDateInterval) message.getPayload();
                DataStorage.deleteAdmEconDateInterval(dbConn, updatedDateInterval, true);
                break;
            }
                
            default:
                clientBrokeProtocol = true;
                return;
        }

        message.setPayload("OK");
        outputStream.writeObject(message);
        outputStream.flush();
    }

    private void processRetrieveRecord(Message message) throws IOException, SQLException {
        switch (message.getSubCommand()) {
            case MessageCommands.RetrieveSubCommands.PROTECTED_AREA: {
                LinkedList<ProtectedArea> resProtectedAreas = DataStorage.getAllProtectedAreas(dbConn);
                message.setPayload(resProtectedAreas);
                outputStream.writeObject(message);
                break;
            }
            
            case MessageCommands.RetrieveSubCommands.KEY_VALUE_GROUPS: {
                LinkedList<KeyValueGroup> resKeyValueGroups = DataStorage.getAllKeyValueGroups(dbConn);
                message.setPayload(resKeyValueGroups);
                outputStream.writeObject(message);
                break;
            }

            case MessageCommands.RetrieveSubCommands.KEY_VALUES: {
                KeyValueGroup parentKVG = (KeyValueGroup) message.getPayload();
                LinkedList<KeyValue> resKeyValues =
                    DataStorage.getAllKeyValues(dbConn, authedProtectedArea, parentKVG);
                message.setPayload(resKeyValues);
                outputStream.writeObject(message);
                break;
            }

            case MessageCommands.RetrieveSubCommands.EVENTS: {
                Task parentTask = (Task) message.getPayload();
                LinkedList<Event> resEvents = DataStorage.getAllEvents(dbConn, parentTask);
                message.setPayload(resEvents);
                outputStream.writeObject(message);
                break;
            }

            case MessageCommands.RetrieveSubCommands.EXP_CATEGORIES: {
                LinkedList<ExpenseCategory> resExpCats = DataStorage.getAllExpCategories(dbConn);
                message.setPayload(resExpCats);
                outputStream.writeObject(message);
                break;
            }

            case MessageCommands.RetrieveSubCommands.ADM_ECON_TASKS: {
                LinkedList<AdmEconTask> resTasks = DataStorage.getAllAdmEconTasks(dbConn, authedProtectedArea);
                message.setPayload(resTasks);
                outputStream.writeObject(message);
                break;
            }
                
            default:
                clientBrokeProtocol = true;
                return;
        }

        outputStream.flush();
    }
}
