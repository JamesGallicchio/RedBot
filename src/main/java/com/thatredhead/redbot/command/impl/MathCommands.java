package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.command.MessageParser;
import com.thatredhead.redbot.permission.PermissionContext;

import java.util.Arrays;

public class MathCommands extends CommandGroup {

    public MathCommands() {
        commands = Arrays.asList(new EvalCommand());
    }

    public class EvalCommand extends Command {
        @Override
        public PermissionContext getDefaultPermissions() {
            return null;
        }

        @Override
        public void invoke(MessageParser msgp) {

        }
    }
}