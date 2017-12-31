package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandException;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.helpers4d4j.Utilities4D4J;
import com.thatredhead.redbot.permission.PermissionContext;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.RequestBuffer;

import java.util.concurrent.atomic.AtomicInteger;

public class TestCommand extends Command {

    public TestCommand() {
        super("test", "TESTY COMMAND", PermissionContext.BOT_OWNER);
    }

    @Override
    public void invoke(MessageParser msgp) throws CommandException {
        if (msgp.getArgCount() < 2) msgp.reply("Specify count!");
        else {
            IChannel c = msgp.getChannel();
            String mention = msgp.getAuthor().mention();
            int count = Integer.parseInt(msgp.getArg(1));

            for (int i = 0; i < count; i++) {
                RequestBuffer.request(() -> {
                    c.sendMessage(mention);
                }).get();
            }
        }
    }
}
