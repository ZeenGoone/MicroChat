package gruppe22.dtu.dk.mychat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import gruppe22.dtu.dk.mychat.Activities.ChatController;
import gruppe22.dtu.dk.mychat.Activities.LoginActivity;
/**
 * Created by zeeng on 01/05/2016.
 */
public class MainActivity extends AppCompatActivity {
    //Create instance of ChatApplication for data use
    ChatApplication app = new ChatApplication();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        Test if LoginActivity came back with authenticated response,
        if true start chat, if not redo Login
         */
        if(app.isAuthenticated()){
            startChatProgram();
        }else if(!app.isAuthenticated()){
            startLogin();
        }
    }
    //Method for LoginActivity launching
    private void startLogin(){
        Intent launchLoginScreen = new Intent(this, LoginActivity.class);
        startActivity(launchLoginScreen);
    }
    //Method for ChatProgram-Activity launching
    private void startChatProgram(){
        Intent chatApplication = new Intent(this, ChatController.class);
        startActivity(chatApplication);
    }
    @Override
    protected void onResume(){
        super.onResume();
        //If user has tried to exit application, disabled for live android testing
        /*if(app.getQuitApplication()){
            super.onBackPressed();
        }*/
        //If user still authenticated run chat-activity
        if(app.isAuthenticated()){
            startChatProgram();
        }
        //else if(!app.isAuthenticated()){ startLogin(); }
    }
    @Override
    public void onBackPressed() {
        // To ensure a few fast backbuttons will kill the application regardless
        super.onBackPressed();
    }
}
