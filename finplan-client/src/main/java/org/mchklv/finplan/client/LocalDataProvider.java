package org.mchklv.finplan.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.mchklv.finplan.common.AdmEconTask;
import org.mchklv.finplan.common.ConditionIndicator;
import org.mchklv.finplan.common.Event;
import org.mchklv.finplan.common.Expense;
import org.mchklv.finplan.common.ExpenseCategory;
import org.mchklv.finplan.common.FixedPointDec;
import org.mchklv.finplan.common.KeyValue;
import org.mchklv.finplan.common.KeyValueGroup;
import org.mchklv.finplan.common.LocalDateInterval;
import org.mchklv.finplan.common.PartnerOrg;
import org.mchklv.finplan.common.ProtectedArea;
import org.mchklv.finplan.common.RationaleUnit;
import org.mchklv.finplan.common.Task;

import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

public class LocalDataProvider implements DataProvider {
    private List<ProtectedArea> protectedAreasIndex;
    private File dataDirFile, paIndexFile;
    
    private Connection conn;
    private ProtectedArea authedProtectedArea;
    private boolean mustCommit = true;

    
    public LocalDataProvider() {
        protectedAreasIndex = new ArrayList<ProtectedArea>();
        
        AppDirs appDirs = AppDirsFactory.getInstance();
        String dataDirPath = appDirs.getUserDataDir("finplan", null, "mhklv");
        dataDirFile = new File(dataDirPath);

        if (!dataDirFile.exists()) {
            dataDirFile.mkdirs();
        }

        paIndexFile = new File(dataDirPath + File.separator + "pa-index.ser");
    }

	@Override
	public void connect() throws GeneralSecurityException, IOException {
        readPAIndex();
	}

	@Override
	public void closeConnection() throws IOException {
        flushPAIndex();
        protectedAreasIndex = null;
        authedProtectedArea = null;
        
        if (conn != null) {
            try {
                conn.close();
            }
            catch (SQLException e) {
                System.err.println("Error " + e.getMessage());
                e.printStackTrace();
            }
        }
	}

