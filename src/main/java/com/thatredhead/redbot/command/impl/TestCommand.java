package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandException;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.helpers4d4j.Utilities4D4J;
import com.thatredhead.redbot.permission.PermissionContext;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

public class TestCommand extends Command {
    public static final Emoji LOWER = EmojiManager.getForAlias("heavy_minus_sign");
    public static final Emoji RAISE = EmojiManager.getForAlias("heavy_plus_sign");

    private long lastMessage;

    public TestCommand() {
        super("test", "TESTY COMMANDS", PermissionContext.BOT_OWNER);
    }

    @Override
    public void invoke(MessageParser msgp) throws CommandException {
        Utilities4D4J.removeReactionUI(lastMessage);
        AtomicInteger num = new AtomicInteger();
        Utilities4D4J.sendReactionUI(num.toString(), msgp.getChannel(), (m, u, e) -> {
            if (LOWER.equals(e)) {
                num.incrementAndGet();
            } else {
                num.decrementAndGet();
            }
            Utilities4D4J.edit(m, num.toString());
        });
    }
}
