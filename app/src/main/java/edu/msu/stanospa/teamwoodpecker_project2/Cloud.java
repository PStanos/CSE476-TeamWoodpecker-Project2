package edu.msu.stanospa.teamwoodpecker_project2;

import android.util.Xml;
import android.view.View;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Cloud {
    private static final String LOGIN_URL = "http://webdev.cse.msu.edu/~chiversb/cse476/proj02/login.php";
    private static final String CREATE_USER_URL = "http://webdev.cse.msu.edu/~chiversb/cse476/proj02/newuser.php";

    public boolean attemptLogin(final View view, String userId, String password) {

        String query = LOGIN_URL + "?user=" + userId + "&pw=" + password;

        try {
            URL url = new URL(query);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return false;
            }

            XmlPullParser xml = Xml.newPullParser();
            xml.setInput(conn.getInputStream(), "UTF-8");

            xml.nextTag();      // Advance to first tag
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

        } catch (MalformedURLException e) {
            return false;
        } catch(XmlPullParserException ex) {
            return false;
        } catch (IOException ex) {
            return false;
        }
    }

    public boolean createUser(final View view, String userId, String password) {

        String query = CREATE_USER_URL + "?user=" + userId + "&pw=" + password;

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

        } catch (MalformedURLException e) {
            return false;
        } catch(XmlPullParserException ex) {
            return false;
        } catch (IOException ex) {
            return false;
        }
    }
}
