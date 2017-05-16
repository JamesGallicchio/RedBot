package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.helpers4d4j.Utilities4D4J;
import com.thatredhead.redbot.permission.PermissionContext;
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmDaemonLocalEvalScriptEngineFactory;
import sx.blah.discord.util.EmbedBuilder;

import javax.script.ScriptException;
import java.util.ArrayList;

public class EvalCommands extends CommandGroup {
    public EvalCommands() {
        super("eval", "evaluates a kotlin script", "developer", new ArrayList<>());
    }


    public static class Eval extends Command {
        private KotlinJsr223JvmDaemonLocalEvalScriptEngineFactory factory = new KotlinJsr223JvmDaemonLocalEvalScriptEngineFactory();

        public Eval(String keyword, String description, PermissionContext defaultPerm) {
            super(keyword, description, PermissionContext.BOT_OWNER);
        }

        @Override
        public void invoke(MessageParser msgp) {
            String content = msgp.getContentAfter(1);

            Object o = null;
            try {
                factory.getScriptEngine().eval(content.replace("`", ""));
            } catch (ScriptException e) {
                EmbedBuilder em = new EmbedBuilder();
                em.withAuthorName("Failure!");
                em.withDesc(e.getMessage());
                Utilities4D4J.sendEmbed(em.build(), msgp.getChannel());
                return;
            }
            EmbedBuilder em = new EmbedBuilder();
            em.withAuthorName("Success!!");
            em.withDesc(o == null ? "No errors." : o.toString());
            Utilities4D4J.sendEmbed(em.build(), msgp.getChannel());
            return;
        }
    }
}
