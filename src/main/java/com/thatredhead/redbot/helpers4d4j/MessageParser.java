package com.thatredhead.redbot.helpers4d4j;

import com.thatredhead.redbot.command.CommandException;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.*;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageParser {

    private static final Pattern CHNLP = Pattern.compile("<#(\\d+)>");
    private static final Pattern USERP = Pattern.compile("<@(\\d+)>");
    private static final Pattern ROLEP = Pattern.compile("<@&(\\d+)>");

    private IMessage msg;
    private String[] args;

    public MessageParser(IMessage msg) {
        this.msg = msg;
    }

    public boolean construct(String prefix) {

        if(msg.getContent().startsWith(prefix)) {
            args = msg.getContent().substring(prefix.length()).split(" ");
            return true;
        }
        args = msg.getContent().split(" ");
        return false;
    }

    public IMessage getMsg() {
        return msg;
    }

    public IUser getAuthor() {
        return msg.getAuthor();
    }

    public IChannel getChannel() {
        return msg.getChannel();
    }

    public IGuild getGuild() {
        return msg.getGuild();
    }

    public boolean isPrivate() {
        return getChannel().isPrivate();
    }

    public int getArgCount() {
        return args.length;
    }

    public String getArg(int i) {
        try {
            return args[i];
        } catch (IndexOutOfBoundsException e) {
            throw new CommandException();
        }
    }

    public String[] getArgs() {
        return args;
    }

    public String getContentAfter(int i) {
        StringBuilder sb = new StringBuilder();
        for(; i < args.length; i++) {
            sb.append(args[i]);
            sb.append(' ');
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public IChannel getChannelMention(int i) {
        Matcher m = CHNLP.matcher(args[i]);
        if(m.find())
            return msg.getClient().getChannelByID(m.group(1));
        return null;
    }

    public IUser getUserMention(int i) {
        Matcher m = USERP.matcher(args[i]);
        if(m.find())
            return msg.getClient().getUserByID(m.group(1));
        return null;
    }

    public IRole getRoleMention(int i) {
        Matcher m = ROLEP.matcher(args[i]);
        if(m.find())
            return msg.getClient().getRoleByID(m.group(1));
        return null;
    }
    
    public MessageMatcher match(String pattern) {
        return new MessageMatcher(Arrays.copyOfRange(args, 1, args.length), pattern);
    }

    public void reply(String response) {
        Utilities4D4J.sendMessage(response, msg.getChannel());
    }

    public void reply(EmbedObject response) {
        Utilities4D4J.sendEmbed(response, msg.getChannel());
    }
}