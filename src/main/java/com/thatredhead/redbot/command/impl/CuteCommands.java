package com.thatredhead.redbot.command.impl;

import com.google.gson.reflect.TypeToken;
import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandArgumentException;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.data.DataHandler;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.helpers4d4j.Utilities4D4J;
import com.thatredhead.redbot.permission.PermissionContext;
import org.apache.commons.io.IOUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.MessageHistory;

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
    private Map<Long, String> safeties;

    private static final String engine = "014731838518875835789:co91bck4x3g";
    private List<String> keys = Arrays.asList(
            "AIzaSyBbSBRLN55ZhsouwtFjOZNmCNoH1RgpwKE",
            "AIzaSyA-o0KkorYgCx--Wkt6TtL4twh8r7d_7LQ",
            "AIzaSyA4zOXxrwQEN7G-v-hH8sXupSRVzTxYcQE",
            "AIzaSyAdTenA71JZZNTgC-YGBDgAPtjEm7SU7A8",
            "AIzaSyAuRsKevOu2dTjeMkfjSsOUBuxY2rv0fZU");
    private int keyNum;

    public CuteCommands() {
        super("Cute Commands", "Collection of commands to do cute things",
                "cute", null);
        commands = Arrays.asList(new CuteCommand(), new CuteSafetyCommand(), new CuteReportCommand());

        this.datah = RedBot.getDataHandler();
        safeties = datah.get("cutesafety", new TypeToken<Map<Long, String>>() {
        }.getType(), new HashMap<>());
    }

    public class CuteCommand extends Command {

        public CuteCommand() {
            super("cute", "Searches Google for cute images",
                    "cute *search terms* <gif if you want a gif>", PermissionContext.EVERYONE);
        }

        @Override
        public void invoke(MessageParser msgp) {

            long id = Utilities4D4J.stableChannelId(msgp.getChannel());
            if (!safeties.containsKey(id)) {
                safeties.put(id, "high");
                datah.save(safeties, "cutesafety");
            }

            String msg = msgp.getContentAfter(0);
            IChannel channel = msgp.getChannel();

            StringBuilder message = new StringBuilder(msg.toUpperCase());
            String safe = safeties.get(Utilities4D4J.stableChannelId(channel));
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
                    message.append("%2E").append(piece);
            }

            String encoded = "";
            try {
                encoded = URLEncoder.encode(message.toString(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            int startAt = new Random().nextInt(100);

            int startEng = keyNum;
            while (true) {
                String urlString = "https://www.googleapis.com/customsearch/v1?" +
                        "q=cute" + encoded +
                        "&cx=" + engine +
                        type +
                        "&filter=1" +
                        "&num=1" +
                        "&safe=" + safe +
                        "&searchType=image" +
                        "&start=" + startAt +
                        "&fields=items%2Flink" +
                        "&key=" + keys.get(keyNum);
                URL googleURL = null;
                try {
                    googleURL = new URL(urlString);
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
                } catch (IOException e) {

                    if (startEng != nextKey())
                        continue;

                    response.setLength(0);
                    if (e.getMessage().contains("50"))
                        response.append("\nGoogle's servers are probably down. Try again in a minute.");
                    else if (e.getMessage().contains("40"))
                        response.append("\nProbably reached daily request limit. :(");
                    else {
                        response.append("\nError getting an image. Try again in a bit.");
                        RedBot.reportError(e, "Request URL: " + googleURL, msgp);
                    }
                    break;
                }
            }
            Utilities4D4J.sendMessage(response.toString(), channel);
        }
    }

    public class CuteSafetyCommand extends Command {

        public CuteSafetyCommand() {
            super("cutesafety", "Sets search safety for the channel",
                    "cutesafety < off | medium | high >", PermissionContext.ADMIN);
        }

        @Override
        public void invoke(MessageParser msgp) {
            String level = msgp.getArg(1);

            if (!("off".equals(level) || "medium".equals(level) || "high".equals(level)))
                throw new CommandArgumentException(1, level, "Safety level must be off, medium, or high!");

            safeties.put(Utilities4D4J.stableChannelId(msgp.getChannel()), level);
            RedBot.getDataHandler().save(safeties, "cutesafety");

            msgp.reply("Set this channel's safety level to " + level + "!");
        }
    }

    public class CuteReportCommand extends Command {

        public CuteReportCommand() {
            super("cutereport", "Deletes (and records) inappropriate cute images",
                    "cutereport <ID>", PermissionContext.EVERYONE);
        }

        @Override
        public void invoke(MessageParser msgp) {

            String id = msgp.getArg(1).toUpperCase();
            boolean full = msgp.getArgCount() > 2 && msgp.getArg(2).equals("full");

            IMessage reported = null;
            MessageHistory history = msgp.getChannel().getMessageHistory(100);
            for (IMessage msg : history) {
                if (msg.getAuthor().equals(RedBot.getClient().getOurUser()) &&
                        msg.getContent().contains(id))
                    reported = msg;
            }

            if (reported == null) {
                if (full)
                    msgp.reply("None of my responses in this channel use that ID.");
                else
                    msgp.reply("None of the last 100 messages in this channel use that ID."); // Use `cutereport <ID> full` to go through the entire channel.");
                return;
            }

            reported.delete();
            msgp.reply("Removed image *" + id + "* successfully!");
        }
    }

    private int nextKey() {
        if (++keyNum >= keys.size())
            keyNum = 0;
        return keyNum;
    }
}
