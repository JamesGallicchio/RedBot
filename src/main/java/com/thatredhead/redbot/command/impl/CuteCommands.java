package com.thatredhead.redbot.command.impl;

import com.google.gson.reflect.TypeToken;
import com.thatredhead.redbot.DiscordUtils;
import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandArgumentException;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.command.MessageParser;
import com.thatredhead.redbot.data.DataHandler;
import com.thatredhead.redbot.permission.PermissionContext;
import javafx.util.Pair;
import org.apache.commons.io.IOUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.Permissions;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CuteCommands extends CommandGroup {

    private DataHandler datah;
    private HashMap<String, String> safeties;
    private List<Pair<String, String>> engines;
    private int engineNum;

    public CuteCommands() {
        super("Cute Commands", "Collection of commands to do cute things",
                "cute", null);
        commands = Arrays.asList(new CuteCommand(), new CuteSafetyCommand());

        this.datah = RedBot.getDataHandler();
        safeties = datah.get("cutesafety", new TypeToken<HashMap<String, String>>(){}.getType(), new HashMap<>());
        engines = new ArrayList<>();
        engines.add(new Pair<>("014731838518875835789%3Atp7hgh9vtu8", "AIzaSyBbSBRLN55ZhsouwtFjOZNmCNoH1RgpwKE"));
        engines.add(new Pair<>("014731838518875835789%3Aco91bck4x3g", "AIzaSyBbSBRLN55ZhsouwtFjOZNmCNoH1RgpwKE"));
    }

    public class CuteCommand extends Command {

        public CuteCommand() {
            super("cute", "Searches Google for cute images",
                    "cute *search terms* <gif if you want a gif>", "cute", false);
        }

        @Override
        public PermissionContext getDefaultPermissions() {
            return PermissionContext.getNobodyContext();
        }

        @Override
        public void invoke(MessageParser msgp) {
            if (!safeties.containsKey(msgp.getChannel().getID())) {
                safeties.put(msgp.getChannel().getID(), "high");
                datah.save(safeties, "cutesafety");
            }
            cuteSearch(msgp.getContentAfter(0), msgp.getChannel());
        }


    }

    public class CuteSafetyCommand extends Command {

        public CuteSafetyCommand() {
            super("cutesafety", "Sets search safety for the channel", "cutesafety < off | medium | high >");
        }

        @Override
        public PermissionContext getDefaultPermissions() {
            return new PermissionContext(Permissions.ADMINISTRATOR);
        }

        @Override
        public void invoke(MessageParser msgp) {
            String level = msgp.getArg(1);

            if(!("off".equals(level) || "medium".equals(level) || "high".equals(level)))
                throw new CommandArgumentException(1, level, "Safety level must be off, medium, or high!");

            safeties.put(msgp.getChannel().getID(), level);
            RedBot.getDataHandler().save(safeties, "cutesafety");
        }
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
                } else {
                    response.setLength(0);
                    response.append("Your request turned up no results.");
                }
                break;
            } catch (IOException maxDailyLimit) {

                RedBot.LOGGER.error("IO Error encountered on request to Google's servers: ", maxDailyLimit);

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