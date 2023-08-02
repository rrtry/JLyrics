import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class AbstractLyricsClient implements LyricsProvider {

    protected HttpURLConnection urlConnection;

    protected void connect(String url) throws APIError {
        try {

            urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setRequestProperty("User-Agent", getUserAgent());
            urlConnection.setDoOutput(true);

        } catch (IOException e) {
            disconnect(); throw new APIError("Could not connect to the service");
        }
    }

    public void disconnect() {
        if (urlConnection != null) urlConnection.disconnect();
    }
}
