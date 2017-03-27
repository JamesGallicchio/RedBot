package com.thatredhead.redbot.econ;

import sx.blah.discord.handle.obj.IUser;

import java.util.List;

public class Economy {

    private List<Account> accounts;

    public Account getAccountForUser(IUser user) {
        return accounts.stream().filter(acc -> acc.getOwner().equals(user)).findFirst().get();
    }
}
