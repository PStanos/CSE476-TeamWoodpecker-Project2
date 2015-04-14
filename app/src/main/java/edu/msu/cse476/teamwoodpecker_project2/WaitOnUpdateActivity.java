package edu.msu.cse476.teamwoodpecker_project2;

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

public class WaitOnUpdateActivity extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
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

        final GameView viewGame = (GameView)getActivity().findViewById(R.id.gameView);

        new Thread(new Runnable() {

            @Override
            public void run() {
                Cloud cloud = new Cloud();

                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {

                }
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