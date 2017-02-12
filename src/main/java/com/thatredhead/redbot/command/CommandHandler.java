package com.thatredhead.redbot.command;

import com.google.gson.reflect.TypeToken;
import com.thatredhead.redbot.DiscordUtils;
import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.impl.*;
import com.thatredhead.redbot.data.DataHandler;
import com.thatredhead.redbot.permission.PermissionHandler;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class CommandHandler {

    private PermissionHandler perms;
    private DataHandler datah;

    private List<Command> commands;

    private HashMap<IGuild, String> prefixes;

    public CommandHandler() {
        RedBot.getClient().getDispatcher().registerListener(this);
        this.datah = RedBot.getDataHandler();
        this.perms = datah.getPermHandler();

        prefixes = datah.get("guildprefixes", new TypeToken<HashMap<IGuild, String>>(){}.getType(), new HashMap<IGuild, String>());

        List<CommandGroup> commandGroups = Arrays.asList(
                new SystemCommands(),
                new DnDCommands(),
                new CuteCommands(datah)
        );

        List<Command> standaloneCommands = new ArrayList<>();
        standaloneCommands.addAll(Arrays.asList(
                new HelpCommand(commandGroups, standaloneCommands),
                new PermsCommand(perms)
        ));

        commands = commandGroups.stream().flatMap(group -> group.getCommands().stream()).collect(Collectors.toList());
        commands.addAll(standaloneCommands);
    }

    @EventSubscriber
    public void onMessageReceive(MessageReceivedEvent e) {
        IMessage msg = e.getMessage();

        String prefix = getPrefix(msg.getGuild());
        MessageParser msgp = new MessageParser(msg, prefix);

        if(msgp.construct()) {
            boolean success = false;

            for (Command c : commands) {
                if (c.usesPrefix() && c.getKeyword().equals(msgp.getArg(0))) {
                    if (perms.hasPermission(c.getPermission(), msg, c.getDefaultPermissions()))
                        invoke(c, msgp);
                    else
                        DiscordUtils.sendTemporaryMessage("You don't have permission to perform this command.", msg.getChannel());
                    success = true;
                }
            }
            if (!success)
                DiscordUtils.sendTemporaryMessage("Unknown command! Use help command for a list of commands.", msg.getChannel());
        } else
            commands.stream().filter(c -> !c.usesPrefix())
                    .filter(c -> perms.hasPermission(c.getPermission(), msg, c.getDefaultPermissions()))
                    .forEach(c -> c.invoke(msgp));
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
        if(!prefixes.containsKey(guild))
            prefixes.put(guild, "%");
        return prefixes.get(guild);
    }
}