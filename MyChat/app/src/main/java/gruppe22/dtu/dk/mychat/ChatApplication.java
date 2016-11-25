package gruppe22.dtu.dk.mychat;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import gruppe22.dtu.dk.mychat.Activities.ChatController;
import gruppe22.dtu.dk.mychat.Logic.HttpGetToken;
import gruppe22.dtu.dk.mychat.Logic.IChatInitialized;
import gruppe22.dtu.dk.mychat.Logic.ILoginReply;
import gruppe22.dtu.dk.mychat.Logic.Message;

/**
 * Created by zeeng on 01/05/2016.
 */
public class ChatApplication extends Application {
    //Setup variables
    private static ChatApplication singleton;
    private static String username = "", pwd = "", token = "", nickname = "";
    private static boolean authenticated = false;
    public static ILoginReply loginReply = null;
    public static IChatInitialized chatRoomAdded = null;
    private static HashMap<String,ArrayList<Message>> roomsList = new HashMap();
    private static String oldpwd = "",newpwd = "",fileDownloadPath = "";
    private static boolean killApp = false;
    private static Activity current_activity;
    public static int restartCounter = 0;

    public ChatApplication getInstance(){
        return singleton;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
        // Firebase Specific
        Firebase.setAndroidContext(this);
    }
    // Getters and Setters
    public String getPwd() { return pwd; }
    public void setPwd(String password) { pwd = password; }
    public String getUsername() { return username; }
    public void setUsername(String user) { username = user; }
    public boolean isAuthenticated(){ return authenticated; }
    public void setRoomChat(String chatRoomName,ArrayList<Message> chat) {roomsList.put(chatRoomName, chat);}
    public ArrayList<Message> getRoomChat(String key){
        return roomsList.get(key);
    }
    public String getNickname(){ return nickname; }
    public void setNewPwd(String newpassword){newpwd = newpassword;}
    public void setOldPwd(String oldpassword){oldpwd = oldpassword;}
    public Set getCurrentRooms(){ return roomsList.keySet(); }
    public void setFileDownloadPath(String path){ fileDownloadPath = path; }
    public String getFileDownloadPath(){ return fileDownloadPath; }
    public void setCurrentActivity(Activity activity){ current_activity = activity; }
    // For testing purposes, disabled for live testing android
    //public void setQuitApplication(){ killApp = true; }
    //public boolean getQuitApplication(){ return killApp; }
    public void setNOTAuthenticated(){
        authenticated = false;
        username = "";
        pwd = "";
        roomsList.clear();
        fileDownloadPath = "";
    }
    /*
    Convert keyset from hashmap into ordered Array, this to ensure newly
    added chatrooms don't take the indexes of the beginning where chatrooms
    are presently located in the tablayout
     */
    public String[] getRoomsList(){
        String[] roomNameList = new String[roomsList.size()];
        int roomcounter = 0;
        for(int i = roomsList.keySet().toArray().length-1;i >= 0;i--){
            System.out.println("Roomname: " + roomsList.keySet().toArray()[i].toString());
            roomNameList[roomcounter]=roomsList.keySet().toArray()[i].toString();
            roomcounter++;
        }
        return roomNameList;
    }
    /*
    Setup chatroomlistener on the users chatroom directory, this allows knowing what
    chatrooms are currently available to the user and also when more are added to update
    the tabs
     */
    public void setChatRoomUpdater(){

        getFirebase().child("users/"+username+"/chatrooms/").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                roomsList.put(dataSnapshot.getKey(),new ArrayList<Message>());
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                roomsList.put(dataSnapshot.getKey(),new ArrayList<Message>());
                //messageList.setSelection(messageList.getAdapter().getCount()-1);
                //Send broadcast to reset the activity
                Intent RTReturn = new Intent(ChatController.BROADCAST_RESTART_ACTIVITY);
                LocalBroadcastManager.getInstance(current_activity).sendBroadcast(RTReturn);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                roomsList.remove(dataSnapshot.getKey());
                //Send broadcast to reset the activity
                Intent RTReturn = new Intent(ChatController.BROADCAST_RESTART_ACTIVITY);
                LocalBroadcastManager.getInstance(current_activity).sendBroadcast(RTReturn);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                //messageList.setSelection(messageList.getAdapter().getCount()-1);
                //Send broadcast to reset the activity
                Intent RTReturn = new Intent(ChatController.BROADCAST_RESTART_ACTIVITY);
                LocalBroadcastManager.getInstance(current_activity).sendBroadcast(RTReturn);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.d("setChatRoomUpdater", firebaseError.getMessage());
            }
        });
    }
    /*
    listener for the alias(nickname) part of the users accountsettings, to ensure
    the Application data is always newest available from (Firebase)
     */
    public void setNicknameUpdater(){

        getFirebase().child("users/"+username+"/personal/").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                nickname = dataSnapshot.getValue().toString();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                nickname = dataSnapshot.getValue().toString();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                //messageList.setSelection(messageList.getAdapter().getCount()-1);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.d("setNickNameUpdater", firebaseError.getMessage());
            }
        });
    }
    //Getter to allow instances access to firebase
    public Firebase getFirebase(){
        return new Firebase("https://micro-chat.firebaseio.com");
    }
    /*
    Authenticate user against (Firebase) with the token received
    from RetrieveToken, if successfull notify the LoginActivity
     */
    private void authenticateUser(String token){
        Log.d("Token AuthenticateUser", token);
        Firebase appFirebaseInstance = new Firebase("https://micro-chat.firebaseio.com");
        appFirebaseInstance.authWithCustomToken(token,
                new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        // Authentication just completed successfully :)
                        setChatRoomUpdater();
                        setNicknameUpdater();
                        Log.d("Authentication","Succeeded");
                        authenticated = true;
                        if(loginReply != null){
                            //Send message through interface callback to loginactivity
                            loginReply.loginProcessComplete(authenticated);
                        }
                    }
                    @Override
                    public void onAuthenticationError(FirebaseError error) {
                        // Something went wrong :(
                        Log.d("Authentication","Failed");
                        authenticated = false;
                        //Send message through interface callback to loginactivity
                        loginReply.loginProcessComplete(authenticated);
                    }
                });
    }
    //Old version of password changer on server
    public void changePassword(String oldpassword, String newpassword){
        oldpwd = oldpassword;
        newpwd = newpassword;
        //new ChangePassword().execute();
    }
    //Method for getting token from relayserver
    public boolean tryLogin(String username, String password){
        this.username = username;
        this.pwd = password;
        new RetrieveToken().execute();
        return authenticated;
    }
    //Asynktask to contact relayserver for token if authenticated
    private class RetrieveToken extends AsyncTask<URL,Integer,String> {
        protected String doInBackground(URL... urls) {
            //Return response from relayserver
            return HttpGetToken.getToken(username,pwd);
        }
        protected void onPostExecute(String result) {
            token = result;
            if(token != null){
                Log.d("Token",result);
                //Run authenticationmethod for Firebase authentication
                authenticateUser(result);
            }else{
                if(loginReply != null){
                    //Send message through interface callback to loginactivity
                    loginReply.loginProcessComplete(authenticated);
                }
            }
        }
    }
    //For future implementations
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
