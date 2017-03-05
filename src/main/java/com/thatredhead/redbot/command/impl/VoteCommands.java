package com.thatredhead.redbot.command.impl;

import com.google.gson.reflect.TypeToken;
import com.thatredhead.redbot.DiscordUtils;
import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.*;
import com.thatredhead.redbot.permission.PermissionContext;
import org.apache.commons.collections4.set.ListOrderedSet;
import sx.blah.discord.handle.obj.IUser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

;

public class VoteCommands extends CommandGroup {

    private Map<String, Vote> votes;

    public VoteCommands() {
        super("Vote Commands", "Commands to hold votes in Discord channels", "vote", null);
        commands = Arrays.asList(new VoteCommand(), new NewVoteCommand(), new EndVoteCommand());
        votes = RedBot.getDataHandler().get("votes", new TypeToken<Map<String, Vote>>(){}.getType(), new HashMap<>());
    }

    public class VoteCommand extends Command {

        public VoteCommand() {
            super("vote", "Enters a ballot for the ongoing vote in this channel.", "vote <first pick [second pick [third...]]>");
        }

        @Override
        public PermissionContext getDefaultPermissions() {
            return PermissionContext.getNobodyContext();
        }

        @Override
        public void invoke(MessageParser msgp) throws CommandException {
            if (votes.containsKey(msgp.getChannel().getID()))
                votes.get(msgp.getChannel().getID()).castBallot(msgp);
            else
                msgp.reply("There's no active vote in this channel.");
        }
    }

    public class NewVoteCommand extends Command {

        public NewVoteCommand() {
            super("newvote", "Creates a new vote", "newvote <description> {<option 1>, <option 2>, ...}");
        }

        @Override
        public PermissionContext getDefaultPermissions() {
            return PermissionContext.getNobodyContext();
        }

        @Override
        public void invoke(MessageParser msgp) throws CommandException {
            Vote current = votes.get(msgp.getChannel().getID());
            if (current != null && !current.isDone())
                msgp.reply("Please end the current vote first!");
            else {
                votes.put(msgp.getChannel().getID(), new Vote(msgp));
            }
        }
    }

    public class EndVoteCommand extends Command {

        public EndVoteCommand() {
            super("endvote", "Ends the current vote in this channel.");
        }

        @Override
        public PermissionContext getDefaultPermissions() {
            return PermissionContext.getNobodyContext();
        }

        @Override
        public void invoke(MessageParser msgp) throws CommandException {
            Vote current = votes.get(msgp.getChannel().getID());
            if (current == null) msgp.reply("There is no vote in this channel to end.");
            else if (current.isDone()) msgp.reply("This channel's vote has already ended.");
            else msgp.reply(current.end());
        }
    }
}

class Vote {

    private String messageId;

    private String description;
    private List<String> options;
    private int winCount;
    private boolean done;

    private List<Ballot> ballots;

    private static final Pattern NEW_VOTE = Pattern.compile("(.+)\\{(.+)}(\\d+)?");

    public Vote(MessageParser msgp) {

        if (msgp.getArgCount() < 1) throw new CommandArgumentException(1, "", "See usage.");
        Matcher m = NEW_VOTE.matcher(msgp.getContentAfter(1));
        if (m.find()) {
            description = m.group(1);
            options = Arrays.stream(m.group(2).split(","))
                    .map(String::trim)
                    .distinct()
                    .collect(Collectors.toList());
            winCount = m.group(3) == null ? 1 : Integer.parseInt(m.group(3));
        } else throw new CommandException();

        ballots = new ArrayList<>();
        done = false;

        messageId = DiscordUtils.sendMessage(toString(), msgp.getChannel()).get().getID();
    }

    public void castBallot(MessageParser msgp) {
        if (done) {
            msgp.reply("This vote is already complete.");
            return;
        }
        try {
            Set<Integer> choices = new ListOrderedSet<Integer>();
            String[] args = msgp.getArgs();
            for(int i = 1; i < args.length; i++) {
                int choice = Integer.parseInt(args[i]);
                if (options.size() < choice || 1 > choice) throw new NumberFormatException();
                choices.add(choice);
            }
            Ballot ballot = new Ballot(msgp.getAuthor(), choices);
            if(ballots.contains(ballot)) {
                ballots.remove(ballot);
                msgp.reply("Vote updated!");
            } else
                msgp.reply("Vote entered :ok_hand:");
            ballots.add(ballot);
            updateResult();
        } catch (NumberFormatException e) {
            msgp.reply("Your choice(s) must be between 1 and " + options.size());
        }
    }

    public boolean isDone() {
        return done;
    }

    public String end() {
        done = true;
        updateResult();
        return "Verdict: " + getWinnersString();
    }

    public String getWinnersString() {
        if (!isDone())
            return "";

        int[] winners = getWinners();

        StringBuilder result = new StringBuilder();
        for(int winner: winners) {
            result.append(options.get(winner)).append(", ");
        }

        return result.delete(result.length()-2, result.length()).toString();
    }

