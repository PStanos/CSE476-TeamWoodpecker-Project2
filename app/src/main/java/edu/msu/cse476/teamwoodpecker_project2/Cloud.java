package edu.msu.cse476.teamwoodpecker_project2;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Xml;
import android.view.View;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Cloud {

    /*
    * Used as a response to creating a new game
     */
    public class NewGameResponse {
        public NewGameResponse(boolean conn, String u1, String u2) {
            connected = conn;
            userName1 = u1;
            userName2 = u2;
        }

        /**
         * True if the user is connected to the game
         */
        private boolean connected;

        public boolean isConnected() {
            return connected;
        }

        /**
         * The name of player 1; null if no game
         */
        private String userName1;

        public String getUserName1() {
            return userName1;
        }

        /**
         * The name of player 2; null if no game
         */
        private String userName2;

        public String getUserName2() {
            return userName2;
        }
    }

    // Connection URLs
    private static final String LOGIN_URL = "http://webdev.cse.msu.edu/~chiversb/cse476/proj02/login.php";
    private static final String CREATE_USER_URL = "http://webdev.cse.msu.edu/~chiversb/cse476/proj02/newuser.php";
    private static final String WAIT_GAME_URL = "http://webdev.cse.msu.edu/~chiversb/cse476/proj02/waitgame.php";
    private static final String UPDATE_GAME_URL = "http://webdev.cse.msu.edu/~chiversb/cse476/proj02/updategame.php";
    private static final String GET_GAME_DATA_URL = "http://webdev.cse.msu.edu/~chiversb/cse476/proj02/lobbyscript.php";
    private static final String DELETE_URL = "http://webdev.cse.msu.edu/~chiversb/cse476/proj02/deletegame.php";
    private static final String FETCH_GAME_URL = "http://webdev.cse.msu.edu/~chiversb/cse476/proj02/fetchxml.php";

    /**
     * Attempt to log in to the server
     * @param view The view that caused the login attempt
     * @param userId The ID of the user attempting to log in
     * @param password The password of the user attempting to log in
     * @return true if the user succeeded in logging in; false otherwise
     */
    public boolean attemptLogin(final View view, String userId, String password) {

        String query = LOGIN_URL + "?user=" + userId + "&pw=" + password;

        try {
            // Adapted from http://stackoverflow.com/questions/10863030/detecting-if-android-is-connected-to-internet
            ConnectivityManager manager = (ConnectivityManager)view.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = manager.getActiveNetworkInfo();

            if(activeNetwork == null) {

                view.post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(view.getContext(), R.string.network_action_failed, Toast.LENGTH_SHORT).show();
                    }
                });

                return false;
            }

            URL url = new URL(query);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return false;
            }

            XmlPullParser xml = Xml.newPullParser();
            xml.setInput(conn.getInputStream(), "UTF-8");

            xml.nextTag();
            xml.require(XmlPullParser.START_TAG, null, "game");
            String status = xml.getAttributeValue(null, "status");

            if(status.equals("yes")) {
                return true;
            }
            else {
                String msg = xml.getAttributeValue(null, "msg");
                if(msg.equals("user error")) {
                    view.post(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(view.getContext(), R.string.user_does_not_exist_toast, Toast.LENGTH_SHORT).show();
                        }

                    });
                }
                else if(msg.equals("password error")) {
                    view.post(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(view.getContext(), R.string.incorrect_password_toast, Toast.LENGTH_SHORT).show();
                        }

                    });
                }
                else {
                    view.post(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(view.getContext(), R.string.login_failed_generic_toast, Toast.LENGTH_SHORT).show();
                        }

                    });
                }
            }

            return false;

        } catch (MalformedURLException ex) {
            return false;
        } catch(XmlPullParserException ex) {
            return false;
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * Attempt to create a new user
     * @param view The view that caused the create user attempt
     * @param userName The desired name of the new user
     * @param password The desired password of the new user
     * @return true if the new user was created successfully; false otherwise
     */
    public boolean createUser(final View view, String userName, String password) {

        String query = CREATE_USER_URL + "?user=" + userName + "&pw=" + password;

        try {
            URL url = new URL(query);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return false;
            }

            XmlPullParser xml = Xml.newPullParser();
            xml.setInput(conn.getInputStream(), "UTF-8");

            xml.nextTag();
            xml.require(XmlPullParser.START_TAG, null, "game");
            String status = xml.getAttributeValue(null, "status");

            if(status.equals("yes")) {
                return true;
            }
            else {
                String msg = xml.getAttributeValue(null, "msg");
                if(msg.equals("user error")) {
                    view.post(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(view.getContext(), R.string.user_exists_toast, Toast.LENGTH_SHORT).show();
                        }

                    });
                }
                else {
                    view.post(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(view.getContext(), R.string.create_user_failed_generic_toast, Toast.LENGTH_SHORT).show();
                        }

                    });
                }
            }

            return false;

        } catch (MalformedURLException ex) {
            return false;
        } catch(XmlPullParserException ex) {
            return false;
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * Fetch the user's current game
     * @param context The context asking for the user's game
     * @param userName The name of the user to fetch the game for
     * @param password The password of the user to fetch the game for
     * @return the user's current game; null if the user is not part of any game
     */
    public Game userGame(Context context, String userName, String password) {
        String query = FETCH_GAME_URL + "?user=" + userName + "&pw=" + password;

        try {
            URL url = new URL(query);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return null;
            }

            XmlPullParser xml = Xml.newPullParser();
            xml.setInput(conn.getInputStream(), "UTF-8");

            xml.nextTag();

            if(xml.getEventType() == XmlPullParser.START_TAG) {
                boolean parseGame;

                try {
                    xml.require(XmlPullParser.START_TAG, null, "game_data");
                    parseGame = true;
                }
                catch (XmlPullParserException ex) {
                    parseGame = false;
                }

                if(parseGame) {
                    return parseGameXML(context, xml);
                }
                else {
                    return null;
                }
            }
            else {
                return null;
            }

        } catch (MalformedURLException ex) {
            return null;
        } catch(XmlPullParserException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        }

    }

    /**
     * Wait to join a new game
     * @param userName The name of the user that is waiting for the game
     * @param password The password of the user that is waiting for the game
     * @return a response indicating whether a new game was started, and the names of the two players that are in the game
     */
    public NewGameResponse waitForGame(String userName, String password) {
        String query = GET_GAME_DATA_URL + "?user=" + userName + "&pw=" + password;

        while(true) {
            try {
                URL url = new URL(query);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    return new NewGameResponse(false, null, null);
                }

                XmlPullParser xml = Xml.newPullParser();
                xml.setInput(conn.getInputStream(), "UTF-8");

                xml.nextTag();
                xml.require(XmlPullParser.START_TAG, null, "game");
                String status = xml.getAttributeValue(null, "status");

                if(status.equals("yes")) {
                    return new NewGameResponse(true, xml.getAttributeValue(null, "p1"), xml.getAttributeValue(null, "p2"));
                }
                else if(xml.getAttributeValue(null, "msg").equals("New game failure")) {
                    return new NewGameResponse(false, null, null);
                }
            } catch (MalformedURLException ex) {
                return new NewGameResponse(false, null, null);
            } catch(XmlPullParserException ex) {
                return new NewGameResponse(false, null, null);
            } catch (IOException ex) {
                return new NewGameResponse(false, null, null);
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                break;
            }
        }

        // Thread interrupted
        return new NewGameResponse(false, null, null);
    }

    /**
     * Wait on the opponent to post updated data
     * @param context The context that caused the wait
     * @param userName The name of the user that is waiting on the other player
     * @param password The password of the user that is waiting for the other player
     * @return the updated game data; null if a problem occured
     */
    public Game waitOnOpponent(final Context context, String userName, String password) throws InterruptedException {
        String query = WAIT_GAME_URL + "?user=" + userName + "&pw=" + password;

        while(true) {
            try {
                // Adapted from http://stackoverflow.com/questions/10863030/detecting-if-android-is-connected-to-internet
                ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = manager.getActiveNetworkInfo();

                if(activeNetwork == null) {
                    Thread.sleep(3000);

                    continue;
                }

                URL url = new URL(query);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    return null;
                }

                XmlPullParser xml = Xml.newPullParser();
                xml.setInput(conn.getInputStream(), "UTF-8");

                xml.nextTag();

                if(xml.getEventType() == XmlPullParser.START_TAG) {
                    boolean parseGame = false;

                    try {
                        xml.require(XmlPullParser.START_TAG, null, "game_data");
                        parseGame = true;
                    }
                    catch (XmlPullParserException ex) {
                        xml.require(XmlPullParser.START_TAG, null, "game");
                        String msg = xml.getAttributeValue(null, "msg");

                        if(msg.equals("Game not found")) {
                            return null;
                        }
                    }

                    if(parseGame) {
                        return parseGameXML(context, xml);
                    }
                }
                else {
                    return null;
                }
            } catch (MalformedURLException ex) {
                return null;
            } catch(XmlPullParserException ex) {
                return null;
            } catch (IOException ex) {
                return null;
            }

            Thread.sleep(3000);
        }
    }

    /**
     * Submit updated game data to the server
     * @param game The game data to submit
     * @param userName The name of the user that is submitting the data
     * @param password The password of the user that is submitting the data
     * @return true if the data was submitted; false otherwise
     */
    public boolean submitUpdatedGame(Game game, String userName, String password) {
        try {
            URL url = new URL(UPDATE_GAME_URL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            String postStr = "xml=" + generateXml(game) + "&user=" + userName + "&pw=" + password;

            byte[] data = postStr.getBytes();

            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Length", Integer.toString(data.length));
            conn.setUseCaches(false);

            OutputStream out = conn.getOutputStream();
            out.write(data);
            out.close();

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return false;
            }

            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * Generate the XML for a game
     * @param game The game to generate XML for
     * @return the XML of the game
     */
    private String generateXml(Game game) {
        try {
            ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();

            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(byteArrayStream, "UTF-8");
            serializer.startDocument("UTF-8", true);

            game.serialize(serializer);

            serializer.endDocument();

            return byteArrayStream.toString("UTF-8");
        } catch (IOException ex) {
            return "";
        }
    }

    /**
     * Parse XML and turn it into a Game object
     * @param context The context asking for the Game
     * @param parser The parser that has the XML
     * @return the Game object
     * @throws IOException
     * @throws XmlPullParserException
     */
    private Game parseGameXML(Context context, XmlPullParser parser) throws IOException, XmlPullParserException {
        return Game.deserialize(context, parser);
    }

    /**
     * Requests the player's game gets deleted from the server
     * @param userName The name of the user asking to delete their game
     * @param password The password of the user asking to delete their game
     * @return true if the game was deleted; false otherwise
     */
    public boolean deleteGameOnServer(String userName, String password)
    {
        String query = DELETE_URL + "?user=" + userName + "&pw=" + password;

        try {
            URL url = new URL(query);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return false;
            }

            XmlPullParser xml = Xml.newPullParser();
            xml.setInput(conn.getInputStream(), "UTF-8");

            xml.nextTag();
            xml.require(XmlPullParser.START_TAG, null, "game");
            String status = xml.getAttributeValue(null, "status");

            if(status.equals("yes")) {
                return true;
            }

            return false;

        } catch (MalformedURLException e) {
            return false;
        } catch(XmlPullParserException ex) {
            return false;
        } catch (IOException ex) {
            return false;
        }
    }
}
