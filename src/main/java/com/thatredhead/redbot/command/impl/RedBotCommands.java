package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandException;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.helpers4d4j.Utilities4D4J;
import com.thatredhead.redbot.permission.PermissionContext;

import java.util.Arrays;

public class RedBotCommands extends CommandGroup {

    public RedBotCommands() {
        super("RedBot Commands", "General commands for users and administrators", "redbot", Arrays.asList(new InviteCommand()));
    }

    public static class InviteCommand extends Command {

        public InviteCommand() {
            super("invite", "Gives a link to invite the bot to other servers", PermissionContext.EVERYONE);
        }

        @Override
        public void invoke(MessageParser msgp) throws CommandException {
            Utilities4D4J.sendEmbed(msgp.getChannel(), "RedBot Invite", "[Invite me to another guild!](" + RedBot.INVITE + ")", false);
        }
    }
}
