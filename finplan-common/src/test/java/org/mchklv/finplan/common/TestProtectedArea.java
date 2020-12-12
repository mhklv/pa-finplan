package org.mchklv.finplan.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class TestProtectedArea {
    @Test
    public void testHashing() {
        ProtectedArea pa1 = new ProtectedArea();
        ProtectedArea pa2 = new ProtectedArea();

        pa1.generateHash("pass1");
        
        assertEquals(64, pa1.getPassHash().length());
        assertEquals(10, pa1.getPassSalt().length());
        assertFalse(pa1.isPasswordValid("123456"));
        assertTrue(pa1.isPasswordValid("pass1"));

        assertFalse(pa2.isPasswordValid("asdasd"));

        pa2.generateHash("pass123");

        assertEquals(64, pa2.getPassHash().length());
        assertEquals(10, pa2.getPassSalt().length());
        assertFalse(pa2.isPasswordValid("pass1"));
        assertTrue(pa2.isPasswordValid("pass123"));

        assertNotEquals(pa1.getPassHash(), pa2.getPassHash());
    }
}
