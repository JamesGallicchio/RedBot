package com.thatredhead.redbot.command.impl;

import com.google.gson.reflect.TypeToken;
import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.helpers4d4j.Utilities4D4J;
import com.thatredhead.redbot.permission.PermissionContext;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReminderCommand extends Command {

    private List<RemindObj> reminders = RedBot.getDataHandler().get("reminders", new TypeToken<List<RemindObj>>(){}.getType(), new ArrayList<>());
    private void saveReminders() {
        RedBot.getDataHandler().save(reminders, "reminders");
    }

    private List<RemindObj> queued = RedBot.getDataHandler().get("reminder_queue", new TypeToken<List<RemindObj>>(){}.getType(), new ArrayList<RemindObj>());
    private void saveQueued() {
        RedBot.getDataHandler().save(queued, "reminder_queue");
    }

    private ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();

    public ReminderCommand() {
        super("reminder", "Sets a reminder in this channel",
                "reminder <in:<duration> | at:<time> [on:<date>]> [repeat:<duration|hourly|daily|weekly|monthly|yearly>] message:<message>",
                PermissionContext.BOT_OWNER);

        // Schedule any queued reminders
        queued.forEach(this::schedule);

        // Every minute, queue upcoming reminders
        ex.scheduleAtFixedRate(this::queueUpcoming, 0, 1, TimeUnit.MINUTES);
    }

    private void queueUpcoming() {
        long target = System.currentTimeMillis()/1000 + 61; // 61 just in case

        // Queue reminders in the next minute
        reminders.removeIf(r -> {
            if (r.t < target) {
                queued.add(r);
                schedule(r);
                return false;
            } else return true;
        });

        saveReminders();
        saveQueued();
    }

    private void schedule(RemindObj r) {
        ex.schedule(() -> {

            Utilities4D4J.sendMessage(r.m, RedBot.getClient().getChannelByID(r.c));
            queued.remove(r);

            if (r.repeats())
                addNew(r.next());

        }, r.t - System.currentTimeMillis()/1000, TimeUnit.SECONDS);
    }

    private void addNew(RemindObj r) {
        if (r.t < System.currentTimeMillis()/1000 + 61) {
            queued.add(r);
            schedule(r);

            saveQueued();
        } else {
            reminders.add(r);
            saveReminders();
        }
    }

    @Override
    public void invoke(MessageParser msgp) {
        addNew(new RemindObj(
                msgp.getChannel().getLongID(),
                msgp.getContentAfter(2),
                Instant.now().plusSeconds(Integer.parseInt(msgp.getArg(1))).getEpochSecond(),
                null,
                null
        ));
    }

    public static class RemindObj {;
        public final String m;
        public final long c;
        public final long t;
        public final Map<RepeatTime, Long> r;
        public final String z;

        public RemindObj(long channelID, String message, long time, Map<RepeatTime, Long> repeat, String zone) {
            c = channelID;
            t = time;
            m = message;
            r = repeat;
            z = zone;
        }

        public boolean repeats() {
            return r != null && z != null;
        }

        public RemindObj next() {
            ZonedDateTime time = Instant.ofEpochSecond(t).atZone(ZoneId.of(z, ZoneId.SHORT_IDS));
            for (Map.Entry<RepeatTime, Long> e : r.entrySet()) {
                switch(e.getKey()) {
                    case SECONDS: time = time.plusSeconds(e.getValue());
                    case MINUTES: time = time.plusMinutes(e.getValue());
                    case HOURS: time = time.plusHours(e.getValue());
                    case DAYS: time = time.plusDays(e.getValue());
                    case WEEKS: time = time.plusWeeks(e.getValue());
                    case MONTHS: time = time.plusMonths(e.getValue());
                    case YEARS: time = time.plusYears(e.getValue());
                }
            }
            return new RemindObj(c, m, time.toEpochSecond(), r, z);
        }

        @Override
        public int hashCode() {
            return Long.hashCode(t) ^ m.hashCode() ^ r.hashCode() ^ z.hashCode();
        }
    }

    public enum RepeatTime {
        SECONDS, MINUTES, HOURS, DAYS, WEEKS, MONTHS, YEARS
    }
}