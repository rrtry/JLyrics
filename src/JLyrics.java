import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class JLyrics {

    public static String getLyrics(String artist, String title, boolean sync) {
        try {

            return LyricsFinder.findLyrics(artist, title, sync);

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String[] getLyrics(String artist, String title) {
        try {

            return LyricsFinder.findLyrics(artist, title);

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return new String[] { "", "" };
        }
    }

    public static HashMap<Integer, String> getSyncLyrics(String artist, String title) {
        return LRCParser.parseSynchronisedLyrics(getLyrics(artist, title, true));
    }

    public static HashMap<Integer, String> getSyncLyrics(String lyrics) {
        return LRCParser.parseSynchronisedLyrics(lyrics);
    }

    public static HashMap<Integer, String> getSyncLyrics(File file) throws IOException {
        return LRCParser.parseSynchronisedLyrics(file);
    }

    public static void writeSyncLyrics(HashMap<Integer, String> lyrics, File out) throws IOException {
        LRCParser.writeSyncLyrics(lyrics, out);
    }
}
