package com.thatredhead.redbot.listeners;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MentionEvent;

public class Mention {

    @EventSubscriber
    public static void onMention(MentionEvent e) {
        e.getMessage().reply("Boop! *Use `%help` for a list of commands you can do (all commands should be prefixed with `%`)*");
    }
}
