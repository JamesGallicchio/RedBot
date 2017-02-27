package com.thatredhead.redbot.command;

import com.google.gson.reflect.TypeToken;
import com.thatredhead.redbot.DiscordUtils;
import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.impl.HelpCommand;
import com.thatredhead.redbot.permission.PermissionHandler;
import org.reflections.Reflections;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

import java.util.*;
import java.util.stream.Collectors;

public class CommandHandler {

    private PermissionHandler perms;

    private List<Command> commands;

    private HashMap<String, String> prefixes;

    public CommandHandler() {
        RedBot.getClient().getDispatcher().registerListener(this);
        this.perms = RedBot.getPermHandler();

        prefixes = RedBot.getDataHandler().get("guildprefixes", new TypeToken<HashMap<String, String>>(){}.getType(), new HashMap<String, String>());

        Reflections r = new Reflections("com.thatredhead.redbot.command.impl");

        List<CommandGroup> commandGroups = r.getSubTypesOf(CommandGroup.class).stream().map(clazz -> {
            try {
                return clazz.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        List<Command> standaloneCommands = r.getSubTypesOf(Command.class).stream().map(clazz -> {
            try {
                if(clazz.getName().contains("$") || clazz.equals(HelpCommand.class))
                    return null;
                else return clazz.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        commands = commandGroups.stream().flatMap(group -> group.getCommands().stream()).collect(Collectors.toList());
        commands.addAll(standaloneCommands);
        commands.add(new HelpCommand(commandGroups, standaloneCommands));
    }

    @EventSubscriber
    public void onMessageReceive(MessageReceivedEvent e) {
        IMessage msg = e.getMessage();

        String prefix = getPrefix(msg.getGuild());
        MessageParser msgp = new MessageParser(msg, prefix);

        if(msgp.construct()) {
            boolean success = false;

            for (Command c : commands) {
                if (c.usesPrefix() && c.getKeyword().equalsIgnoreCase(msgp.getArg(0))) {
                    invoke(c, msgp);
                    /*if (perms.hasPermission(c.getPermission(), msg, c.getDefaultPermissions()))
                        invoke(c, msgp);
                    else
                        DiscordUtils.sendTemporaryMessage("You don't have permission to perform this command.", msg.getChannel());*/
                    success = true;
                }
            }
            if (!success)
                DiscordUtils.sendTemporaryMessage("Unknown command! Use help command for a list of commands.", msg.getChannel());
        } else
            for (Command c : commands) {
                if (!c.usesPrefix() && c.getKeyword().equalsIgnoreCase(msgp.getArg(0))) {
                    //if (perms.hasPermission(c.getPermission(), msg, c.getDefaultPermissions()))
                        invoke(c, msgp);
                    //else
                    //    DiscordUtils.sendTemporaryMessage("You don't have permission to perform this command.", msg.getChannel());
                }
            }
    }

    private static void invoke(Command c, MessageParser msg) {
        try {
            c.invoke(msg);
        } catch (CommandArgumentException e) {
            DiscordUtils.sendTemporaryMessage("Invalid argument #" + e.idx + " \"" + e.arg
                                              + "\"! Proper format:\n`" + e.correctFormat + "`", msg.getChannel());
        } catch (CommandException e) {
            DiscordUtils.sendTemporaryMessage(e.getMessage(), msg.getChannel());
        }
    }

    private String getPrefix(IGuild guild) {
        if(!prefixes.containsKey(guild.getID())) {
            prefixes.put(guild.getID(), "%");
            RedBot.getDataHandler().save(prefixes, "guildprefixes");
        }
        return prefixes.get(guild.getID());
    }
}