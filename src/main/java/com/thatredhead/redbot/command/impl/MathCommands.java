package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.permission.PermissionContext;
import org.mariuszgromada.math.mxparser.Expression;

import java.util.Arrays;

public class MathCommands extends CommandGroup {

    public MathCommands() {
        super("Math Commands", "Commands to help with math related stuff", "math", Arrays.asList(new EvalCommand()));
    }

    public static class EvalCommand extends Command {

        public EvalCommand() {
            super("eval", "Evaluates a math expression", "eval <expression>", PermissionContext.EVERYONE);
        }

        @Override
        public void invoke(MessageParser msgp) {

            String math = msgp.getContentAfter(1);
            Expression e = new Expression(math);

            if (e.checkSyntax()) {
                msgp.reply("Evaluate", "", true,
                        "Input", "```" + e.getExpressionString() + "```",
                        "Output", "```\n= " + e.calculate() + "```");
            } else {
                msgp.reply("Evaluate", "", true,
                        "Input", "```" + e.getExpressionString() + "```",
                        "Output", "ERROR!\n" + e.getErrorMessage());
            }
        }
    }
}
