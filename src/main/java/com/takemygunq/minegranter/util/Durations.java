package com.takemygunq.minegranter.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Durations like "90s", "2m", "1h 30m" — parsing for the config and formatting for players. */
public final class Durations {

    private static final Pattern PART = Pattern.compile("(\\d+)\\s*([a-z]+)");

    private Durations() {
    }

    /** Seconds in a duration string; units: s, m/min, h, d (and their full names). Unknown units are ignored. */
    public static long parseSeconds(String text) {
        long total = 0;
        Matcher m = PART.matcher(text.toLowerCase(Locale.ROOT));
        while (m.find()) {
            long n = Long.parseLong(m.group(1));
            total += switch (m.group(2)) {
                case "s", "sec", "second", "seconds" -> n;
                case "m", "min", "minute", "minutes" -> n * 60;
                case "h", "hour", "hours" -> n * 3600;
                case "d", "day", "days" -> n * 86400;
                default -> 0;
            };
        }
        return total;
    }

    /** "2 hours 5 minutes 1 second"; anything under a second is shown as "1 second". */
    public static String format(long seconds) {
        if (seconds < 1) return "1 second";
        long d = seconds / 86400, h = seconds % 86400 / 3600, m = seconds % 3600 / 60, s = seconds % 60;
        StringBuilder out = new StringBuilder();
        append(out, d, "day");
        append(out, h, "hour");
        append(out, m, "minute");
        append(out, s, "second");
        return out.toString();
    }

    private static void append(StringBuilder out, long n, String unit) {
        if (n == 0) return;
        if (!out.isEmpty()) out.append(' ');
        out.append(n).append(' ').append(unit).append(n == 1 ? "" : "s");
    }
}
