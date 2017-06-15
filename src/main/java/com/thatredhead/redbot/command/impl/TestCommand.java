package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandException;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.permission.PermissionContext;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TestCommand extends Command {

    public TestCommand() {
        super("test", "TESTY COMMANDS", PermissionContext.BOT_OWNER);
    }

    @Override
    public void invoke(MessageParser msgp) throws CommandException {
        if (msgp.getArgCount() < 2) {
            msgp.reply("Specify a filepath!");
        } else {
            Path p = Paths.get(msgp.getArg(1));
            msgp.reply("exists? " + p.toFile().exists());
            msgp.reply("path: " + p.toString());
        }
    }
}
