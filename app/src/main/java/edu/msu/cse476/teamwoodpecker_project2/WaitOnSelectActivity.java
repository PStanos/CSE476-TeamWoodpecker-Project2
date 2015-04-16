package edu.msu.cse476.teamwoodpecker_project2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.InterruptedIOException;

import edu.msu.cse476.teamwoodpecker_project2.R;

public class WaitOnSelectActivity extends DialogFragment {

    private static final String LOCAL_NAME = "local_name";
    private static final String LOCAL_PASSWORD = "local_password";

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set the title
        builder.setTitle(R.string.waiting_title);

        LayoutInflater inflater = getActivity().getLayoutInflater();
//        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.activity_wait_on_update, null);
        builder.setView(view);

        builder.setNegativeButton(R.string.quit_game, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // TODO: handle quitting here
            }
        });

        final SelectionView viewSelect = (SelectionView)getActivity().findViewById(R.id.selectionView);

        new Thread(new Runnable() {

            @Override
            public void run() {
                Cloud cloud = new Cloud();



//                cloud.submitUpdatedGame(viewSelect.getContext(), game, game.getLocalName(), game.getLocalPassword())
                final Game game = cloud.waitOnOpponent(viewSelect.getContext(), ((SelectionActivity) viewSelect.getContext()).getUser(), ((SelectionActivity) viewSelect.getContext()).getPass());

                ((SelectionActivity)viewSelect.getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((SelectionActivity)viewSelect.getContext()).updateGame(game);
                    }
                });
/*


                viewGame.post(new Runnable() {
                    @Override
                    public void run() {
                        // TODO: UI update work here

                    }
                });
*/
                dismiss();

            }
        }).start();

        AlertDialog dlg = builder.create();
        dlg.setCanceledOnTouchOutside(false);
        return dlg;
    }
}