package org.mchklv.finplan.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.Console;
import java.text.NumberFormat;
import java.util.ArrayList;

import org.junit.Test;
import org.junit.function.ThrowingRunnable;


public class TestFixedDecimal {
    @Test
    public void testConstructors() {
        String[][] constuctorCorrectCases = {
            {"123", "123,00"},
            {"1,1", "1,10"},
            {"9,51", "9,51"},
            {"45,67", "45,67"},
            {"0,16", "0,16"},
            {"0,60", "0,60"},
            {",13", "0,13"},
            {",5", "0,50"},
            {"0,05", "0,05"},
            {"123456789,12", "123456789,12"},
            {"0012", "12,00"},
            {"0012,06", "12,06"},
            {"0012,6", "12,60"},
            {"0012,60", "12,60"}
        };

        for (String[] testCase : constuctorCorrectCases) {
            FixedPointDec a = new FixedPointDec(testCase[0]);
            assertEquals(testCase[1], a.toString());
        }


        String[] throwingCases = {
            "1,12421",
            "02131.213123",
            "2sd21312d",
            "123q,23we1",
            "1,2,3",
            "1,2.2",
            "123123345.12"
        };

        for (final String throwingCase : throwingCases) {
            assertThrows(NumberFormatException.class, new ThrowingRunnable(){
                public void run() throws NumberFormatException {
                    FixedPointDec m = new FixedPointDec(throwingCase);
                }
            });
        }
    }

    @Test
    public void addTest() {
        String[][] addCases = {
            {"123", "321,0", "444,00"},
            {"123,12", "234,56", "357,68"},
            {"123456789123456789", "987654321123456789,14", "1111111110246913578,14"}
        };

        for (String[] testCase : addCases) {
            FixedPointDec a = new FixedPointDec(testCase[0]);
            FixedPointDec b = new FixedPointDec(testCase[1]);
            a.add(b);
            assertEquals(testCase[2], a.toString());
        }

        for (String[] testCase : addCases) {
            FixedPointDec a = new FixedPointDec(testCase[0]);
            FixedPointDec b = new FixedPointDec(testCase[1]);
            String aStr = a.toString();
            String bStr = b.toString();
            FixedPointDec c = a.added(b);
            
            assertEquals(testCase[2], c.toString());
            assertEquals(aStr, a.toString());
            assertEquals(bStr, b.toString());
        }
    }

    @Test
    public void subtractTest() {
        String[][] addCases = {
            {"321,0", "123", "198,00"},
            {"234,56", "123,12", "111,44"},
            {"987654321123456789,14", "123456789123456789", "864197532000000000,14"}
        };

        for (String[] testCase : addCases) {
            FixedPointDec a = new FixedPointDec(testCase[0]);
            FixedPointDec b = new FixedPointDec(testCase[1]);
            a.subtract(b);
            assertEquals(testCase[2], a.toString());
        }

        for (String[] testCase : addCases) {
            FixedPointDec a = new FixedPointDec(testCase[0]);
            FixedPointDec b = new FixedPointDec(testCase[1]);
            String aStr = a.toString();
            String bStr = b.toString();
            FixedPointDec c = a.subtracted(b);
            
            assertEquals(testCase[2], c.toString());
            assertEquals(aStr, a.toString());
            assertEquals(bStr, b.toString());
        }
    }

    @Test
    public void multiplyTest() {
        String[][] addCases = {
            {"321,0", "123", "39483,00"},
            {"234,56", "123,12", "28879,02"},
            {"987654321123456789,14", "123456789123456789", "121932631249809478895351319227474471,46"}
        };

        for (String[] testCase : addCases) {
            FixedPointDec a = new FixedPointDec(testCase[0]);
            FixedPointDec b = new FixedPointDec(testCase[1]);
            a.multiply(b);
            assertEquals(testCase[2], a.toString());
        }

        for (String[] testCase : addCases) {
            FixedPointDec a = new FixedPointDec(testCase[0]);
            FixedPointDec b = new FixedPointDec(testCase[1]);
            String aStr = a.toString();
            String bStr = b.toString();
            FixedPointDec c = a.multiplied(b);
            
            assertEquals(testCase[2], c.toString());
            assertEquals(aStr, a.toString());
            assertEquals(bStr, b.toString());
        }
    }

    @Test
    public void divideTest() {
        String[][] addCases = {
            {"321,0", "123", "2,60"},
            {"234,56", "123,12", "1,90"},
            {"987654321123456789,14", "123456789123456789", "8,00"}
        };

        for (String[] testCase : addCases) {
            FixedPointDec a = new FixedPointDec(testCase[0]);
            FixedPointDec b = new FixedPointDec(testCase[1]);
            a.divide(b);
            assertEquals(testCase[2], a.toString());
        }

        for (String[] testCase : addCases) {
            FixedPointDec a = new FixedPointDec(testCase[0]);
            FixedPointDec b = new FixedPointDec(testCase[1]);
            String aStr = a.toString();
            String bStr = b.toString();
            FixedPointDec c = a.divided(b);
            
            assertEquals(testCase[2], c.toString());
            assertEquals(aStr, a.toString());
            assertEquals(bStr, b.toString());
        }

        assertThrows(ArithmeticException.class, new ThrowingRunnable(){
            public void run() throws ArithmeticException {
                FixedPointDec a = new FixedPointDec("123");
                FixedPointDec b = new FixedPointDec("0");
                FixedPointDec c = a.divided(b);
            }
        });
    }

    @Test
    public void negativesTest() {
        FixedPointDec a = new FixedPointDec("100");
        a.subtract(new FixedPointDec("200"));
        a.add(new FixedPointDec("150"));
        assertEquals("50,00", a.toString());
    }
}
