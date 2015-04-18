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



import edu.msu.cse476.teamwoodpecker_project2.R;

public class WaitOnSelectActivity extends DialogFragment {

    private static final String LOCAL_NAME = "local_name";
    private static final String LOCAL_PASSWORD = "local_password";

    Thread serverThread;

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
                if(serverThread != null && serverThread.isAlive()){
                    serverThread.interrupt();
                }
                ((SelectionActivity)getActivity()).onQuitGame();
            }
        });

        final SelectionView viewSelect = (SelectionView)getActivity().findViewById(R.id.selectionView);

        serverThread = new Thread(new Runnable() {

            @Override
            public void run() {
                Cloud cloud = new Cloud();
                final Game game = cloud.waitOnOpponent(viewSelect.getContext(), ((SelectionActivity) viewSelect.getContext()).getUser(), ((SelectionActivity) viewSelect.getContext()).getPass());

                if(game == null) {
                    ((SelectionActivity)viewSelect.getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(viewSelect.getContext(), R.string.opponent_quit, Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(viewSelect.getContext(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            ((SelectionActivity)viewSelect.getContext()).finish();
                        }
                    });

                    return;
                }

                ((SelectionActivity)viewSelect.getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((SelectionActivity)viewSelect.getContext()).updateGame(game);
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