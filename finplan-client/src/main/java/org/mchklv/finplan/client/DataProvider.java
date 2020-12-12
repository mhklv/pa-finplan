package org.mchklv.finplan.client;

import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.util.LinkedList;

import javax.net.ssl.*;

import org.mchklv.finplan.common.*;
import org.mchklv.finplan.common.messageBundles.*;



public interface DataProvider {
    public void connect() throws GeneralSecurityException, IOException;

    public void closeConnection() throws IOException;

    
    public void register(ProtectedArea protectedArea, String regQuotaKey, String newPassword)
            throws IOException, ClassNotFoundException, IllegalArgumentException, GeneralSecurityException;
    
    public void authorize(ProtectedArea protectedArea, String password)
            throws IOException, ClassNotFoundException, GeneralSecurityException;



    
    public void insertKeyValue(KeyValue newKeyValue, KeyValueGroup parentGroup)
            throws IOException, ClassNotFoundException, GeneralSecurityException;

    public void insertRationaleUnit(RationaleUnit newRU, KeyValue parentKeyValue)
            throws IOException, ClassNotFoundException, GeneralSecurityException;
    
    public void insertCondIndicator(ConditionIndicator newCI, RationaleUnit parentRU)
            throws IOException, ClassNotFoundException, GeneralSecurityException;

    public void insertTask(Task newTask, RationaleUnit parentRU)
            throws IOException, ClassNotFoundException, GeneralSecurityException;

    public void insertEvent(Event newEvent, Task parentTask)
            throws IOException, ClassNotFoundException, GeneralSecurityException;

    public void insertPartnerOrg(PartnerOrg newPartnerOrg, Event parentEvent)
            throws IOException, ClassNotFoundException, GeneralSecurityException;

    public void insertExpCategory(ExpenseCategory newExpenseCategory)
            throws IOException, ClassNotFoundException, GeneralSecurityException, GeneralSecurityException;

    public void insertExpense(Expense newExp, Event parentEvent)
            throws IOException, ClassNotFoundException, GeneralSecurityException;

    public void insertEventDateInterval(LocalDateInterval newDateInterval, Event parentEvent)
            throws IOException, ClassNotFoundException, GeneralSecurityException;

    public void insertAdmEconTask(AdmEconTask newTask)
            throws IOException, ClassNotFoundException, GeneralSecurityException;

    public void insertAdmEconCI(ConditionIndicator newCondIndicator, AdmEconTask parentTask)
            throws IOException, ClassNotFoundException, GeneralSecurityException;

    public void insertAdmEconEvent(Event newEvent, AdmEconTask parentTask)
            throws IOException, ClassNotFoundException, GeneralSecurityException;

    public void insertAdmEconExpense(Expense newExpense, Event parentEvent)
            throws IOException, ClassNotFoundException, GeneralSecurityException;

    public void insertAdmEconDateInterval(LocalDateInterval newDateInterval, Event parentEvent)
            throws IOException, ClassNotFoundException, GeneralSecurityException;

    
    public LinkedList<ProtectedArea> getAllProtectedAreas()
            throws IOException, ClassNotFoundException, GeneralSecurityException;

    public LinkedList<KeyValueGroup> getAllKeyValueGroups()
            throws IOException, ClassNotFoundException, GeneralSecurityException;

    public LinkedList<KeyValue> getAllKeyValues(KeyValueGroup parentKVG)
            throws IOException, ClassNotFoundException, GeneralSecurityException;

    public LinkedList<Event> getAllEvents(Task parentTask)
            throws IOException, ClassNotFoundException, GeneralSecurityException;

    public LinkedList<ExpenseCategory> getAllExpCats()
            throws IOException, ClassNotFoundException, GeneralSecurityException;

    public LinkedList<AdmEconTask> getAllAdmEconTasks()
        throws IOException, ClassNotFoundException, GeneralSecurityException;

    
    
    public void updateKeyValue(KeyValue keyValue) throws IOException, ClassNotFoundException, GeneralSecurityException;

    public void updateRationaleUnit(RationaleUnit rationaleUnit) throws IOException, ClassNotFoundException, GeneralSecurityException;

    public void updateCondIndicator(ConditionIndicator condIndicator) throws IOException, ClassNotFoundException, GeneralSecurityException;

    public void updateTask(Task task) throws IOException, ClassNotFoundException, GeneralSecurityException;

    public void updateEvent(Event event) throws IOException, ClassNotFoundException, GeneralSecurityException;

    public void updateEventDateInterval(LocalDateInterval dateInterval) throws IOException, ClassNotFoundException, GeneralSecurityException;

    public void updatePartnerOrg(PartnerOrg partnerOrg) throws IOException, ClassNotFoundException, GeneralSecurityException;

    public void updateExpCategory(ExpenseCategory expCat) throws IOException, ClassNotFoundException, GeneralSecurityException;

    public void updateExpense(Expense exp) throws IOException, ClassNotFoundException, GeneralSecurityException;

    public void updateAdmEconTask(AdmEconTask task) throws IOException, ClassNotFoundException, GeneralSecurityException;

    public void updateAdmEconCI(ConditionIndicator condIndicator) throws IOException, ClassNotFoundException, GeneralSecurityException, GeneralSecurityException;

    public void updateAdmEconEvent(Event event) throws IOException, ClassNotFoundException, GeneralSecurityException;

    public void updateAdmEconExpense(Expense expense) throws IOException, ClassNotFoundException, GeneralSecurityException;

    public void updateAdmEconDateInterval(LocalDateInterval dateInterval) throws IOException, ClassNotFoundException, GeneralSecurityException;

    
    public void deleteKeyValue(KeyValue keyValue) throws IOException, ClassNotFoundException, GeneralSecurityException;
    
    public void deleteRationaleUnit(RationaleUnit rationaleUnit) throws IOException, ClassNotFoundException, GeneralSecurityException;
    
    public void deleteCondIndicator(ConditionIndicator condIndicator) throws IOException, ClassNotFoundException, GeneralSecurityException;
    
    public void deleteTask(Task task) throws IOException, ClassNotFoundException, GeneralSecurityException;
    
    public void deleteEvent(Event event) throws IOException, ClassNotFoundException, GeneralSecurityException;
    
    public void deletePartnerOrg(PartnerOrg partnerOrg) throws IOException, ClassNotFoundException, GeneralSecurityException;
    
    public void deleteExpCategory(ExpenseCategory expCategory) throws IOException, ClassNotFoundException, GeneralSecurityException;
    
    public void deleteExpense(Expense exp) throws IOException, ClassNotFoundException, GeneralSecurityException;

    public void deleteEventDateInterval(LocalDateInterval dateInterval) throws IOException, ClassNotFoundException, GeneralSecurityException;

    public void deleteAdmEconTask(AdmEconTask task) throws IOException, ClassNotFoundException, GeneralSecurityException;

    public void deleteAdmEconCI(ConditionIndicator condIndicator) throws IOException, ClassNotFoundException, GeneralSecurityException;

    public void deleteAdmEconEvent(Event event) throws IOException, ClassNotFoundException, GeneralSecurityException;

    public void deleteAdmEconExpense(Expense expense) throws IOException, ClassNotFoundException, GeneralSecurityException;

    public void deleteAdmEconDateInterval(LocalDateInterval dateInterval) throws IOException, ClassNotFoundException, GeneralSecurityException;
}
