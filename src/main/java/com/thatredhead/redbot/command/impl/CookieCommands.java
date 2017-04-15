package com.thatredhead.redbot.command.impl;

import com.google.gson.reflect.TypeToken;
import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.permission.PermissionContext;
import sx.blah.discord.handle.obj.IUser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CookieCommands extends CommandGroup {

    private List<CookieClickerAccount> cookieAccounts;

    public CookieCommands() {

        super("Cookie Clicker Commands", "Commands for managing your cookie clicking!", "cookie",
                null);

        super.commands = Arrays.asList(new CookiesCommand());

        cookieAccounts = RedBot.getDataHandler().get("cookies",
                new TypeToken<List<CookieClickerAccount>>() {
                }.getType());

        if (cookieAccounts == null)
            cookieAccounts = new ArrayList<>();
    }

    public class CookiesCommand extends Command {

        public CookiesCommand() {
            super("cookies", "Sends a ~cookie command center~", PermissionContext.EVERYONE);
        }

        public void invoke(MessageParser msgp) {
            CookieClickerAccount user = cookieAccounts.stream()
                    .filter(acc -> msgp.getAuthor().equals(acc.getUser()))
                    .findFirst()
                    .orElseGet(() -> {
                        CookieClickerAccount acc = new CookieClickerAccount(msgp.getAuthor());
                        cookieAccounts.add(acc);
                        save();
                        return acc;
                    });

            msgp.reply(user.toString());
        }
    }

    private void save() {
        RedBot.getDataHandler().save(cookieAccounts, "cookies");
    }
}

class CookieClickerAccount {

    private String id;
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
        this.id = user.getID();
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

    public void update() {

        long newTime = System.nanoTime();

        long seconds = (newTime - lastUpdate + 500000000L) / 1000000000L;

        if (seconds != 0) {
            cookies.add(getCookiesPerSecond().multiply(BigInteger.valueOf(seconds)));
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

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("**RedBot Cookie Clicker**\n")
                .append("You have ").append(CookieClickerAccount.formatNumber(getCookies())).append("\n")
                .append("Cookies/Click: ").append(CookieClickerAccount.formatNumber(getCookiesPerClick())).append("\n")
                .append("Cookies/Second: ").append(CookieClickerAccount.formatNumber(getCookiesPerSecond())).append("\n\n")
                .append("Click Upgrades: ").append(clickUpgrades).append("\n")
                .append("Click Upgrade Cost: ").append(formatNumber(getClickUpgradeCost())).append("\n")
                .append("Speed Upgrades: ").append(autoUpgrades).append("\n")
                .append("Speed Upgrade Cost: ").append(formatNumber(getAutoUpgradeCost())).append("\n");

        return sb.toString();
    }
}