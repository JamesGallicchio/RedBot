package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandException;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.helpers4d4j.Utilities4D4J;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.permission.PermissionContext;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.*;

import java.util.Arrays;

public class SystemCommands extends CommandGroup {

    public SystemCommands() {
        super("System Commands", "Commands for administrative purposes (for ThatRedhead)", "system", Arrays.asList(new RebuildCommand(), new RestartCommand(), new ShutdownCommand(), new InfoCommand(), new GetByIDCommand(), new AnnounceCommand()));
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
            Utilities4D4J.sendMessage(info.toString(), msgp.getChannel());
        }
    }

    public static class RebuildCommand extends Command {

        public RebuildCommand() {
            super("rebuild", "Rebuilds RedBot", PermissionContext.BOT_OWNER);
        }

        public void invoke(MessageParser msgp) {
            msgp.reply("Rebuilding RedBot!");
            RedBot.rebuild();
            msgp.reply("Done rebuilding!");
        }
    }

    public static class RestartCommand extends Command {

        public RestartCommand() {
            super("restart", "Restarts RedBot", PermissionContext.BOT_OWNER);
        }

        public void invoke(MessageParser msgp) {
            if(RedBot.OWNER_ID.equals(msgp.getAuthor().getID())) {
                msgp.reply("Restarting RedBot!");

                RedBot.restart();
            } else
                msgp.reply("Only Red can do that to me!");
        }
    }

    public static class ShutdownCommand extends Command {

        public ShutdownCommand() {
            super("shutdown", "Shuts down RedBot", PermissionContext.BOT_OWNER);
        }

        public void invoke(MessageParser msgp) {
            msgp.reply("Shutting down RedBot!");

            RedBot.shutdown();
        }
    }

    public static class GetByIDCommand extends Command {

        public GetByIDCommand() {
            super("getbyid", "Gets a Discord object by its ID", "getbyid <id>", PermissionContext.BOT_OWNER);
        }

        public void invoke(MessageParser msgp) {
            IDiscordClient client = RedBot.getClient();

            IUser user = client.getUserByID(msgp.getArg(1));
            if(user != null) {
                msgp.reply("User: `" + user.getName() + "`");
                return;
            }

            IGuild guild = client.getGuildByID(msgp.getArg(1));
            if(guild != null) {
                msgp.reply("Guild: `" + guild.getName() + "` (Owned by `" + guild.getOwner().getID() + "`)");
                return;
            }

            IChannel channel = client.getChannelByID(msgp.getArg(1));
            if(channel != null) {
                if(channel.isPrivate()) {
                    IPrivateChannel priv = (IPrivateChannel) channel;
                    msgp.reply("Private channel: `" + priv.getRecipient().getName() + "`");
                }
                else
                    msgp.reply("Channel: " + channel.mention() + " (Guild `" + channel.getGuild().getID() + "`)");
                return;
            }

            IRole role = client.getRoleByID(msgp.getArg(1));
            if(role != null) {
                msgp.reply("Role: `" + role.getName() + "` (Guild `" + role.getGuild().getID() + "`)");
                return;
            }

            IMessage message = client.getMessageByID(msgp.getArg(1));
            if(message != null) {
                msgp.reply("Message: `" + message.getContent() + "` (Channel `" + message.getChannel().getID() + "`)");
                return;
            }

            msgp.reply("No object found matching that ID :(");
        }
    }

    public static class AnnounceCommand extends Command {

        public AnnounceCommand() {
            super("announce", "Announces a message to the general channel in all guilds RedBot is in", PermissionContext.BOT_OWNER);
        }

        @Override
        public void invoke(MessageParser msgp) {

            String announcement = msgp.getContentAfter(1);

            for(IGuild g: RedBot.getClient().getGuilds())
                Utilities4D4J.sendMessage(announcement, g.getGeneralChannel());

            msgp.reply("Announcement sent!");
        }
    }
}
