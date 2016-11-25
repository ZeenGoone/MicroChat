package gruppe22.dtu.dk.mychat.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.HashMap;

import gruppe22.dtu.dk.mychat.Activities.ChatController;
import gruppe22.dtu.dk.mychat.ChatApplication;
import gruppe22.dtu.dk.mychat.Logic.IChatInitialized;

/**
 * Created by zeeng on 30/04/2016.
 */
public class AlertWindow extends AppCompatActivity {
    //Initialize variables
    public static IChatInitialized chatroomRefresh = null;
    public AlertWindow(){
    }
    public boolean typeAlertWindow(final Context mainActivity, String title, String message, final String chatRoom){
        //Initiate view specifics and variables
        final AlertDialog.Builder alertPrompt = new AlertDialog.Builder(mainActivity);
        final EditText alertWindow = new EditText(mainActivity);
        final String alertTitle = title;
        final String alertMessage = message;
        final String chatroomName = chatRoom;
        //Initiate instance of Application
        final ChatApplication app = new ChatApplication();
        //Setup the alertwindow view programatically
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        alertWindow.setLayoutParams(lp);
        alertPrompt.setView(alertWindow);
        alertPrompt.setTitle(alertTitle);
        alertPrompt.setMessage(alertMessage);
        //Click listener for positive button (OK)
        alertPrompt.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                String joinRoom = "";
                //conditional statements depending on the caller of the alertWindow
                if(alertTitle == "Join Chatroom"){
                    joinRoom = alertWindow.getText().toString();
                    /**
                     * If user wants to join/create new chatroom
                     */
                    if(!app.getRoomsList().toString().contains(joinRoom)){
                        //Testing purposes
                        System.out.println("if check i new chat room, joinroom er: " + joinRoom);
                        //Set Hashmap for chatroom initialization
                        HashMap<String,Object> tempChatRoomSpec = new HashMap<>();
                        tempChatRoomSpec.put(joinRoom,"");
                        //Testing purposes
                        System.out.println("tempchatroomspec: " + tempChatRoomSpec);
                        //Update the users/user/chatroom directory to include new chat
                        app.getFirebase().child("users/"+app.getUsername()+"/chatrooms/").updateChildren(tempChatRoomSpec);
                        //Signal through broadcast the ChatController to update tab-View/Viewpager
                        Intent RTReturn = new Intent(ChatController.BROADCAST_RESTART_ACTIVITY);
                        LocalBroadcastManager.getInstance(getParent()).sendBroadcast(RTReturn);
                    }
                }else if(alertTitle == "Password Required"){
                    /*
                    If chat is password protected, user has been prompted for password,
                    Save it into hashmap and update Firebase with new information
                     */
                    HashMap tempChatRoomSpec = new HashMap<>();
                    tempChatRoomSpec.put(chatroomName,alertWindow.getText().toString());
                    app.getFirebase().child("users/"+app.getUsername()+"/chatrooms/").updateChildren(tempChatRoomSpec);
                }
                else if(alertTitle == "Type NickName"){
                    // Set new Alias a.k.a Nickname in Firebase
                    HashMap tempChatRoomSpec = new HashMap<>();
                    tempChatRoomSpec.put("alias",alertWindow.getText().toString());
                    app.getFirebase().child("users/"+app.getUsername()+"/personal/").setValue(tempChatRoomSpec);
                }
                /*this was deprecated due to choice of Activity instead of alertwindow
                else if(alertTitle == "Change Password"){

                }*/
                else if(alertTitle == "Set Chat Password"){
                    //Set password of current chat
                    HashMap password = new HashMap<>();
                    password.put("password",alertWindow.getText().toString());
                    //First update chat-rooms/chat with password
                    app.getFirebase().child("chat-rooms/" + chatRoom).updateChildren(password);
                    //Next update users/user/chatrooms with new password
                    HashMap tempChatRoomSpec = new HashMap<>();
                    tempChatRoomSpec.put(chatRoom,alertWindow.getText().toString());
                    app.getFirebase().child("users/"+app.getUsername()+"/chatrooms/").updateChildren(tempChatRoomSpec);
                    //Broadcast ChatController recreate to reflect changes
                    Intent RTReturn = new Intent(ChatController.BROADCAST_RESTART_ACTIVITY);
                    LocalBroadcastManager.getInstance(getParent()).sendBroadcast(RTReturn);
                }
            }
        })
                //For future implementation maybe a cancel has meaning cept removing alertwindow
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        // close dialog window
                    }
                }).show();
        return true;
    }
}
