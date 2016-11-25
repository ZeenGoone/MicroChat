package gruppe22.dtu.dk.mychat.Activities;

import android.app.ProgressDialog;
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
 * Created by zeeng on 03/05/2016.
 */
public class DeleteFileServer extends AppCompatActivity {

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
        setContentView(R.layout.activity_delete_file);
        fileView = (ListView) findViewById(R.id.list_choose_file_delete);

        // Instantiate Application for data
        app = new ChatApplication();

        // Run Async call to Server to send chosen File
        new GetFilesServer().execute();
        // Show user an action is being performed in the background
        waitForServer = ProgressDialog.show(this, "Server Interaction",
                "Downloading List of files", true);
        // Set itemclicklistener for listview
        fileView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Testing purposes
                makeToast();
                // Note position of item that was clicked
                filePosition = position;
                new DeleteFile().execute();
                deleteProgress();
            }
        });
    }
    private void deleteProgress(){
        // Show user an action is being performed in the background
        waitForServer = ProgressDialog.show(this, "Server Interaction",
                "Deleting chosen file", true);
    }
    // Testing purposes
    private void makeToast(){
        Toast.makeText(this, "Refresh chats Selected", Toast.LENGTH_SHORT)
                .show();
    }
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
    // AsyncTask class for getting list of files from fileserver
    private class GetFilesServer extends AsyncTask<URL,Integer,String> {
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
            // Send notice through toast and System.out.println to user and log
            // For testing purposes, removed on live android testing
            //makeToast("Server replied for FileChosen in DeleteFile: " + result);
            //System.out.println("Reply from server, DeleteFile: " + result);
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
        }
    }
    // AsyncTask class for sending file to fileserver
    private class DeleteFile extends AsyncTask<URL,Integer,String> {
        protected String doInBackground(URL... urls) {
            // return reply from server
            try {
                System.out.println("Chosen file: " +files.get(filePosition).toString().substring(2,files.get(filePosition).toString().length()-1));
                return fileClient.deleteFile(app.getUsername(),files.get(filePosition).toString().substring(2,files.get(filePosition).toString().length()-1),app.getUsername(),app.getPwd()).toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        protected void onPostExecute(String result) {
            // Send notice through toast and System.out.println to user and log
            makeToast("File Deleted");
            System.out.println("Reply from server, DeleteFile: " + result);
            waitForServer.dismiss();
            // End Activity
            finish();
        }
    }
}
