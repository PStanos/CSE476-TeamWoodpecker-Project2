package edu.msu.cse476.teamwoodpecker_project2;


import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class SelectionActivity extends ActionBarActivity {

    private Game game;

    private SelectionView selectionView;

    private TextView selectionText;

    private Toast noBirdToast;

    private Toast birdSelectedToast;

    private static final String LOCAL_NAME = "local_name";
    private static final String LOCAL_PASSWORD = "local_password";

    private String userName = null;
    private String password = null;

    Thread submitDataThread;

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        if(getFragmentManager().findFragmentByTag("wait") != null) {
            ((WaitOnSelectActivity)getFragmentManager().findFragmentByTag("wait")).stopWaiting();
        }

        super.onSaveInstanceState(bundle);

        selectionView.saveInstanceState(bundle);
        game.saveInstanceState(bundle, this);
        bundle.putString(LOCAL_NAME, userName);
        bundle.putString(LOCAL_PASSWORD, password);
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_selection);

        selectionView = (SelectionView)findViewById(R.id.selectionView);

        if(bundle != null) {
            game = (Game)bundle.getSerializable(getString(R.string.game_state));
            userName = bundle.getString(LOCAL_NAME);
            password = bundle.getString(LOCAL_PASSWORD);
            selectionView.loadInstanceState(bundle);
        }
        else {
            game = (Game)getIntent().getExtras().getSerializable(getString(R.string.game_state));
            userName = getIntent().getExtras().getString(LOCAL_NAME);
            password = getIntent().getExtras().getString(LOCAL_PASSWORD);
        }

        this.selectionText = (TextView) findViewById(R.id.playerNameLabel);
        setPlayerSelectionText();

        if( !userName.equals("") && !userName.equals(game.getCurrentPlayerName())) {
            WaitOnSelectActivity dlgWaitSelect = new WaitOnSelectActivity();
            dlgWaitSelect.show(getFragmentManager(), "wait");
        }
    }

    /**
     * set the text at the top of the selection screen to the appropriate player
     */
    private void setPlayerSelectionText() {
        selectionText.setText(game.getCurrentPlayerName() + " " + getString(R.string.player_select));
    }

    public void onConfirmSelection(View view) {
        if(submitDataThread != null && submitDataThread.isAlive()) {
            return;
        }

        if (selectionView.isSelected()) {
            selectionView.setPlayerSelection(game);

            final SelectionActivity act = this;
            submitDataThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Cloud cloud = new Cloud();
                    while(!cloud.submitUpdatedGame(act, game, userName, password)) {

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
        } else {
            if (noBirdToast == null) {
                noBirdToast = Toast.makeText(this, getString(R.string.no_bird_toast), Toast.LENGTH_SHORT);
            }
            TextView v = (TextView) noBirdToast.getView().findViewById(android.R.id.message);
            v.setTextColor(Color.RED);
            noBirdToast.show();
            Log.i("onConfirmSelection", "bird not selected");
        }
    }

    private void onGameDataSubmitted() {
        Bundle bundle = new Bundle();
        game.saveInstanceState(bundle, this);

        if (game.inPlacementState()) {
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtras(bundle);
            //intent.putExtras(intent.getExtras());
            intent.putExtra(LOCAL_NAME, getIntent().getExtras().getString(LOCAL_NAME));
            intent.putExtra(LOCAL_PASSWORD, getIntent().getExtras().getString(LOCAL_PASSWORD));
            startActivity(intent);
            finish();
        } else {
            setPlayerSelectionText();
            if (!userName.equals(game.getCurrentPlayerName())) {
                WaitOnSelectActivity dlgWaitSelect = new WaitOnSelectActivity();
                dlgWaitSelect.show(getFragmentManager(), "wait");
            }
        }
    }

    public void updateGame(Game g){
        game = g;
        setPlayerSelectionText();

        if(game.inPlacementState()) {
            Bundle bundle = new Bundle();
            game.saveInstanceState(bundle, this);

            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtras(bundle);
            intent.putExtra(LOCAL_NAME, getIntent().getExtras().getString(LOCAL_NAME));
            intent.putExtra(LOCAL_PASSWORD, getIntent().getExtras().getString(LOCAL_PASSWORD));
            startActivity(intent);
            finish();
        }
    }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_selection, menu);
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
