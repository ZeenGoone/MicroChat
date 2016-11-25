package gruppe22.dtu.dk.mychat.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import gruppe22.dtu.dk.mychat.ChatApplication;
import gruppe22.dtu.dk.mychat.MainActivity;
import gruppe22.dtu.dk.mychat.Utilities.ChatPager;
import gruppe22.dtu.dk.mychat.Logic.FileLogic;
import gruppe22.dtu.dk.mychat.R;

/**
 * Created by zeeng on 01/05/2016.
 */
public class ChatController extends AppCompatActivity implements TabLayout.OnTabSelectedListener{
    //The tabIndex
    public static int tabIndex = 0;
    //The applications chat tablayout
    private TabLayout tabLayout;
    //The chats viewpager
    private ViewPager chatPager;
    private ChatPager adapter;
    //Static variables
    private static String currentActiveChat = "";
    private static String[] rooms;
    private static int currentPosition;
    private static Toolbar toolbar;
    //Setup ChatApplication instance
    ChatApplication app = new ChatApplication();
    //Broadcast response
    public static final String BROADCAST_RESTART_ACTIVITY = "ACTIVITY_RESTART_BROADCAST";
    //BroadcastReceiver initialized
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Reset and Recreate ChatController to update changes from (Firebase)
            if(intent.getAction().equals(BROADCAST_RESTART_ACTIVITY)) {
                tabLayout.removeAllTabs();
                chatPager.setAdapter(null);
                adapter=null;
                recreate();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setup Broadcastmanager
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_RESTART_ACTIVITY);
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter);

        //Initializing the tablayout
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        //Initializing viewPager
        chatPager = (ViewPager) findViewById(R.id.chatpager);

        //Adding toolbar to the activity
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Setup the viewpager and tabs
        setupTabFragments();
    }
    //Set currently viewed Chat
    public static void setCurrentActiveChat(String chat){
        currentActiveChat = chat;
    }
    //Initiate Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    //Setup switch to respond to menu-choices
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Setup AlertWindow
        AlertWindow promptUser = new AlertWindow();
        switch (item.getItemId()) {
            // Menu item Join chat selected
            case R.id.menu_new_chat:
                Toast.makeText(this, "New Chat Selected", Toast.LENGTH_SHORT)
                        .show();
                //Open alertwindow with correct title and message
                promptUser.typeAlertWindow(this,"Join Chatroom","Type Chatroom to Join","");
                break;
            // Menu item Save chat selected
            case R.id.menu_save_file:
                Toast.makeText(this, "Save File Selected", Toast.LENGTH_SHORT)
                        .show();
                //Initiate FileLogic for writing to device
                FileLogic fileWriter = new FileLogic();
                //Write the currently active chat to device external storage
                fileWriter.writeFile(rooms[currentPosition],app.getRoomChat(rooms[currentPosition]));
                break;
            // Menu item change nickname selected
            case R.id.menu_change_nick:
                Toast.makeText(this, "Change Nick Selected", Toast.LENGTH_SHORT)
                        .show();
                //Open alertwindow with correct title and message
                promptUser.typeAlertWindow(this,"Type NickName","Type New Nickname","");
                break;
            // Menu item change password selected
            case R.id.menu_change_pwd:
                Toast.makeText(this, "Change Password Selected", Toast.LENGTH_SHORT)
                        .show();
                //Start the ChangeServerPassword Activity to change server-password on userlevel
                Intent launchLoginScreen = new Intent(this, ChangeServerPassword.class);
                startActivity(launchLoginScreen);
                break;
            // Menu item settings selected
            case R.id.menu_change_chat_pwd:
                Toast.makeText(this, "Change Chat Password Selected", Toast.LENGTH_SHORT)
                        .show();
                //Open alertwindow with correct title and message
                promptUser.typeAlertWindow(this,"Set Chat Password","Type new password",currentActiveChat);
                break;
            case R.id.menu_refresh:
                Toast.makeText(this, "Refresh chats Selected", Toast.LENGTH_SHORT)
                        .show();
                //Send broadcast to reset the activity
                Intent RTReturn = new Intent(ChatController.BROADCAST_RESTART_ACTIVITY);
                LocalBroadcastManager.getInstance(getParent()).sendBroadcast(RTReturn);
                break;
            case R.id.menu_logout:
                // Notify application to reset lists and variables that are user dependant and restart mainactivity
                app.setNOTAuthenticated();
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);;
                startActivity(intent);
                break;
            default:
                break;
        }

        return true;
    }
    //Instantiate the views elements
    public void setupTabFragments(){
        //Remove current tabs from tablayout
        tabLayout.removeAllTabs();
        //Reset tabIndex to zero
        ChatController.tabIndex = 0;
        //Get rooms from the Application
        rooms = app.getRoomsList();
        //counter for tab-adding
        int tabcounter = 0;
        if(rooms.length>0){
            app.restartCounter = 0;
            for(String roomName:rooms){
                //For testing purposes
                System.out.println("Roomname: " + roomName);
                //Setup currently selected tab as first one upon creation
                if(tabcounter == 0){ tabLayout.addTab(tabLayout.newTab().setText(roomName),tabcounter,true); }
                else{ tabLayout.addTab(tabLayout.newTab().setText(roomName),tabcounter,false); }
                tabcounter++;
            }
        }else if(app.restartCounter < 3){
            //If no rooms have been received from (Firebase) recreate Activity as a delay might have happened
            this.recreate();
            app.restartCounter++;
        }else{
            Toast.makeText(this,"You are subscribed to no chats, press the menu to Join a chat",Toast.LENGTH_LONG).show();
        }
        //Create pager adapter
        adapter = new ChatPager(getSupportFragmentManager(), tabLayout.getTabCount());
        //Adding adapter to pager
        chatPager.setAdapter(adapter);
        //Adding onTabSelectedListener to swipe views
        tabLayout.setOnTabSelectedListener(this);
        //Addonpagelistener for the viewpager
        chatPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.setScrollPosition(position,0,true);
                //update currently active chat and position
                currentActiveChat = rooms[position];
                ChatController.tabIndex = position;
                //For testing purposes, removed for android live testing
                //System.out.println("Current Chat: " + currentActiveChat + " Located at Tab: " + ChatController.tabIndex);
            }
            //Future implementation purposes
            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {}
            @Override
            public void onPageScrollStateChanged(int arg0) {}

        });
        //Tell adapter changes has been made
        adapter.notifyDataSetChanged();
    }
    //When a tab is selected update viewpager and set currentchatname and position
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        Log.d("Currentchat",currentActiveChat);
        currentActiveChat = rooms[tab.getPosition()];
        currentPosition = tab.getPosition();
        chatPager.setCurrentItem(tab.getPosition());
    }
    //Rest is for future implementation purposes
    @Override
    public void onTabUnselected(TabLayout.Tab tab) { }

    @Override
    public void onTabReselected(TabLayout.Tab tab) { }

    @Override
    public void onStop(){
        super.onStop();
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
    }
    @Override
    public void onPause(){
        super.onPause();
    }
    @Override
    public void onResume(){
        super.onResume();
    }
}
