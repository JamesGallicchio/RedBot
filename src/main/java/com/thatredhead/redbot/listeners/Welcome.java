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
            Utilities4D4J.sendEmbedToGuild(
                    Utilities4D4J.makeEmbed("Hi!", "**Thanks for inviting me to this guild!**\nHere's a short guide on how to set up everything.", false,
                            "Commands", "RedBot's prefix by default is `%` (custom prefixes coming soon!). Commands are as you'd expect- `%<keyword> <arguments>`.\n\nTo see a list of different commands available with RedBot, use %commands.",
                            "Enabling Commands", "RedBot comes with some cool commands, but to use them, you need to enable them. This allows you to control what commands people can use (per channel too!).\n\nTo enable a command, use `%enable <command>`. You can also enable entire command groups at once using that command group's name, or enable all of RedBot's commands in one sweep with `%enable all`.\nIn case you want to target a specific channel, you can mention that channel (or channels)- `%enable <command> #general` and so forth. If no channel is specified, the command will be enabled for the whole guild.\n\nIf a command is enabled in the whole guild but disabled in channel `#example`, then the channel will override the guild and the command won't be allowed.\n\nYou can also disable commands with `%disable` with the same rules as %enable`.",
                            "Permissions", "Every command has some required level of access- some commands are available for everyone, while others are restricted to guild admins or the guild's owner.\n\n`%help` will send you a DM with all the information on all commands you have permission to use.\n\nCustom permission setups are coming soon!"),
                    e.getGuild()
            );
        }

        RedBot.LOGGER.debug(e.getGuild() + " joined at " + joined + " and now it's " + now);
    }
}