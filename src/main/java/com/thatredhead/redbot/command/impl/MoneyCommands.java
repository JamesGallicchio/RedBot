package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.permission.PermissionContext;

import java.text.DecimalFormat;
import java.util.Arrays;

public class MoneyCommands extends CommandGroup {

    private static final DecimalFormat df = new DecimalFormat(
            "###,##0.00");

    public MoneyCommands() {
        super("Money Commands", "The commands for users to access/manage their bank accounts", "money",
                Arrays.asList(
                        Command.of("money", "Shows current balance", "money", "money", true, false, PermissionContext.EVERYONE,
                                msgp -> msgp.reply("Your account balance is :red_circle:**" +
                                        df.format(RedBot.getEconomy().getAccountForUser(msgp.getAuthor()).getAmount()) +
                                        "**"
                                ))
                ));
    }
}