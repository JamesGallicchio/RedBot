package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandException;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.helpers4d4j.Utilities4D4J;
import com.thatredhead.redbot.permission.PermissionContext;
import sx.blah.discord.util.EmbedBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RedBotCommands extends CommandGroup {

    public RedBotCommands() {
        super("RedBot Commands", "General commands for users and administrators", "redbot", Arrays.asList(new InviteCommand(), new InfoCommand(), new HelpCommand(), new AdminHelpCommand()));
    }

    public static class InviteCommand extends Command {

        public InviteCommand() {
            super("invite", "Gives a link to invite the bot to other servers", PermissionContext.EVERYONE);
        }

        @Override
        public void invoke(MessageParser msgp) throws CommandException {
            Utilities4D4J.sendEmbed(msgp.getChannel(), "RedBot Invite", "[Invite me to another guild!](" + RedBot.INVITE + ")", false);
        }
    }

    public static class InfoCommand extends Command {

        public InfoCommand() {
            super("info", "Provides some info about the bot", PermissionContext.EVERYONE);
        }

        @Override
        public void invoke(MessageParser msgp) throws CommandException {
            Utilities4D4J.sendEmbed(msgp.getChannel(), "RedBot Info",
                    "Hi! I'm RedBot- a Discord bot written by <@" + RedBot.OWNER_ID +
                            ">! I'm written using [Discord4J](http://github.com/austinv11/Discord4J), and my source code " +
                            "[is publicly available online](http://github.com/JamesGallicchio/RedBot).", false,
                    "Getting Started", "Use `%help` to find out what you can do. You can use whatever commands this server's administration has enabled, or PM RedBot to use all of its functions.\n"+
                                "For administrators: Use `%adminhelp` for information on how to set up RedBot in your server.",
                            "Support", "Join my support guild [here](currently disabled). There you can find help, suggest new features, talk to Red, etc."
                    );
        }
    }

    public static class HelpCommand extends Command {

        private List<CommandGroup> cmdGroups;
        private List<Command> cmds;

        public HelpCommand() {
            super("help", "Get PM'd a list of commands you can use in this channel", true, PermissionContext.EVERYONE);
        }

        @Override
        public void invoke(MessageParser msgp) throws CommandException {
            if(cmdGroups == null)
                cmdGroups = RedBot.getCommandHandler().getCommandGroups();
            if(cmds == null)
                cmds = RedBot.getCommandHandler().getStandaloneCommands();

            EmbedBuilder help = new EmbedBuilder();
            help.withTitle("RedBot Help for " + msgp.getChannel().mention());

            for(CommandGroup cg : cmdGroups.stream()
                    .filter(cg -> cg.getCommands().stream()
                            .anyMatch(c -> RedBot.getPermHandler().hasPermission(c, msgp.getAuthor(), msgp.getChannel())))
                    .collect(Collectors.toList())) {

                StringBuilder sb = new StringBuilder();

                for(Command c : cg.getCommands().stream()
                        .filter(it -> RedBot.getPermHandler().hasPermission(it, msgp.getAuthor(), msgp.getChannel()))
                        .collect(Collectors.toList())) {
                    sb.append(c.getKeyword());
                    sb.append(": *");
                    sb.append(c.getUsage());
                    sb.append("* - ");
                    sb.append(c.getDescription());
                    sb.append("\n");
                }

                help.appendField(cg.getName(), sb.toString(), true);
            }

            StringBuilder sb = new StringBuilder();

            for(Command c : cmds.stream()
                    .filter(it -> RedBot.getPermHandler().hasPermission(it, msgp.getAuthor(), msgp.getChannel()))
                    .collect(Collectors.toList())) {
                sb.append(c.getKeyword());
                sb.append(": *");
                sb.append(c.getUsage());
                sb.append("* - ");
                sb.append(c.getDescription());
                sb.append("\n");
            }
            help.appendField("Miscellaneous", sb.toString(), true);
            Utilities4D4J.sendPrivateMessage(help.build(), msgp.getAuthor());
            Utilities4D4J.sendTemporaryMessage("Check your PMs!", msgp.getChannel());
        }
    }

    public static class AdminHelpCommand extends Command {

        public AdminHelpCommand() {
            super("adminhelp", "Provides help information for administrators looking to set up RedBot in their guild", PermissionContext.ADMIN);
        }

        public void invoke(MessageParser msgp) {
            msgp.reply(Utilities4D4J.makeEmbed("Hi!", "**Thanks for inviting me to this guild!**\nHere's a short guide on how to set up everything.", false,
                    "Commands", "RedBot's prefix by default is `%` (custom prefixes coming soon!). Commands are as you'd expect- `%<keyword> <arguments>`.\n\nTo see a list of different commands available with RedBot, use %commands.",
                    "Enabling Commands", "RedBot comes with some cool commands, but to use them, you need to enable them. This allows you to control what commands people can use (per channel too!).\n\nTo enable a command, use `%enable <command>`. You can also enable entire command groups at once using that command group's name, or enable all of RedBot's commands in one sweep with `%enable all`.\nIn case you want to target a specific channel, you can mention that channel (or channels)- `%enable <command> #general` and so forth. If no channel is specified, the command will be enabled for the whole guild.\n\nIf a command is enabled in the whole guild but disabled in channel `#example`, then the channel will override the guild and the command won't be allowed.\n\nYou can also disable commands with `%disable` with the same rules as %enable`.",
                    "Permissions", "Every command has some required level of access- some commands are available for everyone, while others are restricted to guild admins or the guild's owner.\n\n`%help` will send you a PM with all the information on all commands you have permission to use.\n\nCustom permission setups are coming soon!"
            ));
        }
    }
}
