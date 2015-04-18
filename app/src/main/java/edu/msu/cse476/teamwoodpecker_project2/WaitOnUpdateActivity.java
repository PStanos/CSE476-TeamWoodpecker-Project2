package edu.msu.cse476.teamwoodpecker_project2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

public class WaitOnUpdateActivity extends DialogFragment {

    Thread serverThread;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set the title
        builder.setTitle(R.string.waiting_title);

        LayoutInflater inflater = getActivity().getLayoutInflater();
//        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.activity_wait_on_update, null);
        builder.setView(view);

        final GameView viewGame = (GameView)getActivity().findViewById(R.id.gameView);

        builder.setNegativeButton(R.string.quit_game, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if(serverThread != null && serverThread.isAlive()){
                    serverThread.interrupt();
                }
                ((GameActivity)getActivity()).onQuitGame();
            }
        });


        serverThread = new Thread(new Runnable() {

            @Override
            public void run() {
                Cloud cloud = new Cloud();
                final Game game = cloud.waitOnOpponent(viewGame.getContext(), (((GameActivity) viewGame.getContext()).getUser()), (((GameActivity) viewGame.getContext()).getPass()));

                if(game == null) {
                    ((GameActivity)viewGame.getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(viewGame.getContext(), R.string.game_not_found, Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(viewGame.getContext(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            ((GameActivity)viewGame.getContext()).finish();
                        }
                    });

                    return;
                }

                ((GameActivity)viewGame.getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((GameActivity)viewGame.getContext()).updateGame(game);
                    }
                });
                dismiss();
            }
        });
        serverThread.start();

        AlertDialog dlg = builder.create();
        dlg.setCanceledOnTouchOutside(false);
        return dlg;
    }

    public void stopWaiting() {
        if(serverThread != null && serverThread.isAlive()){
            serverThread.interrupt();
            dismiss();
        }
    }
}