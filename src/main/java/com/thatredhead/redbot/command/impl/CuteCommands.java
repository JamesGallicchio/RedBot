package com.thatredhead.redbot.command.impl;

import com.google.gson.reflect.TypeToken;
import com.thatredhead.redbot.DiscordUtils;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.command.MessageParser;
import com.thatredhead.redbot.data.DataHandler;
import com.thatredhead.redbot.permission.PermissionContext;
import javafx.util.Pair;
import org.apache.commons.io.IOUtils;
import sx.blah.discord.handle.obj.IChannel;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CuteCommands extends CommandGroup {

    private DataHandler datah;
    private List<Command> commands;
    private HashMap<String, String> safeties;
    private List<Pair<String, String>> engines;
    private int engineNum;

    public CuteCommands(DataHandler datah) {
        name = "Cute Commands";
        description = "Collection of commands to do cute things";
        permission = "cute";

        this.datah = datah;
        safeties = datah.get("cutesafety", new TypeToken<HashMap<String, String>>(){}.getType(), new HashMap<>());
        engines = datah.get("cuteengine", new TypeToken<List<Pair<String, String>>>(){}.getType(), new ArrayList<>());
        commands = new ArrayList<>();
        commands.add(new CuteCommand());
    }

    public List<Command> getCommands() {
        return commands;
    }

    public class CuteCommand extends Command {

        public CuteCommand() {
            keyword = permission = "cute";
            description = "Searches Google for cute images";
            usage = "cute *search terms* <gif if you want a gif>";
        }

        @Override
        public PermissionContext getDefaultPermissions() {
            return PermissionContext.getNobodyContext();
        }

        @Override
        public void invoke(MessageParser msgp) {
            if (!safeties.containsKey(msgp.getChannel().toString()))
                safeties.put(msgp.getChannel().getID(), "safe");
            cuteSearch(msgp.getContentAfter(0), msgp.getChannel());
        }

        private void cuteSearch(String msg, IChannel channel) {
            StringBuilder message = new StringBuilder(msg.toUpperCase());
            String safe = safeties.get(channel.getID());
            String type;
            if (message.toString().endsWith("GIF")) {
                type = "&fileType=gif";
                message.delete(message.length() - 4, message.length());
            } else {
                type = "";
            }
            message.delete(0, 4);
            StringBuilder response;
            if (message.toString().isEmpty()) {
                response = new StringBuilder("CUTE");
            } else {
                response = new StringBuilder(message.toString());
            }

            response.append(" *");
            response.append(Integer.toHexString(new Random().nextInt()).toUpperCase());
            response.append("*");
            if (message.toString().contains(".")) {
                String[] pieces = message.toString().split(".");
                message.setLength(0);
                for (String piece : pieces)
                    message.append("%2E" + piece);
            }

            String encoded = "";
            try {
                encoded = URLEncoder.encode(message.toString(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            int startAt = new Random().nextInt(100);

            int startEng = engineNum;
            while (true) {
                try {
                    String urlString = "https://www.googleapis.com/customsearch/v1?" +
                            "q=cute" + encoded +
                            "&cx=" + engines.get(engineNum).getKey() +
                            type +
                            "&filter=1" +
                            "&num=1" +
                            "&safe=" + safe +
                            "&searchType=image" +
                            "&start=" + startAt +
                            "&fields=items%2Flink" +
                            "&key=" + engines.get(engineNum).getValue();
                    URL googleURL = new URL(urlString);
                    String jsonString = IOUtils.toString(new InputStreamReader(googleURL.openStream()));
                    Pattern pattern = Pattern.compile("\"link\": \"(.+)\"", Pattern.DOTALL);
                    Matcher matcher = pattern.matcher(jsonString);
                    if (matcher.find()) {
                        response.append("\n");
                        response.append(matcher.group(1));
                    }
                    break;
                } catch (IOException maxDailyLimit) {

                    if (startEng != nextEng())
                        continue;

                    response.setLength(0);
                    response.append("\nError occurred- Possibly reached daily request limit.");
                    break;
                }
            }
            DiscordUtils.sendMessage(response.toString(), channel);
        }

        private int nextEng() {
            if(++engineNum >= engines.size())
                engineNum = 0;
            return engineNum;
        }
    }
}