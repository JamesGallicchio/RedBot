package com.thatredhead.redbot.command.impl;

import com.google.gson.reflect.TypeToken;
import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.helpers4d4j.Utilities4D4J;
import com.thatredhead.redbot.permission.PermissionContext;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionRemoveEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.math.BigInteger;
import java.util.*;

public class CookieCommands extends CommandGroup {

    public static final Emoji CLICK_UPGRADE_EMOJI = EmojiManager.getByUnicode("\uD83D\uDC46");
    public static final Emoji COOKIE_EMOJI = EmojiManager.getByUnicode("\uD83C\uDF6A");
    public static final Emoji AUTO_UPGRADE_EMOJI = EmojiManager.getByUnicode("\u2B06");

    private List<CookieClickerAccount> accounts;
    private Map<Long, Utilities4D4J.SerializableMessage> messages; // UserID -> (ChannelID, MessageID)

    public CookieCommands() {

        super("Cookie Clicker Commands", "Commands for managing your cookie clicking!", "cookie",
                null);

        super.commands = Arrays.asList(new CookiesCommand());

        accounts = RedBot.getDataHandler().get("cookie_accounts",
                new TypeToken<List<CookieClickerAccount>>() {
                }.getType());

        messages = RedBot.getDataHandler().get("cookie_messages",
                new TypeToken<Map<Long, Utilities4D4J.SerializableMessage>>() {
                }.getType());

        if (accounts == null)
            accounts = new ArrayList<>();

        if (messages == null)
            messages = new HashMap<>();
    }

    public class CookiesCommand extends Command {

        public CookiesCommand() {
            super("cookies", "Sends a ~cookie command center~", PermissionContext.EVERYONE);
        }

        public void invoke(MessageParser msgp) {
            CookieClickerAccount user = getAccountForUser(msgp.getAuthor());

            if (messages.containsKey(msgp.getAuthor().getLongID())) {
                IMessage msg = messages.remove(msgp.getAuthor().getLongID()).get();
                Utilities4D4J.edit(msg, "RedBot Cookie Clicker", "Session expired. Use `cookies` command again to get a new one.", false);
            }

            IMessage msg = msgp.reply(user.toEmbed()).get();

            messages.put(msgp.getAuthor().getLongID(), new Utilities4D4J.SerializableMessage(msg));

            Utilities4D4J.addReactions(msg, CLICK_UPGRADE_EMOJI, COOKIE_EMOJI, AUTO_UPGRADE_EMOJI);
            save();
        }
    }

    @EventSubscriber
    public void onReactionAdd(ReactionAddEvent event) {
        handle(event);
    }

    @EventSubscriber
    public void onReactionRemove(ReactionRemoveEvent event) {
        handle(event);
    }

    public void handle(ReactionEvent event) {

        Utilities4D4J.SerializableMessage cache = messages.get(event.getUser().getLongID());
        if(cache != null &&
                cache.getID() == event.getMessage().getLongID()) {

            CookieClickerAccount acc = getAccountForUser(event.getUser());
            Emoji emoji = event.getReaction().getUnicodeEmoji();

            if(COOKIE_EMOJI.equals(emoji)) {
                acc.click();
                return;
            }
            else if(CLICK_UPGRADE_EMOJI.equals(emoji))
                acc.upgradeClick();
            else if(AUTO_UPGRADE_EMOJI.equals(emoji))
                acc.upgradeAuto();
            else
                return;

            Utilities4D4J.edit(event.getMessage(), acc.toEmbed());
            save();
        }
    }

    private CookieClickerAccount getAccountForUser(IUser user) {
        return accounts.stream()
                .filter(acc -> user.equals(acc.getUser()))
                .findFirst()
                .orElseGet(() -> {
                    CookieClickerAccount acc = new CookieClickerAccount(user);
                    accounts.add(acc);
                    save();
                    return acc;
                });
    }

    private void save() {
        RedBot.getDataHandler().save(accounts, "cookie_accounts");
        RedBot.getDataHandler().save(messages, "cookie_messages");
    }
}

class CookieClickerAccount {

    private long id;
    private BigInteger cookies;
    private int clickUpgrades;
    private int autoUpgrades;
    private long lastUpdate;

    private transient IUser user;
    private transient BigInteger cookiesPerClick;
    private transient BigInteger cookiesPerSecond;

    public CookieClickerAccount() {
    }

    public CookieClickerAccount(IUser user) {
        this.user = user;
        this.id = user.getLongID();
        this.lastUpdate = System.nanoTime();
        this.cookies = BigInteger.ZERO;
    }

