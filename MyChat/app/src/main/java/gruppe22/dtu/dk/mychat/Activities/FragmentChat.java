package gruppe22.dtu.dk.mychat.Activities;

/**
 * Created by zeeng on 30/04/2016.
 */
import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import gruppe22.dtu.dk.mychat.ChatApplication;
import gruppe22.dtu.dk.mychat.Utilities.MessageArrayAdapter;
import gruppe22.dtu.dk.mychat.Logic.MicroChatFileServerClient;
import gruppe22.dtu.dk.mychat.Logic.Message;
import gruppe22.dtu.dk.mychat.MainActivity;
import gruppe22.dtu.dk.mychat.R;


public class FragmentChat extends Fragment {

    // Instantiate variables
    public static final String POSITION_KEY = "FragmentChatPositionKey";
    public static final String CURRENT_CHAT_NAME = "FragmentChatName";
    public static final String CURRENT_USER_NAME = "FragmentUserName";
    private static int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10;
    private ProgressDialog downloadDialog;
    private Firebase ref;
    private MessageArrayAdapter chatAdapter;
    private EditText MessageField;
    private Button SendButton;
    private ImageButton ShareImageButton;
    private ImageButton ShareFileButton;
    private ImageButton UploadFileButton;
    private ImageButton DeleteFileButton;
    private ImageButton LeaveChatButton;
    private ListView messagesView;
    private ArrayList<Message> chat = new ArrayList();
    private ChatApplication app = new ChatApplication();
    private MicroChatFileServerClient fileServer = new MicroChatFileServerClient();
    // List to handle items with URL's in them
    private ArrayList<Integer> urlList = new ArrayList();
    // Variables to handle Download Request
    private String fileOwner = "";
    private String fileDownload = "";

