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
    private List<ICommand> sysCommands;

    public SystemCommands() {
        commands = new ArrayList<>();
        commands.add(new InfoCommand());
        commands.add(new InviteCommand());
    }

    @Override
    public List<ICommand> getCommands() {
        return commands;
    }

    public class InfoCommand implements ICommand {

        @Override
        public String getKeyword() {
            return "info";
        }

        @Override
        public String getDescription() {
            return "Gives information about the system";
        }

        @Override
        public String getUsage() {
            return "info";
        }

        @Override
        public String getPermission() {
            return "system.info";
        }

        @Override
        public PermissionContext getDefaultPermissions() {
            return PermissionContext.getEveryoneContext();
        }

        @Override
        public void invoke(MessageParser msgp) throws CommandException {
            StringBuilder info = new StringBuilder("**System Info**");
            info.append("\nGuild count: ").append(RedBot.getClient().getGuilds().size());
            info.append("\nCurrent uptime: ").append(RedBot.getUptime());
            info.append("\nThread count: ").append(Thread.activeCount());
            long total = Runtime.getRuntime().totalMemory();
            long free = Runtime.getRuntime().freeMemory();
            info.append("\nMemory usage (MB): ").append((total-free)/1024/1024).append("/").append(total/1024/1024);
            info.append("\nVersion: ").append(RedBot.getVersion());
            DiscordUtils.sendMessage(info.toString(), msgp.getChannel());
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
            return PermissionContext.getEveryoneContext();
        }


        @Override
        public void invoke(MessageParser msgp) throws CommandException {
            DiscordUtils.sendMessage("Invite me to another guild: " + RedBot.INVITE, msgp.getChannel());
        }
    }
}
