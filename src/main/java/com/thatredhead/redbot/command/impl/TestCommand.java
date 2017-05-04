package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandException;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.permission.PermissionContext;
import sx.blah.discord.util.MessageHistoryBuilder;

import java.util.Arrays;
import java.util.stream.Collectors;

public class TestCommand extends Command {

    public TestCommand() {
        super("test", "TESTY COMMANDS", PermissionContext.BOT_OWNER);
    }

    @Override
    public void invoke(MessageParser msgp) throws CommandException {

        String[] history = new MessageHistoryBuilder(msgp.getChannel())
                .withMaxCount(5)
                .build()
                .stream()
                .flatMap(msg -> Arrays.stream(new String[]{msg.getStringID(), msg.getContent()}))
                .collect(Collectors.toList())
                .toArray(new String[10]);

        msgp.reply("Pong!", "Testy test test tester testing test, testing tested tester tests", false, history);
    }
}
