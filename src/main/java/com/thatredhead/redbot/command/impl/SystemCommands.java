package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.DiscordUtils;
import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.CommandException;
import com.thatredhead.redbot.command.ICommand;
import com.thatredhead.redbot.command.ICommandGroup;
import com.thatredhead.redbot.command.MessageParser;
import com.thatredhead.redbot.permission.PermissionContext;

import java.util.ArrayList;
import java.util.List;

public class SystemCommands implements ICommandGroup {

    private List<ICommand> commands;

    public SystemCommands() {
        commands = new ArrayList<>();
        commands.add(new UptimeCommand());
        commands.add(new InviteCommand());
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
        public String getDescription() {
            return "Gives the current length of time the bot has been running";
        }

        @Override
        public String getUsage() {
            return "uptime";
        }

        @Override
        public String getPermission() {
            return "system.uptime";
        }

        @Override
        public PermissionContext getDefaultPermissions() {
            return new PermissionContext();
        }

        @Override
        public void invoke(MessageParser msgp) throws CommandException {
            DiscordUtils.sendMessage("Current uptime: " + RedBot.getUptime(), msgp.getChannel());
        }
    }

    public class InviteCommand implements ICommand {

        @Override
        public String getKeyword() {
            return "invite";
        }

        @Override
        public String getDescription() {
            return "Gives a link to invite the bot to other servers";
        }

        @Override
        public String getUsage() {
            return "invite";
        }

        @Override
        public String getPermission() {
            return "system.invite";
        }

        @Override
        public PermissionContext getDefaultPermissions() {
            return new PermissionContext();
        }


        @Override
        public void invoke(MessageParser msgp) throws CommandException {
            DiscordUtils.sendMessage("Invite me to another guild: " + RedBot.INVITE, msgp.getChannel());
        }
    }
}
