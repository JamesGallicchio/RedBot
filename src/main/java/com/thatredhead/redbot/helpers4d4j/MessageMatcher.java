package com.thatredhead.redbot.helpers4d4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageMatcher {

    public enum MessageToken {
        USER_MENTION,
        ROLE_MENTION,
        CHANNEL_MENTION,
        TEXT,
        ONE_OR_MORE,
        OPTIONAL;

        private static final Pattern CHNLP = Pattern.compile("<#(\\d+)>");
        private static final Pattern USERP = Pattern.compile("<@(\\d+)>");
        private static final Pattern ROLEP = Pattern.compile("<@&(\\d+)>");

        public static MessageToken[] compile(String pattern) {
            MessageToken[] compiled = new MessageToken[pattern.length()];

            for(int idx = 0; idx < pattern.length(); idx++)
                switch(pattern.charAt(idx)) {
                    case 'u':
                    case 'U':
                        compiled[idx] = USER_MENTION; break;
                    case 'r':
                    case 'R':
                        compiled[idx] = ROLE_MENTION; break;
                    case 'c':
                    case 'C':
                        compiled[idx] = CHANNEL_MENTION; break;
                    case 't':
                    case 'T':
                    case 'w':
                    case 'W':
                        compiled[idx] = TEXT; break;
                    case '+':
                        compiled[idx] = ONE_OR_MORE; break;
                    case '?':
                        compiled[idx] = OPTIONAL; break;
                    default:
                        throw new IllegalArgumentException();
                }

            if(compiled[0] == ONE_OR_MORE || compiled[0] == OPTIONAL)
                throw new IllegalArgumentException();

            MessageToken last = OPTIONAL;
            for(MessageToken token : compiled) {
                if ((token == ONE_OR_MORE || token == OPTIONAL) && last == token)
                    throw new IllegalArgumentException();
                last = token;
            }

            return compiled;
        }

        public boolean matches(String[] words, int idx) {
            switch(this) {
                case USER_MENTION:
                    Matcher mu = USERP.matcher(words[idx]);
                    if(mu.find()) {
                        words[idx] = mu.group(1);
                        return true;
                    }
                    return false;
                case ROLE_MENTION:
                    Matcher mr = ROLEP.matcher(words[idx]);
                    if(mr.find()) {
                        words[idx] = mr.group(1);
                        return true;
                    }
                    return false;
                case CHANNEL_MENTION:
                    Matcher mc = CHNLP.matcher(words[idx]);
                    if(mc.find()) {
                        words[idx] = mc.group(1);
                        return true;
                    }
                    return false;
                case TEXT: return !(USER_MENTION.matches(words, idx) || ROLE_MENTION.matches(words, idx) || CHANNEL_MENTION.matches(words, idx));
                default: return false;
            }
        }
    }

    private String[] words;
    private MessageToken[] tokens;

    List<String[]> tokenWords;

    public MessageMatcher(String[] words, String pattern) {
        this.words = words;
        tokens = MessageToken.compile(pattern);
    }

    public boolean match() {
        tokenWords = new ArrayList<>();

        int tokenIdx = 0, startWordIdx = 0, endWordIdx = 0;
        MessageToken last = tokens[0];
        while(tokenIdx++ < tokens.length && endWordIdx < words.length) {

            if (!last.matches(words, startWordIdx)) {
                if(tokens[tokenIdx++] == MessageToken.OPTIONAL ||
                        tokens[tokenIdx++] == MessageToken.OPTIONAL)
                    tokenWords.add(null);
                else return false;

            } else {

                if (tokens[tokenIdx] == MessageToken.ONE_OR_MORE) {
                    tokenIdx++;
                    while (endWordIdx < words.length && last.matches(words, endWordIdx)) endWordIdx++;
                }

                tokenWords.add(Arrays.copyOfRange(words, startWordIdx, endWordIdx));

                startWordIdx = endWordIdx;

                last = tokens[tokenIdx];
            }
        }

        if(tokenIdx == tokens.length) return true;

        while(++tokenIdx < tokens.length) {
            if(tokens[tokenIdx] == MessageToken.OPTIONAL ||
                    ++tokenIdx < tokens.length && tokens[tokenIdx] == MessageToken.OPTIONAL)
                tokenWords.add(new String[0]);
            else return false;
        }

        return true;
    }

    public String[] get(int index) {
        return tokenWords.get(index);
    }
}
