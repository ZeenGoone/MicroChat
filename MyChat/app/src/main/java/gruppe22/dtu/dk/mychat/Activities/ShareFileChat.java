package gruppe22.dtu.dk.mychat.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import gruppe22.dtu.dk.mychat.ChatApplication;
import gruppe22.dtu.dk.mychat.Logic.MicroChatFileServerClient;
import gruppe22.dtu.dk.mychat.R;

/**
 * Created by zeeng on 02/05/2016.
 */
public class ShareFileChat extends AppCompatActivity {
    // Initialize variables
    private ChatApplication app;
    private ListView fileView;
    private ArrayList files = new ArrayList();
    private MicroChatFileServerClient fileClient = new MicroChatFileServerClient();
    private int filePosition;
    private ProgressDialog waitForServer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set view and initialize listview
        setContentView(R.layout.activity_share_file);
        fileView = (ListView) findViewById(R.id.list_choose_download_file);

        // Instantiate Application for data
        app = new ChatApplication();

        // Run Async call to Server to send chosen File
        new SendFile().execute();
        // Show user an action is being performed in the background
        waitForServer = ProgressDialog.show(this, "Server Interaction",
                "Downloading List of files", true);
        // Set itemclicklistener for listview
        fileView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Testing purposes, removed on live android device
                //makeToast();
                // Note position of item that was clicked
                filePosition = position;

                // Give result back to calling Activity
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result",files.get(filePosition).toString());
                setResult(Activity.RESULT_OK,returnIntent);
                // End Activity
                finish();
            }
        });
    }
    // Testing purposes, removing now on live android device
    /*private void makeToast(){
        Toast.makeText(this, "Refresh chats Selected", Toast.LENGTH_SHORT)
                .show();
    }*/
    // Give notice to user when server replies
    private void makeToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG)
                .show();
    }
    private void setArrayAdapter(){
        // Set arrayadapter on listview for file-list
        fileView.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,files));
        waitForServer.dismiss();
    }
    // AsyncTask class for sending file to fileserver
    private class SendFile extends AsyncTask<URL,Integer,String> {
        protected String doInBackground(URL... urls) {
              try {
                // return reply from server
                return fileClient.listFiles(app.getUsername(),app.getUsername(),app.getPwd()).toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        protected void onPostExecute(String result) {
            System.out.println("Reply from server, ShareFile!: " + result);
            String[] fileList = result.split(",");

            fileList[0] = fileList[0].substring(1);
            fileList[fileList.length-1] = fileList[fileList.length-1].substring(0,fileList[fileList.length-1].length()-1);
            boolean firstItem = true;
            for(String item:fileList){
                if(firstItem){
                    firstItem = false;
                }else{
                    files.add(item);
                }
            }
            setArrayAdapter();
            // Send notice through toast and System.out.println to user and log
            makeToast("File list retrieved");
        }
    }
}
