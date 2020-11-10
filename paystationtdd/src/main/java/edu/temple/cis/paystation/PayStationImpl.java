/**
 * Implementation of the pay station.
 *
 * Responsibilities:
 *
 * 1) Accept payment; 
 * 2) Calculate parking time based on payment; 
 * 3) Know earning, parking time bought; 
 * 4) Issue receipts; 
 * 5) Handle buy and cancel events.
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

import java.util.HashMap;
import java.util.Map;

public class PayStationImpl implements PayStation {

    /**
     * I refactored the PayStation class so that it relies solely the coin Map for managing state during the transactions.
     * Instead of static variables, timeBought and insertedSoFar have been rewritten as functions which
     * dynamically calculate the values for both variables in real time. This is both to avoid the possibility
     * of inconsistent state between the Map and the variables, as well as allowing for unit testing the functionality
     * of cancel() and buy() (such as clearing the Map) without having to create new getters, modifying the scope of
     * variables, or using Reflection.
     */

    private int totalMoney;
    private Map<Integer, Integer> coins;

    public PayStationImpl(){
        coins = new HashMap<>();
        totalMoney = 0;
    }

    private int insertedSoFar(){
        // Convert the values of the Map to a stream and use Integer.sum() in a reduce function to get the total
        return coins.values().stream().reduce(0, Integer::sum);
    }

    private int timeBought(){
        return insertedSoFar() / 5 * 2;
    }

    @Override
    public void addPayment(int coinValue)
            throws IllegalCoinException {
        switch (coinValue) {
            case 5: break;
            case 10: break;
            case 25: break;
            default:
                throw new IllegalCoinException("Invalid coin: " + coinValue);
        }

        coins.put(coinValue, coins.getOrDefault(coinValue, 0) + 1);
        /**
         * While I can't take credit for this alternative solution below, if I had to do this lab over
         * again I would probably pick this mostly cause I am fond of functional programming solutions.
         *
         * coins.merge(coinValue, 1, Integer::sum)
         */

    }

    @Override
    public int readDisplay() {
        return timeBought();
    }

    @Override
    public Receipt buy() {
        Receipt r = new ReceiptImpl(timeBought());
        totalMoney += insertedSoFar();
        reset();
        return r;
    }

    @Override
    public Map<Integer, Integer> cancel() {
        Map<Integer, Integer> coinReturn = coins;
        reset();
        return coinReturn;
    }

    /**
     * Empties the money collected in the PayStation since the previous call to empty()
     *
     * @return The amount of money collected in the PayStation
     */
    @Override
    public int empty() {
        int toReturn = totalMoney;
        totalMoney = 0;
        return toReturn;
    }

    /**
     * Resets the PayStation for a new transaction
     */
    private void reset() {
        coins = new HashMap<>();
    }
}
