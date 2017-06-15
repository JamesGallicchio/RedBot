package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandException;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.helpers4d4j.Utilities4D4J;
import com.thatredhead.redbot.permission.PermissionContext;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CAHCommand extends Command {
    public static final List<Card> WHITE_CARDS;
    public static final List<Card> BLACK_CARDS;
    public static final EmbedObject HELP_EMBED = Utilities4D4J.makeEmbed("Cards Against Humanity Help", "You can start a new game in any channel where the `cah` command is enabled using `cah create`. To join the game, each player should use `cah join`. When you're ready to start, use `cah start`.", false);

    static {
        List<Card> whites = null;
        List<Card> blacks = null;
        try {
            whites = Files.readAllLines(Paths.get("white-cards.txt")).stream()
                    .map(s -> new Card(s, false, false))
                    .collect(Collectors.toList());
            blacks = Files.readAllLines(Paths.get("black-cards.txt")).stream()
                    .map(s -> new Card(s, false, false))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            WHITE_CARDS = whites;
            BLACK_CARDS = blacks;
        }
    }

    private Map<Long, CAHGame> games;

    public CAHCommand() {
        super("cah", "The root command for Cards Against Humanity games", PermissionContext.EVERYONE);
        games = new HashMap<>();
    }

    @Override
    public void invoke(MessageParser msgp) throws CommandException {
        if (msgp.getArgCount() < 1) Utilities4D4J.sendEmbed(HELP_EMBED, msgp.getChannel());

        String keyword = msgp.getArg(1).toLowerCase();

        if ("create".equals(keyword)) {
            if (games.containsKey(msgp.getChannel().getLongID())) {
                msgp.reply("There's already a game going on in this channel.");
            } else {
                games.put(msgp.getChannel().getLongID(), new CAHGame(7, 5, 3, 20));
                games.get(msgp.getChannel().getLongID()).addPlayer(msgp.getAuthor());
                msgp.reply("New game created! `cah join` to join the game.");
            }
        } else {
            CAHGame g = games.get(msgp.getChannel().getLongID());
            if (g == null) {
                msgp.reply("There is no game going on right now. Use `cah` for help.");
            } else if (g.hasStarted()) {
                if ("end".equals(keyword)) {
                    if (msgp.getAuthor().getLongID() == g.getCzar()) {
                        games.remove(msgp.getChannel().getLongID());
                        msgp.reply("__Ended the game! Final scores__\n" + scores(g));
                    } else {
                        msgp.reply("Only the current czar can end a game.");
                    }
                } else {
                    msgp.reply("This game has already started!");
                }
            } else if ("join".equals(keyword)) {
                if (g.isPlayer(msgp.getAuthor())) {
                    msgp.reply("You're already in this game.");
                } else {
                    g.addPlayer(msgp.getAuthor());
                    msgp.reply("Successfully joined the game!");
                }
            } else if ("start".equals(keyword)) {
                g.start();
                takeTurn(msgp.getChannel(), g);
            } else {
                msgp.reply("Unknown command " + msgp.getArg(1) + "! Use `cah` for help.");
            }
        }
    }

    private static void takeTurn(IChannel c, CAHGame g) {
        Utilities4D4J.sendEmbed(c, "Cards Against Humanity", "Current czar: <@" + g.getCzar() + ">", true, "Scores", scores(g));

    }

    private static String scores(CAHGame g) {
        List<Player> ps = g.getPlayers();
        ps.sort(Comparator.comparing(Player::getScore));

        StringBuilder s = new StringBuilder();
        for (Player p : ps) {
            s.append("<@").append(p.getUserID()).append(">").append(": ")
                    .append(p.getScore()).append("\n");
        }
        return s.toString();
    }

    private static class CAHGame {
        private final int minPlayers;
        private final int maxPlayers;
        private final int handSize;
        private final int cardsToWin;
        private boolean isStarted;

        private LinkedList<Player> players;

        private List<Card> whiteDeck;
        private List<Card> whiteDiscard;
        private List<Card> blackDeck;
        private List<Card> blackDiscard;

        private Card currentBlack;
        private List<Card> submitted;

        public CAHGame(int hands, int winCount, int minPlay, int maxPlay) {
            handSize = hands;
            cardsToWin = winCount;
            minPlayers = minPlay;
            maxPlayers = maxPlay;
            players = new LinkedList<>();
            whiteDeck = new ArrayList<>();
            whiteDeck.addAll(WHITE_CARDS);
            whiteDiscard = new ArrayList<>();
            blackDeck = new ArrayList<>();
            blackDeck.addAll(BLACK_CARDS);
            blackDiscard = new ArrayList<>();
        }

        public void addPlayer(IUser u) {
            if (isStarted) throw new IllegalStateException("Game already started!");
            if (isPlayer(u)) throw new IllegalStateException("Player already in the game!");

            Player p = new Player(u);
            for (int i = 0; i < handSize; i++) p.getCards().add(getNextWhiteCard());
            p.choice = -2;
            players.addLast(p);
        }

        public void removePlayer(IUser u) {
            if (players.getFirst().getUserID() == u.getLongID())
                throw new IllegalStateException("Czar can't leave the game during their turn!");
            if (!players.removeIf(p -> p.getUserID() == u.getLongID())) {
                throw new IllegalStateException("Can't remove someone who isn't in the game!");
            }
        }

        public boolean isPlayer(IUser u) {
            return getPlayer(u.getLongID()).isPresent();
        }

        public Optional<Player> getPlayer(long id) {
            return players.stream().filter(p -> p.userID == id).findFirst();
        }

        public List<Player> getPlayers() {
            return players;
        }

        public void start() {
            if (players.size() < minPlayers || players.size() > maxPlayers)
                throw new IllegalStateException("Player count is not within range!");
            if (isStarted) throw new IllegalStateException("Already started!");
            isStarted = true;
            players.forEach(p -> p.choice = -1);
        }

        public boolean hasStarted() {
            return isStarted;
        }

        public long getCzar() {
            return players.getFirst().userID;
        }

        public void chooseCard(IUser u, int choice) {
            if (!hasStarted())
                throw new IllegalStateException("Game hasn't started yet!");
            if (u.getLongID() == getCzar())
                throw new IllegalArgumentException("That player is the czar!");

            Player p = getPlayer(u.getLongID()).orElse(null);
            if (p == null)
                throw new IllegalArgumentException("Given user isn't a player!");
            if (choice <= 0 || p.cards.size() < choice)
                throw new IllegalArgumentException("Out of range selection!");
            p.choice = choice;
        }

        public boolean isWaiting() {
            return players.stream().anyMatch(p -> p.choice == -1 && p.getUserID() != getCzar());
        }

        public Map<Long, Card> getChosen() {
            return players.stream().collect(Collectors.toMap(Player::getUserID, Player::getChoiceCard));
        }

        public void czarChoose(long userID) {
            getPlayer(userID).orElseThrow(() -> new IllegalArgumentException("Given user ID doesn't correspond to a player!"))
                    .incrScore();
            players.forEach(p -> {
                if (p.getChoice() >= 0) {
                    whiteDiscard.add(p.removeChoiceCard());
                    p.dealCard(whiteDeck.get(whiteDeck.size() - 1));
                    p.setChoice(-1);
                }
            });

            players.addLast(players.removeFirst());
        }

        private Card getNextWhiteCard() {
            if (whiteDeck.isEmpty()) {
                while (!whiteDiscard.isEmpty()) {
                    whiteDeck.add(whiteDiscard.remove((int) (whiteDiscard.size() * Math.random())));
                }
            }
            return whiteDeck.remove(whiteDeck.size() - 1);
        }

        private Card getNextBlackCard() {
            if (blackDeck.isEmpty()) {
                while (!blackDiscard.isEmpty()) {
                    blackDeck.add(blackDiscard.remove((int) (blackDiscard.size() * Math.random())));
                }
            }
            return blackDeck.remove(blackDeck.size() - 1);
        }
    }

    private static class Player {
        private static final Emoji LEFT = EmojiManager.getByUnicode("←");
        private static final Emoji RIGHT = EmojiManager.getByUnicode("→");
        private long userID;
        private long cardsMessageID;
        private int score;
        private List<Card> cards;
        private int choice;

        public Player() {
        }

        public Player(IUser user) {
            userID = user.getLongID();
            cards = new ArrayList<>();
            cardsMessageID = Utilities4D4J.sendReactionUI(
                    Utilities4D4J.makeEmbed("Your CAH Hand", "Your hand will be dealt when the game starts!", false),
                    user.getOrCreatePMChannel(),
                    (m, u, e) -> {
                        if (score > -2) {
                            if (LEFT.equals(e)) {
                                choice--;
                                if (choice < 0) {
                                    choice = cards.size();
                                }
                            } else {
                                choice++;
                                if (choice >= cards.size()) {
                                    choice = 0;
                                }
                            }
                            AtomicInteger i = new AtomicInteger();
                            Utilities4D4J.edit(m, toEmbed());
                        }
                    }, LEFT, RIGHT
            ).getLongID();
        }

        public long getUserID() {
            return userID;
        }

        public IMessage getCardsMessage() {
            return RedBot.getClient().getUserByID(userID).getOrCreatePMChannel().getMessageByID(cardsMessageID);
        }

        public int getScore() {
            return score;
        }

        public void incrScore() {
            score++;
        }

        public void dealCard(Card c) {
            cards.add(c);
        }

        public List<Card> getCards() {
            return cards;
        }

        public void setChoice(int i) {
            choice = i;
        }

        public int getChoice() {
            return choice;
        }

        public Card getChoiceCard() {
            return choice < 0 ? null : cards.get(choice);
        }

        public Card removeChoiceCard() {
            return choice < 0 ? null : cards.remove(choice);
        }

        private EmbedObject toEmbed() {
            List<Card> c = getCards();
            String[] fields = new String[c.size() * 2];
            for (int i = 0; i < c.size(); i++) {
                fields[i * 2] = i == getChoice() ? "Card " + i : "**Card " + i + "**";
                fields[i * 2 + 1] = c.get(i).getText();
            }
            return Utilities4D4J.makeEmbed("Your CAH Hand", "", true, fields);
        }
    }

    private static class Card {
        private String text;
        private boolean isBlack;
        private boolean isCustomCard;

        public Card() {
        }

        public Card(String text, boolean isBlack, boolean isCustomCard) {
            this.isBlack = isBlack;
            this.text = text;
            this.isCustomCard = isCustomCard;
        }

        public boolean isBlack() {
            return isBlack;
        }

        public String getText() {
            return text;
        }

        public boolean isCustomCard() {
            return isCustomCard;
        }
    }
}
