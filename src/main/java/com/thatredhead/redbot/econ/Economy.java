package com.thatredhead.redbot.econ;

import com.thatredhead.redbot.RedBot;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;
import java.util.List;

public class Economy {

    private List<Account> accounts;

    public Economy() {
        accounts = new ArrayList<>();
    }

    public Account getAccountForUser(IUser user) {
        return accounts.stream().filter(acc -> acc.getOwner().equals(user)).findFirst().orElseGet(() -> {
            Account act = new Account(user);
            accounts.add(act);
            save();
            return act;
        });
    }

    public void save() {
        RedBot.getDataHandler().save(this, RedBot.ECON_FILE_NAME);
    }
}
