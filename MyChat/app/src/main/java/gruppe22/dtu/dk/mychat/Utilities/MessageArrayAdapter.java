package gruppe22.dtu.dk.mychat.Utilities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import gruppe22.dtu.dk.mychat.Logic.Message;
import gruppe22.dtu.dk.mychat.R;

/**
 * Created by zeeng on 24/04/2016.
 */
public class MessageArrayAdapter extends ArrayAdapter<Message> {
    //Initiate variable
    private ArrayList<Message> items;

    //Set custom constructor for adapter
    public MessageArrayAdapter(Context context, int textViewResourceId, ArrayList<Message> items) {
        super(context, textViewResourceId, items);
        this.items = items;
    }
    //Method for setting the view with customadapter
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext())
                    .inflate(R.layout.message_list_element, parent, false);
        }
        TextView messageView = (TextView) convertView.findViewById(R.id.message_element);
        Message item = (Message) items.get(position);
        if (item!= null) {
            //Format of message-passing to the chat
            messageView.setText(item.getName() + ": " + item.getMessage());
        }
        return convertView;
    }
}