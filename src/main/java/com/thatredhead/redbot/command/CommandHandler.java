package com.thatredhead.redbot.command;

import com.google.gson.reflect.TypeToken;
import com.thatredhead.redbot.DiscordUtils;
import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.impl.CuteCommands;
import com.thatredhead.redbot.command.impl.DnDCommands;
import com.thatredhead.redbot.command.impl.PermsCommand;
import com.thatredhead.redbot.command.impl.SystemCommands;
import com.thatredhead.redbot.data.DataHandler;
import com.thatredhead.redbot.permission.PermissionHandler;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
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

    private List<ICommand> commands;
    private List<ICommand> noPrefixCommands;

    private HashMap<IGuild, String> prefixes;

    public CommandHandler() {
        RedBot.getClient().getDispatcher().registerListener(this);
        this.datah = RedBot.getDataHandler();
        this.perms = datah.getPermHandler();

        prefixes = datah.get("guildprefixes", new TypeToken<HashMap<IGuild, String>>(){}.getType(), new HashMap<IGuild, String>());

        commands = Arrays.stream(new ICommandGroup[] {
                new SystemCommands(),
                new DnDCommands(),
                new CuteCommands(datah)
        }).flatMap(group -> group.getCommands().stream()).collect(Collectors.toList());

        commands.add(new PermsCommand(perms));

        noPrefixCommands = new ArrayList<>();
    }

    @EventSubscriber
    public void onMessageReceive(MessageReceivedEvent e) {
        IMessage msg = e.getMessage();

        String prefix = getPrefix(msg.getGuild());
        MessageParser msgp = new MessageParser(msg, prefix);

        if(msgp.construct()) {
            boolean success = false;
            for (ICommand c : commands) {
                if(c.getKeyword().equals(msgp.getArg(0))) {
                    if(perms.hasPermission(c.getPermission(), msg.getAuthor(), msg.getChannel(), c.getDefaultPermissions()))
                        invoke(c, msgp);
                    else
                        DiscordUtils.sendTemporaryMessage("You don't have permission to perform this command.", msg.getChannel());
                    success = true;
                }
            }
            if(!success)
                DiscordUtils.sendTemporaryMessage("Unknown command! Use help command for a list of commands.", msg.getChannel());
        }

        noPrefixCommands.stream()
                .filter(it -> perms.hasPermission(it.getPermission(), msg, it.getDefaultPermissions()))
                .forEach(it -> invoke(it, msgp));
    }

    private static void invoke(ICommand c, MessageParser msg) {
        try {
            c.invoke(msg);
        } catch (CommandArgumentException e) {
            DiscordUtils.sendTemporaryMessage("Invalid argument #" + e.idx + " \"" + e.arg
                                              + "\"! Proper format:\n`" + e.correctFormat + "`", msg.getChannel());
        } catch (CommandException e) {
            DiscordUtils.sendTemporaryMessage(e.reason, msg.getChannel());
        }
    }

    private String getPrefix(IGuild guild) {
        if(!prefixes.containsKey(guild))
            prefixes.put(guild, "%");
        return prefixes.get(guild);
    }
}