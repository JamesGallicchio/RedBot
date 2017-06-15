package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandException;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.helpers4d4j.Utilities4D4J;
import com.thatredhead.redbot.permission.PermissionContext;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

public class SearchCommands extends CommandGroup {
    private static final Gson g = new Gson();

    public SearchCommands() {
        super("Search Commands", "Commands to search popular engines like Google, Wikipedia, WolframAlpha, and Urban Dictionary!",
                "search",Arrays.asList(
                        new LMGTFYCommand(),
                        new WolframAlphaSearchCommand(),
                        new UrbanDictCommand()
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
                msgp.reply("http://www.lmgtfy.com/?q=" + URLEncoder.encode(search, "UTF-8"));
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

    public static class UrbanDictCommand extends Command {
        public UrbanDictCommand() {
            super("urban", "Search for an Urban Dictionary definition", "urban <term>", PermissionContext.EVERYONE);
        }

        @Override
        public void invoke(MessageParser msgp) {
            String urlString = "http://api.urbandictionary.com/v0/define?term=" + URLEncoder.encode(msgp.getContentAfter(1));

            try {
                UrbanDictResponse r = g.fromJson(new InputStreamReader(new URL(urlString).openStream()), UrbanDictResponse.class);

                if ("exact".equals(r.result_type)) {
                    Definition d = r.list.get(0);
                    Utilities4D4J.sendEmbed(msgp.getChannel(), "Urban Dictionary: " + d.word, d.definition + "\n\n" + (d.example != null && !d.example.isEmpty() ? "*" + d.example + "*" : ""), true);
                } else {
                    msgp.reply("Couldn't find that search on Urban Dictionary :(");
                }
            } catch (IOException ignored) {}
        }

        public static class UrbanDictResponse {
            public String result_type;
            public List<Definition> list;
        }

        public static class Definition {
            public String word;
            public String definition;
            public String example;
        }
    }
}
