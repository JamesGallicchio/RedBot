package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandException;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.helpers4d4j.DiscordUtils;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.permission.PermissionContext;

import java.util.Arrays;

public class SystemCommands extends CommandGroup {

    public SystemCommands() {
        super("System Commands", "Commands for administrative purposes (for ThatRedhead)", "system", Arrays.asList(new InfoCommand()));
    }

    public static class InfoCommand extends Command {

        public InfoCommand() {
            super("info", "Gives information about the system", PermissionContext.BOT_OWNER);
        }

        public void invoke(MessageParser msgp) throws CommandException {
            StringBuilder info = new StringBuilder("**System Info**");
            info.append("\nGuild count: ").append(RedBot.getClient().getGuilds().size());
            info.append("\nUser count: ").append(RedBot.getClient().getUsers().size());
            info.append("\nCurrent uptime: ").append(RedBot.getUptime());
            info.append("\nThread count: ").append(Thread.activeCount());
            long total = Runtime.getRuntime().totalMemory();
            long free = Runtime.getRuntime().freeMemory();
            info.append("\nMemory usage (MB): ").append((total-free)/1024/1024).append("/").append(total/1024/1024);
            info.append("\nVersion: ").append(RedBot.getVersion());
            DiscordUtils.sendMessage(info.toString(), msgp.getChannel());
        }
    }
}
