package org.mchklv.finplan.client;

import java.io.*;
import java.net.SocketTimeoutException;
import java.security.*;
import java.security.cert.*;
import java.util.LinkedList;

import javax.net.ssl.*;

import org.mchklv.finplan.client.ui.DateIntervalNode;
import org.mchklv.finplan.common.*;
import org.mchklv.finplan.common.messageBundles.*;


public class NetworkDataProvider implements DataProvider {
    private SSLSocket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private String serverAddress;

    private ProtectedArea lastAuthedProtectedArea;
    private String lastAuthedPassword;
    
    
    
    public NetworkDataProvider(String address) {
        this.serverAddress = address;
    }

    public void connect() throws GeneralSecurityException, IOException {
        if (socket != null) {
            socket.close();
        }

        int remotePort = LocalStorageManager.getSettingsManager().getRemoteServerPort();
        socket = getSocket(serverAddress, remotePort);
        // TODO: calculate timeout dynamically based on latency.
        // Probably need to 'ping' server every N seconds in a separate thread.
        socket.setKeepAlive(true);
        outputStream = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        outputStream.flush();
        inputStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
        
        socket.setSoTimeout(3000);
    }

    public void closeConnection() throws IOException {
        if (socket != null) {
            socket.close();
        }
    }

    
    public void register(ProtectedArea protectedArea, String regQuotaKey, String newPassword)
        throws IOException, ClassNotFoundException, IllegalArgumentException, GeneralSecurityException {
        
        RegistrationBundle regBundle = new RegistrationBundle(regQuotaKey, protectedArea, newPassword, null);
        Message regMessage = new Message(MessageCommands.REGISTER, 0, regBundle);

        // tryWriteObject(regMessage);
        // // Message answerMessage = (Message) inputStream.readObject();
        // Message answerMessage = (Message) tryReadObject();
        Message answerMessage = (Message) tryRequestResponse(regMessage);
        
        RegistrationBundle answerRegBundle = (RegistrationBundle) answerMessage.getPayload();
        
        if (isErrorMessage(answerMessage)) {
            throw new IllegalArgumentException(answerRegBundle.getAnswer());
        }
        else if (answerMessage.getCommand() == MessageCommands.REGISTER) {
            ProtectedArea answerPA = answerRegBundle.getProtectedArea();
            protectedArea.setId(answerPA.getId());
        }
        else {
            throw new IllegalArgumentException("Unknown error in server's response");
        }
    }
    
    public void authorize(ProtectedArea protectedArea, String password)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        AuthorizationBundle authBundle = new AuthorizationBundle(protectedArea, password, null);
        Message authMessage = new Message(MessageCommands.AUTHORIZE, 0, authBundle);
        // outputStream.writeObject(authMessage);
        // outputStream.flush();
        
        // tryWriteObject(authMessage);
        // // Message answerMessage = (Message) inputStream.readObject();
        // Message answerMessage = (Message) tryReadObject();
        Message answerMessage = (Message) tryRequestResponse(authMessage);
        
        AuthorizationBundle answerAuthBundle = (AuthorizationBundle) answerMessage.getPayload();

