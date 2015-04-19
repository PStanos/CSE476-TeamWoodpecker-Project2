package edu.msu.cse476.teamwoodpecker_project2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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

    private static final String LOCAL_NAME = "local_name";
    private static final String LOCAL_PASSWORD = "local_password";
    private static final String FILE_REMEMBER = "REMEMBER_ID_PASSWORD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.getIntent().setFlags((Intent.FLAG_ACTIVITY_SINGLE_TOP));

        game = new Game(this);

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        remember = (CheckBox)findViewById(R.id.checkRemember);


        try {
            FileInputStream rememberIn = openFileInput(FILE_REMEMBER);

            StringBuffer rememberInRead = new StringBuffer();
            String rememberString;

            InputStreamReader rememberInStream = new InputStreamReader(rememberIn);
            BufferedReader rememberInBuff = new BufferedReader(rememberInStream);
            while ((rememberString = rememberInBuff.readLine()) != null) {
                rememberInRead.append(rememberString);
            }
            rememberInBuff.close();
            String Remember_This = rememberInRead.toString();

            if(!Remember_This.equals("")) {
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
            String rememberInfo = username+'&'+password;
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
        game.setPlayerNames(username, getString(R.string.opponent_name));

        Bundle bundle = new Bundle();
        game.saveInstanceState(bundle, this);

        Intent intent = new Intent(this, AwaitActivity.class);
        intent.putExtra(LOCAL_NAME, username);
        intent.putExtra(LOCAL_PASSWORD, password);
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


    private void onQuitGame(){
        // do nothing
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
        if(id == R.id.menu_quit){
            onQuitGame();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
