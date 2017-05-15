package com.thatredhead.redbot.listeners;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MentionEvent;

public class Mention {

    @EventSubscriber
    public void onMention(MentionEvent e) {
        if(!(e.getMessage().mentionsEveryone() || e.getMessage().mentionsHere()))
            e.getMessage().reply("boop! *Use `%help` for a list of commands you can do (all commands should be prefixed with `%`)*");
    }
}