    public static FragmentChat newInstance(Bundle args){
        FragmentChat fragment = new FragmentChat();
        fragment.setArguments(args);
        Log.d("newInstanceCall", "We entered FragmentChat constructor");
        return fragment;
    }
    // Show user something is being performed in the background
    private void tryingDownloadDialog(){
        downloadDialog = ProgressDialog.show(getActivity(), "File Server",
                "Downloading", true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View chatView = inflater.inflate(R.layout.chat_list_view, container, false);

        // Keeping these here for reference
        // getArguments().getInt(FragmentChat.POSITION_KEY);
        // getArguments().getString(FragmentChat.CURRENT_CHAT_NAME);
        // getArguments().getString(FragmentChat.CURRENT_USER_NAME);

        // Grab firebase instance from application
        ref = app.getFirebase();

        // Setup the view
        messagesView = (ListView) chatView.findViewById(R.id.message_list_container);
        SendButton = (Button) chatView.findViewById(R.id.btn_send);
        UploadFileButton = (ImageButton) chatView.findViewById(R.id.btn_upload_file);
        ShareImageButton = (ImageButton) chatView.findViewById(R.id.btn_share_image);
        ShareFileButton = (ImageButton) chatView.findViewById(R.id.btn_share_file);
        DeleteFileButton = (ImageButton) chatView.findViewById(R.id.btn_delete_file_server);
        LeaveChatButton = (ImageButton) chatView.findViewById(R.id.btn_leave_chat);
        MessageField = (EditText) chatView.findViewById(R.id.edt_send_text);

        // Adding clicklistener to LeaveChat button
        LeaveChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                app.setCurrentActivity(getActivity());
                app.getFirebase().child("users/"+getArguments().getString(FragmentChat.CURRENT_USER_NAME)+"/chatrooms/"+getArguments().getString(FragmentChat.CURRENT_CHAT_NAME)).removeValue();
            }
        });
        // Adding clicklistener to uploadfile button
        UploadFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the upload file activity
                Intent launchLoginScreen = new Intent(getActivity(), UploadFileServer.class);
                startActivityForResult(launchLoginScreen,3);
            }
        });
        // Adding clicklistener to deletefile button
        DeleteFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the delete file activity
                Intent launchLoginScreen = new Intent(getActivity(), DeleteFileServer.class);
                startActivityForResult(launchLoginScreen,2);
            }
        });
        // Adding clicklistener to sharefile button
        ShareFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the share file activity
                Intent launchLoginScreen = new Intent(getActivity(), ShareFileChat.class);
                startActivityForResult(launchLoginScreen,1);
            }
        });
        // Adding clicklistener to shareimage button, not implemented yet "nice to have"
        ShareImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        // Adding clicklistener to send button
        SendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Grab data from messagefield and set it to empty
                sendMessage(MessageField.getText().toString());
                MessageField.setText("");
            }
        });
        // Set onclicklistener for listview to handle downloads of URL
        messagesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(urlList.contains(position)) {
                    Log.d("ListonItemClickListener","Found URL in list item: "+position);
                    downloadMessageFile(chat.get(position).getMessage());
                }
            }
        });
        // Instantiate the MessageArrayAdapter
        chatAdapter = new MessageArrayAdapter(getActivity(), R.id.message_element, chat);

        // Set the adapter to the messageView
        messagesView.setAdapter(chatAdapter);

        // Run method with instantiation of tabs and viewpager
        setChatListener();

        // Return initiated View
        return chatView;
    }
    // Method to download file given in a chat-message
    public void downloadMessageFile(String message){
        /**
         * Order of information to download
         * owner : of file
         * filename : of file on server
         * filedestination : locally
         * username : of user
         * password : of user
         */
        String[] filePathFixer = message.split("/");
        fileOwner = filePathFixer[filePathFixer.length-2];
        fileDownload = filePathFixer[filePathFixer.length-1];
        // Testing purposes, removed in android live testing
        System.out.println("User: " + fileOwner + " File: " + fileDownload);

        // Checking current activity is active one
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission from user
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

            /**
             * MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE is an
             * app-defined int constant. The callback method gets the
             * result of the request.
              */

        }else{
            // If no authorization from user required, run download
            new DownloadFile().execute();
            tryingDownloadDialog();
        }
    }
    // To handle new changes in Android 6.0 in terms of user authorization
    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults){

        switch(permsRequestCode){
            case 10:
                Log.d("Permission",Boolean.toString(grantResults[0]== PackageManager.PERMISSION_GRANTED));
                new DownloadFile().execute();
                tryingDownloadDialog();
                break;
            default:
                makeToast("Permission Denied");
                break;
        }
    }

    // AsyncTask class for sending file to fileserver
    private class DownloadFile extends AsyncTask<URL,Integer,String> {
        protected String doInBackground(URL... urls) {
            // Set up filepath to use for servercall
            // Set state to test the resource is available

            final String state = Environment.getExternalStorageState();
            // Get file-names from /microchat_chats directory of sdcard storage
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                //instantiate storage directory
                String fileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();

                // Testing purposes, removed in live android testing
                //System.out.println("FilePath to download server: " + filepath);
                try {
                    // return reply from server
                    Log.d("FilePath",fileDir);
                    return Integer.toString(fileServer.downloadFile(fileOwner,fileDownload,fileDir+"/",app.getUsername(),app.getPwd()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
        protected void onPostExecute(String result) {
            // Send notice through toast and System.out.println to user and log
            if(result != null){
                if(result.contains("200")){
                    makeToast("File has been Downloaded to device");
                }
                else if(result.contains("404")){
                    makeToast("File doesn't exist on the server");
                }
            }else if(result == null){
                makeToast("File doesn't exist on the server");
            }
            downloadDialog.dismiss();
            Log.d("Reply from server", "Download: " + result);
        }
    }
    // Give notice to user when server replies
    private void makeToast(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG)
                .show();
    }
    // Method for receiving result from ShareFileChat Activity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            // Use the result from ShareFileChat to send FileMessage to current chatroom
            if(resultCode == Activity.RESULT_OK){
                String result=data.getStringExtra("result");
                sendFileMessage(result);
            }
            // In case some failed notification is to be implemented
            if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
    }//onActivityResult

    /*
     Getter for currently active chatrooms chatlog, currently only used for
     Testing purposes with System.out.println()
      */
    public ArrayList<Message> getChat(){
        for(Message message:chat){
            System.out.println(message.toString());
        }
        return chat;
    }

    // Specialized sendMessage to include file-info into message
    public void sendFileMessage(String serverPath){
        Message newMessage;
        // Test if user has an alias to use as name in message
        if(app.getNickname() != ""){
            newMessage = new Message(app.getNickname(), fileServer.getRestService() +app.getUsername()+"/"+ serverPath.substring(2,serverPath.length()-1));
        }else{
            newMessage = new Message(getArguments().getString(FragmentChat.CURRENT_USER_NAME), fileServer.getRestService() +app.getUsername()+"/"+ serverPath.substring(2,serverPath.length()-1));
        }
        // (Firebase) Test to see if Chat-room exists in users chatrooms directory
        if(app.getCurrentRooms().contains(getArguments().getString(FragmentChat.CURRENT_CHAT_NAME))){
            //Testing purposes, removed for live android testing
            //System.out.println("Contains room tries to send: ");
            ref.child("chat-rooms/" + getArguments().getString(FragmentChat.CURRENT_CHAT_NAME)).push().setValue(newMessage);
        }
        // (Firebase) If it doesn't exist in user chatroom directory, add it and create chat
        else{
            System.out.println("Doesn't contain room name");
            HashMap authenticationChatroom = new HashMap<>();
            authenticationChatroom.put(getArguments().getString(FragmentChat.CURRENT_CHAT_NAME),"");
            // Adds the room to users/chatrooms/ directory of (Firebase)
            ref.child("users/"+getArguments().getString(FragmentChat.CURRENT_USER_NAME)+"/chatrooms/").updateChildren(authenticationChatroom);
            /*
             Adds the message to existing or new room in chat-rooms directory,
             if chat doesn't exist it will be created and message pushed.
              */
            ref.child("chat-rooms/" + getArguments().getString(FragmentChat.CURRENT_CHAT_NAME)).push().setValue(newMessage);
        }
    }

    // Send message to Firebase method
    public void sendMessage(String message){
        // Test to ensure an empty message isn't sent
        if(!message.equals("")) {
            Message newMessage;
            // Test if user has an alias/nickname
            if(app.getNickname() != ""){
                Log.d("Nickname found","success");
                newMessage = new Message(app.getNickname(), message);
            }else{
                newMessage = new Message(getArguments().getString(FragmentChat.CURRENT_USER_NAME), message);
            }
            // Test if chatroom exists in the users/user/chatrooms directory
            if(app.getCurrentRooms().contains(getArguments().getString(FragmentChat.CURRENT_CHAT_NAME))){
                System.out.println("Contains room tries to send: ");
                ref.child("chat-rooms/" + getArguments().getString(FragmentChat.CURRENT_CHAT_NAME)).push().setValue(newMessage);
            }
            /*
            If it doesn't exist add it to the users/user/chatrooms directory,
            then push message to chat-rooms/roomname chat (Firebase)
             */
            else{
                Log.d("SendMessage","Doesn't contain room name");
                HashMap authenticationChatroom = new HashMap<>();
                authenticationChatroom.put(getArguments().getString(FragmentChat.CURRENT_CHAT_NAME),"");
                // Adds the room to the users/user/chatrooms directory (Firebase)
                ref.child("users/"+getArguments().getString(FragmentChat.CURRENT_USER_NAME)+"/chatrooms/").updateChildren(authenticationChatroom);
                // Pushes message to new or existing chatroom in chat-rooms directory (Firebase)
                ref.child("chat-rooms/" + getArguments().getString(FragmentChat.CURRENT_CHAT_NAME)).push().setValue(newMessage);
            }
        }
    }
    /*
    Set up listener to the current chat, so that new messages will be pushed by (Firebase)
    to this chat
     */
    public void setChatListener(){
        app.getFirebase().child("chat-rooms/" + getArguments().getString(FragmentChat.CURRENT_CHAT_NAME)).addChildEventListener(new ChildEventListener() {
            // Variable for testing purposes
            private DataSnapshot testing;
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                testing = dataSnapshot;
                /*
                 Since the chat expects a Message.class object for json parsing
                 there is a test to make sure the password property of a chat
                 doesn't crash the application at runtime
                  */
                if(dataSnapshot.getKey() != "password" && dataSnapshot.getKey() != null){
                    // Make Message variable and push to message-list
                    Message newMessage = dataSnapshot.getValue(Message.class);
                    chat.add(newMessage);
                    // Check for File in message
                    if(newMessage.getMessage() != null){
                        if(newMessage.getMessage().contains(fileServer.getRestService())) {
                            urlList.add(chat.size()-1);
                            //Testing purposes, removed on live android testing
                        /*System.out.println("Added file-message: position="+urlList.get(urlList.size()-1));
                        System.out.println("Message: " + chat.get(urlList.get(urlList.size()-1)));*/
                        }
                    }
                    // Notify adapter that data has been added
                    chatAdapter.notifyDataSetChanged();
                }
                // Make the list-view set its selection to newest message
                messagesView.setSelection(chat.size()-1);
                // Update HashMap of Chats in ChatApplication.java
                app.setRoomChat(getArguments().getString(FragmentChat.CURRENT_CHAT_NAME),chat);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                /*
                 Since the chat expects a Message.class object for json parsing
                 there is a test to make sure the password property of a chat
                 doesn't crash the application at runtime, mainly for testing
                 purposes as changing message-items hasn't been implemented yet.
                  */
                if(!dataSnapshot.getKey().equals("password")){
                    // Make Message variable and push to message-list
                    Message newMessage = dataSnapshot.getValue(Message.class);
                    chat.add(newMessage);
                    // Notify adapter that data has been added
                    chatAdapter.notifyDataSetChanged();
                }
                // Make the list-view set its selection to newest message
                messagesView.setSelection(chat.size()-1);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // Make the list-view set its selection to newest message
                messagesView.setSelection(chat.size()-1);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                // For testing purposes
                testing = dataSnapshot;
                // Make the list-view set its selection to newest message
                messagesView.setSelection(chat.size()-1);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                /*
                The two errors found with current testing are handled based on
                cause, permission denied has shown to be wrong password for
                chat, so alertWindow is run to ask for password from user
                 */
                System.out.println("The message read failed: " + firebaseError.getMessage());
                if(firebaseError.getMessage().equals("Permission denied")){
                    AlertWindow passwordRequest = new AlertWindow();
                    passwordRequest.typeAlertWindow(getActivity(),"Password Required", "Type password for Chat: "+getArguments().getString(FragmentChat.CURRENT_CHAT_NAME),getArguments().getString(FragmentChat.CURRENT_CHAT_NAME));
                }
                // Nothing to handle this far in implementation in regards to the error below
                else if(firebaseError.getMessage().equals("This client does not have permission to perform this operation")){
                    System.out.println("Firebase error permission to perform: " + testing);
                }
            }

        });
    }
}