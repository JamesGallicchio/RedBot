package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandException;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.permission.PermissionContext;

public class TestCommand extends Command {

    public TestCommand() {
        super("test", "TESTY COMMANDS", PermissionContext.BOT_OWNER);
    }

    @Override
    public void invoke(MessageParser msgp) throws CommandException {
        msgp.reply("Pong!", "Testy test test tester testing test, testing tested tester tests", false);
    }
}
