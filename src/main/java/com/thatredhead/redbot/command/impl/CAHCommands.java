package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.command.CommandGroup;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class CAHCommands extends CommandGroup{
    public static final List<Card> WHITE_CARDS;
    public static final List<Card> BLACK_CARDS;

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

    private Map<IChannel, CAHGame> games;

    public CAHCommands() {
        super("Cards Against Humanity", "For playing games of CAH!",
                "cah", Arrays.asList());
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
            blackDeck = new ArrayList<>();
            blackDeck.addAll(BLACK_CARDS);
        }

        public void addPlayer(IUser u) {
            if (isStarted) throw new IllegalStateException("Game already started!");
            if (isPlayer(u)) throw new IllegalStateException("Player already in the game!");

            Player p = new Player(u);
            for (int i= 0; i < handSize; i++) p.getCards().add(getNextWhiteCard());
            players.addLast(p);
        }

        public void removePlayer(IUser u) {
            if (players.getFirst().getUserID() == u.getLongID()) throw new IllegalStateException("Czar can't leave the game during their turn!");
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

        public void start() {
            if (players.size() < minPlayers || players.size() > maxPlayers) throw new IllegalStateException("Player count is not within range!");
            if (isStarted) throw new IllegalStateException("Already started!");
            isStarted = true;
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
            return players.stream().collect(Collectors.toMap(Player::getUserID, Player::getChoice));
        }

        public void czarChoose(long userID) {
            getPlayer(userID).orElseThrow(() -> new IllegalArgumentException("Given user ID doesn't correspond to a player!"))
                    .incrScore();
            players.forEach(p -> {
                if (p.getChoice() != null) {
                    whiteDiscard.add(p.getChoice());
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
            return whiteDeck.remove(whiteDeck.size()-1);
        }

        private Card getNextBlackCard() {
            if (blackDeck.isEmpty()) {
                while (!blackDiscard.isEmpty()) {
                    blackDeck.add(blackDiscard.remove((int) (blackDiscard.size() * Math.random())));
                }
            }
            return blackDeck.remove(blackDeck.size()-1);
        }
    }

    private static class Player {
        private long userID;
        private int score;
        private List<Card> cards;
        private int choice;

        public Player() {}

        public Player(IUser u) {
            userID = u.getLongID();
            cards = new ArrayList<>();
        }

        public long getUserID() {
            return userID;
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

        public Card getChoice() {
            return choice < 0 ? null : cards.get(choice);
        }
    }

    private static class Card {
        private String text;
        private boolean isBlack;
        private boolean isCustomCard;

        public Card() {}

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