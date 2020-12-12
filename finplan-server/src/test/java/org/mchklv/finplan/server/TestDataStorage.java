package org.mchklv.finplan.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mchklv.finplan.common.*;



public class TestDataStorage {
    private Connection DBConn;
    
    @Before
    public void initTestDB() throws SQLException {
        String url = "jdbc:mysql://localhost/test";
        DBConn = DriverManager.getConnection(url, "testuser", "123");
        DBConn.setAutoCommit(false);

        DataStorage.dropAllTables(DBConn);
        DataStorage.createAllTables(DBConn);
        DataStorage.insertDefaultKVGroups(DBConn, true);
        DataStorage.insertDefaultExpCats(DBConn, true);
    }

    
    @Test
    public void generalStorageTest() throws SQLException {
        LinkedList<KeyValueGroup> kVGroups = DataStorage.getAllKeyValueGroups(DBConn);
        assertEquals(11, kVGroups.size());
        assertEquals("Эталонные экосистемы", kVGroups.get(0).getName());

        for (KeyValueGroup group : kVGroups) {
            assertNotNull(group.getId());
        }

        ProtectedArea ourPA = new ProtectedArea(null, "Кургалжино");
        ourPA.generateHash("123");
        DataStorage.insertProtectedArea(DBConn, ourPA, true);
        assertNotNull(ourPA.getId());
                
        KeyValueGroup kvg1 = kVGroups.get(0);
        KeyValueGroup kvg2 = kVGroups.get(1);

        KeyValue kv1 = new KeyValue(null, "Снежный барс", "Котя");
        KeyValue kv2 = new KeyValue(null, "Вторая кличевая ценность в группе", "Lorem ipsum dolor sit amet ...");
        KeyValue kv3 = new KeyValue(null, "Ключевая ценность из другой группы", "Lorem ipsum dolor  ...");
        
        DataStorage.insertKeyValue(DBConn, kv1, ourPA, kvg1, true);
        DataStorage.insertKeyValue(DBConn, kv2, ourPA, kvg1, true);
        DataStorage.insertKeyValue(DBConn, kv3, ourPA, kvg2, true);

        ConditionIndicator ci1 = new ConditionIndicator(null, "Индикатор 1", "123 га", "1234 га");
        ConditionIndicator ci2 = new ConditionIndicator(null, "Индикатор 2", "123 m", "1234 m");
        ConditionIndicator ci3 = new ConditionIndicator(null, "Индикатор 3", "123 km", "1234 km");

        Task task1 = new Task(null, "Задача 1");
        Task task2 = new Task(null, "Задача 2");
        Task task3 = new Task(null, "Задача 3");

        LinkedList<ConditionIndicator> indicators1 = new LinkedList<ConditionIndicator>();
        indicators1.add(ci1);
        indicators1.add(ci2);

        LinkedList<ConditionIndicator> indicators2 = new LinkedList<ConditionIndicator>();
        indicators2.add(ci3);        


        LinkedList<Task> tasks1 = new LinkedList<Task>();
        tasks1.add(task1);
        tasks1.add(task2);
        tasks1.add(task3);

        RationaleUnit ru1 = new RationaleUnit(null, "Угрозы 1. ... ", "Причины ... ", "проблемы 1 э...");
        RationaleUnit ru2 = new RationaleUnit(null, "Угрозы 2. ... ", "Причины ... ", "проблемы 2 э...");
        RationaleUnit ru3 = new RationaleUnit(null, "Угрозы 3. ... ", "Причины ... ", "проблемы 3 э...");

        DataStorage.insertRationaleUnit(DBConn, ru1, kv1, true);
        DataStorage.insertRationaleUnit(DBConn, ru2, kv1, true);
        DataStorage.insertRationaleUnit(DBConn, ru3, kv2, true);

        assertNotNull(ru1.getId());
        assertNotNull(ru2.getId());
        assertNotNull(ru3.getId());

        DataStorage.insertTask(DBConn, task1, ru1, true);
        DataStorage.insertTask(DBConn, task2, ru1, true);
        DataStorage.insertTask(DBConn, task3, ru2, true);

        assertNotNull(task1.getId());
        assertNotNull(task2.getId());
        assertNotNull(task3.getId());

        DataStorage.insertCondIndicator(DBConn, ci1, ru1, true);
        DataStorage.insertCondIndicator(DBConn, ci2, ru1, true);
        DataStorage.insertCondIndicator(DBConn, ci3, ru2, true);

        assertNotNull(ci1.getId());
        assertNotNull(ci2.getId());
        assertNotNull(ci3.getId());

        LinkedList<KeyValue> returnedKeyValues1 = DataStorage.getAllKeyValues(DBConn, ourPA, kvg1);
        LinkedList<KeyValue> returnedKeyValues2 = DataStorage.getAllKeyValues(DBConn, ourPA, kvg2);

        assertEquals(2, returnedKeyValues1.size());
        for (KeyValue kv : returnedKeyValues1) {
            assertNotNull(kv.getId());
        }
        
        assertEquals("Снежный барс", returnedKeyValues1.get(0).getName());
        assertEquals("Lorem ipsum dolor sit amet ...", returnedKeyValues1.get(1).getDescription());


        LinkedList<RationaleUnit> returnedRUs1 = returnedKeyValues1.get(0).getRationaleUnits();
        LinkedList<RationaleUnit> returnedRUs2 = returnedKeyValues1.get(1).getRationaleUnits();
        LinkedList<RationaleUnit> returnedRUs3 = returnedKeyValues2.get(0).getRationaleUnits();

        assertEquals(2, returnedRUs1.size());
        assertEquals(1, returnedRUs2.size());
        // assertTrue(returnedRUs3.isEmpty());
        assertNull(returnedRUs3);

        assertEquals("Угрозы 2. ... ", returnedRUs1.get(1).getThreat());
        assertEquals("Угрозы 3. ... ", returnedRUs2.get(0).getThreat());

        LinkedList<ConditionIndicator> returnedCIs1 = returnedRUs1.get(0).getCondIndicators();
        LinkedList<ConditionIndicator> returnedCIs2 = returnedRUs1.get(1).getCondIndicators();
        LinkedList<ConditionIndicator> returnedCIs3 = returnedRUs2.get(0).getCondIndicators();

        // for (ConditionIndicator ci : returnedCIs1) {
        //     System.out.println(ci.getName());
        // }
        
        assertEquals(2, returnedCIs1.size());
        assertEquals(1, returnedCIs2.size());
        assertNull(returnedCIs3);

        LinkedList<Task> returnedTasks1 = returnedRUs1.get(0).getTasks();
        LinkedList<Task> returnedTasks2 = returnedRUs1.get(1).getTasks();
        LinkedList<Task> returnedTasks3 = returnedRUs2.get(0).getTasks();

        assertEquals(2, returnedTasks1.size());
        assertEquals(1, returnedTasks2.size());
        assertNull(returnedTasks3);

        ci1.setName("New name");
        DataStorage.updateCondIndicator(DBConn, ci1, true);

        task1.setContent("New contents");
        DataStorage.updateTask(DBConn, task1, true);

        LinkedList<KeyValue> returnedKeyValues = DataStorage.getAllKeyValues(DBConn, ourPA, kvg1);
        assertEquals("New name", returnedKeyValues.get(0).getRationaleUnits().get(0).getCondIndicators().get(0).getName());
        assertEquals("New contents", returnedKeyValues.get(0).getRationaleUnits().get(0).getTasks().get(0).getContent());


        testEventsAccess(DBConn, tasks1);
        testRegistration(DBConn);
    }


