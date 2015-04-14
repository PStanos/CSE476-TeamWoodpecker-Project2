package edu.msu.cse476.teamwoodpecker_project2;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
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

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        selectionView.saveInstanceState(bundle);
        game.saveInstanceState(bundle, this);
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_selection);

        if(bundle != null) {
            game = (Game)bundle.getSerializable(getString(R.string.game_state));
            game.setLocalName(getIntent().getExtras().getString(LOCAL_NAME));
        }
        else {
            game = (Game)getIntent().getExtras().getSerializable(getString(R.string.game_state));
        }
        selectionView = (SelectionView)findViewById(R.id.selectionView);

        this.selectionText = (TextView) findViewById(R.id.playerNameLabel);
        setPlayerSelectionText();

        if (bundle != null){
            Log.i("onCreate()", "restoring state...");
            selectionView.loadInstanceState(bundle);
        }

        if( !game.getLocalName().equals("") && !game.getLocalName().equals(game.getCurrentPlayerName())) {
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
        Bundle bundle = new Bundle();
        game.saveInstanceState(bundle, this);

        if (selectionView.isSelected()) {
            selectionView.setPlayerSelection(game);

            if (!game.inSelectionState()){

                Intent intent = new Intent(this, GameActivity.class);
                intent.putExtras(bundle);
                intent.putExtras(intent.getExtras());
                intent.putExtra(LOCAL_NAME, getIntent().getExtras().getString(LOCAL_NAME) );
                startActivity(intent);
                finish();
            }
            else {
                if(!game.getLocalName().equals(game.getCurrentPlayerName())) {
                    WaitOnSelectActivity dlgWaitSelect = new WaitOnSelectActivity();
                    dlgWaitSelect.show(getFragmentManager(), "wait");
                }
                else
                    setPlayerSelectionText();
            }

        } else {
            if(noBirdToast == null) {
                noBirdToast = Toast.makeText(this, getString(R.string.no_bird_toast), Toast.LENGTH_SHORT);
            }
            TextView v = (TextView) noBirdToast.getView().findViewById(android.R.id.message);
            v.setTextColor(Color.RED);
            noBirdToast.show();
            Log.i("onConfirmSelection", "bird not selected");
        }
    }

}