    public IUser getUser() {
        return user == null ?
                user = RedBot.getClient().getUserByID(id)
                : user;
    }

    public BigInteger getCookies() {
        return cookies;
    }

    public BigInteger getCookiesPerClick() {
        if (cookiesPerClick != null)
            return cookiesPerClick;

        return cookiesPerClick = calculateCPC(clickUpgrades);
    }

    public BigInteger getCookiesPerSecond() {
        if (cookiesPerSecond != null)
            return cookiesPerSecond;

        return cookiesPerSecond = calculateCPS(autoUpgrades);
    }

    public BigInteger getClickUpgradeCost() {
        return calculateCPCUpgradeCost(clickUpgrades + 1);
    }

    public BigInteger getAutoUpgradeCost() {
        return calculateCPSUpgradeCost(autoUpgrades + 1);
    }

    public boolean upgradeClick() {

        BigInteger cost = calculateCPCUpgradeCost(clickUpgrades + 1);

        if (cookies.compareTo(cost) >= 0) {
            cookies = cookies.subtract(cost);
            cookiesPerClick = calculateCPC(++clickUpgrades);
            return true;
        }

        return false;
    }

    public boolean upgradeAuto() {

        BigInteger cost = calculateCPSUpgradeCost(autoUpgrades + 1);

        if (cookies.compareTo(cost) >= 0) {
            cookies = cookies.subtract(cost);
            cookiesPerSecond = calculateCPC(++autoUpgrades);
            return true;
        }

        return false;
    }

    private BigInteger calculateCPC(int upgrades) {
        return BigInteger.valueOf(upgrades + 1).pow(2);
    }

    private BigInteger calculateCPS(int upgrades) {
        return BigInteger.valueOf(upgrades).pow(2);
    }

    private BigInteger calculateCPCUpgradeCost(int upgrades) {
        return calculateCPC(upgrades - 1).multiply(BigInteger.valueOf((100 - (int) Math.log10(upgrades)) / 2));
    }

    private BigInteger calculateCPSUpgradeCost(int upgrades) {
        return calculateCPS(upgrades + 1).multiply(BigInteger.valueOf(100 - (int) Math.log10(upgrades + 1)));
    }

    public void click() {
        cookies = cookies.add(getCookiesPerClick());
    }

    public void update() {

        long newTime = System.nanoTime();

        long seconds = (newTime - lastUpdate + 500000000L) / 1000000000L;

        if (seconds != 0) {
            cookies = cookies.add(getCookiesPerSecond().multiply(BigInteger.valueOf(seconds)));
            lastUpdate = newTime;
        }

    }

    public static String formatNumber(BigInteger num) {

        String number = num.toString();

        if (number.length() < 4)
            return number;

        int thousands = (number.length() - 1) / 3;
        String ending = endingFor(thousands);
        if (ending == null) return "Infinity";

        int splitPoint = number.length() - thousands * 3;

        return number.substring(0, splitPoint) + "." + number.substring(splitPoint, splitPoint + 3) + " " + ending;
    }

    private static String[] words = {"thousand", "million", "billion", "trillion", "quadrillion", "quintillion", "sextillion", "septillion", "octillion", "nonillion"};
    private static String[] onesPrefixes = {"", "un", "duo", "tre", "quattuor", "quin", "sex", "sept", "octo", "novem"};
    private static String[] tensPrefixes = {"", "dec", "vigin", "trigin", "quadragin", "quinquagin", "sexagin", "septuagin", "octogin", "nonagin", "cent"};

    private static String endingFor(int count) {
        count -= 1;

        if (count < 0)
            return "";

        if (count < 10)
            return words[count];

        if (count > 100)
            return null;

        return onesPrefixes[count % 10] + tensPrefixes[count / 10] + "illion";
    }

    public EmbedObject toEmbed() {
        return Utilities4D4J.makeEmbed("RedBot Cookie Clicker :cookie:", "", true,
                "Cookie count", CookieClickerAccount.formatNumber(getCookies()),
                "Cookies/Click", CookieClickerAccount.formatNumber(getCookiesPerClick()) +
                        "\nClick Upgrades: " + clickUpgrades +
                        "\nNext Upgrade: " + formatNumber(getClickUpgradeCost()),
                "Cookies/Second", CookieClickerAccount.formatNumber(getCookiesPerSecond()) +
                        "\nSpeed Upgrades: " + autoUpgrades +
                        "\nNext Upgrade: " + formatNumber(getAutoUpgradeCost()));
    }
}