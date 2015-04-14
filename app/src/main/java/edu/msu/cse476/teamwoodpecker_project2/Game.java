package edu.msu.cse476.teamwoodpecker_project2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class Game implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Used to track what state the game is currently in
     */
    private enum GameState {
        nameEntry,
        birdSelection,
        birdPlacement,
        gameOver
    }

    /**
     * Percentage of the view width/height that is occupied by the game
     */
    private final static float SCALE_IN_VIEW = 0.9f;

    /**
     * Width of the border around the game
     */
    private final static float BORDER_WIDTH = 3.0f;

    /**
     * Paint for outlining the area the game is in
     */
    private static Paint outlinePaint;

    /**
     * The size of the game field
     */
    private transient int gameSize;


    /**
     * The 1:1 scaling width of the game
     */
    private transient float scalingWidth;

    /**
     * the scaling factor for drawing birds
     */
    private transient float scaleFactor;

    /**
     * Collection of the birds that have been placed
     */
    private ArrayList<Bird> birds = new ArrayList<>();

    /**
     * The first player in the game
     */
    private Player player1;

    /**
     * The second player in the game
     */
    private Player player2;

    /**
     * The player that won the game
     */
    private Player winner;

    /**
     * The player turn: the first player to go for 0, or the second player to go for 1
     */
    private int playerTurn = 0;

    /**
     * The current round number (0 based)
     */
    private int roundNum = 0;

    /**
     * Is there a bird currently being dragged
     */
    private Bird dragging = null;

    /**
     * Most recent relative X touch when dragging
     */
    private float lastRelX;

    /**
     * Most recent relative Y touch when dragging
     */
    private float lastRelY;

    /**
     * The current stage of the game
     */
    private GameState state = GameState.birdSelection;

    /*
    * identifiers for whether current player is P1 or P2 in game
     */
    private int localPlayer;
    private String localName;

    /**
     * @param context the current context
     */
    public Game(Context context) {
        // Create the paint for outlining the play area
        outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(BORDER_WIDTH);
        outlinePaint.setColor(Color.RED);

        // Birds will be scaled so that the game is "1.5 ostriches" wide
        Bitmap scaleBird = BitmapFactory.decodeResource(context.getResources(), R.drawable.ostrich);
        scalingWidth = scaleBird.getWidth()*1.5f;

        localPlayer = 0;
        localName = "";
    }

    /**
     * Determines if the game is in the selection state
     * @return true if the game is in the selection state; false otherwise
     */
    public boolean inSelectionState() {
        return state.equals(GameState.birdSelection);
    }

    /**
     * Determines if the game is in the game over state
     * @return true if the game is over; false otherwise
     */
    public boolean inGameOverState() { return state.equals(GameState.gameOver); }


    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    /**
     * Get the current player who's turn it is
     * @return the player who's turn it is
     */
    private Player getCurrentPlayer() {
        if(localPlayer == 0){
            if(localName.equals(player1.getName())){
                localPlayer = 1;
            }
            else if(localName.equals(player2.getName())){
                localPlayer = 2;
            }
            else{
                localPlayer = 0;
            }
        }

        Log.i("LOCALPLAYER", localName+" is P"+localPlayer+"; round/turn = "+roundNum+"/"+playerTurn);

        if(playerTurn == 0) {
            if(roundNum % 2 == 0) return player1;
            else return player2;
        }
        else {
            if(roundNum % 2 == 1) return player1;
            else return player2;
        }
    }

    /**
     * Get the player who's turn is next
     * @return the player who's turn is next
     */
    private Player getNextPlayer() {
        if(getCurrentPlayer() == player1) return player2;
        else return player1;
    }

    /**
     * Advance the game by one turn
     */
    private void advanceTurn() {
        if(isSecondTurn()) {
            playerTurn = 0;

            if(state == GameState.birdSelection) {
                state = GameState.birdPlacement;
                dragging = getCurrentPlayer().getSelectedBird();
            }
            else {
                state = GameState.birdSelection;
                dragging = null;
                roundNum++;
            }
        }
        else {
            playerTurn = 1;
            dragging = getCurrentPlayer().getSelectedBird();
        }
    }

    /**
     * Get whether the second player in the current state has their turn now
     * @return true if the second player in the current state is playing; false otherwise
     */
    private boolean isSecondTurn() {
        return playerTurn == 1;
    }

    /**
     * Set the names of the players playing the game
     * @param name1 player 1's name
     * @param name2 player 2's name
     */
    public void setPlayerNames(String name1, String name2) {
        player1 = new Player(name1);
        player2 = new Player(name2);

        state = GameState.birdSelection;
    }

    public void setPlayers(Player p1, Player p2) {
        player1 = p1;
        player2 = p2;
    }

    /**
     * Set the current player's bird selection
     * @param selection the bird selected to place this round
     */
    public void setPlayerSelection(Bird selection) {
        Bird copyOfSelected = new Bird(selection);
        getCurrentPlayer().setSelectedBird(copyOfSelected);

        advanceTurn();
    }

    /**
     * Confirms the player has chosen where their bird goes
     */
    public void confirmBirdPlacement() {
        // Check to see if the player's bird collides with any other bird
        for(int itr = 0; itr < birds.size(); itr++) {
            if(getCurrentPlayer().getSelectedBird().collisionTest(birds.get(itr))) {
                declareWinner(getNextPlayer());
                return;
            }
        }

        birds.add(getCurrentPlayer().getSelectedBird());

        advanceTurn();
    }

    private void addBird(Bird bird) {
        birds.add(bird);
    }

    /**
     * Set the passed player as the winner, and move the game into the final state
     * @param winner the player who won
     */
    private void declareWinner(Player winner) {
        this.winner = winner;
        state = GameState.gameOver;
    }

    /**
     * Gets the current player's name
     * @return the player's name
     */
    public String getCurrentPlayerName() {
        return getCurrentPlayer().getName();
    }

    /**
     * Get the name of the player who won
     * @return the name of the player who won
     */
    public String getWinningPlayerName() { return winner.getName(); }

    /**
     * Get the current number of birds placed
     * @return the current number of birds placed
     */
    public int getNumBirdsPlaced() { return birds.size(); }

    /**
     * Draw the game
     * @param canvas the canvas to draw on
     */
    public void draw(Canvas canvas) {

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // The puzzle size is the view scale ratio of the minimum dimension, to make it square
        int minSide = (int)((width < height ? width : height) * SCALE_IN_VIEW);

        scaleFactor = minSide/scalingWidth;

        // Margins for centering the puzzle
        int marginX = (int) ((width - minSide) / (scaleFactor * 2));
        int marginY = (int) ((height - minSide) / (scaleFactor * 2));


        gameSize = (int)scalingWidth;


        canvas.save();

        canvas.scale(scaleFactor, scaleFactor);

        // Draw the outline of the gameplay area
        canvas.drawRect(marginX - BORDER_WIDTH, marginY - BORDER_WIDTH,
                marginX + gameSize + BORDER_WIDTH, marginY + gameSize + BORDER_WIDTH, outlinePaint);



        for (Bird bird : birds) {
            bird.draw(canvas, marginX, marginY, gameSize);
        }

        if(dragging != null) {
            dragging.draw(canvas, marginX, marginY, gameSize);
        }

        canvas.restore();
    }

    public void reloadBirds(Context context) {
        for (Bird bird : birds) {
            bird.reloadBitmap(context);
        }

        player1.getSelectedBird().reloadBitmap(context);
        player2.getSelectedBird().reloadBitmap(context);

        // Birds will be scaled so that the game is "1.5 ostriches" wide
        Bitmap scaleBird = BitmapFactory.decodeResource(context.getResources(), R.drawable.ostrich);
        scalingWidth = scaleBird.getWidth()*1.5f;
    }

    /**
     * Handle a touch event from the view.
     * @param view The view that is the source of the touch
     * @param event The motion event describing the touch
     * @return true if the touch is handled.
     */
    public boolean onTouchEvent(View view, MotionEvent event) {

        // Convert an x,y location to a relative location in the puzzle
        float relX = event.getX();
        float relY = event.getY();



        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:
                lastRelX = relX;
                lastRelY = relY;
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;

            case MotionEvent.ACTION_MOVE:
                if (dragging != null) {
                    dragging.move((relX - lastRelX) * 1/scaleFactor, (relY - lastRelY) * 1/scaleFactor, gameSize);
                    lastRelX = relX;
                    lastRelY = relY;
                    view.invalidate();
                    return true;
                }
                break;
        }

        return false;
    }
    public void saveInstanceState(Bundle bundle, Context context) {
        bundle.putSerializable(context.getString(R.string.game_state), this);
    }

    public void serialize(XmlSerializer serializer) throws IOException {
        serializer.startTag(null, "game_data");
        serializer.attribute(null, "playerTurn", String.valueOf(playerTurn));
        serializer.attribute(null, "roundNum", String.valueOf(roundNum));
        serializer.attribute(null, "gameState", String.valueOf(state));

        serializer.startTag(null, "players");
        player1.serialize(serializer);
        player2.serialize(serializer);
        serializer.endTag(null, "players");

        serializer.startTag(null, "birds");
        serializer.attribute(null, "numBirds", String.valueOf(birds.size()));

        for(int itr = 0; itr < birds.size(); itr++) {
            birds.get(itr).serialize(serializer);
        }

        serializer.endTag(null, "birds");

        serializer.endTag(null, "game_data");
    }

    public static Game deserialize(Context context, XmlPullParser parser) throws IOException, XmlPullParserException {
        Game game = new Game(context);

        parser.nextTag();
        parser.require(XmlPullParser.START_TAG, null, "game_data");
        parser.nextTag();
        parser.require(XmlPullParser.START_TAG, null, "players");
        Player player1 = Player.deserialize(context, parser);
        Player player2 = Player.deserialize(context, parser);

        game.setPlayers(player1, player2);
        parser.require(XmlPullParser.END_TAG, null, "players");

        parser.require(XmlPullParser.START_TAG, null, "birds");

        int numBirds = Integer.getInteger(parser.getAttributeValue(null, "numBirds"));

        for(int itr = 0; itr < numBirds; itr++) {
            game.addBird(Bird.deserialize(context, parser));
        }

        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, null, "birds");

        parser.require(XmlPullParser.END_TAG, null, "game_data");

        return game;
    }
}
