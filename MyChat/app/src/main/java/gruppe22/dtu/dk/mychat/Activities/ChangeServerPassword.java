package gruppe22.dtu.dk.mychat.Activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.net.URL;

import gruppe22.dtu.dk.mychat.ChatApplication;
import gruppe22.dtu.dk.mychat.Logic.HttpGetToken;
import gruppe22.dtu.dk.mychat.R;

/**
 * Created by zeeng on 02/05/2016.
 */
public class ChangeServerPassword extends AppCompatActivity {
    //Initiate variables
    private ChatApplication app;
    private String username = "",oldpwd = "", newpwd = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        //Initialize Application instance
        app = new ChatApplication();

        //Setup view elements
        final EditText currentPassword = (EditText) findViewById(R.id.editTextCurrentPassword);
        final EditText newPassword = (EditText) findViewById(R.id.editTextNewPassword);
        Button changePasswordButton = (Button) findViewById(R.id.btnChangePassword);

        /**
         * Set onclicklistener to get old/new password from edittext fields,
         * then run asynktask to change password through relayserver
         */

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = app.getUsername();
                oldpwd = currentPassword.getText().toString();
                newpwd = newPassword.getText().toString();
                new ChangePassword().execute();
            }
        });
    }

    /**
     * AsynkTask to send username, old password and new password to
     * relay-server, then run tryLogin with new info on Application
     * to Update the token, finish Activity afterwards
     */
    private class ChangePassword extends AsyncTask<URL,Integer,String> {
        protected String doInBackground(URL... urls) {
            return  HttpGetToken.changePassword(username,oldpwd,newpwd);
        }
        protected void onPostExecute(String result) {
            app.tryLogin(username,newpwd);
            finish();
        }
    }
}
