package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.helpers4d4j.DiscordUtils;
import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandException;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.permission.PermissionContext;

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

        StringBuilder help = new StringBuilder();
        help.append("Commands you can use in channel ").append(msgp.getChannel().mention()).append("\n```md\n");

        for(CommandGroup cg : cmdGroups.stream()
                .filter(cg -> cg.getCommands().stream()
                        .anyMatch(c -> RedBot.getPermHandler().hasPermission(c, msgp.getAuthor(), msgp.getChannel())))
                .collect(Collectors.toList())) {

            help.append(cg.getName()).append("\n").append(dashes(cg.getName().length())).append("\n");

            for(Command c : cg.getCommands().stream()
                    .filter(it -> RedBot.getPermHandler().hasPermission(it, msgp.getAuthor(), msgp.getChannel()))
                    .collect(Collectors.toList())) {
                help.append("* ");
                help.append(c.getKeyword());
                help.append(" - [ ");
                help.append(c.getDescription());
                help.append(" ]( ");
                help.append(c.getUsage());
                help.append(" )\n");
            }
            help.append('\n');
        }

        help.append("Miscellaneous\n-------------\n");
        for(Command c : cmds.stream()
                .filter(it -> RedBot.getPermHandler().hasPermission(it, msgp.getAuthor(), msgp.getChannel()))
                .collect(Collectors.toList())) {
            help.append("* ");
            help.append(c.getKeyword());
            help.append(" - [ ");
            help.append(c.getDescription());
            help.append(" ]( ");
            help.append(c.getUsage());
            help.append(" )\n");
        }
        help.append("```");
        DiscordUtils.sendPrivateMessage(help.toString(), msgp.getAuthor());
        DiscordUtils.sendTemporaryMessage("Check your PMs!", msgp.getChannel());
    }

    private String dashes(int count) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < count; i++)
            sb.append('-');
        return sb.toString();
    }
}
