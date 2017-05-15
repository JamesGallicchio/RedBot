package com.thatredhead.redbot.command.impl;

import com.thatredhead.jmath.interp.MathInterpreter;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.permission.PermissionContext;

import java.util.Arrays;

public class MathCommands extends CommandGroup {

    public MathCommands() {
        super("Math Commands", "Commands to help with math related stuff", "math", Arrays.asList(new EvalCommand()));
    }

    public static class EvalCommand extends Command {

        public EvalCommand() {
            super("eval", "Evaluates a math expression", "eval <expression>", PermissionContext.BOT_OWNER);
        }

        @Override
        public void invoke(MessageParser msgp) {

            String math = msgp.getContentAfter(1);

            try {
                String result = new MathInterpreter(math).interpret().simplify().toString();

                msgp.reply("Evaluate", "", true,
                        "Input", "```" + math + "```",
                        "Output", "```" + result + "```");
            } catch (IllegalArgumentException e) {
                msgp.reply("Evaluate", "", true,
                        "Input", "```" + math + "```",
                        "Output", "ERROR! Cannot evaluate.");
            }
        }
    }
}