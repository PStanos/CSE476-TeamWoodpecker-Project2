package edu.msu.cse476.teamwoodpecker_project2;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class GameActivity extends ActionBarActivity {


    private GameView gameView;

    private static final String LOCAL_NAME = "local_name";
    private static final String LOCAL_PASSWORD = "local_password";
    private String local_username;
    private String local_password;


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_game);

        gameView = (GameView)findViewById(R.id.gameView);

        if(bundle != null) {
            gameView.loadInstanceState(bundle, this);

        }
        else {
            Game game = (Game)getIntent().getExtras().getSerializable(getString(R.string.game_state));
            gameView.setGame(game);
        }

        local_username = getIntent().getExtras().getString(LOCAL_NAME);
        local_password = getIntent().getExtras().getString(LOCAL_PASSWORD);


        TextView tv = (TextView)findViewById(R.id.placementText);
        tv.setText(String.format(getString(R.string.bird_placement_info),
                gameView.getGame().getCurrentPlayerName()));

        gameView.reloadBirds();

        if(!local_username.equals(gameView.getGame().getCurrentPlayerName())) {
            // if game is in waiting state:
            WaitOnUpdateActivity dlgWait = new WaitOnUpdateActivity();
            dlgWait.show(getFragmentManager(), "wait");
        }

    }

    public void onPlaceBird(View view) {
        gameView.onPlaceBird();

        Bundle bundle = new Bundle();
        gameView.getGame().saveInstanceState(bundle, this);

            if (gameView.inGameOverState()) {

                Intent intent = new Intent(this, FinalScoreActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            } else if (gameView.inSelectionState()) {

                Intent intent = new Intent(this, SelectionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
            else{
                if(!local_username.equals(gameView.getGame().getCurrentPlayerName())) {
                    // if game is in waiting state:
                    WaitOnUpdateActivity dlgWait = new WaitOnUpdateActivity();
                    dlgWait.show(getFragmentManager(), "wait");
                }
            }

        TextView tv = (TextView)findViewById(R.id.placementText);
        tv.setText(String.format(getString(R.string.bird_placement_info),
                gameView.getGame().getCurrentPlayerName()));

    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        gameView.saveInstanceState(bundle, this);
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
            onQuit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onQuit(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
