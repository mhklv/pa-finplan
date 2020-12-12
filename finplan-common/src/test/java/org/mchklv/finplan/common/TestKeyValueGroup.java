package org.mchklv.finplan.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;

import org.junit.Test;

public class TestKeyValueGroup {
    @Test
    public void testRefs() {
        KeyValue kv1 = new KeyValue(1, "name 1", "desc 1");
        KeyValue kv2 = new KeyValue(2, "name 2", "desc 2");
        KeyValueGroup kvg = new KeyValueGroup(1, "kvg desc 1");

        LinkedList<KeyValue> keyValues = new LinkedList<KeyValue>();
        keyValues.add(kv1);
        keyValues.add(kv2);

        kvg.setKeyValues(keyValues);

        kv1.setName("naaame 42");

        keyValues = kvg.getKeyValues();
        KeyValue kv = keyValues.get(0);

        assertEquals(kv.getName(), "naaame 42");
        assertTrue(kv1 == kv);
        
    }
}
