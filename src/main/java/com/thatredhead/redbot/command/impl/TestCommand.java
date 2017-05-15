package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandException;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.helpers4d4j.Utilities4D4J;
import com.thatredhead.redbot.permission.PermissionContext;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class TestCommand extends Command {

    public TestCommand() {
        super("test", "TESTY COMMANDS", PermissionContext.BOT_OWNER);
    }

    private static final Emoji plus = EmojiManager.getForAlias("heavy_plus_sign");
    private static final Emoji minus = EmojiManager.getForAlias("heavy_minus_sign");

    @Override
    public void invoke(MessageParser msgp) throws CommandException {

        Utilities4D4J.sendReactionUI("0", msgp.getChannel(), new Utilities4D4J.ReactionListener() {
            @Override
            public void onReactionToggle(IMessage msg, IUser user, Emoji emoji) {
                if(emoji.equals(plus)) {
                    Utilities4D4J.edit(msg, Integer.parseInt(msg.getContent())+1+"");
                } else if(emoji.equals(minus)) {
                    Utilities4D4J.edit(msg, Integer.parseInt(msg.getContent())-1+"");
                }
            }
        }, plus, minus);
    }
}