    private void testEventsAccess(Connection conn, LinkedList<Task> tasks) throws SQLException {
        PartnerOrg org1 = new PartnerOrg(null, "Org 1");
        PartnerOrg org2 = new PartnerOrg(null, "Org 2");
        PartnerOrg org3 = new PartnerOrg(null, "Org 3");

        Event event1 = new Event(null, "Content 1", "Programme 1", "Department 1");
        Event event2 = new Event(null, "Content 2", "Programme 2", "Department 2");
        Event event3 = new Event(null, "Content 3", "Programme 3", "Department 3");

        DataStorage.insertEvent(conn, event1, tasks.get(0), true);
        DataStorage.insertEvent(conn, event2, tasks.get(0), true);
        DataStorage.insertEvent(conn, event3, tasks.get(1), true);

        assertNotNull(event1.getId());
        assertNotNull(event2.getId());
        assertNotNull(event3.getId());

        DataStorage.insertPartnerOrg(conn, org1, event1, true);
        DataStorage.insertPartnerOrg(conn, org2, event1, true);
        DataStorage.insertPartnerOrg(conn, org3, event2, true);

        assertNotNull(org1.getId());
        assertNotNull(org2.getId());
        assertNotNull(org3.getId());

        ExpenseCategory expCat1 = new ExpenseCategory(null, "name 1", "123", "src 1");
        ExpenseCategory expCat2 = new ExpenseCategory(null, "name 2", "312", "src 2");
        ExpenseCategory expCat3 = new ExpenseCategory(null, "name 3", "213", "src 3");
        
        DataStorage.insertExpCategory(conn, expCat1, event1, true);
        DataStorage.insertExpCategory(conn, expCat2, event1, true);
        DataStorage.insertExpCategory(conn, expCat3, event2, true);

        assertNotNull(expCat1.getId());
        assertNotNull(expCat2.getId());
        assertNotNull(expCat3.getId());

        Expense exp1 = new Expense(null, "name 1", "unit 1", new FixedPointDec("123,1"), new FixedPointDec("311,1"));
        Expense exp2 = new Expense(null, "name 2", "unit 2", new FixedPointDec("123,1"), new FixedPointDec("311,1"));
        Expense exp3 = new Expense(null, "name 3", "unit 3", new FixedPointDec("123,1"), new FixedPointDec("311,1"));

        DataStorage.insertExpense(conn, exp1, expCat1, true);
        DataStorage.insertExpense(conn, exp2, expCat1, true);
        DataStorage.insertExpense(conn, exp3, expCat2, true);

        assertNotNull(exp1.getId());
        assertNotNull(exp2.getId());
        assertNotNull(exp3.getId());

        LinkedList<Event> returnedEvents1 = DataStorage.getAllEvents(conn, tasks.get(0));
        LinkedList<Event> returnedEvents2 = DataStorage.getAllEvents(conn, tasks.get(1));
        LinkedList<Event> returnedEvents3 = DataStorage.getAllEvents(conn, tasks.get(2));

        assertEquals(2, returnedEvents1.size());
        assertEquals(1, returnedEvents2.size());
        assertEquals(0, returnedEvents3.size());


        LinkedList<PartnerOrg> returnedOrgs1 = returnedEvents1.get(0).getPartnerOrgs();

        assertEquals(2, returnedOrgs1.size());
        assertTrue(org1.equals(returnedOrgs1.get(0)));
        assertTrue(org2.equals(returnedOrgs1.get(1)));

        LinkedList<ExpenseCategory> returnedExpCats1 = returnedEvents1.get(0).getExpCategories();

        assertEquals(2, returnedExpCats1.size());

        LinkedList<Expense> returnedExps1 = returnedExpCats1.get(0).getExpenses();

        assertEquals(2, returnedExps1.size());
        assertTrue(exp1.equals(returnedExps1.get(0)));
        assertTrue(exp2.equals(returnedExps1.get(1)));
    }

