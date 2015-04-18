package edu.msu.cse476.teamwoodpecker_project2;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class AwaitActivity extends ActionBarActivity {
    Bundle hold;

    private static final String LOCAL_NAME = "local_name";
    private static final String LOCAL_PASSWORD = "local_password";

    private Game game = null;
    private String userName = null;
    private String password = null;
    private Thread waitOnGameThread = null;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_await);

        if(bundle == null) {
            userName = getIntent().getExtras().getString(LOCAL_NAME);
            password = getIntent().getExtras().getString(LOCAL_PASSWORD);
        }
        else {
            userName = bundle.getString(LOCAL_NAME);
            password = bundle.getString(LOCAL_PASSWORD);
        }

        waitOnGameThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Cloud cloud = new Cloud();

                Game existingGame = cloud.userGame(getBaseContext(), userName, password);

                if(existingGame != null) {
                    game = existingGame;
                    onPlayersConnected();

                    return;
                }

                Cloud.NewGameResponse response = cloud.waitForGame(getBaseContext(), userName, password);

                if(!response.isConnected()) {
                    onGameCreationFailed();
                    return;
                }

                if(response.getUserName1().equals(userName)) {
                    game = new Game(getBaseContext());
                    game.setPlayerNames(response.getUserName1(), response.getUserName2());

                    if(!cloud.submitUpdatedGame(getBaseContext(), game, userName, password)) {
                        onGameCreationFailed();
                        return;
                    }
                }
                else {
                    game = cloud.waitOnOpponent(getBaseContext(), userName, password);

                    if(game != null) {
                        onPlayersConnected();
                    }
                    else {
                        onGameCreationFailed();
                    }
                    return;
                }

                onPlayersConnected();
            }
        });

        waitOnGameThread.start();
    }

    public void onPlayersConnected() {
        Bundle bundle = new Bundle();
        game.saveInstanceState(bundle, this);

        Intent intent = null;

        if(game.inSelectionState()) {
            intent = new Intent(this, SelectionActivity.class);
        }
        else if(game.inPlacementState()) {
            intent = new Intent(this, GameActivity.class);
        }
        else if(game.inGameOverState()) {
            intent = new Intent(this, FinalScoreActivity.class);
        }
        else {
            Log.e(null, "Game exists but is not in any playable state!");
            intent = new Intent(this, MainActivity.class);
        }

        intent.putExtras(bundle);
        intent.putExtra(LOCAL_NAME, userName);
        intent.putExtra(LOCAL_PASSWORD, password);
        startActivity(intent);
    }

    private void onGameCreationFailed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void onQuitGame(View view){
        if(waitOnGameThread.isAlive()) {
            waitOnGameThread.interrupt();
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    public void onQuitGame(){
        if(waitOnGameThread.isAlive()) {
            waitOnGameThread.interrupt();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Cloud cloud = new Cloud();
                cloud.deleteGameOnServer(userName, password);
            }
        }).start();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_await, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if(id == R.id.menu_quit){
            onQuitGame();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        bundle.putString(LOCAL_NAME, userName);
        bundle.putString(LOCAL_PASSWORD, password);

        if(waitOnGameThread != null) {
            waitOnGameThread.interrupt();
        }
    }
}