        if (isErrorMessage(answerMessage)) {
            String errorMsg = (answerAuthBundle == null) ?
                "Server returned error code" :
                answerAuthBundle.getAnswer();
            throw new IllegalArgumentException(errorMsg);
        }
        else if (answerMessage.getCommand() == MessageCommands.AUTHORIZE) {
            lastAuthedProtectedArea = protectedArea;
            lastAuthedPassword = password;
            return;
        }
        else {
            throw new IllegalArgumentException("Unknown error in server's response");
        }
    }


    
    
    public void insertKeyValue(KeyValue newKeyValue, KeyValueGroup parentGroup)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        KeyValue tmpKeyValue = new KeyValue(newKeyValue);
        tmpKeyValue.setRationaleUnits(null);
        KeyValueGroup tmpParentKVG = new KeyValueGroup(parentGroup);
        tmpParentKVG.setKeyValues(null);

        KeyValue ansKV = (KeyValue) insertRecord(tmpKeyValue, tmpParentKVG, MessageCommands.RecordTypes.KEY_VALUE);
        newKeyValue.setId(ansKV.getId());
    }

    public void insertRationaleUnit(RationaleUnit newRU, KeyValue parentKeyValue)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        RationaleUnit tmpRU = new RationaleUnit(newRU);
        tmpRU.setCondIndicators(null);
        tmpRU.setTasks(null);
        KeyValue tmpParentKeyValue = new KeyValue(parentKeyValue);
        tmpParentKeyValue.setRationaleUnits(null);
        
        RationaleUnit ansRU = (RationaleUnit) insertRecord(tmpRU, tmpParentKeyValue,
                MessageCommands.RecordTypes.RATIONALE_UNIT);
        newRU.setId(ansRU.getId());
    }
    
    public void insertCondIndicator(ConditionIndicator newCI, RationaleUnit parentRU)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        RationaleUnit tmpParentRU = new RationaleUnit(parentRU);
        tmpParentRU.setTasks(null);
        tmpParentRU.setCondIndicators(null);

        ConditionIndicator ansCI = (ConditionIndicator) insertRecord(newCI, tmpParentRU,
                MessageCommands.RecordTypes.COND_INDICATOR);
        newCI.setId(ansCI.getId());
    }

    public void insertTask(Task newTask, RationaleUnit parentRU)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        Task tmpTask = new Task(newTask);
        tmpTask.setEvents(null);
        RationaleUnit tmpParentRU = new RationaleUnit(parentRU);
        tmpParentRU.setTasks(null);
        tmpParentRU.setCondIndicators(null);

        Task ansTask = (Task) insertRecord(tmpTask, tmpParentRU, MessageCommands.RecordTypes.TASK);
        newTask.setId(ansTask.getId());
    }

    public void insertEvent(Event newEvent, Task parentTask)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        Event tmpEvent = new Event(newEvent);
        tmpEvent.setExpenses(null);
        tmpEvent.setDateIntervals(null);
        Task tmpParentTask = new Task(parentTask);
        tmpParentTask.setEvents(null);

        Event ansEvent = (Event) insertRecord(tmpEvent, tmpParentTask, MessageCommands.RecordTypes.EVENT);
        newEvent.setId(ansEvent.getId());
    }
    
    public void insertPartnerOrg(PartnerOrg newPartnerOrg, Event parentEvent)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        Event tmpParentevent = new Event(parentEvent);
        tmpParentevent.setPartnerOrgs(null);
        tmpParentevent.setExpenses(null);
        tmpParentevent.setDateIntervals(null);

        PartnerOrg ansOrg = (PartnerOrg) insertRecord(newPartnerOrg, tmpParentevent,
                MessageCommands.RecordTypes.PARTNER_ORG);
        newPartnerOrg.setId(ansOrg.getId());
    }

    public void insertExpCategory(ExpenseCategory newExpenseCategory)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        ExpenseCategory tmpExpenseCategory = new ExpenseCategory(newExpenseCategory);

        ExpenseCategory ansExpCat = (ExpenseCategory) insertRecord(tmpExpenseCategory, null,
                MessageCommands.RecordTypes.EXP_CATEGORY);
        newExpenseCategory.setId(ansExpCat.getId());
    }

    public void insertExpense(Expense newExp, Event parentEvent)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        Event tmpParentEvent = new Event(parentEvent);
        tmpParentEvent.setExpenses(null);
        tmpParentEvent.setDateIntervals(null);
        // tmpParentEvent.setPartnerOrgs(null);
        
        Expense ansExp = (Expense) insertRecord(newExp, tmpParentEvent, MessageCommands.RecordTypes.EXPENSE);
        newExp.setId(ansExp.getId());
    }

    public void insertEventDateInterval(LocalDateInterval newDateInterval, Event parentEvent)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        Event tmpParentEvent = new Event(parentEvent);
        tmpParentEvent.setExpenses(null);
        tmpParentEvent.setDateIntervals(null);

        LocalDateInterval ansInterval = (LocalDateInterval) insertRecord(newDateInterval,
                                                                         tmpParentEvent, MessageCommands.RecordTypes.EVENT_DATE_INTERVAL);
        newDateInterval.setId(ansInterval.getId());
    }

    public void insertAdmEconTask(AdmEconTask newTask)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        AdmEconTask tmpTask = new AdmEconTask(newTask);
        tmpTask.setAdmEconEvents(null);
        tmpTask.setAdmEconCondIndicators(null);

        AdmEconTask ansTask = (AdmEconTask) insertRecord(tmpTask, null, MessageCommands.RecordTypes.ADM_ECON_TASK);
        newTask.setId(ansTask.getId());
    }

    public void insertAdmEconCI(ConditionIndicator newCondIndicator, AdmEconTask parentTask)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        AdmEconTask tmpParentTask = new AdmEconTask(parentTask);
        tmpParentTask.setAdmEconEvents(null);
        tmpParentTask.setAdmEconCondIndicators(null);

        ConditionIndicator ansCI = (ConditionIndicator) insertRecord(newCondIndicator, tmpParentTask,
                                                                     MessageCommands.RecordTypes.ADM_ECON_CI);
        newCondIndicator.setId(ansCI.getId());
    }

    public void insertAdmEconEvent(Event newEvent, AdmEconTask parentTask)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        AdmEconTask tmpParentTask = new AdmEconTask(parentTask);
        tmpParentTask.setAdmEconEvents(null);
        tmpParentTask.setAdmEconCondIndicators(null);
        Event tmpEvent = new Event(newEvent);
        // tmpEvent.setPartnerOrgs(null);
        tmpEvent.setExpenses(null);
        tmpEvent.setDateIntervals(null);

        Event ansEvent = (Event) insertRecord(tmpEvent, tmpParentTask, MessageCommands.RecordTypes.ADM_ECON_EVENT);
        newEvent.setId(ansEvent.getId());
        
    }

    public void insertAdmEconExpense(Expense newExpense, Event parentEvent)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        Event tmpParentEvent = new Event(parentEvent);
        tmpParentEvent.setExpenses(null);
        tmpParentEvent.setDateIntervals(null);
        // tmpParentEvent.setPartnerOrgs(null);

        Expense ansExp = (Expense) insertRecord(newExpense, tmpParentEvent, MessageCommands.RecordTypes.ADM_ECON_EXPENSE);
        newExpense.setId(ansExp.getId());
    }

    public void insertAdmEconDateInterval(LocalDateInterval newDateInterval, Event parentEvent)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        Event tmpParentEvent = new Event(parentEvent);
        tmpParentEvent.setExpenses(null);
        tmpParentEvent.setDateIntervals(null);

        LocalDateInterval ansInterval = (LocalDateInterval) insertRecord(newDateInterval, tmpParentEvent,
                                                                         MessageCommands.RecordTypes.ADM_ECON_EVENT_DATE_INTERVAL);
        newDateInterval.setId(ansInterval.getId());
    }

    private Object insertRecord(Object record, Object parentRecord, int recordType)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        InsertRecordBundle insBundle = new InsertRecordBundle(record, parentRecord);
        Message insMessage = new Message(MessageCommands.INSERT_RECORD, recordType, insBundle);

        // outputStream.writeObject(insMessage);
        // outputStream.flush();
        
        // tryWriteObject(insMessage);
        // // Message ansMessage = (Message) inputStream.readObject();
        // Message ansMessage = (Message) tryReadObject();
        Message ansMessage = (Message) tryRequestResponse(insMessage);
        outputStream.reset();
        
        if (isErrorMessage(ansMessage)) {
            throw new IllegalArgumentException("Server returned error code " + ansMessage.getCommand());
        }
        else if (ansMessage.getCommand() == MessageCommands.INSERT_RECORD) {
            return ansMessage.getPayload();
        }
        else {
            throw new IllegalArgumentException("Unknown error in server's response");
        }
    }
    


    
    public LinkedList<ProtectedArea> getAllProtectedAreas()
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        return (LinkedList<ProtectedArea>) retrieveRecords(MessageCommands.RetrieveSubCommands.PROTECTED_AREA, null);
    }

    public LinkedList<KeyValueGroup> getAllKeyValueGroups()
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        return (LinkedList<KeyValueGroup>) retrieveRecords(MessageCommands.RetrieveSubCommands.KEY_VALUE_GROUPS, null);
    }

    public LinkedList<KeyValue> getAllKeyValues(KeyValueGroup parentKVG)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        return (LinkedList<KeyValue>) retrieveRecords(MessageCommands.RetrieveSubCommands.KEY_VALUES, parentKVG);
    }

    public LinkedList<Event> getAllEvents(Task parentTask)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        return (LinkedList<Event>) retrieveRecords(MessageCommands.RetrieveSubCommands.EVENTS, parentTask);
    }

    public LinkedList<ExpenseCategory> getAllExpCats()
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        return (LinkedList<ExpenseCategory>) retrieveRecords(MessageCommands.RetrieveSubCommands.EXP_CATEGORIES, null);
    }

    public LinkedList<AdmEconTask> getAllAdmEconTasks()
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        return (LinkedList<AdmEconTask>) retrieveRecords(MessageCommands.RetrieveSubCommands.ADM_ECON_TASKS, null);
    }

    


    private Object retrieveRecords(int recordType, Object payload)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        Message retrieveMessage = new Message(MessageCommands.RETRIEVE, recordType);
        retrieveMessage.setPayload(payload);

        Message ansMessage = (Message) tryRequestResponse(retrieveMessage);

        if (isErrorMessage(ansMessage)) {
            throw new IllegalArgumentException("Server returned error code " + ansMessage.getCommand());
        }
        else if (ansMessage.getCommand() == MessageCommands.RETRIEVE) {
            return ansMessage.getPayload();
        }
        else {
            throw new IllegalArgumentException("Unknown error in server's response");
        }
    }



    
    public void updateKeyValue(KeyValue keyValue)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        KeyValue tmpKeyValue = new KeyValue(keyValue);
        tmpKeyValue.setRationaleUnits(null);
        updateRecord(tmpKeyValue, MessageCommands.RecordTypes.KEY_VALUE);
    }

    public void updateRationaleUnit(RationaleUnit rationaleUnit)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        RationaleUnit tmpRU = new RationaleUnit(rationaleUnit);
        tmpRU.setTasks(null);
        tmpRU.setCondIndicators(null);
        updateRecord(tmpRU, MessageCommands.RecordTypes.RATIONALE_UNIT);
    }

    public void updateCondIndicator(ConditionIndicator condIndicator)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        System.out.println(condIndicator.getName());
        updateRecord(condIndicator, MessageCommands.RecordTypes.COND_INDICATOR);

        // ConditionIndicator tmpCI = new ConditionIndicator(condIndicator);
        // updateRecord(tmpCI, MessageCommands.RecordTypes.COND_INDICATOR);
    }

    public void updateTask(Task task)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        Task tmpTask = new Task(task);
        tmpTask.setEvents(null);
        updateRecord(tmpTask, MessageCommands.RecordTypes.TASK);
    }

    public void updateEvent(Event event)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        Event tmpEvent = new Event(event);
        // tmpEvent.setPartnerOrgs(null);
        tmpEvent.setExpenses(null);
        tmpEvent.setDateIntervals(null);
        updateRecord(tmpEvent, MessageCommands.RecordTypes.EVENT);
    }

    public void updatePartnerOrg(PartnerOrg partnerOrg)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        updateRecord(partnerOrg, MessageCommands.RecordTypes.PARTNER_ORG);
    }

    public void updateExpCategory(ExpenseCategory expCat)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        ExpenseCategory tmpExpCat = new ExpenseCategory(expCat);
        updateRecord(tmpExpCat, MessageCommands.RecordTypes.EXP_CATEGORY);
    }

    public void updateExpense(Expense exp)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        updateRecord(exp, MessageCommands.RecordTypes.EXPENSE);
    }

    public void updateEventDateInterval(LocalDateInterval dateInterval)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        updateRecord(dateInterval, MessageCommands.RecordTypes.EVENT_DATE_INTERVAL);
    }

    public void updateAdmEconTask(AdmEconTask task)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        AdmEconTask tmpTask = new AdmEconTask(task);
        tmpTask.setAdmEconEvents(null);
        tmpTask.setAdmEconCondIndicators(null);
        updateRecord(tmpTask, MessageCommands.RecordTypes.ADM_ECON_TASK);
    }

    public void updateAdmEconCI(ConditionIndicator condIndicator)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        updateRecord(condIndicator, MessageCommands.RecordTypes.ADM_ECON_CI);
    }

    public void updateAdmEconEvent(Event event)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        Event tmpEvent = new Event(event);
        // tmpEvent.setPartnerOrgs(null);
        tmpEvent.setExpenses(null);
        tmpEvent.setDateIntervals(null);
        updateRecord(tmpEvent, MessageCommands.RecordTypes.ADM_ECON_EVENT);
    }

    public void updateAdmEconExpense(Expense expense)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        updateRecord(expense, MessageCommands.RecordTypes.ADM_ECON_EXPENSE);
    }

    public void updateAdmEconDateInterval(LocalDateInterval dateInterval)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        updateRecord(dateInterval, MessageCommands.RecordTypes.ADM_ECON_EVENT_DATE_INTERVAL);
    }

    private void updateRecord(Object record, int recordType)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        Message updMessage = new Message(MessageCommands.UPDATE_RECORD, recordType, record);

        Message ansMessage = (Message) tryRequestResponse(updMessage);
        outputStream.reset();

        if (isErrorMessage(ansMessage)) {
            throw new IllegalArgumentException("Server returned error code " + ansMessage.getCommand());
        }
        else if (ansMessage.getCommand() == MessageCommands.UPDATE_RECORD) {
            return;
        }
        else {
            throw new IllegalArgumentException("Unknown error in server's response");
        }
    }

    
    public void deleteKeyValue(KeyValue keyValue)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        KeyValue tmpKeyValue = new KeyValue(keyValue);
        tmpKeyValue.setRationaleUnits(null);
        deleteRecord(tmpKeyValue, MessageCommands.RecordTypes.KEY_VALUE);
    }
    
    public void deleteRationaleUnit(RationaleUnit rationaleUnit)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        RationaleUnit tmpRU = new RationaleUnit(rationaleUnit);
        tmpRU.setCondIndicators(null);
        tmpRU.setTasks(null);
        deleteRecord(tmpRU, MessageCommands.RecordTypes.RATIONALE_UNIT);
    }
    
    public void deleteCondIndicator(ConditionIndicator condIndicator)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        deleteRecord(condIndicator, MessageCommands.RecordTypes.COND_INDICATOR);
    }
    
    public void deleteTask(Task task)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        Task tmpTask = new Task(task);
        tmpTask.setEvents(null);
        deleteRecord(tmpTask, MessageCommands.RecordTypes.TASK);
    }
    
    public void deleteEvent(Event event)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        Event tmpEvent = new Event(event);
        // tmpEvent.setPartnerOrgs(null);
        tmpEvent.setExpenses(null);
        tmpEvent.setDateIntervals(null);
        deleteRecord(tmpEvent, MessageCommands.RecordTypes.EVENT);
    }
    
    public void deletePartnerOrg(PartnerOrg partnerOrg)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        deleteRecord(partnerOrg, MessageCommands.RecordTypes.PARTNER_ORG);
    }
    
    public void deleteExpCategory(ExpenseCategory expCategory)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        ExpenseCategory tmpExpCat = new ExpenseCategory(expCategory);
        deleteRecord(tmpExpCat, MessageCommands.RecordTypes.EXP_CATEGORY);
    }
    
    public void deleteExpense(Expense exp)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        deleteRecord(exp, MessageCommands.RecordTypes.EXPENSE);
    }

    public void deleteEventDateInterval(LocalDateInterval dateInterval)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        deleteRecord(dateInterval, MessageCommands.RecordTypes.EVENT_DATE_INTERVAL);
    }

    public void deleteAdmEconTask(AdmEconTask task)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        AdmEconTask tmpTask = new AdmEconTask(task);
        tmpTask.setAdmEconEvents(null);
        tmpTask.setAdmEconCondIndicators(null);
        deleteRecord(tmpTask, MessageCommands.RecordTypes.ADM_ECON_TASK);
    }

    public void deleteAdmEconCI(ConditionIndicator condIndicator)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        deleteRecord(condIndicator, MessageCommands.RecordTypes.ADM_ECON_CI);
    }

    public void deleteAdmEconEvent(Event event)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        Event tmpEvent = new Event(event);
        // tmpEvent.setPartnerOrgs(null);
        tmpEvent.setExpenses(null);
        tmpEvent.setDateIntervals(null);
        deleteRecord(tmpEvent, MessageCommands.RecordTypes.ADM_ECON_EVENT);
    }

    public void deleteAdmEconExpense(Expense expense)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        deleteRecord(expense, MessageCommands.RecordTypes.ADM_ECON_EXPENSE);
    }

    public void deleteAdmEconDateInterval(LocalDateInterval dateInterval)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        deleteRecord(dateInterval, MessageCommands.RecordTypes.ADM_ECON_EVENT_DATE_INTERVAL);
    }


    private void deleteRecord(Object record, int recordType)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        Message delMessage = new Message(MessageCommands.DELETE_RECORD, recordType, record);
        
        Message ansMessage = (Message) tryRequestResponse(delMessage);

        if (isErrorMessage(ansMessage)) {
            throw new IllegalArgumentException("Server returned error code " + ansMessage.getCommand());
        }
        else if (ansMessage.getCommand() == MessageCommands.DELETE_RECORD) {
            return;
        }
        else {
            throw new IllegalArgumentException("Unknown error in server's response");
        }
    }



    private static SSLSocket getSocket(String domain, int port)
        throws NoSuchAlgorithmException, KeyStoreException, CertificateException,
        IOException, KeyManagementException, UnrecoverableKeyException {
        
        SSLContext context = SSLContext.getInstance("TLS");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        KeyStore keyStore = KeyStore.getInstance("JKS");
        InputStream truststoreStream = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("truststore.jks");
        InputStream certPublicKeyStream = new BufferedInputStream(truststoreStream);
        keyStore.load(certPublicKeyStream, null);
        certPublicKeyStream.close();
        tmf.init(keyStore);
        context.init(null, tmf.getTrustManagers(), null);
        SSLSocket resultSocket = (SSLSocket) context.getSocketFactory().createSocket(domain, port);

        return resultSocket;
    }


    private static boolean isErrorMessage(Message message) {
        if (message == null) {
            return false;
        }
        
        if (message.getCommand() == MessageCommands.ERROR ||
            message.getCommand() == MessageCommands.IO_ERROR) {
            return true;
        }
        else {
            return false;
        }
    }

    private void tryWriteObject(Object obj) throws ClassNotFoundException, GeneralSecurityException {
        try {
            outputStream.writeObject(obj);
            outputStream.flush();
        }
        catch (IOException e1) {
            System.err.println("Error " + e1.getMessage());
            e1.printStackTrace();

            if (!socket.isClosed()) {
                try {
                    socket.close();
                }
                catch (IOException e2) {
                    System.out.println("Error " + e2.getMessage());
                    e2.printStackTrace();
                }
            }

            boolean connectionReestablished = false;

            for (int i = 0; i < 3; ++i) {
                try {
                    connect();
                    if (lastAuthedProtectedArea != null) {
                        authorize(lastAuthedProtectedArea, lastAuthedPassword);
                    }
                }
                catch (IOException e2) {
                    System.out.println("Error " + e2.getMessage());
                    e2.printStackTrace();
                    try {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException eInt) {
                        System.out.println("Error " + eInt.getMessage());
                        eInt.printStackTrace();
                    }

                    continue;
                }

                connectionReestablished = true;
            }

            if (connectionReestablished) {
                try {
                    outputStream.writeObject(obj);
                    outputStream.flush();
                }
                catch (IOException e) {
                    System.out.println("Error " + e.getMessage());
                    e.printStackTrace();

                    UICustomUtils.showUnrecoverableNetworkError(this);
                }
                
                System.out.println("Connection sucessfully reestablished.");
            }
            else {
                UICustomUtils.showUnrecoverableNetworkError(this);
                // throw new IOException("Failed to recover from network error.");
            }
        }
    }

    private Object tryReadObject() throws ClassNotFoundException, GeneralSecurityException {
        Object obj;
        try {
            obj = inputStream.readObject();
            return obj;
        }
        // catch (SocketTimeoutException e) {
            
        // }
        catch (IOException e1) {
            System.err.println("Error " + e1.getMessage());
            e1.printStackTrace();

            if (!socket.isClosed()) {
                try {
                    socket.close();
                }
                catch (IOException e2) {
                    System.out.println("Error " + e2.getMessage());
                    e2.printStackTrace();
                }
            }
            
            boolean connectionReestablished = false;

            for (int i = 0; i < 3; ++i) {
                try {
                    connect();
                    if (lastAuthedProtectedArea != null) {
                        authorize(lastAuthedProtectedArea, lastAuthedPassword);
                    }
                }
                catch (IOException e2) {
                    System.out.println("Error " + e2.getMessage());
                    e2.printStackTrace();
                    try {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException eInt) {
                        System.out.println("Error " + eInt.getMessage());
                        eInt.printStackTrace();
                    }

                    continue;
                }

                connectionReestablished = true;
                break;
            }

            if (connectionReestablished) {
                try {
                    obj = inputStream.readObject();
                    return obj;
                }
                catch (IOException e) {
                    System.out.println("Error " + e.getMessage());
                    e.printStackTrace();

                    UICustomUtils.showUnrecoverableNetworkError(this);
                }
                
                System.out.println("Connection sucessfully reestablished.");
            }
            else {
                UICustomUtils.showUnrecoverableNetworkError(this);
                // throw new IOException("Failed to recover from network error.");
            }
        }
        
        return null;
    }

    private Object tryRequestResponse(Object requestObj) throws ClassNotFoundException, GeneralSecurityException {
        Object responseObj;
        try {
            outputStream.writeObject(requestObj);
            outputStream.flush();
            responseObj = inputStream.readObject();
            return responseObj;
            
            // obj = inputStream.readObject();
            // return obj;
        }
        catch (IOException e1) {
            System.err.println("Error " + e1.getMessage());
            e1.printStackTrace();

            if (!socket.isClosed()) {
                try {
                    socket.close();
                }
                catch (IOException e2) {
                    System.out.println("Error " + e2.getMessage());
                    e2.printStackTrace();
                }
            }
            
            boolean connectionReestablished = false;

            for (int i = 0; i < 3; ++i) {
                try {
                    connect();
                    if (lastAuthedProtectedArea != null) {
                        authorize(lastAuthedProtectedArea, lastAuthedPassword);
                    }
                }
                catch (IOException e2) {
                    System.out.println("Error " + e2.getMessage());
                    e2.printStackTrace();
                    try {
                        Thread.sleep(300);
                    }
                    catch (InterruptedException eInt) {
                        System.out.println("Error " + eInt.getMessage());
                        eInt.printStackTrace();
                    }

                    continue;
                }

                connectionReestablished = true;
                break;
            }

            if (connectionReestablished) {
                try {
                    outputStream.writeObject(requestObj);
                    outputStream.flush();
                    responseObj = inputStream.readObject();

                    System.out.println("Connection sucessfully reestablished.");
                    
                    return responseObj;
                    
                    // obj = inputStream.readObject();
                    // return obj;
                }
                catch (IOException e) {
                    System.out.println("Error " + e.getMessage());
                    e.printStackTrace();

                    UICustomUtils.showUnrecoverableNetworkError(this);
                }
            }
            else {
                UICustomUtils.showUnrecoverableNetworkError(this);
                // throw new IOException("Failed to recover from network error.");
            }
        }
        
        return null;
    }
}
