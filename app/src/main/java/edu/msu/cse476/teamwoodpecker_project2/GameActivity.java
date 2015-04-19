package edu.msu.cse476.teamwoodpecker_project2;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class GameActivity extends ActionBarActivity {


    private GameView gameView;

    private static final String LOCAL_NAME = "local_name";
    private static final String LOCAL_PASSWORD = "local_password";
    private String userName;
    private String password;

    /**
     * The thread that is submitting the game data to the server
     */
    private Thread submitDataThread;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_game);

        gameView = (GameView)findViewById(R.id.gameView);

        if(bundle != null) {
            gameView.loadInstanceState(bundle, this);
            userName = bundle.getString(LOCAL_NAME);
            password = bundle.getString(LOCAL_PASSWORD);
        }
        else {
            Game game = (Game)getIntent().getExtras().getSerializable(getString(R.string.game_state));
            gameView.setGame(game);
            userName = getIntent().getExtras().getString(LOCAL_NAME);
            password = getIntent().getExtras().getString(LOCAL_PASSWORD);
        }


        TextView tv = (TextView)findViewById(R.id.placementText);
        tv.setText(String.format(getString(R.string.bird_placement_info),
                gameView.getGame().getCurrentPlayerName()));

        gameView.reloadBirds();

        if(!userName.equals(gameView.getGame().getCurrentPlayerName())) {
            // if game is in waiting state:
            WaitOnUpdateActivity dlgWait = new WaitOnUpdateActivity();
            dlgWait.show(getFragmentManager(), "wait");
        }
    }

    public void onPlaceBird(View view) {
        if(submitDataThread != null && submitDataThread.isAlive()) {
            return;
        }

        gameView.onPlaceBird();

        final GameActivity act = this;
        submitDataThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Cloud cloud = new Cloud();
                while(!cloud.submitUpdatedGame(gameView.getGame(), userName, password)) {

                    act.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(act, R.string.submit_data_failed, Toast.LENGTH_SHORT).show();
                        }
                    });

                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        break;
                    }
                }

                act.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        onGameDataSubmitted();
                    }
                });
            }
        });

        submitDataThread.start();
    }

    /**
     * Once game data has been pushed successfully to server, this is called
     */
    private void onGameDataSubmitted() {
        Bundle bundle = new Bundle();
        gameView.getGame().saveInstanceState(bundle, this);

        if (gameView.inGameOverState()) {

            Intent intent = new Intent(this, FinalScoreActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(LOCAL_NAME, userName);
            intent.putExtra(LOCAL_PASSWORD, password);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        } else if (gameView.inSelectionState()) {

            Intent intent = new Intent(this, SelectionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(LOCAL_NAME, userName);
            intent.putExtra(LOCAL_PASSWORD, password);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
        else {
            TextView tv = (TextView)findViewById(R.id.placementText);
            tv.setText(String.format(getString(R.string.bird_placement_info),
                    gameView.getGame().getCurrentPlayerName()));

            if(!userName.equals(gameView.getGame().getCurrentPlayerName())) {
                // if game is in waiting state:
                WaitOnUpdateActivity dlgWait = new WaitOnUpdateActivity();
                dlgWait.show(getFragmentManager(), "wait");
            }
        }
    }

    /**
     * Update the game with latest data
     * @param g The latest game data
     */
    public void updateGame(final Game g){
        gameView.setGame(g);

        if(gameView.getGame().inSelectionState()) {
            Bundle bundle = new Bundle();
            gameView.getGame().saveInstanceState(bundle, this);

            Intent intent = new Intent(this, SelectionActivity.class);
            intent.putExtras(bundle);
            intent.putExtra(LOCAL_NAME, userName);
            intent.putExtra(LOCAL_PASSWORD, password);
            startActivity(intent);
            finish();
        }
        else if(gameView.getGame().inGameOverState()) {
            Bundle bundle = new Bundle();
            gameView.getGame().saveInstanceState(bundle, this);

            Intent intent = new Intent(this, FinalScoreActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(LOCAL_NAME, userName);
            intent.putExtra(LOCAL_PASSWORD, password);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        if(getFragmentManager().findFragmentByTag("wait") != null) {
            ((WaitOnUpdateActivity)getFragmentManager().findFragmentByTag("wait")).stopWaiting();
        }

        super.onSaveInstanceState(bundle);

        gameView.saveInstanceState(bundle, this);
        bundle.putString(LOCAL_NAME, userName);
        bundle.putString(LOCAL_PASSWORD, password);
    }

    /**
     * The current user wants to quit the game
     */
    public void onQuitGame(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Cloud cloud = new Cloud();
                cloud.deleteGameOnServer(userName, password);
            }
        }).start();

        if(submitDataThread != null && submitDataThread.isAlive()) {
            submitDataThread.interrupt();
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * Game ended by other player or through timeout
     */
    public void onGameEnded() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        onQuitGame();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game, menu);
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

    public String getUser(){return userName;}
    public String getPass(){return password;}

}
