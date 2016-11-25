package gruppe22.dtu.dk.mychat.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import gruppe22.dtu.dk.mychat.ChatApplication;
import gruppe22.dtu.dk.mychat.Logic.ILoginReply;
import gruppe22.dtu.dk.mychat.R;

/**
 * Created by zeeng on 01/05/2016.
 */
public class LoginActivity extends AppCompatActivity implements ILoginReply {
    //Initiate variables
    private EditText username;
    private EditText password;
    private ChatApplication app;
    private ProgressDialog loginResponse;
    private boolean internetIsAvailable = false;
    private Context activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Save this activities context for use in the clicklisteners
        activity = this;
        // Set contentview
        setContentView(R.layout.activity_login);
        // For testing if there is internet connection before trying to login
        ConnectivityManager checkConnectivity =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = checkConnectivity.getActiveNetworkInfo();
        // Set the boolean for connected or not
        internetIsAvailable = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        //Setting up required stuff
        app = new ChatApplication();
        app.loginReply = this;

        //Adding toolbar to the activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Setting up login parts
        password = (EditText) findViewById(R.id.editTextLoginPassword);
        username = (EditText) findViewById(R.id.editTextLoginUsername);
        username.requestFocus();
        username.requestFocusFromTouch();
        Button loginButton = (Button) findViewById(R.id.btnLogin);

        // touchListener to remove softkeyboard when pressing on something other than the edittexts
        findViewById(R.id.base_login).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onBackPressed();
                return false;
            }
        });

        /*
         * Onclicklistener for button, sets up username/password from edittext-fields,
         * sends them to Application for data storage and runs the tryLogin method from
         * Application
         */

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If there is an internet connection
                if(internetIsAvailable){
                    // If there is both a username and password
                    if(username.getText().toString() != "" && password.getText().toString() != ""){
                        tryingLoginDialog();
                        Log.d("LoginActivityUser",username.getText().toString());
                        Log.d("LoginActivityPassword",password.getText().toString());
                        app.setUsername(username.getText().toString());
                        app.setPwd(password.getText().toString());
                        app.tryLogin(app.getUsername(), app.getPwd());
                    }else{
                        // User has left either username or password empty
                        Toast.makeText(activity,"Either username or password is empty!",Toast.LENGTH_LONG).show();
                    }

                }else{
                    // If no internet connection is detected prompt user
                    Toast.makeText(activity,"You are Currently not connected to the Internet!",Toast.LENGTH_LONG).show();
                    username.setText("");
                    password.setText("");
                }
            }
        });
    }
    // To enable pressing the screen to remove softkeyboard
    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)  this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
    }
    /*
    Method used by the callback from the interface, either finishes
    Login or does Nothing, to be added, a Toast to show user authentication
    failed.
     */
    private void tryingLoginDialog(){
        loginResponse = ProgressDialog.show(this, "Login Server",
                "Authenticating", true);
    }
    public void testLoginResult(boolean reply){
        if(reply){
            loginResponse.dismiss();
            this.finish();
        }
        else{
            Toast.makeText(this,"Login Failed", Toast.LENGTH_LONG);
            loginResponse.dismiss();
            password.setText("");
            username.setText("");

            System.out.println("Authenticated failed in LoginActivity: " + reply);
        }
    }
    //Interface callback receiver
    @Override
    public void loginProcessComplete(boolean reply){
        testLoginResult(reply);
    }
    @Override
    public void onBackPressed() {
        hideSoftKeyboard();
    }
}
