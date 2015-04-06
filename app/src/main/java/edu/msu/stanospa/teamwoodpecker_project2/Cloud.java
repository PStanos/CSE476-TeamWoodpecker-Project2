package edu.msu.stanospa.teamwoodpecker_project2;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Cloud {
    private static final String MAGIC = "NechAtHa6RuzeR8x";
    private static final String LOGIN_URL = "FILL THIS IN";

    public boolean attemptLogin(final String userId, final String password) {

        // Create a get query
        String query = LOGIN_URL + "?user=" + userId + "&pw=" + password + "&magic=" + MAGIC;

        try {
            URL url = new URL(query);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return false;
            }

            // TODO: Parse XML for success

            return false;

        } catch (MalformedURLException e) {
            // Should never happen
            return false;
        } catch (IOException ex) {
            return false;
        }
    }
}
