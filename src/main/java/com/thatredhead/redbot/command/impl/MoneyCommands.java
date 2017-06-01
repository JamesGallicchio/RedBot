package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.econ.Account;
import com.thatredhead.redbot.econ.Economy;
import com.thatredhead.redbot.permission.PermissionContext;
import sx.blah.discord.handle.obj.IUser;

import java.util.Arrays;
import java.util.List;

public class MoneyCommands extends CommandGroup {

    public MoneyCommands() {
        super("Money Commands", "The commands for users to access/manage their bank accounts", "money",
                Arrays.asList(
                        Command.of("money", "Shows current balance", "money", "money", true, false, PermissionContext.EVERYONE,
                                msgp -> msgp.reply("Your account balance is **" +
                                        Economy.format(RedBot.getEconomy().getAccountForUser(msgp.getAuthor()).getAmount()) +
                                        "**"
                                )),
                        Command.of("pay", "Pays money to another", "pay <user mention> <amount>", "pay", true, false, PermissionContext.EVERYONE, msgp -> {
                            IUser target = msgp.getUserMention(1);

                            if(target == null) msgp.reply("Invalid user mention! Check help for correct usage.");
                            else {
                                try {
                                    double amt = Double.parseDouble(msgp.getArg(2));
                                    Account from = RedBot.getEconomy().getAccountForUser(msgp.getAuthor());
                                    Account recip = RedBot.getEconomy().getAccountForUser(target);

                                    from.transferTo(recip, amt);

                                    msgp.reply("Paid :diamonds:" + amt + " to " + msgp.getAuthor().getName());

                                } catch (NumberFormatException e) {
                                    msgp.reply("Invalid money amount! Check help for correct usage.");
                                }
                            }
                        }),
                        Command.of("top", "Lists the users richest in RedBucks!", "top", "top", true, false, PermissionContext.EVERYONE, msgp -> {
                            StringBuilder response = new StringBuilder("Most Wealthiest\n");

                            List<Account> richest = RedBot.getEconomy().getRichest(10);

                            for(int i = 0; i < richest.size(); i++) {
                                Account next = richest.get(i);
                                response.append("#").append(i+1).append(": ").append(next.getOwner().getName()).append(" - ").append(Economy.format(next.getAmount())).append("\n");
                            }

                            msgp.reply(response.toString());
                        })
                ));
    }
}