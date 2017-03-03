package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.command.MessageParser;
import com.thatredhead.redbot.permission.PermissionContext;

import java.util.Arrays;

public class MathCommands extends CommandGroup {

    public MathCommands() {
        super("Math Commands", "Commands to help with math related stuff", "math", Arrays.asList(new EvalCommand()));
    }

    public static class EvalCommand extends Command {

        public EvalCommand() {
            super("", "");
        }

        @Override
        public PermissionContext getDefaultPermissions() {
            return null;
        }

        @Override
        public void invoke(MessageParser msgp) {
            msgp.reply("Not currently functional :(");
        }
    }
}