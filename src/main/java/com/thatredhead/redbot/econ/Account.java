package com.thatredhead.redbot.econ;

import com.thatredhead.redbot.RedBot;
import sx.blah.discord.handle.obj.IUser;

public class Account {

    private long userID;
    private double amount;

    private transient IUser user;

    public Account(IUser user) {
        this.user = user;
        this.userID = user.getLongID();
        this.amount = 0;
    }

    public Account(IUser user, double amount) {
        this.user = user;
        this.userID = user.getLongID();
        this.amount = amount;
    }

    public long getUserID() {
        return userID;
    }

    public IUser getOwner() {
        return user == null ?
                user = RedBot.getClient().getUserByID(userID) :
                user;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double newAmount) {
        amount = newAmount;
    }

    public void add(double addend) {
        amount += addend;
    }

    public void subtract(double amount) {
        this.amount -= amount;
    }

    public void transferTo(Account recip, double amount) {
        if(this.amount > amount) {
            this.amount -= amount;
            recip.amount += amount;
        } else
            throw new IllegalArgumentException();
    }

    @Override
    public String toString() {
        return "User: " + (getOwner() == null ? "N/A" : user.getName()) + ". Balance: " + amount;
    }
}
