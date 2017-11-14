package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandException;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.helpers4d4j.Utilities4D4J;
import com.thatredhead.redbot.permission.PermissionContext;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.*;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.Arrays;

public class SystemCommands extends CommandGroup {

    public SystemCommands() {
        super("System Commands", "Commands for administrative purposes (for ThatRedhead)", "system",
                Arrays.asList(new RebuildCommand(), new RestartCommand(), new ShutdownCommand(), new SysInfoCommand(),
                        new PingCommand(), new GetByIDCommand(), new AnnounceCommand(), new ExecCommand()));
    }

    public static class SysInfoCommand extends Command {

        public SysInfoCommand() {
            super("sysinfo", "Displays information about the system", PermissionContext.BOT_OWNER);
        }

        public void invoke(MessageParser msgp) throws CommandException {

            long total = Runtime.getRuntime().totalMemory();
            long free = Runtime.getRuntime().freeMemory();

            msgp.reply(Utilities4D4J.makeEmbed("System Info", "", false,
                    "Guild count", "" + RedBot.getClient().getGuilds().size(),
                    "User count", "" + RedBot.getClient().getUsers().size(),
                    "Current uptime", RedBot.getUptime(),
                    "Thread count", "" + Thread.activeCount(),
                    "Memory usage", "Usage: " + (total - free) / 1024 / 1024 + " MB\nTotal: " + total / 1024 / 1024 + " MB",
                    "Version", RedBot.getVersion()));
        }
    }

    public static class PingCommand extends Command {
        public PingCommand() {
            super("ping", "Shows response time of the bot", PermissionContext.EVERYONE);
        }

        public void invoke(MessageParser msgp) throws CommandException {
            IMessage msg = msgp.reply("Please wait...").get();

            long diff = msg.getTimestamp().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    - msgp.getMsg().getTimestamp().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long responseTime = msgp.getMsg().getShard().getResponseTime();

            Utilities4D4J.edit(msg, "Request ping: " + diff / 1000.0 + "s\nHeartbeat ping: " + responseTime / 1000.0 + "s");
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
            if (RedBot.OWNER_ID == msgp.getAuthor().getLongID()) {
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

            long id = Long.parseUnsignedLong(msgp.getArg(1));

            IUser user = client.getUserByID(id);
            if (user != null) {
                msgp.reply("User: `" + user.getName() + "`");
                return;
            }

            IGuild guild = client.getGuildByID(id);
            if (guild != null) {
                msgp.reply("Guild: `" + guild.getName() + "` (Owned by `" + guild.getOwner().getStringID() + "`)");
                return;
            }

            IChannel channel = client.getChannelByID(id);
            if (channel != null) {
                if (channel.isPrivate()) {
                    IPrivateChannel priv = (IPrivateChannel) channel;
                    msgp.reply("Private channel: `" + priv.getRecipient().getName() + "`");
                } else
                    msgp.reply("Channel: " + channel.mention() + " (Guild `" + channel.getGuild().getStringID() + "`)");
                return;
            }

            IRole role = client.getRoleByID(id);
            if (role != null) {
                msgp.reply("Role: `" + role.getName() + "` (Guild `" + role.getGuild().getStringID() + "`)");
                return;
            }

            IMessage message = Utilities4D4J.getMessageByID(id);
            if (message != null) {
                msgp.reply("Message: `" + message.getContent() + "` (Channel `" + message.getChannel().getStringID() + "`)");
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

            for (IGuild g : RedBot.getClient().getGuilds())
                Utilities4D4J.sendMessageToGuild(announcement, g);

            msgp.reply("Announcement sent!");
        }
    }

    public static class ExecCommand extends Command {
        public static String IMPORTS = "";

        static {
            try {
                Files.readAllLines(Paths.get("script-imports.txt"))
                        .stream().filter(s -> !s.trim().isEmpty())
                        .forEach(s -> IMPORTS += "import " + s + "\n");
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }

            System.setProperty("kotlin.compiler.jar", "kotlinc/lib/kotlin-compiler.jar");
        }

        private KotlinJsr223JvmLocalScriptEngineFactory factory = new KotlinJsr223JvmLocalScriptEngineFactory();

        public ExecCommand() {
            super("exec", "evaluates a kotlin script", PermissionContext.BOT_OWNER);
        }

        @Override
        public void invoke(MessageParser msgp) {
            String content = IMPORTS + "val msgp: MessageParser = bindings.get(\"MSGP\") as MessageParser\n" + msgp.getContentAfter(1).replace("```kotlin", "").replace("```kt", "").replace("`", "");

            Object o;
            try {
                ScriptEngine e = factory.getScriptEngine();
                e.put("MSGP", msgp);
                o = e.eval(content);
            } catch (ScriptException e) {
                Utilities4D4J.sendEmbed(msgp.getChannel(), "RedBot Script Executor", "", false,
                        "Failure!", "```\n" + StringUtils.abbreviate(e.getMessage(), 1000) + "```");
                return;
            }
            Utilities4D4J.sendEmbed(msgp.getChannel(), "RedBot Script Executor", "", false,
                    "Success!", o == null? "" : "```\n" + o.toString() + "```");
        }
    }
}
