package com.thatredhead.redbot.listeners;

import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.helpers4d4j.Utilities4D4J;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class Welcome {

    @EventSubscriber
    public void onGuildCreateEvent(GuildCreateEvent e) {

        long now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();
        long joined = e.getGuild().getJoinTimeForUser(e.getClient().getOurUser()).atZone(ZoneId.systemDefault()).toEpochSecond();

        if (now - joined < 5) {
            Utilities4D4J.sendMessageToGuild("", e.getGuild());
        }

        RedBot.LOGGER.debug(e.getGuild() + " joined at " + joined + " and now it's " + now);
    }
}