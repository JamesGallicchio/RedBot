package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandException;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.permission.PermissionContext;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;

public class SearchCommands extends CommandGroup {

    public SearchCommands() {
        super("Search Commands", "Commands to search popular engines like Google, Wikipedia, WolframAlpha, and Urban Dictionary!",
                "search",Arrays.asList(
                        new LMGTFYCommand(),
                        new WolframAlphaSearchCommand()
                ));
    }

    public static class LMGTFYCommand extends Command {

        public LMGTFYCommand() {
            super("lmgtfy", "Generate a LMGTFY link", "lmgtfy <search>", PermissionContext.EVERYONE);
        }

        @Override
        public void invoke(MessageParser msgp) throws CommandException {
            String search = msgp.getContentAfter(1);

            try {
                msgp.reply("http://www.lmgtfy/q=" + URLEncoder.encode(search, "UTF-8"));
            } catch (UnsupportedEncodingException ignored) {

            }
        }
    }

    public static class WolframAlphaSearchCommand extends Command {

        public WolframAlphaSearchCommand() {
            super("wolfram", "Query WolframAlpha", "wolfram <search>", PermissionContext.EVERYONE);
        }

        @Override
        public void invoke(MessageParser msgp) throws CommandException {
            String search = msgp.getContentAfter(1);


        }
    }
}

class Search {

}