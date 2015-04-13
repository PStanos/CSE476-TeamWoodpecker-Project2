package edu.msu.stanospa.teamwoodpecker_project2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MainActivity extends ActionBarActivity {

    private Game game;
    private File FileRemember;

    private EditText username;
    private EditText password;
    private CheckBox remember;

    private static final String FILE_REMEMBER = "REMEMBER_ID_PASSWORD";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        game = new Game(this);

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        remember = (CheckBox)findViewById(R.id.checkRemember);


        try {
            FileInputStream rememberIn = openFileInput(FILE_REMEMBER);

            StringBuffer rememberInRead = new StringBuffer();
            String rememberString = "";

            InputStreamReader rememberInStream = new InputStreamReader(rememberIn);
            BufferedReader rememberInBuff = new BufferedReader(rememberInStream);
            while ((rememberString = rememberInBuff.readLine()) != null) {
                rememberInRead.append(rememberString);
            }
            rememberInBuff.close();
            String Remember_This = rememberInRead.toString();

            if(Remember_This != "") {
                String[] Remember_These = Remember_This.split("&", 2);
                if(Remember_These.length == 2) {
                    EditText username = (EditText) findViewById(R.id.username);
                    username.setText(Remember_These[0]);
                    EditText password = (EditText) findViewById(R.id.password);
                    password.setText(Remember_These[1]);
                }
            }

        }catch(IOException FileNotFoundException){
            // file not found, does not exist, no user data stored locally
            try {
                String data="";
                OutputStreamWriter rememberCreate = new OutputStreamWriter(openFileOutput(FILE_REMEMBER, Context.MODE_PRIVATE));
                rememberCreate.write(data);
                rememberCreate.close();
            }
            catch (IOException e) {
                // fail silently
            }
        }
    }

    public void onLogIn(final View view) {
        final String username = ((EditText)findViewById(R.id.username)).getText().toString();
        final String password = ((EditText)findViewById(R.id.password)).getText().toString();

        if(remember.isChecked()){
            String rememberInfo = username.toString()+'&'+password.toString();
            try {
                FileOutputStream outputStream = openFileOutput(FILE_REMEMBER, Context.MODE_PRIVATE);
                outputStream.write(rememberInfo.getBytes());
                outputStream.close();
            } catch (Exception IOException) {
                //fail silently on remember data
            }
            remember.setChecked(false);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {

                Cloud cloud = new Cloud();
                boolean loginSuccess = cloud.attemptLogin(view, username, password);

                if(loginSuccess) {
                    onLoginSuccessful(username, password);
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
