package edu.msu.stanospa.teamwoodpecker_project2;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends ActionBarActivity {

    private Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        game = new Game(this);
    }

    public void onLogIn(View view) {
        final String username = ((EditText)findViewById(R.id.username)).getText().toString();
        final String password = ((EditText)findViewById(R.id.password)).getText().toString();

        new Thread(new Runnable() {
            @Override
            public void run() {

                Cloud cloud = new Cloud();
                boolean loginSuccess = cloud.attemptLogin(username, password);

                if(loginSuccess) {
                    onLoginSuccessful(username, password);
                }
                else {
                    // TODO: toast
                }
            }
        }).start();
    }

    private void onLoginSuccessful(String username, String password) {
        game.setPlayerNames(username, password);  //TODO: set username for local player only

        Bundle bundle = new Bundle();
        game.saveInstanceState(bundle, this);

        Intent intent = new Intent(this, AwaitActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void onViewInstructions(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

        builder.setTitle(R.string.instructions_title);
        builder.setMessage(R.string.instructions_text);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void onCreateUser(View view){
        Intent intent = new Intent(this, CreateUserActivity.class);
        startActivity(intent);
    }
}
