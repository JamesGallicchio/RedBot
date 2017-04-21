package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandException;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.helpers4d4j.MessageMatcher;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.helpers4d4j.Utilities4D4J;
import com.thatredhead.redbot.permission.PermissionContext;
import com.thatredhead.redbot.permission.PermissionHandler;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.EmbedBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PermsCommands extends CommandGroup {

    private PermissionHandler perms;

    public PermsCommands() {
        super("Perms Commands", "Commands to edit command permissions in this guild", "perms", null);
        commands = Arrays.asList(
                new PermsCommand(),
                new EnableCommand(),
                new DisableCommand(),
                new CommandsCommand()
        );
        this.perms = RedBot.getPermHandler();
    }

    public class PermsCommand extends Command {

        public PermsCommand() {
            super("perms", "Lists permissions for this guild the specified channel",
                    "perms [channel mention]", true, PermissionContext.ADMIN);
        }

        @Override
        public void invoke(MessageParser msgp) {

            if (msgp.getArgCount() == 1)
                Utilities4D4J.sendEmbed(format(msgp.getGuild()), msgp.getChannel());
            else
                Utilities4D4J.sendEmbed(format(msgp.getChannelMention(1)), msgp.getChannel());
        }

        private EmbedObject format(IGuild g) {
            EmbedBuilder result = new EmbedBuilder()
                    .withTitle("Permissions for " + g.getName());

            for (Map.Entry<String, PermissionContext> entry : perms.getPermissionsFor(g).entrySet())
                result.appendField(entry.getKey(), entry.getValue().name(), false);

            return result.build();
        }

        private EmbedObject format(IChannel c) {
            EmbedBuilder result = new EmbedBuilder()
                    .withTitle("Permissions for " + c.getName());

            for (Map.Entry<String, PermissionContext> entry : perms.getPermissionsFor(c).entrySet())
                result.appendField(entry.getKey(), entry.getValue().name(), false);

            return result.build();
        }
    }

    public class EnableCommand extends Command {

        public EnableCommand() {
            super("enable", "Enables a command or commandgroup in the guild or the specified channel(s)",
                    "enable < all | {Command} | {CommandGroup} > [channel mention [channel mention...]]", true, PermissionContext.ADMIN);
        }

        @Override
        public void invoke(MessageParser msgp) throws CommandException {
            MessageMatcher matcher = msgp.match("T+C+?");

            if (matcher.match()) {

                String cmdName = concat(matcher.get(0), " ");

                String perm = "";
                PermissionContext context = PermissionContext.NULL;

                Command cmd = RedBot.getCommandHandler().getCommand(cmdName);
                boolean all = false;

                if (cmd != null) {
                    perm = cmd.getPermission();
                    context = cmd.getDefaultPermissions();
                } else {
                    CommandGroup cmdGroup = RedBot.getCommandHandler().getCommandGroup(cmdName);
                    if (cmdGroup != null) {
                        perm = cmdGroup.getPermission();
                        context = null;
                    } else if ("all".equalsIgnoreCase(cmdName))
                        all = true;
                    else {
                        msgp.reply("That command or command group doesn't exist!");
                        return;
                    }
                }

                String[] channels = matcher.get(1);

                if (channels.length != 0) {

                    List<IChannel> channelList = Arrays.stream(matcher.get(1))
                            .map(id -> RedBot.getClient().getChannelByID(id)).collect(Collectors.toList());

                    if (channelList.stream().anyMatch(it -> !perms.hasPermission(this, msgp.getAuthor(), it)))
                        msgp.reply("You don't have permission in all of those channels to enable commands!");
                    else {
                        if(all)
                            RedBot.getCommandHandler().getCommands().forEach(command ->
                                    channelList.forEach(channel -> perms.add(command.getPermission(), channel, command.getDefaultPermissions())));
                        else {
                            final String fPerm = perm;
                            final PermissionContext fContext = context;
                            channelList.forEach(channel -> perms.add(fPerm, channel, fContext));
                        }
                        msgp.reply("Enabled for those channels :ok_hand:");
                    }
                } else {
                    if(all)
                        RedBot.getCommandHandler().getCommands()
                                .forEach(command -> perms.add(command.getPermission(), msgp.getGuild(), command.getDefaultPermissions()));
                    else
                        perms.add(perm, msgp.getGuild(), context);
                    msgp.reply("Enabled for this guild :ok_hand:");
                }

            } else msgp.reply("Command not formatted correctly. See perms usage.");
        }
    }

    public class DisableCommand extends Command {

        public DisableCommand() {
            super("disable", "Disables a command or commandgroup in the guild or the specified channel(s)", true, PermissionContext.ADMIN);
        }

        @Override
        public void invoke(MessageParser msgp) throws CommandException {

        }
    }

    public class CommandsCommand extends Command {

        public CommandsCommand() {
            super("commands", "Lists available commands and command groups", true, PermissionContext.ADMIN);
        }

        @Override
        public void invoke(MessageParser msgp) throws CommandException {
            EmbedBuilder embed = new EmbedBuilder();

            RedBot.getCommandHandler().getCommandGroups().forEach(group -> {
                StringBuilder sb = new StringBuilder();
                group.getCommands().stream().filter(cmd -> PermissionContext.BOT_OWNER != cmd.getDefaultPermissions())
                        .forEachOrdered(cmd -> {
                            if (perms.isEnabledFor(cmd.getPermission(), msgp.getGuild()))
                                sb.append('\u2705');
                            else
                                sb.append('\u274c');
                            sb.append(cmd.getKeyword());
                            sb.append('\n');
                        });

                if (sb.toString().trim().length() > 0)
                    embed.appendField(group.getName(), sb.toString(), true);
            });

            StringBuilder sb = new StringBuilder();
            RedBot.getCommandHandler().getStandaloneCommands().stream()
                    .filter(cmd -> PermissionContext.BOT_OWNER != cmd.getDefaultPermissions())
                    .forEachOrdered(cmd -> {
                        if (perms.isEnabledFor(cmd.getPermission(), msgp.getGuild()))
                            sb.append('\u2705');
                        else
                            sb.append('\u274c');
                        sb.append(cmd.getKeyword());
                        sb.append('\n');
                    });

            if (sb.toString().trim().length() > 0)
                embed.appendField("Miscellaneous", sb.toString(), true);


            msgp.reply(embed.build());
        }
    }

    private static String concat(String[] words, String delimiter) {
        StringBuilder result = new StringBuilder();
        boolean isFirst = true;

        for (String word : words) {
            if (!isFirst) {
                result.append(delimiter);
            } else isFirst = false;

            result.append(word);
        }

        return result.toString();
    }
}