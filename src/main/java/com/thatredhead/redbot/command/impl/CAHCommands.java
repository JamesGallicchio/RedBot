package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandException;
import com.thatredhead.redbot.command.CommandGroup;
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
import java.util.stream.Collectors;

public class CAHCommands extends CommandGroup {

    public CAHCommands() {
        super("CAH Commands", "Commands for playing cards against humanity", "cah", Arrays.asList(new CAHCommand()));
    }

    public static class CAHCommand extends Command {
        public static final List<Card> WHITE_CARDS;
        public static final List<Card> BLACK_CARDS;
        public static final EmbedObject HELP_EMBED = Utilities4D4J.makeEmbed("Cards Against Humanity Help", "You can start a new game in any channel where the `cah` command is enabled using `cah create`. To join the game, each player should use `cah join`. When you're ready to start, use `cah start`.", false);
        public static final Emoji LEFT = EmojiManager.getForAlias("arrow_left");
        public static final Emoji RIGHT = EmojiManager.getForAlias("arrow_right");
        public static final Emoji CHOOSE = EmojiManager.getForAlias("white_check_mark");

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
            if (msgp.getArgCount() < 2) {
                Utilities4D4J.sendEmbed(HELP_EMBED, msgp.getChannel());
            } else {
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
                    } else if ("leave".equals(keyword)) {
                        if (g.isPlayer(msgp.getAuthor())) {
                            g.removePlayer(msgp.getAuthor());
                            msgp.reply("You've left the game!");
                        } else {
                            msgp.reply("You're not a player in this game!");
                        }
                    } else if ("end".equals(keyword)) {
                        if (msgp.getAuthor().getLongID() == g.getCzar()) {
                            g.getPlayers().forEach(p -> Utilities4D4J.removeReactionUI(p.getCardsMessage().getLongID()));
                            games.remove(msgp.getChannel().getLongID());
                            msgp.reply("__Ended the game! Final scores__\n" + scores(g));
                        } else {
                            msgp.reply("Only the current czar can end a game.");
                        }
                    } else if ("join".equals(keyword)) {
                        if (g.isPlayer(msgp.getAuthor())) {
                            msgp.reply("You're already in this game.");
                        } else if (g.getPlayers().size() >= g.maxPlayers) {
                            msgp.reply("The max number of players has been reached! Someone will need to leave before you can join.");
                        } else {
                            g.addPlayer(msgp.getAuthor());
                            msgp.reply("Successfully joined the game!");
                        }
                    } else if ("start".equals(keyword)) {
                        if (g.hasStarted()) {
                            msgp.reply("This game has already started!");
                        } else if (g.getPlayers().size() < g.minPlayers) {
                            msgp.reply("There aren't enough players to start! You need at least " + g.minPlayers + " to begin.");
                        } else {
                            takeTurn(msgp.getChannel(), g);
                            g.start();
                        }
                    } else {
                        msgp.reply("Unknown command " + msgp.getArg(1) + "! Use `cah` for help.");
                    }
                }
            }
        }

        private static void takeTurn(IChannel c, CAHGame g) {
            g.blackDiscard.add(g.currentBlack);
            g.currentBlack = g.getNextBlackCard();

            Utilities4D4J.sendEmbed(c, "Cards Against Humanity", "Current czar: <@" + g.getCzar() + ">", true, "Scores", scores(g), "Black Card", g.currentBlack.getText());
        }

        private void triggerUpdate(CAHGame g) {
            games.entrySet().stream().filter(e -> e.getValue().equals(g)).findFirst().ifPresent(e -> g.triggerUpdate(RedBot.getClient().getChannelByID(e.getKey())));
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
            public final int minPlayers;
            public final int maxPlayers;
            public final int handSize;
            public final int cardsToWin;
            private boolean isStarted;

            private LinkedList<Player> players;

            private List<Card> whiteDeck;
            private List<Card> whiteDiscard;
            private List<Card> blackDeck;
            private List<Card> blackDiscard;

            private Card currentBlack;
            private Map<Player, Card> submitted;

            private int selected;

            public CAHGame(int hands, int winCount, int minPlay, int maxPlay) {
                handSize = hands;
                cardsToWin = winCount;
                minPlayers = minPlay;
                maxPlayers = maxPlay;
                players = new LinkedList<>();
                whiteDeck = new ArrayList<>();
                whiteDiscard = new ArrayList<>();
                blackDeck = new ArrayList<>();
                blackDiscard = new ArrayList<>();
                whiteDiscard.addAll(WHITE_CARDS);
                blackDiscard.addAll(BLACK_CARDS);

                submitted = new HashMap<>();
            }

            public void addPlayer(IUser u) {
                if (isStarted) throw new IllegalStateException("Game already started!");
                if (isPlayer(u)) throw new IllegalStateException("Player already in the game!");

                Player p = new Player(u, this);
                for (int i = 0; i < handSize; i++) p.getCards().add(getNextWhiteCard());
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
                players.forEach(p -> {
                    p.choice = -1;
                    Utilities4D4J.edit(p.getCardsMessage(), p.toEmbed());
                });
            }

            public boolean hasStarted() {
                return isStarted;
            }

            public long getCzar() {
                return players.getFirst().userID;
            }

            public void chooseCard(long userID, Card choice) {
                if (!hasStarted())
                    throw new IllegalStateException("Game hasn't started yet!");
                if (userID == getCzar())
                    throw new IllegalArgumentException("That player is the czar!");

                Player p = getPlayer(userID).orElse(null);
                if (p == null)
                    throw new IllegalArgumentException("Given user isn't a player!");
                if (submitted.containsKey(p))
                    throw new IllegalArgumentException("Given user already submitted!");
                submitted.put(p, choice);
            }

            public boolean isWaiting() {
                return players.stream().anyMatch(p -> p.choice == -1 && p.getUserID() != getCzar());
            }

            public Map<Long, Card> getChosen() {
                return players.stream().collect(Collectors.toMap(Player::getUserID, Player::getChoiceCard));
            }

            public void triggerUpdate(IChannel c) {
                if (!isWaiting()) {
                    selected = -1;
                    Utilities4D4J.sendReactionUI(toEmbed(), c, (m, u, e) -> {
                        if (u.getLongID() == getCzar()) {
                            if (LEFT.equals(e)) {
                                selected--;
                                if (selected < 0) selected = submitted.size() - 1;
                            } else if (RIGHT.equals(e)) {
                                selected++;
                                if (selected >= submitted.size()) selected = 0;
                            } else {
                                czarChoose(submitted.keySet().toArray(new Player[0])[selected].getUserID());
                                Utilities4D4J.removeReactionUI(m.getLongID());
                                takeTurn(c, this);
                            }
                        }
                    }, LEFT, CHOOSE, RIGHT);
                }
            }

            private EmbedObject toEmbed() {
                String[] fields = new String[submitted.size() * 2];
                List<Card> c = new ArrayList<>(submitted.values());
                for (int i = 0; i < c.size(); i++) {
                    fields[i * 2] = i == selected ? "**Card " + (i + 1) + "**" : "" + (i + 1);
                    fields[i * 2 + 1] = i == selected ? "**" + c.get(i).getText() + "**" : c.get(i).getText();
                }
                return Utilities4D4J.makeEmbed("Cards Against Humanity", "Choose a card, <@" + getCzar() + ">!", true, fields);
            }

            public void czarChoose(long userID) {
                getPlayer(userID).orElseThrow(() -> new IllegalArgumentException("Given user ID doesn't correspond to a player!"))
                        .incrScore();
                whiteDiscard.addAll(submitted.values());
                submitted.clear();
                players.forEach(p -> {
                    if (p.getChoice() >= 0) {
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
            private long userID;
            private long cardsMessageID;
            private int score;
            private List<Card> cards;
            private int choice;
            private boolean submitted;
            private String submittedText;

            private CAHGame game;

            public Player() {
            }

            public Player(IUser user, CAHGame g) {
                userID = user.getLongID();
                cards = new ArrayList<>();
                choice = -2;
                game = g;
                cardsMessageID = Utilities4D4J.sendReactionUI(
                        toEmbed(),
                        user.getOrCreatePMChannel(),
                        (m, u, e) -> {
                            if (choice > -2 && !submitted) {
                                if (LEFT.equals(e)) {
                                    choice--;
                                    if (choice < 0) {
                                        choice = cards.size();
                                    }
                                } else if (RIGHT.equals(e)) {
                                    choice++;
                                    if (choice >= cards.size()) {
                                        choice = 0;
                                    }
                                } else {
                                    if (choice >= 0) {
                                        submittedText = getChoiceCard().getText();
                                        game.chooseCard(userID, removeChoiceCard());
                                        submitted = true;
                                        game.triggerUpdate(m.getChannel());
                                    }
                                }
                                Utilities4D4J.edit(m, toEmbed());
                            }
                        }, LEFT, CHOOSE, RIGHT
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

            public void deSubmit() {
                submitted = false;
            }

            private EmbedObject toEmbed() {
                if (choice < -1) {
                    return Utilities4D4J.makeEmbed("Your CAH Hand", "Your hand will be dealt when the game starts!", true);
                } else {
                    List<Card> c = getCards();
                    String[] fields = new String[c.size() * 2];
                    for (int i = 0; i < c.size(); i++) {
                        fields[i * 2] = i == getChoice() ? "**Card " + (i + 1) + "**" : "Card " + (i + 1);
                        fields[i * 2 + 1] = i == getChoice() ? "**" + c.get(i).getText() + "**" : c.get(i).getText();
                    }
                    return Utilities4D4J.makeEmbed("Your CAH Hand", "Black card: " + (game.currentBlack == null ? "" : game.currentBlack.getText()) + "\nSubmitted card: " + (submittedText == null ? "" : submittedText), true, fields);
                }
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
}