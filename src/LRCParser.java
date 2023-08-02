import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class LRCParser {

    public static void writeSyncLyrics(HashMap<Integer, String> lyrics, File out) throws IOException {
        SortedSet<Integer> keys = new TreeSet<>(lyrics.keySet());
        try (FileWriter fileWriter = new FileWriter(out)) {
            for (Integer timestamp : keys) {

                int minutes = timestamp / 1000 / 60;
                int seconds = (timestamp / 1000) % 60;
                int millis  = timestamp % 1000;

                String line = String.format("[%02d:%02d.%02d]%s", minutes, seconds, millis, lyrics.get(timestamp));
                if (line.charAt(line.length() - 1) != '\n') line += '\n';
                fileWriter.write(line);
            }
        }
    }

    public static HashMap<Integer, String> parseSynchronisedLyrics(File file) throws IOException {
        try (FileInputStream in = new FileInputStream(file)) {

            byte[] buff = new byte[in.available()];
            in.read(buff);
            in.close();

            return parseSynchronisedLyrics(new String(buff, StandardCharsets.UTF_8));
        }
    }

    public static HashMap<Integer, String> parseSynchronisedLyrics(String lyrics) {

        String[] lines = lyrics.split("\n");
        HashMap<Integer, String> syncedLyrics = new HashMap<>();

        for (String line : lines) {

            if (line.isEmpty()) {
                continue;
            }

            Pattern pattern = Pattern.compile("(\\[\\d\\d\\:\\d\\d\\.\\d+?(?=\\])\\]+)|(\\[\\d\\d\\:\\d+?(?=\\])\\]+)");
            Matcher matcher = pattern.matcher(line);

            ArrayList<String> matches = new ArrayList<>();
            int endIndex = 0;

            while (matcher.find()) {
                matches.add(matcher.group());
                endIndex = Math.max(endIndex, matcher.end());
            }

            String lyricsLine = line.substring(endIndex);
            for (String match : matches) {

                String timeString = match.replace("[", "")
                        .replace("]", "");

                int minutes = 0;
                int seconds = 0;
                int millis  = 0;

                boolean hasMillis = timeString.contains(".");
                String regex      = hasMillis ? "[\\.:]" : ":";
                String[] parts    = timeString.split(regex);

                minutes = Integer.parseInt(parts[0]);
                seconds = Integer.parseInt(parts[1]);

                if (hasMillis) millis = Integer.parseInt(parts[2]);
                int timestamp = minutes * 60 * 1000 + seconds * 1000 + millis;
                syncedLyrics.put(timestamp, lyricsLine);
            }
        }
        return syncedLyrics;
    }
}
