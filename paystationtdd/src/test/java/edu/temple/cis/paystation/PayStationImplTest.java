/**
 * Testcases for the Pay Station system.
 *
 * This source code is from the book "Flexible, Reliable Software: Using
 * Patterns and Agile Development" published 2010 by CRC Press. Author: Henrik B
 * Christensen Computer Science Department Aarhus University
 *
 * This source code is provided WITHOUT ANY WARRANTY either expressed or
 * implied. You may study, use, modify, and distribute it for non-commercial
 * purposes. For any commercial use, see http://www.baerbak.com/
 */
package edu.temple.cis.paystation;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PayStationImplTest {

    PayStation ps;

    @Before
    public void setup() {
        ps = new PayStationImpl();
    }

    /**
     * Entering 5 cents should make the display report 2 minutes parking time.
     */
    @Test
    public void shouldDisplay2MinFor5Cents()
            throws IllegalCoinException {
        ps.addPayment(5);
        assertEquals("Should display 2 min for 5 cents",
                2, ps.readDisplay());
    }

    /**
     * Entering 25 cents should make the display report 10 minutes parking time.
     */
    @Test
    public void shouldDisplay10MinFor25Cents() throws IllegalCoinException {
        ps.addPayment(25);
        assertEquals("Should display 10 min for 25 cents",
                10, ps.readDisplay());
    }

    /**
     * Verify that illegal coin values are rejected.
     */
    @Test(expected = IllegalCoinException.class)
    public void shouldRejectIllegalCoin() throws IllegalCoinException {
        ps.addPayment(17);
    }

    /**
     * Entering 10 and 25 cents should be valid and return 14 minutes parking
     */
    @Test
    public void shouldDisplay14MinFor10And25Cents()
            throws IllegalCoinException {
        ps.addPayment(10);
        ps.addPayment(25);
        assertEquals("Should display 14 min for 10+25 cents",
                14, ps.readDisplay());
    }

    /**
     * Buy should return a valid receipt of the proper amount of parking time
     */
    @Test
    public void shouldReturnCorrectReceiptWhenBuy()
            throws IllegalCoinException {
        ps.addPayment(5);
        ps.addPayment(10);
        ps.addPayment(25);
        Receipt receipt;
        receipt = ps.buy();
        assertNotNull("Receipt reference cannot be null",
                receipt);
        assertEquals("Receipt value must be 16 min.",
                16, receipt.value());
    }

    /**
     * Buy for 100 cents and verify the receipt
     */
    @Test
    public void shouldReturnReceiptWhenBuy100c()
            throws IllegalCoinException {
        ps.addPayment(10);
        ps.addPayment(10);
        ps.addPayment(10);
        ps.addPayment(10);
        ps.addPayment(10);
        ps.addPayment(25);
        ps.addPayment(25);

        Receipt receipt;
        receipt = ps.buy();
        assertEquals(40, receipt.value());
    }

    /**
     * Verify that the pay station is cleared after a buy scenario
     */
    @Test
    public void shouldClearAfterBuy()
            throws IllegalCoinException {
        ps.addPayment(25);
        ps.buy(); // I do not care about the result
        // verify that the display reads 0
        assertEquals("Display should have been cleared",
                0, ps.readDisplay());
        // verify that a following buy scenario behaves properly
        ps.addPayment(10);
        ps.addPayment(25);
        assertEquals("Next add payment should display correct time",
                14, ps.readDisplay());
        Receipt r = ps.buy();
        assertEquals("Next buy should return valid receipt",
                14, r.value());
        assertEquals("Again, display should be cleared",
                0, ps.readDisplay());
    }

    /**
     * Verify that cancel clears the pay station
     */
    @Test
    public void shouldClearAfterCancel()
            throws IllegalCoinException {
        ps.addPayment(10);
        ps.cancel();
        assertEquals("Cancel should clear display",
                0, ps.readDisplay());
        ps.addPayment(25);
        assertEquals("Insert after cancel should work",
                10, ps.readDisplay());
    }

    /**
     * Call to empty should return the total amount collected over several transactions.
     * This test performs 2 transactions, then calls empty() and verifies that the amounts match.
     */
    @Test
    public void shouldReturnTotalWhenEmptied() throws IllegalCoinException {

        ps.addPayment(5);
        ps.addPayment(25);
        ps.buy();

        ps.addPayment(10);
        ps.addPayment(25);
        ps.buy();

        int totalInserted = 65;

        assertEquals("The money returned by empty() should equal the money inserted",
                totalInserted,
                ps.empty());
    }

    /**
     * Calling empty() should reset the total amount of money to 0.
     */
    @Test
    public void emptyShouldResetToZero() throws IllegalCoinException {
        ps.addPayment(10);
        ps.addPayment(25);
        ps.buy();

        ps.empty();
        // Since the first call to empty() should reset to 0, calling empty() again without any new transactions should return 0
        assertEquals("Total amount of money should be 0", 0, ps.empty());
    }

    /**
     * Canceled transaction should not add to the total amount of money
     */
    @Test
    public void canceledEntryShouldNotAddTotal() throws IllegalCoinException {
        ps.addPayment(5);
        ps.addPayment(25);
        ps.buy();

        ps.addPayment(10);
        ps.addPayment(25);
        ps.cancel();

        int totalInserted = 30;

        assertEquals("Total amount of money should not contain canceled transactions", totalInserted, ps.empty());
    }

    /**
     * Call to cancel returns a map containing 1 coin entered
     */
    @Test
    public void cancelShouldReturnMap() throws IllegalCoinException {
        Map<Integer, Integer> expected = new HashMap<>(){{
            put(5, 1);
        }};

        ps.addPayment(5);
        assertEquals("Cancel should return a map", expected, ps.cancel());
    }

    /**
     * Call to cancel returns a map containing the mixture of coins entered
     */
    @Test
    public void cancelShouldReturnCoinMixture() throws IllegalCoinException {
        Map<Integer, Integer> expected = new HashMap<>(){{
            put(5, 1);
            put(10, 2);
            put(25, 1);
        }};

        ps.addPayment(5);
        ps.addPayment(10);
        ps.addPayment(25);
        ps.addPayment(10);
        assertEquals("Cancel should return a map with a mixture of coins", expected, ps.cancel());
    }

    /**
     * Map returned by cancel() does not contain any superfluous keys
     */
    @Test
    public void cancelMapShouldNotContainEmptyKeys() throws IllegalCoinException {
        Map<Integer, Integer> expected = new HashMap<>(){{
            put(25, 1);
            put(5, 1);
        }};

        ps.addPayment(5);
        ps.addPayment(25);
        assertFalse("Cancel should return a map without any unused keys", ps.cancel().containsKey(10));
    }

    /**
     * Cancel() clears the map inside the PayStation
     */
    @Test
    public void cancelShouldClearMap() throws IllegalCoinException {
        ps.addPayment(10);
        ps.addPayment(25);
        ps.cancel();

        // If cancel resets the map, then the value stored in the PayStation should be 0, therefore reading the display should return 0 minutes
        assertEquals("Cancel should clear the map, resulting in 0 minutes on the display", 0, ps.readDisplay());
    }

    /**
     * Buy should clear the map inside the PayStation
     */
    @Test
    public void buyShouldClearMap() throws IllegalCoinException {
        ps.addPayment(10);
        ps.addPayment(25);
        ps.buy();

        // If buy resets the map, then the value stored in the PayStation should be 0, therefore reading the display should return 0 minutes
        assertEquals("Buy should clear the map, resulting in 0 minutes on the display", 0, ps.readDisplay());
    }
}