	@Override
	public void register(ProtectedArea protectedArea, String regQuotaKey, String newPassword)
			throws IOException, ClassNotFoundException, IllegalArgumentException, GeneralSecurityException {

        protectedArea.setId(getNextPAId());
        String newDBUrl = "jdbc:sqlite:" + getLocalProtectedAreaDBPath(protectedArea);
        Connection newConn = null;

        try {
            newConn = DriverManager.getConnection(newDBUrl);
            newConn.setAutoCommit(false);
            initDBWithTables(newConn);
            insertDefaultKVGroups(newConn, true);
            insertDefaultExpCats(newConn, true);
            insertDefaultAdmEconTasks(newConn, true);
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
        finally {
            if (newConn != null) {
                try {
                    newConn.close();
                }
                catch (SQLException e) {
                    System.err.println("Error " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        
        protectedAreasIndex.add(protectedArea);
        flushPAIndex();
	}

	@Override
	public void authorize(ProtectedArea protectedArea, String password)
			throws IOException, ClassNotFoundException, GeneralSecurityException {
        
        String connUrlString = "jdbc:sqlite:" + getLocalProtectedAreaDBPath(protectedArea);
        
        try {
            conn = DriverManager.getConnection(connUrlString);
            conn.setAutoCommit(false);
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
        authedProtectedArea = protectedArea;
	}

	@Override
	public void insertKeyValue(KeyValue keyValue, KeyValueGroup parentKeyValueGroup)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        if (keyValue.getId() != null) {
            throw new IllegalArgumentException("Id of the element to be inserted must be null before insertion");
        }

        if (parentKeyValueGroup.getId() == null) {
            throw new IllegalArgumentException("Parent's id must not be null before insertion");
        }

        try {
            PreparedStatement stmnt = conn.prepareStatement(
                "INSERT INTO keyValue (kVName, kVDesc, kVKVGRef)\nVALUES (?, ?, ?) ",
                Statement.RETURN_GENERATED_KEYS);
            stmnt.setString(1, keyValue.getName());
            stmnt.setString(2, keyValue.getDescription());
            stmnt.setInt(3, parentKeyValueGroup.getId());

            int affectedRowsCount = stmnt.executeUpdate();

            if (affectedRowsCount == 0) {
                stmnt.close();
                throw new SQLException("Failed to retrieve autogenerated id");
            }

            ResultSet generatedIdSet = stmnt.getGeneratedKeys();
            if (generatedIdSet.next()) {
                keyValue.setId(generatedIdSet.getInt(1));
            }
            else {
                stmnt.close();
                throw new SQLException("Failed to retrieve autogenerated id");
            }

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
	}

	@Override
	public void insertRationaleUnit(RationaleUnit rationaleUnit, KeyValue parentKeyValue)
			throws IOException, ClassNotFoundException, GeneralSecurityException {
        if (rationaleUnit.getId() != null) {
            throw new IllegalArgumentException("Id of the element to be inserted must be null before insertion");
        }

        if (parentKeyValue.getId() == null) {
            throw new IllegalArgumentException("Parent's id must not be null before insertion");
        }

        try {
            PreparedStatement stmnt = conn.prepareStatement("INSERT INTO rationaleUnit (rUThreat, rUThreatReasons, rUProblems, rUKVRef)\nVALUES (?, ?, ?, ?) ",
                                                            Statement.RETURN_GENERATED_KEYS);
            stmnt.setString(1, rationaleUnit.getThreat());
            stmnt.setString(2, rationaleUnit.getThreatReasons());
            stmnt.setString(3, rationaleUnit.getProblems());
            stmnt.setInt(4, parentKeyValue.getId());

            int affectedRowsCount = stmnt.executeUpdate();

            if (affectedRowsCount == 0) {
                stmnt.close();
                throw new SQLException("Filed to retrieve autogenerated id");
            }

            ResultSet generatedIdSet = stmnt.getGeneratedKeys();
            if (generatedIdSet.next()) {
                rationaleUnit.setId(generatedIdSet.getInt(1));
            }
            else {
                stmnt.close();
                throw new SQLException("Filed to retrieve autogenerated id");
            }

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
	}

	@Override
	public void insertCondIndicator(ConditionIndicator condIndicator, RationaleUnit parentRationaleUnit)
			throws IOException, ClassNotFoundException, GeneralSecurityException {

        try {
            if (condIndicator.getId() != null) {
                throw new IllegalArgumentException("Id of the element to be inserted must be null before insertion");
            }

            if (parentRationaleUnit.getId() == null) {
                throw new IllegalArgumentException("Parent's id must not be null before insertion");
            }

            PreparedStatement stmnt = conn.prepareStatement("INSERT INTO condIndicator (cIName, cICurrentVal, cITargetVal, cIRURef)\nVALUES (?, ?, ?, ?) ",
                                                            Statement.RETURN_GENERATED_KEYS);
            stmnt.setString(1, condIndicator.getName());
            stmnt.setString(2, condIndicator.getCurrentValue());
            stmnt.setString(3, condIndicator.getTargetValue());
            stmnt.setInt(4, parentRationaleUnit.getId());

            int affectedRowsCount = stmnt.executeUpdate();

            if (affectedRowsCount == 0) {
                stmnt.close();
                throw new SQLException("Failed to retrieve autogenerated id");
            }

            ResultSet generatedIdSet = stmnt.getGeneratedKeys();
            if (generatedIdSet.next()) {
                condIndicator.setId(generatedIdSet.getInt(1));
            }
            else {
                stmnt.close();
                throw new SQLException("Failed to retrieve autogenerated id");
            }

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
	}

	@Override
	public void insertTask(Task task, RationaleUnit parentRationaleUnit)
			throws IOException, ClassNotFoundException, GeneralSecurityException {
		
        try {
            if (task.getId() != null) {
                throw new IllegalArgumentException("Id of the element to be inserted must be null before insertion");
            }
        
        
            if (parentRationaleUnit.getId() == null) {
                throw new IllegalArgumentException("Parent's id must not be null before insertion");
            }

            PreparedStatement stmnt = conn.prepareStatement("INSERT INTO task (taskContent, taskRURef)\nVALUES (?, ?) ",
                                                            Statement.RETURN_GENERATED_KEYS);
            stmnt.setString(1, task.getContent());
            stmnt.setInt(2, parentRationaleUnit.getId());

            int affectedRowsCount = stmnt.executeUpdate();

            if (affectedRowsCount == 0) {
                stmnt.close();
                throw new SQLException("Failed to retrieve autogenerated id");
            }

            ResultSet generatedIdSet = stmnt.getGeneratedKeys();
            if (generatedIdSet.next()) {
                task.setId(generatedIdSet.getInt(1));
            }
            else {
                stmnt.close();
                throw new SQLException("Failed to retrieve autogenerated id");
            }

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
	}

	@Override
	public void insertEvent(Event event, Task parentTask)
			throws IOException, ClassNotFoundException, GeneralSecurityException {
		
        try {
            if (event.getId() != null) {
                throw new IllegalArgumentException("Id of the element to be inserted must be null before insertion");
            }

            if (parentTask.getId() == null) {
                throw new IllegalArgumentException("Parent's id must not be null before insertion");
            }

            PreparedStatement stmnt = conn.prepareStatement(
                "INSERT INTO event (eventContent, eventProgramme, eventDepartment,\n                   eventFinSrc, eventPatnerOrgs, eventTaskRef)\nVALUES (?, ?, ?, ?, ?, ?) ",
                Statement.RETURN_GENERATED_KEYS);
            stmnt.setString(1, event.getContent());
            stmnt.setString(2, event.getProgramme());
            stmnt.setString(3, event.getDepartment());
            stmnt.setString(4, event.getFinSource());
        
            stmnt.setString(5, event.getPartnerOrgs());
            stmnt.setInt(6, parentTask.getId());

            int affectedRowsCount = stmnt.executeUpdate();

            if (affectedRowsCount == 0) {
                stmnt.close();
                throw new SQLException("Failed to retrieve autogenerated id");
            }

            ResultSet generatedIdSet = stmnt.getGeneratedKeys();
            if (generatedIdSet.next()) {
                event.setId(generatedIdSet.getInt(1));
            }
            else {
                stmnt.close();
                throw new SQLException("Failed to retrieve autogenerated id");
            }

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
	}

	@Override
	public void insertPartnerOrg(PartnerOrg partnerOrg, Event parentEvent)
			throws IOException, ClassNotFoundException, GeneralSecurityException {
	
		try {
            if (partnerOrg.getId() != null) {
                throw new IllegalArgumentException("Id of the element to be inserted must be null before insertion");
            }

            if (parentEvent.getId() == null) {
                throw new IllegalArgumentException("Parent's id must not be null before insertion");
            }

            PreparedStatement stmnt = conn.prepareStatement(
                "INSERT INTO partnerOrg (partnerOrgName, partnerOrgEventRef)\nVALUES (?, ?) ",
                Statement.RETURN_GENERATED_KEYS);
        
            stmnt.setString(1, partnerOrg.getName());
            stmnt.setInt(2, parentEvent.getId());

            int affectedRowsCount = stmnt.executeUpdate();

            if (affectedRowsCount == 0) {
                stmnt.close();
                throw new SQLException("Failed to retrieve autogenerated id");
            }

            ResultSet generatedIdSet = stmnt.getGeneratedKeys();
            if (generatedIdSet.next()) {
                partnerOrg.setId(generatedIdSet.getInt(1));
            }
            else {
                stmnt.close();
                throw new SQLException("Failed to retrieve autogenerated id");
            }

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
	}

	@Override
	public void insertExpCategory(ExpenseCategory expCategory)
			throws IOException, ClassNotFoundException, GeneralSecurityException, GeneralSecurityException {

		try {
            if (expCategory.getId() != null) {
                throw new IllegalArgumentException("Id of the element to be inserted must be null before insertion");
            }

            PreparedStatement stmnt = conn.prepareStatement(
                "INSERT INTO expenseCategory (expCatName, expCatCode, expCatFinSrc)\nVALUES (?, ?, ?) ",
                Statement.RETURN_GENERATED_KEYS);
        
            stmnt.setString(1, expCategory.getName());
            stmnt.setString(2, expCategory.getCategoryCode());
            stmnt.setString(3, expCategory.getFinSource());

            int affectedRowsCount = stmnt.executeUpdate();

            if (affectedRowsCount == 0) {
                stmnt.close();
                throw new SQLException("Failed to retrieve autogenerated id");
            }

            ResultSet generatedIdSet = stmnt.getGeneratedKeys();
            if (generatedIdSet.next()) {
                expCategory.setId(generatedIdSet.getInt(1));
            }
            else {
                stmnt.close();
                throw new SQLException("Failed to retrieve autogenerated id");
            }

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
	}

	@Override
	public void insertExpense(Expense expense, Event parentEvent)
			throws IOException, ClassNotFoundException, GeneralSecurityException {
	
		try {
            if (expense.getId() != null) {
                throw new IllegalArgumentException("Id of the element to be inserted must be null before insertion");
            }
        
            if (parentEvent.getId() == null) {
                throw new IllegalArgumentException("Parent's id must not be null before insertion");
            }

            PreparedStatement stmnt = conn.prepareStatement(
                "INSERT INTO expense (expName, expUnit, expAmount, expUnitCost, expEventRef)\nVALUES (?, ?, ?, ?, ?) ",
                Statement.RETURN_GENERATED_KEYS);
        
            stmnt.setString(1, expense.getName());
            stmnt.setString(2, expense.getUnit());
            stmnt.setString(3, expense.getAmount().toString());
            stmnt.setString(4, expense.getUnitCost().toString());
            stmnt.setInt(5, parentEvent.getId());

            int affectedRowsCount = stmnt.executeUpdate();

            if (affectedRowsCount == 0) {
                stmnt.close();
                throw new SQLException("Failed to retrieve autogenerated id");
            }

            ResultSet generatedIdSet = stmnt.getGeneratedKeys();
            if (generatedIdSet.next()) {
                expense.setId(generatedIdSet.getInt(1));
            }
            else {
                stmnt.close();
                throw new SQLException("Failed to retrieve autogenerated id");
            }

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
	}

	@Override
	public void insertEventDateInterval(LocalDateInterval dateInterval, Event parentEvent)
			throws IOException, ClassNotFoundException, GeneralSecurityException {
	
		try {
            if (dateInterval.getId() != null) {
                throw new IllegalArgumentException("Id of the element to be inserted must be null before insertion");
            }

            if (parentEvent.getId() == null) {
                throw new IllegalArgumentException("Parent's id must not be null before insertion");
            }

            PreparedStatement stmnt = conn.prepareStatement(
                "INSERT INTO eventDateInterval (startDate, endDate, dateIntervalEventRef)\nVALUES (?, ?, ?) ",
                Statement.RETURN_GENERATED_KEYS);
            if (dateInterval.getStartDate() != null) {
                stmnt.setDate(1, Date.valueOf(dateInterval.getStartDate()));
            }
            else {
                stmnt.setNull(1, Types.DATE);
            }

            if (dateInterval.getEndDate() != null) {
                stmnt.setDate(2, Date.valueOf(dateInterval.getEndDate()));
            }
            else {
                stmnt.setNull(2, Types.DATE);
            }
        
            stmnt.setInt(3, parentEvent.getId());

            int affectedRowsCount = stmnt.executeUpdate();

            if (affectedRowsCount == 0) {
                stmnt.close();
                throw new SQLException("Failed to retrieve autogenerated id");
            }

            ResultSet generatedIdSet = stmnt.getGeneratedKeys();
            if (generatedIdSet.next()) {
                dateInterval.setId(generatedIdSet.getInt(1));
            }
            else {
                stmnt.close();
                throw new SQLException("Failed to retrieve autogenerated id");
            }

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
	}

	@Override
	public void insertAdmEconTask(AdmEconTask admEconTask)
			throws IOException, ClassNotFoundException, GeneralSecurityException {

		try {
            if (admEconTask.getId() != null) {
                throw new IllegalArgumentException("Id of the element to be inserted must be null before insertion");
            }
            
            PreparedStatement stmnt = conn.prepareStatement(
                    "INSERT INTO admEconTask (admEconTaskCont)\nVALUES (?) ",
                Statement.RETURN_GENERATED_KEYS);
            stmnt.setString(1, admEconTask.getContent());

            int affectedRowsCount = stmnt.executeUpdate();

            if (affectedRowsCount == 0) {
                stmnt.close();
                throw new SQLException("Failed to retrieve autogenerated id");
            }

            ResultSet generatedIdSet = stmnt.getGeneratedKeys();
            if (generatedIdSet.next()) {
                admEconTask.setId(generatedIdSet.getInt(1));
            }
            else {
                stmnt.close();
                throw new SQLException("Failed to retrieve autogenerated id");
            }

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
	}

	@Override
	public void insertAdmEconCI(ConditionIndicator condIndicator, AdmEconTask parentAdmEconTask)
			throws IOException, ClassNotFoundException, GeneralSecurityException {

		try {
            if (condIndicator.getId() != null) {
                throw new IllegalArgumentException("Id of the element to be inserted must be null before insertion");
            }

            if (parentAdmEconTask.getId() == null) {
                throw new IllegalArgumentException("Parent's id must not be null before insertion");
            }

            PreparedStatement stmnt = conn.prepareStatement(
                "INSERT INTO admEconCI (admEconCIName, admEconCICurVal, admEconCITargVal, admEconCITaskRef)\nVALUES (?, ?, ?, ?)\n",
                Statement.RETURN_GENERATED_KEYS);
            stmnt.setString(1, condIndicator.getName());
            stmnt.setString(2, condIndicator.getCurrentValue());
            stmnt.setString(3, condIndicator.getTargetValue());
            stmnt.setInt(4, parentAdmEconTask.getId());

            int affectedRowsCount = stmnt.executeUpdate();

            if (affectedRowsCount == 0) {
                stmnt.close();
                throw new SQLException("Failed to retrieve autogenerated id");
            }

            ResultSet generatedIdSet = stmnt.getGeneratedKeys();
            if (generatedIdSet.next()) {
                condIndicator.setId(generatedIdSet.getInt(1));
            }
            else {
                stmnt.close();
                throw new SQLException("Failed to retrieve autogenerated id");
            }

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
	}

	@Override
	public void insertAdmEconEvent(Event event, AdmEconTask parentTask)
			throws IOException, ClassNotFoundException, GeneralSecurityException {

		try {
            if (event.getId() != null) {
                throw new IllegalArgumentException("Id of the element to be inserted must be null before insertion");
            }

            if (parentTask.getId() == null) {
                throw new IllegalArgumentException("Parent's id must not be null before insertion");
            }

            PreparedStatement stmnt = conn.prepareStatement(
                "INSERT INTO admEconEvent (admEconEventContent,\n                   admEconEventProg,\n                   admEconEventDep,\n                   admEconEventFinSrc,\n                   admEconEventPartnerOrgs,\n                   admEconEventTaskRef)\nVALUES (?, ?, ?, ?, ?, ?) ",
                Statement.RETURN_GENERATED_KEYS);
            stmnt.setString(1, event.getContent());
            stmnt.setString(2, event.getProgramme());
            stmnt.setString(3, event.getDepartment());
            stmnt.setString(4, event.getFinSource());
            stmnt.setString(5, event.getPartnerOrgs());
            stmnt.setInt(6, parentTask.getId());

            int affectedRowsCount = stmnt.executeUpdate();

            if (affectedRowsCount == 0) {
                stmnt.close();
                throw new SQLException("Failed to retrieve autogenerated id");
            }

            ResultSet generatedIdSet = stmnt.getGeneratedKeys();
            if (generatedIdSet.next()) {
                event.setId(generatedIdSet.getInt(1));
            }
            else {
                stmnt.close();
                throw new SQLException("Failed to retrieve autogenerated id");
            }

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
	}

	@Override
	public void insertAdmEconExpense(Expense expense, Event parentEvent)
			throws IOException, ClassNotFoundException, GeneralSecurityException {
        
		try {
            if (expense.getId() != null) {
                throw new IllegalArgumentException("Id of the element to be inserted must be null before insertion");
            }
        
            if (parentEvent.getId() == null) {
                throw new IllegalArgumentException("Parent's id must not be null before insertion");
            }

            PreparedStatement stmnt = conn.prepareStatement(
                "INSERT INTO admEconExpense (admEconExpName, admEconExpUnit, admEconExpAmount, admEconExpUnitCost, admEconExpEventRef)\nVALUES (?, ?, ?, ?, ?)\n\n\n",
                Statement.RETURN_GENERATED_KEYS);
        
            stmnt.setString(1, expense.getName());
            stmnt.setString(2, expense.getUnit());
            stmnt.setString(3, expense.getAmount().toString());
            stmnt.setString(4, expense.getUnitCost().toString());
            stmnt.setInt(5, parentEvent.getId());

            int affectedRowsCount = stmnt.executeUpdate();

            if (affectedRowsCount == 0) {
                stmnt.close();
                throw new SQLException("Failed to retrieve autogenerated id");
            }

            ResultSet generatedIdSet = stmnt.getGeneratedKeys();
            if (generatedIdSet.next()) {
                expense.setId(generatedIdSet.getInt(1));
            }
            else {
                stmnt.close();
                throw new SQLException("Failed to retrieve autogenerated id");
            }

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
	}

	@Override
	public void insertAdmEconDateInterval(LocalDateInterval dateInterval, Event parentEvent)
			throws IOException, ClassNotFoundException, GeneralSecurityException {
		
		try {
            if (dateInterval.getId() != null) {
                throw new IllegalArgumentException("Id of the element to be inserted must be null before insertion");
            }

            if (parentEvent.getId() == null) {
                throw new IllegalArgumentException("Parent's id must not be null before insertion");
            }

            PreparedStatement stmnt = conn.prepareStatement(
                "INSERT INTO admEconEventDateInterval (admEconStartDate, admEconEndDate, admEconDateIntervalEventRef)\nVALUES (?, ?, ?) ",
                Statement.RETURN_GENERATED_KEYS);
            if (dateInterval.getStartDate() != null) {
                stmnt.setDate(1, Date.valueOf(dateInterval.getStartDate()));
            }
            else {
                stmnt.setNull(1, Types.DATE);
            }

            if (dateInterval.getEndDate() != null) {
                stmnt.setDate(2, Date.valueOf(dateInterval.getEndDate()));
            }
            else {
                stmnt.setNull(2, Types.DATE);
            }
        
            stmnt.setInt(3, parentEvent.getId());

            int affectedRowsCount = stmnt.executeUpdate();

            if (affectedRowsCount == 0) {
                stmnt.close();
                throw new SQLException("Failed to retrieve autogenerated id");
            }

            ResultSet generatedIdSet = stmnt.getGeneratedKeys();
            if (generatedIdSet.next()) {
                dateInterval.setId(generatedIdSet.getInt(1));
            }
            else {
                stmnt.close();
                throw new SQLException("Failed to retrieve autogenerated id");
            }

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
	}

	@Override
	public LinkedList<ProtectedArea> getAllProtectedAreas()
			throws IOException, ClassNotFoundException, GeneralSecurityException {
        LinkedList<ProtectedArea> resList = new LinkedList<ProtectedArea>(protectedAreasIndex);
		return resList;
	}

	@Override
	public LinkedList<KeyValueGroup> getAllKeyValueGroups()
        throws IOException, ClassNotFoundException, GeneralSecurityException {

        try {
            Statement stmnt = conn.createStatement();
            ResultSet groupsResSet = stmnt.executeQuery("SELECT kVGId, kVGName FROM keyValueGroup");
            LinkedList<KeyValueGroup> resGroups = new LinkedList<KeyValueGroup>(); 

            while (groupsResSet.next()) {
                resGroups.add(new KeyValueGroup(groupsResSet.getInt(1), groupsResSet.getString(2)));
            }

            stmnt.close();
        
            return resGroups;
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
            return new LinkedList<>();
        }
	}

	@Override
	public LinkedList<KeyValue> getAllKeyValues(KeyValueGroup keyValueGroup)
			throws IOException, ClassNotFoundException, GeneralSecurityException {

        try {
            if (keyValueGroup.getId() == null) {
                throw new IllegalArgumentException("Ids of protected area and key value group must not be null");
            }
        
            PreparedStatement stmnt = conn.prepareStatement(
                "SELECT kVId, kVName, kVDesc, rUId, rUThreat, rUThreatReasons, rUProblems, cIId, cIName, cICurrentVal, cITargetVal, taskId, taskContent\nFROM\n((keyValue LEFT JOIN rationaleUnit ON keyValue.kVId = rationaleUnit.rUKVRef) LEFT JOIN\ncondIndicator ON rationaleUnit.rUId = condIndicator.cIRURef) LEFT JOIN\ntask ON rationaleUnit.rUId = task.taskRURef \nWHERE kVKVGRef = ?\nORDER BY kVId, rUId, cIId, taskId ASC\n");
        
            stmnt.setInt(1, keyValueGroup.getId());
        
            ResultSet groupsResSet = stmnt.executeQuery();

            LinkedList<KeyValue> resValues = new LinkedList<KeyValue>();
            KeyValue currentKeyValue = null;
            RationaleUnit currentRationaleUnit = null;
            ConditionIndicator currentCondIndicator = null;
            Task currentTask = null;

            Set<Integer> currentCIIDs = new HashSet<Integer>();
            Set<Integer> currentTaskIDs = new HashSet<Integer>();
        
            while (groupsResSet.next()) {
                // Process key value
                if (currentKeyValue == null || groupsResSet.getInt(1) != currentKeyValue.getId()) {
                    currentKeyValue = new KeyValue(groupsResSet.getInt(1),
                                                   groupsResSet.getString(2),
                                                   groupsResSet.getString(3));
                    resValues.add(currentKeyValue);
                }

                // Process rationale unit
                groupsResSet.getInt(4);
                if (groupsResSet.wasNull()) {
                    continue;
                }
                if (currentRationaleUnit == null || groupsResSet.getInt(4) != currentRationaleUnit.getId()) {
                    currentRationaleUnit = new RationaleUnit(groupsResSet.getInt(4),
                                                             groupsResSet.getString(5),
                                                             groupsResSet.getString(6),
                                                             groupsResSet.getString(7));
                    if (currentKeyValue.getRationaleUnits() == null) {
                        currentKeyValue.setRationaleUnits(new LinkedList<RationaleUnit>());
                    }
                    currentKeyValue.getRationaleUnits().add(currentRationaleUnit);
                    currentCIIDs.clear();
                    currentTaskIDs.clear();
                }

                // Process condition indicator
                groupsResSet.getInt(8);
                if (!groupsResSet.wasNull()) {
                    if ((currentCondIndicator == null || groupsResSet.getInt(8) != currentCondIndicator.getId()) &&
                        !currentCIIDs.contains(groupsResSet.getInt(8))) {
                        currentCondIndicator = new ConditionIndicator(groupsResSet.getInt(8),
                                                                      groupsResSet.getString(9),
                                                                      groupsResSet.getString(10),
                                                                      groupsResSet.getString(11));
                        if (currentRationaleUnit.getCondIndicators() == null) {
                            currentRationaleUnit.setCondIndicators(new LinkedList<ConditionIndicator>());
                        }
                        currentRationaleUnit.getCondIndicators().add(currentCondIndicator);
                        currentCIIDs.add(currentCondIndicator.getId());
                    }
                }

                // Process task
                groupsResSet.getInt(12);
                if (!groupsResSet.wasNull() && !currentTaskIDs.contains(groupsResSet.getInt(12))) {
                    if (currentTask == null || groupsResSet.getInt(12) != currentTask.getId()) {
                        currentTask = new Task(groupsResSet.getInt(12),
                                               groupsResSet.getString(13));

                        if (currentRationaleUnit.getTasks() == null) {
                            currentRationaleUnit.setTasks(new LinkedList<Task>());
                        }
                        currentRationaleUnit.getTasks().add(currentTask);
                        currentTaskIDs.add(currentTask.getId());
                    }
                }
            }
        
            stmnt.close();

            return resValues;
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
            return new LinkedList<>();
        }
	}

	@Override
	public LinkedList<Event> getAllEvents(Task parentTask)
			throws IOException, ClassNotFoundException, GeneralSecurityException {

        try {
            if (parentTask.getId() == null) {
                throw new IllegalArgumentException("Id of the task must not be null");
            }
        
            PreparedStatement stmnt = conn.prepareStatement(
                "SELECT eventId, eventContent, eventProgramme, eventDepartment, eventFinSrc, eventPatnerOrgs, partnerOrgId, partnerOrgName, expId, expName, expUnit, expAmount, expUnitCost, expExpCatRef, dateIntervalId, startDate, endDate\nFROM\n((event LEFT JOIN partnerOrg ON eventId = partnerOrgEventRef) LEFT JOIN expense ON eventId = expEventRef)\nLEFT JOIN eventDateInterval ON eventId = dateIntervalEventRef\nWHERE eventTaskRef = ?\nORDER BY eventId, partnerOrgId, expId, dateIntervalId ASC\n");
        
            stmnt.setInt(1, parentTask.getId());
        
            ResultSet resSet = stmnt.executeQuery();

            LinkedList<Event> resEvents = new LinkedList<Event>();
            Event currentEvent = null;
            PartnerOrg currentPartnerOrg = null;
            Expense currentExpense = null;
            LocalDateInterval currentDateInterval = null;

            Set<Integer> currentPartnerOrgIDs = new HashSet<Integer>();
            Set<Integer> currentExpenseIDs = new HashSet<Integer>();
            Set<Integer> currentDateIntervalsIDs = new HashSet<Integer>();
        
            // boolean isNotEmpty = groupsResSet.next();
        
            while (resSet.next()) {
                // Process event
                if (currentEvent == null || resSet.getInt(1) != currentEvent.getId()) {
                    currentEvent = new Event(resSet.getInt(1), resSet.getString(2), resSet.getString(3), resSet.getString(4));
                    currentEvent.setFinSource(resSet.getString(5));
                    currentEvent.setPartnerOrgs(resSet.getString(6));
                    resEvents.add(currentEvent);

                    currentPartnerOrgIDs.clear();
                    currentExpenseIDs.clear();
                    currentDateIntervalsIDs.clear();
                }

                // Process partner orgs
                resSet.getInt(7);
                if (!resSet.wasNull()) {
                    if ((currentPartnerOrg == null || resSet.getInt(7) != currentPartnerOrg.getId()) &&
                        !currentPartnerOrgIDs.contains(resSet.getInt(7))) {
                        currentPartnerOrg = new PartnerOrg(resSet.getInt(7), resSet.getString(8));
                        if (currentEvent.getPartnerOrgs() == null) {
                            // currentEvent.setPartnerOrgs(new LinkedList<PartnerOrg>());
                        }
                        // currentEvent.getPartnerOrgs().add(currentPartnerOrg);
                        currentPartnerOrgIDs.add(currentPartnerOrg.getId());
                    }
                }
            
                // Process expense
                resSet.getInt(9);
                if (!resSet.wasNull()) {
                    if ((currentExpense == null || resSet.getInt(9) != currentExpense.getId()) &&
                        !currentExpenseIDs.contains(resSet.getInt(9))) {
                        currentExpense = new Expense(resSet.getInt(9),
                                                     resSet.getString(10),
                                                     resSet.getString(11),
                                                     new FixedPointDec(resSet.getString(12)),
                                                     new FixedPointDec(resSet.getString(13)),
                                                     resSet.getInt(14));
                        if (currentEvent.getExpenses() == null) {
                            currentEvent.setExpenses(new LinkedList<Expense>());
                        }
                        currentEvent.getExpenses().add(currentExpense);
                        currentExpenseIDs.add(currentExpense.getId());
                    }
                }

                // Process date intervals
                resSet.getInt(15);
                if (!resSet.wasNull()) {
                    if ((currentDateInterval == null || resSet.getInt(15) != currentDateInterval.getId()) &&
                        !currentDateIntervalsIDs.contains(resSet.getInt(15))) {
                        currentDateInterval = new LocalDateInterval(resSet.getInt(15),
                                                                    (resSet.getDate(16) == null) ? null : resSet.getDate(16).toLocalDate(),
                                                                    (resSet.getDate(17) == null) ? null : resSet.getDate(17).toLocalDate());
                        if (currentEvent.getDateIntervals() == null) {
                            currentEvent.setDateIntervals(new ArrayList<LocalDateInterval>());
                        }
                        currentEvent.getDateIntervals().add(currentDateInterval);
                        currentDateIntervalsIDs.add(currentDateInterval.getId());
                    }
                }
            }
        
            stmnt.close();

            return resEvents;
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
            return new LinkedList<>();
        }
	}

	@Override
	public LinkedList<ExpenseCategory> getAllExpCats()
			throws IOException, ClassNotFoundException, GeneralSecurityException {

        try {
            Statement stmnt = conn.createStatement();
            ResultSet resCatsSet = stmnt.executeQuery("SELECT expCatId, expCatName, expCatCode, expCatFinSrc\nFROM expenseCategory");
            LinkedList<ExpenseCategory> resCategories = new LinkedList<ExpenseCategory>(); 

            while (resCatsSet.next()) {
                resCategories.add(new ExpenseCategory(resCatsSet.getInt(1), resCatsSet.getString(2), resCatsSet.getString(3),
                                                      resCatsSet.getString(4)));
            }

            stmnt.close();
        
            return resCategories;
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
            return new LinkedList<>();
        }
	}

	@Override
	public LinkedList<AdmEconTask> getAllAdmEconTasks()
			throws IOException, ClassNotFoundException, GeneralSecurityException {

        try {
            PreparedStatement stmnt = conn.prepareStatement(
                    "SELECT admEconTaskId, admEconTaskCont, admEconTaskProblems, admEconCIId, admEconCIName, admEconCICurVal, admEconCITargVal, admEconEventId, admEconEventContent, admEconEventProg, admEconEventDep, admEconEventFinSrc, admEconEventPartnerOrgs, admEconExpId, admEconExpName, admEconExpUnit, admEconExpAmount, admEconExpUnitCost, admEconExpCatRef, admEconDateIntervalId, admEconStartDate, admEconEndDate\nFROM\n(((admEconTask LEFT JOIN admEconCI ON admEconTask.admEconTaskId = admEconCI.admEconCITaskRef) LEFT JOIN\nadmEconEvent ON admEconEvent.admEconEventTaskRef = admEconTask.admEconTaskId) LEFT JOIN\nadmEconExpense ON admEconExpense.admEconExpEventRef = admEconEvent.admEconEventId) LEFT JOIN\nadmEconEventDateInterval ON admEconEventDateInterval.admEconDateIntervalEventRef = admEconEvent.admEconEventId\nORDER BY admEconTaskId, admEconCIId, admEconEventId, admEconExpId, admEconDateIntervalId ASC\n");

            ResultSet resSet = stmnt.executeQuery();

            LinkedList<AdmEconTask> resTasks = new LinkedList<AdmEconTask>();
            AdmEconTask currentTask = null;
            ConditionIndicator currentCondIndicator = null;
            Event currentEvent = null;
            Expense currentExpense = null;
            LocalDateInterval currentDateInterval = null;

            Set<Integer> currentCIIDs = new HashSet<Integer>();
            Set<Integer> currentEventIDs = new HashSet<Integer>();
            Set<Integer> currentExpIDs = new HashSet<Integer>();
            Set<Integer> currentDateIntervalIDs = new HashSet<Integer>();
        
            while (resSet.next()) {
                // Process adm-econ. task
                if (currentTask == null || resSet.getInt(1) != currentTask.getId()) {
                    currentTask = new AdmEconTask(resSet.getInt(1), resSet.getString(2), resSet.getString(3));
                    resTasks.add(currentTask);
                
                    currentCIIDs.clear();
                    currentEventIDs.clear();
                }

                // Process condition indicators
                resSet.getInt(4);
                if (!resSet.wasNull()) {
                    if ((currentCondIndicator == null || resSet.getInt(4) != currentCondIndicator.getId()) &&
                        !currentCIIDs.contains(resSet.getInt(4))) {
                        currentCondIndicator = new ConditionIndicator(resSet.getInt(4),
                                                                      resSet.getString(5),
                                                                      resSet.getString(6),
                                                                      resSet.getString(7));
                    
                        if (currentTask.getAdmEconCondIndicators() == null) {
                            currentTask.setAdmEconCondIndicators(new LinkedList<ConditionIndicator>());
                        }
                        currentTask.getAdmEconCondIndicators().add(currentCondIndicator);
                        currentCIIDs.add(currentCondIndicator.getId());
                    }
                }
            
                // Process events
                resSet.getInt(8);
                if (!resSet.wasNull()) {
                    if ((currentEvent == null || resSet.getInt(8) != currentEvent.getId()) &&
                        !currentEventIDs.contains(resSet.getInt(8))) {
                        currentEvent = new Event(resSet.getInt(8), resSet.getString(9),
                                                 resSet.getString(10), resSet.getString(11));
                        currentEvent.setFinSource(resSet.getString(12));
                        currentEvent.setPartnerOrgs(resSet.getString(13));
                    
                        if (currentTask.getAdmEconEvents() == null) {
                            currentTask.setAdmEconEvents(new LinkedList<Event>());
                        }
                        currentTask.getAdmEconEvents().add(currentEvent);
                        currentEventIDs.add(currentEvent.getId());
                        currentExpIDs.clear();
                        currentDateIntervalIDs.clear();
                    }
                }
            
                // Process expenses
                resSet.getInt(14);
                if (!resSet.wasNull()) {
                    if ((currentExpense == null || resSet.getInt(14) != currentExpense.getId()) &&
                        !currentExpIDs.contains(resSet.getInt(14))) {
                        currentExpense = new Expense(resSet.getInt(14),
                                                     resSet.getString(15),
                                                     resSet.getString(16),
                                                     new FixedPointDec(resSet.getString(17)),
                                                     new FixedPointDec(resSet.getString(18)),
                                                     resSet.getInt(19));
                    
                        if (currentEvent.getExpenses() == null) {
                            currentEvent.setExpenses(new LinkedList<Expense>());
                        }
                        currentEvent.getExpenses().add(currentExpense);
                        currentExpIDs.add(currentExpense.getId());
                    }
                }

                // Process date intervals
                resSet.getInt(20);
                if (!resSet.wasNull()) {
                    if ((currentDateInterval == null || resSet.getInt(20) != currentDateInterval.getId()) &&
                        !currentDateIntervalIDs.contains(resSet.getInt(20))) {
                        currentDateInterval = new LocalDateInterval(resSet.getInt(20),
                                                                    (resSet.getDate(21) == null) ? null : resSet.getDate(21).toLocalDate(),
                                                                    (resSet.getDate(22) == null) ? null : resSet.getDate(22).toLocalDate());
                        if (currentEvent.getDateIntervals() == null) {
                            currentEvent.setDateIntervals(new ArrayList<LocalDateInterval>());
                        }
                        currentEvent.getDateIntervals().add(currentDateInterval);
                        currentDateIntervalIDs.add(currentDateInterval.getId());
                    }
                }
            }
        
            stmnt.close();

            return resTasks;
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
            return new LinkedList<>();
        }
	}

	@Override
	public void updateKeyValue(KeyValue keyValue) throws IOException, ClassNotFoundException, GeneralSecurityException {
        try {
            if (keyValue.getId() == null) {
                throw new IllegalArgumentException("Id of the element to be updated must not be null before update");
            }

            PreparedStatement stmnt = conn.prepareStatement(
                "UPDATE keyValue\nSET kVName = ?,\nkVDesc = ?\nWHERE kVId = ?\n");
            stmnt.setString(1, keyValue.getName());
            stmnt.setString(2, keyValue.getDescription());
            stmnt.setInt(3, keyValue.getId());

            stmnt.executeUpdate();

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
	}

	@Override
	public void updateRationaleUnit(RationaleUnit rationaleUnit)
			throws IOException, ClassNotFoundException, GeneralSecurityException {
		
        try {
            if (rationaleUnit.getId() == null) {
                throw new IllegalArgumentException("Id of the element to be updated must not be null before update");
            }

            PreparedStatement stmnt = conn.prepareStatement(
                "UPDATE rationaleUnit\nSET rUThreat = ?,\nrUThreatReasons = ?,\nrUProblems = ?\nWHERE rUId = ?\n");
            stmnt.setString(1, rationaleUnit.getThreat());
            stmnt.setString(2, rationaleUnit.getThreatReasons());
            stmnt.setString(3, rationaleUnit.getProblems());
            stmnt.setInt(4, rationaleUnit.getId());

            stmnt.executeUpdate();

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
		
	}

	@Override
	public void updateCondIndicator(ConditionIndicator condIndicator)
			throws IOException, ClassNotFoundException, GeneralSecurityException {

		try {
            if (condIndicator.getId() == null) {
                throw new IllegalArgumentException("Id of the element to be updated must not be null before update");
            }

            System.out.println(condIndicator.getId() + " : " + condIndicator.getName());

            PreparedStatement stmnt = conn.prepareStatement(
                "UPDATE condIndicator\nSET cIName = ?,\ncICurrentVal = ?,\ncITargetVal = ?\nWHERE cIId = ?\n");
            stmnt.setString(1, condIndicator.getName());
            stmnt.setString(2, condIndicator.getCurrentValue());
            stmnt.setString(3, condIndicator.getTargetValue());
            stmnt.setInt(4, condIndicator.getId());
        
            stmnt.executeUpdate();

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
	}

	@Override
	public void updateTask(Task task) throws IOException, ClassNotFoundException, GeneralSecurityException {
        
		try {
            if (task.getId() == null) {
                throw new IllegalArgumentException("Id of the element to be updated must not be null before update");
            }

            PreparedStatement stmnt = conn.prepareStatement(
                "UPDATE task\nSET taskContent = ?\nWHERE taskId = ?\n");
            stmnt.setString(1, task.getContent());
            stmnt.setInt(2, task.getId());

            stmnt.executeUpdate();

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
	}

	@Override
	public void updateEvent(Event event) throws IOException, ClassNotFoundException, GeneralSecurityException {
		
		try {
            if (event.getId() == null) {
                throw new IllegalArgumentException("Id of the element to be updated must not be null before update");
            }

            PreparedStatement stmnt = conn.prepareStatement(
                "UPDATE event\nSET eventContent = ?,\neventProgramme = ?,\neventDepartment = ?,\neventFinSrc = ?,\neventPatnerOrgs = ?\nWHERE eventId = ?\n");
            stmnt.setString(1, event.getContent());
            stmnt.setString(2, event.getProgramme());
            stmnt.setString(3, event.getDepartment());
            stmnt.setString(4, event.getFinSource());
            stmnt.setString(5, event.getPartnerOrgs());
            stmnt.setInt(6, event.getId());

            stmnt.executeUpdate();

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
	}

	@Override
	public void updateEventDateInterval(LocalDateInterval dateInterval)
			throws IOException, ClassNotFoundException, GeneralSecurityException {
		
		try {
            if (dateInterval.getId() == null) {
                throw new IllegalArgumentException("Id of the element to be updated must not be null before update");
            }

            PreparedStatement stmnt = conn.prepareStatement(
                "UPDATE eventDateInterval\nSET startDate = ?,\n    endDate = ?\nWHERE dateIntervalId = ?\n");

            if (dateInterval.getStartDate() != null) {
                stmnt.setDate(1, Date.valueOf(dateInterval.getStartDate()));
            }
            else {
                stmnt.setNull(1, Types.DATE);
            }

            if (dateInterval.getEndDate() != null) {
                stmnt.setDate(2, Date.valueOf(dateInterval.getEndDate()));
            }
            else {
                stmnt.setNull(2, Types.DATE);
            }
        
            stmnt.setInt(3, dateInterval.getId());

            stmnt.executeUpdate();

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
	}

	@Override
	public void updatePartnerOrg(PartnerOrg partnerOrg)
			throws IOException, ClassNotFoundException, GeneralSecurityException {
		
		try {
            if (partnerOrg.getId() == null) {
                throw new IllegalArgumentException("Id of the element to be updated must not be null before update");
            }

            PreparedStatement stmnt = conn.prepareStatement(
                "UPDATE partnerOrg\nSET partnerOrgName = ?\nWHERE partnerOrgId = ?\n");
            stmnt.setString(1, partnerOrg.getName());
            stmnt.setInt(2, partnerOrg.getId());

            stmnt.executeUpdate();

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
	}

	@Override
	public void updateExpCategory(ExpenseCategory expCat)
			throws IOException, ClassNotFoundException, GeneralSecurityException {
		
		try {
            if (expCat.getId() == null) {
                throw new IllegalArgumentException("Id of the element to be updated must not be null before update");
            }

            PreparedStatement stmnt = conn.prepareStatement(
                "UPDATE expenseCategory\nSET expCatName = ?,\nexpCatCode = ?,\nexpCatFinSrc = ?\nWHERE expCatId = ?\n");
            stmnt.setString(1, expCat.getName());
            stmnt.setString(2, expCat.getCategoryCode());
            stmnt.setString(3, expCat.getFinSource());
            stmnt.setInt(4, expCat.getId());

            stmnt.executeUpdate();

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
	}

	@Override
	public void updateExpense(Expense exp) throws IOException, ClassNotFoundException, GeneralSecurityException {
		
		try {
            if (exp.getId() == null) {
                throw new IllegalArgumentException("Id of the element to be updated must not be null before update");
            }

            PreparedStatement stmnt = conn.prepareStatement(
                "UPDATE expense\nSET expName = ?,\nexpUnit = ?,\nexpAmount = ?,\nexpUnitCost = ?,\nexpExpCatRef = ?\nWHERE expId = ?\n");
            stmnt.setString(1, exp.getName());
            stmnt.setString(2, exp.getUnit());
            stmnt.setString(3, exp.getAmount().toString());
            stmnt.setString(4, exp.getUnitCost().toString());
            stmnt.setInt(5, exp.getExpCatId());
            stmnt.setInt(6, exp.getId());

            stmnt.executeUpdate();

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
	}

	@Override
	public void updateAdmEconTask(AdmEconTask task)
			throws IOException, ClassNotFoundException, GeneralSecurityException {
		
		try {
            if (task.getId() == null) {
                throw new IllegalArgumentException("Id of the element to be updated must not be null before update");
            }

            PreparedStatement stmnt = conn.prepareStatement(
                "UPDATE admEconTask\nSET admEconTaskCont = ?,\nadmEconTaskProblems = ?\nWHERE admEconTaskId = ?\n");
            stmnt.setString(1, task.getContent());
            stmnt.setString(2, task.getProblems());
            stmnt.setInt(3, task.getId());

            stmnt.executeUpdate();

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
	}

	@Override
	public void updateAdmEconCI(ConditionIndicator condIndicator)
			throws IOException, ClassNotFoundException, GeneralSecurityException, GeneralSecurityException {
		
		try {
            if (condIndicator.getId() == null) {
                throw new IllegalArgumentException("Id of the element to be updated must not be null before update");
            }

            System.out.println(condIndicator.getId() + " : " + condIndicator.getName());

            PreparedStatement stmnt = conn.prepareStatement(
                "UPDATE admEconCI\nSET admEconCIName = ?,\nadmEconCICurVal = ?,\nadmEconCITargVal = ?\nWHERE admEconCIId = ?\n");
            stmnt.setString(1, condIndicator.getName());
            stmnt.setString(2, condIndicator.getCurrentValue());
            stmnt.setString(3, condIndicator.getTargetValue());
            stmnt.setInt(4, condIndicator.getId());
        
            stmnt.executeUpdate();

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
	}

	@Override
	public void updateAdmEconEvent(Event event) throws IOException, ClassNotFoundException, GeneralSecurityException {

        try {
            if (event.getId() == null) {
                throw new IllegalArgumentException("Id of the element to be updated must not be null before update");
            }

            PreparedStatement stmnt = conn.prepareStatement(
                "UPDATE admEconEvent\nSET admEconEventContent = ?,\nadmEconEventProg = ?,\nadmEconEventDep = ?,\nadmEconEventFinSrc = ?,\nadmEconEventPartnerOrgs = ?\nWHERE admEconEventId = ?\n");
            stmnt.setString(1, event.getContent());
            stmnt.setString(2, event.getProgramme());
            stmnt.setString(3, event.getDepartment());
            stmnt.setString(4, event.getFinSource());
            stmnt.setString(5, event.getPartnerOrgs());
            stmnt.setInt(6, event.getId());

            stmnt.executeUpdate();

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
	}

	@Override
	public void updateAdmEconExpense(Expense exp)
			throws IOException, ClassNotFoundException, GeneralSecurityException {

        try {
            if (exp.getId() == null) {
                throw new IllegalArgumentException("Id of the element to be updated must not be null before update");
            }

            PreparedStatement stmnt = conn.prepareStatement(
                "UPDATE admEconExpense\nSET admEconExpName = ?,\nadmEconExpUnit = ?,\nadmEconExpAmount = ?,\nadmEconExpUnitCost = ?,\nadmEconExpCatRef = ?\nWHERE admEconExpId = ?\n");
            stmnt.setString(1, exp.getName());
            stmnt.setString(2, exp.getUnit());
            stmnt.setString(3, exp.getAmount().toString());
            stmnt.setString(4, exp.getUnitCost().toString());
            stmnt.setInt(5, exp.getExpCatId());
            stmnt.setInt(6, exp.getId());

            stmnt.executeUpdate();

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
		catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
	}

	@Override
	public void updateAdmEconDateInterval(LocalDateInterval dateInterval)
			throws IOException, ClassNotFoundException, GeneralSecurityException {
        
        try {
            if (dateInterval.getId() == null) {
                throw new IllegalArgumentException("Id of the element to be updated must not be null before update");
            }

            PreparedStatement stmnt = conn.prepareStatement(
                "UPDATE admEconEventDateInterval\nSET admEconStartDate = ?,\n    admEconEndDate = ?\nWHERE admEconDateIntervalId = ?\n");

            if (dateInterval.getStartDate() != null) {
                stmnt.setDate(1, Date.valueOf(dateInterval.getStartDate()));
            }
            else {
                stmnt.setNull(1, Types.DATE);
            }

            if (dateInterval.getEndDate() != null) {
                stmnt.setDate(2, Date.valueOf(dateInterval.getEndDate()));
            }
            else {
                stmnt.setNull(2, Types.DATE);
            }
        
            stmnt.setInt(3, dateInterval.getId());

            stmnt.executeUpdate();

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
		catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
    }

	@Override
	public void deleteKeyValue(KeyValue keyValue) throws IOException, ClassNotFoundException, GeneralSecurityException {

        try {
            if (keyValue.getId() == null) {
                throw new IllegalArgumentException("Id of the element to be inserted must not be null before deletion");
            }

            PreparedStatement stmnt = conn.prepareStatement("DELETE FROM keyValue\nWHERE kVId = ?\n");
            stmnt.setInt(1, keyValue.getId());
            stmnt.executeUpdate();

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
		
	}

	@Override
	public void deleteRationaleUnit(RationaleUnit rationaleUnit)
			throws IOException, ClassNotFoundException, GeneralSecurityException {

        try {
            if (rationaleUnit.getId() == null) {
                throw new IllegalArgumentException("Id of the element to be inserted must not be null before deletion");
            }

            PreparedStatement stmnt = conn.prepareStatement("DELETE FROM rationaleUnit\nWHERE rUId = ?\n");
            stmnt.setInt(1, rationaleUnit.getId());
            stmnt.executeUpdate();

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
		
	}

	@Override
	public void deleteCondIndicator(ConditionIndicator condIndicator)
			throws IOException, ClassNotFoundException, GeneralSecurityException {

        try {
            if (condIndicator.getId() == null) {
                throw new IllegalArgumentException("Id of the element to be inserted must not be null before deletion");
            }

            PreparedStatement stmnt = conn.prepareStatement("DELETE FROM condIndicator\nWHERE cIId = ?\n");
            stmnt.setInt(1, condIndicator.getId());
            stmnt.executeUpdate();

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
		
	}

	@Override
	public void deleteTask(Task task) throws IOException, ClassNotFoundException, GeneralSecurityException {

        try {
            if (task.getId() == null) {
                throw new IllegalArgumentException("Id of the element to be inserted must not be null before deletion");
            }

            PreparedStatement stmnt = conn.prepareStatement("DELETE FROM task\nWHERE taskId = ?");
            stmnt.setInt(1, task.getId());
            stmnt.executeUpdate();

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
		
	}

	@Override
	public void deleteEvent(Event event) throws IOException, ClassNotFoundException, GeneralSecurityException {

        try {
            if (event.getId() == null) {
                throw new IllegalArgumentException("Id of the element to be inserted must not be null before deletion");
            }

            PreparedStatement stmnt = conn.prepareStatement("DELETE FROM event\nWHERE eventId = ?\n");
            stmnt.setInt(1, event.getId());
            stmnt.executeUpdate();

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
		
	}

	@Override
	public void deletePartnerOrg(PartnerOrg partnerOrg)
			throws IOException, ClassNotFoundException, GeneralSecurityException {

        try {
            if (partnerOrg.getId() == null) {
                throw new IllegalArgumentException("Id of the element to be inserted must not be null before deletion");
            }

            PreparedStatement stmnt = conn.prepareStatement("DELETE FROM partnerOrg\nWHERE partnerOrgId = ?\n");
            stmnt.setInt(1, partnerOrg.getId());
            stmnt.executeUpdate();

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
		
	}

	@Override
	public void deleteExpCategory(ExpenseCategory expCat)
			throws IOException, ClassNotFoundException, GeneralSecurityException {

        try {
            if (expCat.getId() == null) {
                throw new IllegalArgumentException("Id of the element to be inserted must not be null before deletion");
            }

            PreparedStatement stmnt = conn.prepareStatement("DELETE FROM expenseCategory\nWHERE expCatId = ?\n");
            stmnt.setInt(1, expCat.getId());
            stmnt.executeUpdate();

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
		
	}

	@Override
	public void deleteExpense(Expense exp) throws IOException, ClassNotFoundException, GeneralSecurityException {
		
        try {
            if (exp.getId() == null) {
                throw new IllegalArgumentException("Id of the element to be inserted must not be null before deletion");
            }

            PreparedStatement stmnt = conn.prepareStatement("DELETE FROM expense\nWHERE expId = ?\n");
            stmnt.setInt(1, exp.getId());
            stmnt.executeUpdate();

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
		catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
	}

	@Override
	public void deleteEventDateInterval(LocalDateInterval dateInterval)
			throws IOException, ClassNotFoundException, GeneralSecurityException {

        try {
            if (dateInterval.getId() == null) {
                throw new IllegalArgumentException("Id of the element to be inserted must not be null before deletion");
            }

            PreparedStatement stmnt = conn.prepareStatement("DELETE FROM eventDateInterval\nWHERE dateIntervalId = ?\n");
            stmnt.setInt(1, dateInterval.getId());
            stmnt.executeUpdate();

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
		
	}

	@Override
	public void deleteAdmEconTask(AdmEconTask task)
			throws IOException, ClassNotFoundException, GeneralSecurityException {

        try {
            if (task.getId() == null) {
                throw new IllegalArgumentException("Id of the element to be inserted must not be null before deletion");
            }

            PreparedStatement stmnt = conn.prepareStatement("DELETE FROM admEconTask\nWHERE admEconTaskId = ?");
            stmnt.setInt(1, task.getId());
            stmnt.executeUpdate();

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
		
	}

	@Override
	public void deleteAdmEconCI(ConditionIndicator condIndicator)
        throws IOException, ClassNotFoundException, GeneralSecurityException {

        try {
            if (condIndicator.getId() == null) {
                throw new IllegalArgumentException("Id of the element to be inserted must not be null before deletion");
            }

            PreparedStatement stmnt = conn.prepareStatement("DELETE FROM admEconCI\nWHERE admEconCIId = ?\n");
            stmnt.setInt(1, condIndicator.getId());
            stmnt.executeUpdate();

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
        catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
		
	}

	@Override
	public void deleteAdmEconEvent(Event event) throws IOException, ClassNotFoundException, GeneralSecurityException {
		
		try {
            if (event.getId() == null) {
                throw new IllegalArgumentException("Id of the element to be inserted must not be null before deletion");
            }

            PreparedStatement stmnt = conn.prepareStatement("DELETE FROM admEconEvent\nWHERE admEconEventId = ?\n");
            stmnt.setInt(1, event.getId());
            stmnt.executeUpdate();

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
		catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
	}

	@Override
	public void deleteAdmEconExpense(Expense exp)
			throws IOException, ClassNotFoundException, GeneralSecurityException {
		
		try {
            if (exp.getId() == null) {
                throw new IllegalArgumentException("Id of the element to be inserted must not be null before deletion");
            }

            PreparedStatement stmnt = conn.prepareStatement("DELETE FROM admEconExpense\nWHERE admEconExpId = ?\n");
            stmnt.setInt(1, exp.getId());
            stmnt.executeUpdate();

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
		catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
	}

	@Override
	public void deleteAdmEconDateInterval(LocalDateInterval dateInterval)
			throws IOException, ClassNotFoundException, GeneralSecurityException {
		
		try {
            if (dateInterval.getId() == null) {
                throw new IllegalArgumentException("Id of the element to be inserted must not be null before deletion");
            }

            PreparedStatement stmnt = conn.prepareStatement(
                "DELETE FROM admEconEventDateInterval\nWHERE admEconDateIntervalId = ?\n");
            stmnt.setInt(1, dateInterval.getId());
            stmnt.executeUpdate();

            stmnt.close();

            if (mustCommit) {
                conn.commit();
            }
        }
		catch (SQLException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
	}


    private void readPAIndex() throws IOException {
        if (!paIndexFile.exists()) {
            paIndexFile.createNewFile();
            flushPAIndex();
            return;
        }

        ObjectInputStream fin = null;
        
        try {
            fin =
                new ObjectInputStream(
                    new BufferedInputStream(
                        new FileInputStream(paIndexFile)));
            Object fileObject = fin.readObject();
            protectedAreasIndex = (List<ProtectedArea>) fileObject;
        }
        catch (ClassNotFoundException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
        finally {
            if (fin != null) {
                fin.close();
            }
        }
    }

    private void flushPAIndex() throws IOException {
        if (protectedAreasIndex == null) {
            return;
        }
        
        ObjectOutputStream fout = null;

        try {
            fout =
                new ObjectOutputStream(
                    new BufferedOutputStream(
                        new FileOutputStream(paIndexFile)));
            fout.writeObject(protectedAreasIndex);
        }
        finally {
            if (fout != null) {
                fout.close();
            }
        }
    }

    private String getLocalProtectedAreaDBPath(ProtectedArea protectedArea) {
        if (protectedArea == null || protectedArea.getId() == null) {
            return null;
        }
            
        AppDirs appDirs = AppDirsFactory.getInstance();
        String dataDirPath = appDirs.getUserDataDir("finplan", null, "mhklv");
        File dataDirFile = new File(dataDirPath);
        
        if (!dataDirFile.exists()) {
            dataDirFile.mkdirs();
        }
        
        String dbPath = dataDirPath + File.separator + protectedArea.getId() + "-local-PA.sqlite";

        return dbPath;
    }

    private int getNextPAId() {
        if (protectedAreasIndex == null) {
            return 0;
        }

        int maxId = 0;
        for (ProtectedArea pa : protectedAreasIndex) {
            if (pa.getId() > maxId) {
                maxId = pa.getId();
            }
        }

        return maxId;
    }

    private void initDBWithTables(Connection connection) throws SQLException {
        Statement stmnt = connection.createStatement();
        String[][] tablesNamesAndDefs = LocalDatabaseTables.TABLES_NAMES_AND_DEFINITIONS;
        
        for (int i = 0; i < tablesNamesAndDefs.length; ++i) {
            stmnt.executeUpdate("CREATE TABLE IF NOT EXISTS " +
                                tablesNamesAndDefs[i][0] +
                                " " + tablesNamesAndDefs[i][1]);
        }

        stmnt.close();
    }

    private void insertDefaultKVGroups(Connection conn, boolean mustCommit) throws SQLException {
        Statement stmnt = conn.createStatement();
        stmnt.executeUpdate(
                "INSERT INTO keyValueGroup (kVGName)\nVALUES\n('Эталонные экосистемы'),\n('Значимые экосистемы (редкие, исчезающие, эндемичные, эколого-стабилизирующие функции)'),\n('Редкие виды флоры (Красная книга РК, МСОП)'),\n('Ценные пищевые, лекарственные растения'),\n('Редкие виды фауны (Красная книга РК, МСОП)'),\n('Охотничье-промысловые виды'),\n('Ключевые охраняемые виды и крупные скопления животных'),\n('Экосистемные услуги'),\n('Генетические ресурсы'),\n('Уникальные природные комплексы и объекты'),\n('Глобально значимые природные комплексы и объекты UNESCO, IBA, KBA, Ramsar'),\n('Памятники истории и культуры'),\n('Рекреационные и бальнеологические ресурсы'),\n('Другое')");
        stmnt.close();
        
        if (mustCommit) {
            conn.commit();
        }
    }

    private void insertDefaultAdmEconTasks(Connection conn, boolean mustCommit) throws SQLException {
        Statement stmnt = conn.createStatement();
        stmnt.executeUpdate(
                "INSERT INTO admEconTask (admEconTaskCont)\nVALUES\n('Обеспечение оплаты труда сотрудников ООПТ согласно штатному расписанию'),\n('Содержание капитальных активов'), \n('Содержание инфраструктурных объектов'), \n('Содержание автопарка'),\n('Обеспечение охраны территории ООПТ и предотвращение незаконных видов деятельности (ГСМ, закуп техники, обмундирование, оружие, гужевой транспорт, связь)'),\n('Обеспечение противопожарной безопасности на территории ООПТ и прилегающих территориях'),\n('Обеспечение регулярных биотехнических мероприятий на территории ООПТ (виды рубок,  санитарные мероприятия, , борьба с вредителями и болезнями, подкормки.)'),\n('Осуществление хозяйственной деятельности с целью пополнения спец счета (туризм, продажи, услуги)'),\n('Привлечение дополнительного финансирования на природоохранные цели (гранты, пожертвования)'),\n('Обновление/разработка учредительных, проектных  и стратегических документов согласно законодательству об ООПТ (карты, акты, зем-лес проекты)')\n");
        
        stmnt.close();
        
        if (mustCommit) {
            conn.commit();
        }
    } 

    private void insertDefaultExpCats(Connection conn, boolean mustCommit) throws SQLException {
        Statement stmnt = conn.createStatement();
        stmnt.executeUpdate(
            "INSERT INTO expenseCategory (expCatCode, expCatName)\nVALUES\n('111', 'Оплата труда'),\n('112', 'Дополнительные денежные выплаты'),\n('113', 'Компенсационные выплаты'),\n('116', 'Обязательные пенсионные взносы работодателей'),\n('121', 'Социальный налог'),\n('122', 'Социальное отчисление в Государственный фонд социального страхования'),\n('123', 'Взносы на обязательное страхование'),\n('124', 'Отчисления на обязательное социальное медицинское страхование'),\n('131', 'Оплата труда технического персонала'),\n('135', 'Взносы работодателей по техническому персоналу'),\n('136', 'Командировки и служебные разъезды внутри страны технического персонала'),\n('141', 'Приобретение продуктов питания'),\n('142', 'Приобретение медикаментов и прочих средств медицинского назначения'),\n('143', 'Приобретение, пошив и ремонт предметов вещевого имущества и другого форменного и специального обмундирования'),\n('144', 'Приобретение топлива, горюче-смазочных материалов'),\n('149', 'Приобретение прочих  (расходные материалы)'),\n('151', 'Оплата коммунальных услуг'),\n('152', 'Оплата услуг связи'),\n('153', 'Оплата транспортных услуг'),\n('154', 'Оплата аренды за помещение'),\n('155', 'Оплата услуг в рамках гос соц заказа (НКО)'),\n('156', 'Оплата консалтинговых услуг и исследований'),\n('159', 'Оплата прочих услуг и работ (суб-контракты)'),\n('161', 'Командировки и служебные разъезды внутри страны'),\n('165', 'Исполнение исполнительных документов, судебных актов'),\n('169', 'Прочие текущие затраты'),\n('411', 'Приобретение земли'),\n('412', 'Приобретение помещений, передаточных устройств зданий, сооружений'),\n('413', 'Приобретение втомобильных транспортных средств'),\n('414', 'Приобретение  вычислительного и другого оборудования, инструментов, производственного и хозяйственного инвентаря'),\n('416', 'Приобретение нематериальных активов'),\n('417', 'Приобретение биологических активов'),\n('421', 'Капитальный ремонт  помещений, зданий сооружений, передаточных устройств'),\n('429', 'Капитальный ремонт прочих основных средств'),\n('431', 'Строительство новых объектов и реконструкция имеющихся объектов'),\n('434', 'Создание, внедрение и развитие информационных систем')");
        stmnt.close();
        
        if (mustCommit) {
            conn.commit();
        }
    }
}
