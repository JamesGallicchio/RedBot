package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandException;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.helpers4d4j.Utilities4D4J;
import com.thatredhead.redbot.permission.PermissionContext;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IUser;

public class HugCommand extends Command {
    public static final String HUG_GIF_URL = "https://media1.giphy.com/media/XpgOZHuDfIkoM/giphy.gif";

    public HugCommand() {
        super("hug", "Hugs another person!", "hug <user mention>", PermissionContext.EVERYONE);
    }

    @Override
    public void invoke(MessageParser msgp) throws CommandException {
        if (msgp.getArgCount() < 2) {
            Utilities4D4J.sendTemporaryMessage("You can't hug thin air! `hug <user mention>`", msgp.getChannel());
        } else {
            IUser u = msgp.getUserMention(1);
            if (u == null) {
                Utilities4D4J.sendTemporaryMessage("That's not a valid user mention! `hug <user mention>`", msgp.getChannel());
            } else {
                EmbedObject e = Utilities4D4J.makeEmbed(
                        msgp.getAuthor().getDisplayName(msgp.getGuild()) + " hugged " + u.getDisplayName(msgp.getGuild()) + "! :heart:",
                        "", false);

                e.image = new EmbedObject.ImageObject(HUG_GIF_URL, null, 0, 0);
                Utilities4D4J.sendEmbed(e, msgp.getChannel());
            }
        }
    }
}
