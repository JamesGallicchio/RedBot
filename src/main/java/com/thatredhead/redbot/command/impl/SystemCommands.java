package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.DiscordUtils;
import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandException;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.command.MessageParser;
import com.thatredhead.redbot.permission.PermissionContext;

import java.util.ArrayList;
import java.util.List;

public class SystemCommands extends CommandGroup {

    private List<Command> commands;

    public SystemCommands() {
        name = "System Commands";
        description = "Commands pertinent to RedBot's system";
        permission = "system";

        commands = new ArrayList<>();
        commands.add(new InfoCommand());
        commands.add(new InviteCommand());
    }

    @Override
    public List<Command> getCommands() {
        return commands;
    }

    public class InfoCommand extends Command {

        public InfoCommand() {
            keyword = usage = permission = "info";
            description = "Gives information about the system";
        }

        public PermissionContext getDefaultPermissions() {
            return PermissionContext.getEveryoneContext();
        }

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

    public class InviteCommand extends Command {

        public InviteCommand() {
            keyword = usage = permission = "invite";
            description = "Gives a link to invite the bot to other servers";
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
