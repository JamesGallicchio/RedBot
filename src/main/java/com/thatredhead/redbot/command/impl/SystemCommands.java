package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.DiscordUtils;
import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.CommandException;
import com.thatredhead.redbot.command.ICommand;
import com.thatredhead.redbot.command.ICommandGroup;
import com.thatredhead.redbot.command.MessageParser;

import java.util.ArrayList;
import java.util.List;

public class SystemCommands implements ICommandGroup {

    private List<ICommand> commands;

    public SystemCommands() {
        commands = new ArrayList<>();
        commands.add(new UptimeCommand());
    }

    @Override
    public List<ICommand> getCommands() {
        return commands;
    }

    public class UptimeCommand implements ICommand {

        @Override
        public String getKeyword() {
            return "uptime";
        }

        @Override
        public String getPermission() {
            return "system.uptime";
        }

        @Override
        public void invoke(MessageParser msgp) throws CommandException {
            DiscordUtils.sendMessage("Current uptime: " + RedBot.getUptime(), msgp.getChannel());
        }
    }
}
