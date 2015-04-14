package edu.msu.cse476.teamwoodpecker_project2;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class CreateUserActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);
    }


    public void onCreateNewUser(final View view){
        final String username = ((EditText)findViewById(R.id.newUsername)).getText().toString();
        final String password = ((EditText)findViewById(R.id.newPassword)).getText().toString();
        final String confirmPassword = ((EditText)findViewById(R.id.confirmPassword)).getText().toString();

        if(!password.equals(confirmPassword)) {
            view.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(view.getContext(), R.string.password_mismatch_toast, Toast.LENGTH_SHORT).show();
                }

            });
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {

            Cloud cloud = new Cloud();
            boolean loginSuccess = cloud.createUser(view, username, password);

            if(loginSuccess) {
                onUserCreationSuccessful();
            }
            }
        }).start();
        //TODO: return the newly created username and populate the login field with it?
    }

    private void onUserCreationSuccessful() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_user, menu);
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
