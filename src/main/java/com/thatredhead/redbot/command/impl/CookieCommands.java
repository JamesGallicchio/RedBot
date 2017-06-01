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
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

public class CookieCommands extends CommandGroup {

    public static final Emoji CLICK_UPGRADE_EMOJI = EmojiManager.getByUnicode("\uD83D\uDC46");
    public static final Emoji COOKIE_EMOJI = EmojiManager.getByUnicode("\uD83C\uDF6A");
    public static final Emoji AUTO_UPGRADE_EMOJI = EmojiManager.getByUnicode("\u2B06");

    private static List<CookieClickerAccount> accounts;
    private static Map<Long, Utilities4D4J.SerializableMessage> messages; // UserID -> (ChannelID, MessageID)
    private static Map<Long, Utilities4D4J.RequestQueue<IMessage>> updaters; // MessageID -> Queue
    private static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public CookieCommands() {

        super("Cookie Clicker Commands", "Commands for managing your cookie clicking!", "cookie",
                null);

        super.commands = Arrays.asList(new CookiesCommand(), new CookieLeaderboardCommand());

        if (accounts == null) {
            accounts = RedBot.getDataHandler().get("cookie_accounts",
                    new TypeToken<List<CookieClickerAccount>>() {
                    }.getType());

            if (accounts == null)
                accounts = new ArrayList<>();
        }

        if (messages == null) {

            messages = RedBot.getDataHandler().get("cookie_messages",
                    new TypeToken<Map<Long, Utilities4D4J.SerializableMessage>>() {
                    }.getType());

            if (messages == null)
                messages = new HashMap<>();
        }

        if (updaters == null) {
            updaters = new HashMap<>();
        }
    }

    public class CookiesCommand extends Command {

        public CookiesCommand() {
            super("cookies", "Sends a ~cookie command center~", PermissionContext.EVERYONE);
        }

        public void invoke(MessageParser msgp) {

            msgp.reply(Utilities4D4J.makeEmbed("RedBot Cookie Clicker", "Cookie clicker is currently disabled while I rewrite it! Thanks for your patience :)", false));
            /*
            CookieClickerAccount user = getAccountForUser(msgp.getAuthor());

            if (messages.containsKey(msgp.getAuthor().getLongID())) {
                IMessage msg = messages.remove(msgp.getAuthor().getLongID()).get();
                updaters.remove(msg.getLongID());
                Utilities4D4J.edit(msg, "RedBot Cookie Clicker", "Session expired. Use `cookies` command again to get a new one.", false);
            }

            IMessage msg = msgp.reply(user.toEmbed()).get();

            messages.put(msgp.getAuthor().getLongID(), new Utilities4D4J.SerializableMessage(msg));
            updaters.put(msg.getLongID(), new Utilities4D4J.RequestQueue<>(1, 3, TimeUnit.SECONDS));

            Utilities4D4J.addReactionsOrdered(msg, COOKIE_EMOJI, CLICK_UPGRADE_EMOJI, AUTO_UPGRADE_EMOJI);
            save();*/
        }
    }

    public class CookieLeaderboardCommand extends Command {

        public CookieLeaderboardCommand() {
            super("cookieleaderboard", "Lists the top RedBot Cookie Clickers ~~in the world~~", PermissionContext.EVERYONE);
        }

        public void invoke(MessageParser msgp) {
            StringBuilder sb = new StringBuilder("**Most Wealthiest Cookie Clickers**\n");

            List<CookieClickerAccount> sorted = accounts.stream()
                    .sorted(Comparator.comparing(CookieClickerAccount::getCookies).reversed()).collect(Collectors.toList());

            boolean covered = false;
            for (int i = 0; i < 10 && i < sorted.size(); i++) {
                CookieClickerAccount acc = sorted.get(i);
                sb.append("#").append(i + 1).append(": ").append(acc.getUser().getName()).append(" - ")
                        .append(CookieClickerAccount.formatNumber(acc.getCookies())).append("\n");
                if (acc.getUser().equals(msgp.getAuthor())) {
                    covered = true;
                }
            }

            if (!covered) {
                sorted.stream().filter(a -> a.getUser().equals(msgp.getAuthor())).findFirst()
                        .ifPresent(cookieClickerAccount -> {
                            CookieClickerAccount c = cookieClickerAccount;
                            sb.append("#").append(sorted.indexOf(c) + 1).append(": ").append(c.getUser().getName()).append(" - ")
                                    .append(CookieClickerAccount.formatNumber(c.getCookies()));
                        });
            }

            msgp.reply(sb.toString());
            accounts = sorted;
        }
    }

    public void handle(ReactionEvent event) {

        Utilities4D4J.SerializableMessage cache = messages.get(event.getUser().getLongID());
        if (cache != null &&
                cache.getID() == event.getMessage().getLongID()) {

            CookieClickerAccount acc = getAccountForUser(event.getUser());
            Emoji emoji = event.getReaction().getUnicodeEmoji();

            if (COOKIE_EMOJI.equals(emoji)) {
                acc.click();
            } else if (CLICK_UPGRADE_EMOJI.equals(emoji))
                acc.upgradeClick();
            else if (AUTO_UPGRADE_EMOJI.equals(emoji))
                acc.upgradeAuto();
            else
                return;

            IMessage msg = event.getMessage();

            updaters.get(msg.getLongID()).queue(() -> msg.edit(acc.toEmbed()));
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

    public CookieClickerAccount() {}

    public CookieClickerAccount(IUser user) {
        this.user = user;
        this.id = user.getLongID();
        this.lastUpdate = System.currentTimeMillis();
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
            cookiesPerSecond = calculateCPS(++autoUpgrades);
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

        long newTime = System.currentTimeMillis();

        long seconds = (newTime - lastUpdate) / 1000L;

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
        update();
        return Utilities4D4J.makeEmbed("RedBot Cookie Clicker :cookie:", "", true,
                "Cookie count", formatNumber(getCookies()),
                "Cookies/Click", formatNumber(getCookiesPerClick()) +
                        "\nNext: " + formatNumber(calculateCPC(clickUpgrades + 1)) +
                        "\nCost: " + formatNumber(getClickUpgradeCost()),
                "Cookies/Second", formatNumber(getCookiesPerSecond()) +
                        "\nNext: " + formatNumber(calculateCPS(autoUpgrades + 1)) +
                        "\nCost: " + formatNumber(getAutoUpgradeCost()));
    }
}
