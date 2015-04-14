package edu.msu.cse476.teamwoodpecker_project2;

import android.content.Context;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.Serializable;

public class Player implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * Get the player's name
     * @return the player's name
     */
    public String getName() {
        return name;
    }

    /**
     * The player's name
     */
    private String name;

    /**
     * Get the selected bird
     * @return the selected bird
     */
    public Bird getSelectedBird() {
        return selectedBird;
    }

    /**
     * Set the selected bird
     * @param selectedBird the selected bird
     */
    public void setSelectedBird(Bird selectedBird) {
        Log.i("setSelectedBird()", "bird set!" + selectedBird);
        this.selectedBird = selectedBird;
    }

    /**
     * The selected bird
     */
    private Bird selectedBird;

    private Player() { }

    public Player(String name) {
        this.name = name;
    }

    public void serialize(XmlSerializer serializer) throws IOException {
        serializer.startTag(null, "player");
        serializer.attribute(null, "name", name);

        if(selectedBird != null) {
            selectedBird.serialize(serializer);
        }

        serializer.endTag(null, "player");
    }

    public static Player deserialize(Context context, XmlPullParser parser) throws IOException, XmlPullParserException {
        Player player = new Player();

        parser.nextTag();
        parser.require(XmlPullParser.START_TAG, null, "player");

        player.setSelectedBird(Bird.deserialize(context, parser));

        return player;
    }
}
