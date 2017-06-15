package com.thatredhead.redbot.command;

import com.google.gson.reflect.TypeToken;
import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.helpers4d4j.Utilities4D4J;
import com.thatredhead.redbot.permission.PermissionHandler;
import org.reflections.Reflections;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.Permissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CommandHandler {

    private PermissionHandler perms;

    private List<CommandGroup> commandGroups;

    private List<Command> standaloneCommands;

    private List<Command> commands;

    private HashMap<Long, String> prefixes;

    public CommandHandler() {
        this.perms = RedBot.getPermHandler();

        prefixes = RedBot.getDataHandler().get("guildprefixes", new TypeToken<HashMap<Long, String>>() {
        }.getType(), new HashMap<Long, String>());

        Reflections r = new Reflections("com.thatredhead.redbot.command.impl");

        commandGroups = r.getSubTypesOf(CommandGroup.class).stream().map(clazz -> {
            try {
                return clazz.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        standaloneCommands = r.getSubTypesOf(Command.class).stream().map(clazz -> {
            try {
                if (clazz.getName().contains("$"))
                    return null;
                else return clazz.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        commands = commandGroups.stream().flatMap(group -> group.getCommands().stream()).collect(Collectors.toList());
        commands.addAll(standaloneCommands);
    }

    public List<Command> getCommands() {
        return new ArrayList<>(commands);
    }

    public Command getCommand(String keyword) {
        return commands.stream().filter(it -> it.getKeyword().equalsIgnoreCase(keyword)).findFirst().orElse(null);
    }

    public List<CommandGroup> getCommandGroups() {
        return new ArrayList<>(commandGroups);
    }

    public CommandGroup getCommandGroup(String name) {
        return commandGroups.stream().filter(it -> it.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public List<Command> getStandaloneCommands() {
        return new ArrayList<>(standaloneCommands);
    }

    @EventSubscriber
    public void onMessageReceive(MessageReceivedEvent e) {
        MessageParser msgp = new MessageParser(e.getMessage());
        String prefix = getPrefix(msgp.getGuild());

        if (msgp.construct(prefix)) {
            if (msgp.getChannel().getModifiedPermissions(RedBot.getClient().getOurUser()).contains(Permissions.SEND_MESSAGES)) {
                boolean success = false;

                for (Command c : commands) {
                    if (c.usesPrefix() && c.getKeyword().equalsIgnoreCase(msgp.getArg(0))) {
                        if (perms.hasPermission(c, msgp.getMsg()))
                            invoke(c, msgp);
                        else
                            Utilities4D4J.sendTemporaryMessage("You don't have permission to perform this command.", msgp.getChannel());
                        success = true;
                    }
                }
                if (!success) {
                    Utilities4D4J.sendTemporaryMessage("Unknown command! Use help command for a list of commands.", msgp.getChannel());
                }
            } else {
                Utilities4D4J.sendEmbed(msgp.getAuthor().getOrCreatePMChannel(), "Missing Permissions", "I'm not able to talk in " + msgp.getChannel().mention() + "! If you think this is a mistake, tell the owner of the server to give RedBot the send message permission in that channel.", false);
            }
        } else {
            for (Command c : commands) {
                if (!c.usesPrefix() && c.getKeyword().equalsIgnoreCase(msgp.getArg(0))) {
                    if (msgp.getChannel().getModifiedPermissions(RedBot.getClient().getOurUser()).contains(Permissions.SEND_MESSAGES)) {
                        if (perms.hasPermission(c, msgp.getMsg())) {
                            invoke(c, msgp);
                        } else {
                            Utilities4D4J.sendTemporaryMessage("You don't have permission to perform this command.", msgp.getChannel());
                        }
                    } else {
                        Utilities4D4J.sendEmbed(msgp.getAuthor().getOrCreatePMChannel(), "Missing Permissions", "I'm not able to talk in " + msgp.getChannel().mention() + "! If you think this is a mistake, tell the owner of the server to give RedBot the send message permission in that channel.", false);
                    }
                }
            }
        }
    }

    private static void invoke(Command c, MessageParser msg) {
        try {
            c.invoke(msg);
        } catch (CommandArgumentException e) {
            Utilities4D4J.sendTemporaryMessage("Invalid argument #" + e.idx + " \"" + e.arg
                    + "\"! Proper format:\n`" + e.correctFormat + "`", msg.getChannel());
        } catch (CommandException e) {
            Utilities4D4J.sendTemporaryMessage(e.getMessage(), msg.getChannel());
        }
    }

    private String getPrefix(IGuild guild) {
        if (guild != null && prefixes.containsKey(guild.getLongID()))
            return prefixes.get(guild.getLongID());
        return RedBot.DEFAULT_PREFIX;
    }
}
