import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class JLyrics {

    private LyricsProvider lyricsProvider;

    public JLyrics(LyricsProvider lyricsProvider) {
        this.lyricsProvider = lyricsProvider;
    }

    public String getLyrics(String artist, String title, boolean sync) throws APIError {
        return lyricsProvider.findLyrics(artist, title, true);
    }

    public String[] getLyrics(String artist, String title) throws APIError {
        return lyricsProvider.findLyrics(artist, title);
    }

    public HashMap<Integer, String> getSyncLyrics(String artist, String title) throws APIError {
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
