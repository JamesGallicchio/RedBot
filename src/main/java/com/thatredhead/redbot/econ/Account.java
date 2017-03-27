package com.thatredhead.redbot.econ;

import sx.blah.discord.handle.obj.IUser;

public class Account {

    private IUser user;
    private double amount;

    public Account(IUser user) {
        this.user = user;
        amount = 0;
    }

    public Account(IUser user, double amount) {
        this.user = user;
        this.amount = amount;
    }

    public IUser getOwner() {
        return user;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double newAmount) {
        amount = newAmount;
    }

    public void add(double addend) {
        if(addend == Math.floor(addend))
            amount += addend;
        else
            throw new IllegalArgumentException();
    }

    public void subtract(double amount) {
        if(amount == Math.floor(amount))
            this.amount -= amount;
        else
            throw new IllegalArgumentException();
    }
}
