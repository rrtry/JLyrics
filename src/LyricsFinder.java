import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// based on https://github.com/PedroHLC/ViewLyricsOpenSearcher
class LyricsFinder {

    private static final String API_URL 		  = "http://search.crintsoft.com/searchlyrics.htm";
    private static final String CLIENT_USER_AGENT = "MiniLyrics";
    private static final String CLIENT_TAG 		  = "client=\"ViewLyricsOpenSearcher\"";
    private static final String XML_HEADER 	   	  = "<?xml version='1.0' encoding='utf-8' ?><searchV1 artist=\"%s\" title=\"%s\" OnlyMatched=\"1\" %s/>";
    private static final String SEARCH_QUERY_PAGE = " RequestPage='%d'";

    private static final byte[] MAGIC_KEY = "Mlv1clt4.0".getBytes();

    public static String findLyrics(String artist, String title, boolean syncLyrics) throws IOException, NoSuchAlgorithmException {
        return searchQuery(
                String.format(XML_HEADER, artist, title, CLIENT_TAG +
                        String.format(SEARCH_QUERY_PAGE, 0)),
                syncLyrics
        );
    }

    private static String searchQuery(String searchQuery, boolean syncLyrics) throws IOException, NoSuchAlgorithmException {

        BufferedReader reader;
        HttpURLConnection urlConnection;

        urlConnection = (HttpURLConnection) new URL(API_URL).openConnection();
        urlConnection.setConnectTimeout(10 * 1000);
        urlConnection.setRequestProperty("User-Agent", CLIENT_USER_AGENT);
        urlConnection.setDoOutput(true);

        OutputStream out = urlConnection.getOutputStream();
        out.write(assembleQuery(searchQuery.getBytes(StandardCharsets.UTF_8)));
        out.close();

        if (urlConnection.getResponseCode() != 200) {
            System.err.println("An error occurred: " + urlConnection.getResponseCode());
            return "";
        }

        reader = new BufferedReader(
                new InputStreamReader(urlConnection.getInputStream(), "ISO_8859_1")
        );

        StringBuilder builder = new StringBuilder();
        char[] buffer 		  = new char[8192];

        int read;
        while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
            builder.append(buffer, 0, read);
        }
        reader.close();

        String lyrics = getLyricsURL(decryptResultXML(builder.toString()), syncLyrics);
        if (lyrics.isEmpty()) return lyrics;

        urlConnection = (HttpURLConnection) new URL(lyrics).openConnection();
        urlConnection.setRequestProperty("User-Agent", CLIENT_USER_AGENT);
        reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

        String lyricsLine;
        StringBuilder sb = new StringBuilder();

        while ((lyricsLine = reader.readLine()) != null)
            sb.append(lyricsLine).append("\n");

        reader.close();
        return sb.toString();
    }

    private static byte[] assembleQuery(byte[] value) throws NoSuchAlgorithmException, IOException {

        byte[] pog = new byte[value.length + MAGIC_KEY.length];
        System.arraycopy(value, 0, pog, 0, value.length);
        System.arraycopy(MAGIC_KEY, 0, pog, value.length, MAGIC_KEY.length);

        byte[] pog_md5 = MessageDigest.getInstance("MD5").digest(pog);

        int j = 0;
        for (byte b : value) {
            j += b;
        }

        int xor = (byte)(j / value.length);
        for (int m = 0; m < value.length; m++)
            value[m] = (byte) (xor ^ value[m]);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        out.write(0x02);
        out.write(xor);
        out.write(0x04);
        out.write(0x00);
        out.write(0x00);
        out.write(0x00);

        out.write(pog_md5);
        out.write(value);
        out.close();

        return out.toByteArray();
    }

    private static String decryptResultXML(String value){

        char key = value.charAt(1);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        for (int i = 22; i < value.length(); i++) {
            out.write((byte) (value.charAt(i) ^ key));
        }

        return out.toString();
    }

    private static String getLyricsURL(String result, boolean syncLyrics) {

        String[] parts          = result.split("\u0000");
        String lyricsExtension  = syncLyrics ? "lrc" : "txt";

        ArrayList<String> links = new ArrayList<>();

        String url  = "";
        String link = "";

        Pattern pattern = Pattern.compile("^(.+)\\/([^\\/]+)$");
        for (int i = 0; i < parts.length; i++) {

            String part     = parts[i];
            Matcher matcher = pattern.matcher(part);

            if (part.equals("server_url")) url = parts[++i];
            if (matcher.matches()) links.add(part);
        }

        if (url.isEmpty() || links.isEmpty()) {
            return "";
        }

        link = links.get(0);
        for (String path : links) {

            String[] pathParts = path.split("/");
            String fileName    = pathParts[pathParts.length - 1];

            if (!fileName.contains(".")) {
                continue;
            }

            int separatorIndex = fileName.lastIndexOf(".");
            String extension   = fileName.substring(separatorIndex + 1);

            if (extension.equals(lyricsExtension)) {
                link = path; break;
            }
        }
        return url + link;
    }
}
