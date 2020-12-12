package org.mchklv.finplan.client;

import static org.junit.Assert.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.LinkedList;

import org.junit.Test;
import org.mchklv.finplan.common.*;



public class TestNetworkDataProvider {
    static ProtectedArea ourPA = new ProtectedArea(1, "OOPT 1");
    @Test
    public void generalTest() throws IOException, GeneralSecurityException, ClassNotFoundException {
        DataProvider provider = new NetworkDataProvider("83.220.174.169");
        provider.connect();
        provider.authorize(ourPA, "12345");
        
        testInsertions(provider);
       
        provider.closeConnection();
    }

    private void testInsertions(DataProvider provider) throws IOException, ClassNotFoundException {
        LinkedList<KeyValueGroup> kVGroups = provider.getAllKeyValueGroups();
        assertEquals(11, kVGroups.size());

        LinkedList<ProtectedArea> protectedAreas = provider.getAllProtectedAreas();

        
        
        
        // KeyValueGroup kvg2 = kVGroups.get(1);
        // KeyValue kv1 = new KeyValue(null, "Name kv 1", "Desc kv 2");
        // provider.insertKeyValue(kv1, kvg2);
        // assertNotNull(kv1.getId());

        // LinkedList<KeyValue> keyValues1 = provider.getAllKeyValues();
    }
}
