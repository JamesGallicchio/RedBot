package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandException;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.helpers4d4j.Utilities4D4J;
import com.thatredhead.redbot.permission.PermissionContext;
import sx.blah.discord.util.EmbedBuilder;

import java.util.List;
import java.util.stream.Collectors;

public class HelpCommand extends Command {

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
