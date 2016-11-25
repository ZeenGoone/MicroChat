package gruppe22.dtu.dk.mychat.Utilities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import gruppe22.dtu.dk.mychat.Activities.ChatController;
import gruppe22.dtu.dk.mychat.Activities.FragmentChat;
import gruppe22.dtu.dk.mychat.ChatApplication;

/**
 * Created by zeeng on 30/04/2016.
 */
public class ChatPager extends FragmentStatePagerAdapter {

    private int chatTabCount;

    public ChatPager(FragmentManager fm, int chatTabCount) {
        super(fm);
        // Set current Number of Chats
        this.chatTabCount = chatTabCount;
    }

    @Override
    public Fragment getItem(int position) {
        //Initiate Application instance
        ChatApplication app = new ChatApplication();
        //Initiate a bundle
        Bundle args = new Bundle();
        //Put current data for room into bundle
        args.putString(FragmentChat.CURRENT_CHAT_NAME, app.getRoomsList()[position].toString());
        args.putString(FragmentChat.CURRENT_USER_NAME, app.getUsername());
        args.putInt(FragmentChat.POSITION_KEY, ChatController.tabIndex);
        //Create newInstance of fragment with bundle attached
        Fragment test = FragmentChat.newInstance(args);
        return test;
    }
    //Get current tabcount
    @Override
    public int getCount() {
        return chatTabCount;
    }

   /* @Override
    public int getItemPosition(Object object) { return POSITION_NONE; }*/

}
