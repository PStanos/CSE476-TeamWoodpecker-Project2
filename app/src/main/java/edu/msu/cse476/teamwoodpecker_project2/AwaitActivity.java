package edu.msu.cse476.teamwoodpecker_project2;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class AwaitActivity extends ActionBarActivity {

    Bundle hold;

    private static final String LOCAL_NAME = "local_name";

    private Game game = null;
    private String userName = null;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_await);

        if(bundle == null) {
            userName = getIntent().getExtras().getString(LOCAL_NAME);
        }
        else {
            userName = bundle.getString(LOCAL_NAME);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Cloud cloud = new Cloud();

                Cloud.NewGameResponse response = cloud.waitForGame(getBaseContext(), userName, "P");

                if(!response.isConnected()) {
                    // TODO: What do we do if the server returns an error?
                    return;
                }

                if(response.getUserName1().equals(userName)) {
                    game = new Game(getBaseContext());
                    game.setPlayerNames(response.getUserName1(), response.getUserName2());
                }
                else {
                    cloud.waitOnOpponent(getBaseContext(), game.getCurrentPlayerName(), game.getCurrentPlayerName());
                }

                onPlayersConnected();
            }
        }).start();
    }

    public void onPlayersConnected() {
        Bundle bundle = new Bundle();
        game.saveInstanceState(bundle, this);

        Intent intent = new Intent(this, SelectionActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void onQuit(View view){  // TODO: make this a menu option, not a button
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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

        return super.onOptionsItemSelected(item);
    }

}