    public int[] getWinners() {
        if(!isDone())
            return new int[]{};

        List<SimpleBallot> ballots = this.ballots.stream().map(SimpleBallot::new).collect(Collectors.toList());

        int quota = (int) (ballots.size()/(winCount+1.0))+1;

        int[] winners = new int[options.size()];
        int winnersSoFar = 0;

        while(winnersSoFar < winCount) {

            boolean done = false;
            while(!done) {
                done = true;

                Map<Integer, List<SimpleBallot>> votes = ballots.stream().collect(Collectors.groupingBy(SimpleBallot::choice));
                for(Map.Entry<Integer, List<SimpleBallot>> entry: votes.entrySet()) {

                    List<SimpleBallot> choiceVotes = entry.getValue();
                    double totalVotes = choiceVotes.stream().mapToDouble(it -> it.value).sum();
                    if (totalVotes >= quota) {
                        done = false;

                        winners[winnersSoFar++] = entry.getKey()-1;

                        List<SimpleBallot> nextBallots = entry.getValue().stream().filter(SimpleBallot::nextChoice).collect(Collectors.toList());

                        double value = (totalVotes-quota)/nextBallots.size();

                        nextBallots.forEach(it -> it.value *= value);

                        ballots = ballots.stream().filter(it -> it.removeChoice(entry.getKey())).collect(Collectors.toList());
                    }
                }
            }

            if(winnersSoFar >= winCount)
                break;

            Map<Integer, Integer> votes = ballots.stream().collect(Collectors.groupingBy(SimpleBallot::choice)).entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, it -> it.getValue().size()));

            List<Integer> mins = new ArrayList<>();
            mins.add(0);
            int minVotes = votes.get(0);
            for (Map.Entry<Integer, Integer> entry: votes.entrySet())
                if(entry.getValue() == minVotes)
                    mins.add(entry.getKey());
                else if(entry.getValue() < minVotes) {
                    mins.clear();
                    mins.add(entry.getKey());
                    minVotes = entry.getValue();
                }

            ballots = ballots.stream().filter(it -> mins.contains(it.choice())).filter(SimpleBallot::nextChoice).collect(Collectors.toList());

            ballots = ballots.stream().filter(it -> {
                boolean ok = true;
                for(int i: mins)
                    ok = ok && it.removeChoice(i);
                return ok;
            }).collect(Collectors.toList());
        }

        return Arrays.copyOf(winners, winCount);

        /*

        List<Ballot> ballots

        Map<Choice, Integer> votes

        while not enough winners
            while winners could happen
                count first choices

                for winners
                    move them to winner list
                    remove winner from all ballots
                    value of winner's ballots is surplus/(votes with a second choice)*previous value

            for the loser(s)
                remove them from all ballots

        */

    }

    public void updateResult() {
        DiscordUtils.edit(messageId, this.toString());
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append('[').append(done ? "DONE" : "CURRENT").append("] ").append(description);

        for (int i = 1; i <= options.size(); i++)
            result.append("\n  [").append(i).append("]: ").append(options.get(i-1));

        result.append("\nVERDICT: ").append(getWinnersString());

        return result.toString();
    }
}

class Ballot {

    private String userID;
    private Set<Integer> choices;

    private transient IUser user;

    public Ballot(Ballot copy) {
        this.userID = copy.userID;
        (this.choices = new ListOrderedSet<>()).addAll(copy.choices);
        this.user = copy.user;
    }

    public Ballot(IUser user, Set<Integer> choices) {
        this.user = user;
        userID = user.getID();
        this.choices = choices;
    }

    public IUser getUser() {
        return user == null ? RedBot.getClient().getUserByID(userID) : user;
    }

    public Set<Integer> getChoices() {
        return choices;
    }

    public void setChoices(Set<Integer> choices) {
        this.choices = choices;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof Ballot && this.userID.equals(((Ballot) o).userID);
    }

    public Ballot copy() {
        return new Ballot(this);
    }
}

class SimpleBallot {

    int[] choices;
    double value;

    public SimpleBallot(Ballot ballot) {
        choices = ballot.getChoices().stream().mapToInt(v -> v).toArray();
        value = 1.0;
    }

    public int choice() {
        return choices[0];
    }

    public boolean nextChoice() {
        if(choices.length == 1) return false;
        choices = Arrays.copyOfRange(choices, 1, choices.length);
        return true;
    }

    public boolean removeChoice(int choice) {
        int count = 0;
        for(int i = 0; i < choices.length; i++)
            if(choices[i] == choice) {
                count++;
                int j = i;
                while(j < choices.length && choices[j] == -1) j++;
                if(j == choices.length) {
                    count--;
                    break;
                }
                choices[i] = choices[j];
                choices[j] = -1;
            }

        choices = Arrays.copyOf(choices, choices.length-count);
        return choices.length != 0;
    }
}