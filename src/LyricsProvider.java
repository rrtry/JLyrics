public interface LyricsProvider {

    boolean isSyncLyrics();

    String findLyrics(String artist, String title, boolean syncLyrics) throws APIError;
    String[] findLyrics(String artist, String title) throws APIError;

    String getUserAgent();
}
