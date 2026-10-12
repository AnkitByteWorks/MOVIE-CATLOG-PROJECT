package com.movietracker.watchlog.util;

/**
 * Utility for formatting minutes into human-friendly watch duration strings.
 */
public final class DurationFormatter {

    private DurationFormatter() {
    }

    /**
     * Formats total minutes into a compact representation like "14d 6h 30m", "5h 15m", or "45m".
     *
     * @param totalMinutes total minutes elapsed
     * @return formatted duration string
     */
    public static String formatMinutes(long totalMinutes) {
        if (totalMinutes <= 0) {
            return "0m";
        }

        long days = totalMinutes / (24 * 60);
        long hours = (totalMinutes % (24 * 60)) / 60;
        long minutes = totalMinutes % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0 || days > 0) {
            sb.append(hours).append("h ");
        }
        sb.append(minutes).append("m");

        return sb.toString().trim();
    }
}