    private void testRegistration(Connection conn) throws SQLException {
        String quotaKey1 = "asdght54ngh8y415";
        String quotaKey2 = "asdJhj54ng20y415";
        String quotaKey3 = "YsBJhjv4ng29y415";
        
        assertFalse(DataStorage.regQuotaAvailable(conn, quotaKey1));
        assertFalse(DataStorage.regQuotaAvailable(conn, quotaKey2));
        assertFalse(DataStorage.regQuotaAvailable(conn, quotaKey3));
        
        DataStorage.insertNewRegQuota(conn, quotaKey1, true);
        DataStorage.insertNewRegQuota(conn, quotaKey2, true);

        assertTrue(DataStorage.regQuotaAvailable(conn, quotaKey1));
        assertTrue(DataStorage.regQuotaAvailable(conn, quotaKey2));
        assertFalse(DataStorage.regQuotaAvailable(conn, quotaKey3));

        DataStorage.spendOutRegQuota(conn, quotaKey1, true);

        assertFalse(DataStorage.regQuotaAvailable(conn, quotaKey1));
        assertTrue(DataStorage.regQuotaAvailable(conn, quotaKey2));
        assertFalse(DataStorage.regQuotaAvailable(conn, quotaKey3));

        DataStorage.spendOutRegQuota(conn, quotaKey2, true);

        assertFalse(DataStorage.regQuotaAvailable(conn, quotaKey1));
        assertFalse(DataStorage.regQuotaAvailable(conn, quotaKey2));
        assertFalse(DataStorage.regQuotaAvailable(conn, quotaKey3));
    }
    
    @After
    public void finalizeTestDB() throws SQLException {
        // DataStorage.dropAllTables(DBConn);
    }
}
