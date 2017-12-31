package com.thatredhead.redbot.logging;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Hastebin {
    public static String ENDPOINT = "https://hastebin.com/documents";
    public static Pattern RESPONSE_PATTERN = Pattern.compile("\"key\":\"(.+)\"");

    public static String paste(String content) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(ENDPOINT).openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 RedBot");
            con.setRequestProperty("Content-Type", "text/plain");

            con.setDoOutput(true);
            con.setDoInput(true);

            PrintWriter wr = new PrintWriter(con.getOutputStream());
            wr.write(content);
            wr.flush();
            wr.close();

            String response = new BufferedReader(new InputStreamReader(con.getInputStream())).lines().collect(Collectors.joining());

            Matcher m = RESPONSE_PATTERN.matcher(response);

            if (m.find())
                return "http://hastebin.com/" + m.group(1);
        } catch (Exception ignored) {}
        return "";
    }
}
