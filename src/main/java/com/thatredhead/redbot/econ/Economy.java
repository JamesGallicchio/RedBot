package com.thatredhead.redbot.econ;

import com.thatredhead.redbot.RedBot;
import sx.blah.discord.handle.obj.IUser;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Economy {

    public static final String MONEY_PREFIX = ":diamonds:";
    public static final double STARTER_AMOUNT = 1000.0;

    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat(MONEY_PREFIX + "###,##0.00");

    private List<Account> accounts;

    private double gamblingPayments;
    private double gamblingEarnings;

    public Economy() {
        accounts = new ArrayList<>();
    }

    public List<Account> getRichest(int count) {

        sort();

        return accounts.subList(0, count > accounts.size() ? accounts.size() : count);
    }

    public Account getAccountForUser(IUser user) {
        return accounts.stream().filter(acc -> acc.getUserID() == user.getLongID()).findFirst().orElseGet(() -> {
            Account act = new Account(user, STARTER_AMOUNT);
            accounts.add(act);
            save();
            return act;
        });
    }

    public void gamblingPayment(Account recipient, double amount) {
        gamblingPayments += amount;
        recipient.add(amount);
        save();
    }

    public void gamblingCharge(Account acct, double amount) {
        gamblingEarnings += amount;
        acct.subtract(amount);
    }

    public void save() {
        RedBot.getDataHandler().save(this, RedBot.ECON_FILE_NAME);
    }

    public static String format(double amount) {
        return MONEY_FORMAT.format(amount);
    }

    public void sort() {

        for (int i = 1; i < accounts.size(); i++) {
            int j = i - 1;
            double amount = accounts.get(i).getAmount();
            while (j >= 0 && accounts.get(j).getAmount() < amount)
                j--;
            accounts.set(i, accounts.set(j + 1, accounts.get(i)));
        }
    }
}