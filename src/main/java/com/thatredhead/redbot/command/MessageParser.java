package com.thatredhead.redbot.command;

import sx.blah.discord.handle.impl.obj.Message;
import sx.blah.discord.handle.obj.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageParser {

    private static final Pattern CHNLP = Pattern.compile("<#(\\d+)>");
    private static final Pattern USERP = Pattern.compile("<@(\\d+)>");
    private static final Pattern ROLEP = Pattern.compile("<@&(\\d+)>");

    private IMessage msg;
    private String prefix;
    private String[] args;

    public MessageParser(IMessage msg, String prefix) {
        this.msg = msg;
        this.prefix = prefix;
    }

    public boolean construct() {
        if(!msg.getContent().startsWith(prefix)) {
            args = msg.getContent().split(" ");
            return false;
        }
        args = msg.getContent().substring(prefix.length()).split(" ");
        return true;
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

    public int getArgCount() {
        return args.length;
    }

    public String getArg(int i) {
        return args[i];
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
        Matcher m = CHNLP.matcher(args[i]);
        if(m.find())
            return msg.getClient().getRoleByID(m.group(1));
        return null;
    }
}