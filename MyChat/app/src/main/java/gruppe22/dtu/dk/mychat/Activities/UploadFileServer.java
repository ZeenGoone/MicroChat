package gruppe22.dtu.dk.mychat.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import gruppe22.dtu.dk.mychat.ChatApplication;
import gruppe22.dtu.dk.mychat.Logic.MicroChatFileServerClient;
import gruppe22.dtu.dk.mychat.R;

/**
 * Created by zeeng on 03/05/2016.
 */
public class UploadFileServer extends AppCompatActivity {
    // Initialize variables
    private ChatApplication app;
    private ListView fileView;
    private ArrayList files = new ArrayList();
    private MicroChatFileServerClient fileClient = new MicroChatFileServerClient();
    private int filePosition;
    private ProgressDialog uploadDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set view and initialize listview
        setContentView(R.layout.activity_upload_file);
        fileView = (ListView) findViewById(R.id.list_choose_upload_file);

        // Instantiate Application for data
        app = new ChatApplication();

        // Set state to test the resource is available
        final String state = Environment.getExternalStorageState();
        // Get file-names from /microchat_chats directory of sdcard storage
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File downloadFiles = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()+"/");

            if(downloadFiles.list() != null){

                // Add file-names from directory to file-list
                Log.d("UploadFileServer", downloadFiles.list().toString());
                for(String file:downloadFiles.list()){
                    files.add(file);
                }
            }else{
                files.add("No files in directory");
            }

        }
        // Set itemclicklistener for listview
        fileView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Testing purposes, removed for live android testing
                //makeToast();
                // Note position of item that was clicked
                filePosition = position;
                // Run Async call to Server to send chosen File
                if(!files.contains("No files in directory")){
                    new SendFile().execute();
                    tryingUploadDialog();
                }
            }
        });
        // Set arrayadapter on listview for file-list
        fileView.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,files));
    }
    // Testing purposes, removed for live android testing
    /*private void makeToast(){
        Toast.makeText(this, "Refresh chats Selected", Toast.LENGTH_SHORT)
                .show();
    }*/
    // Show user something is being performed in the background
    private void tryingUploadDialog(){
        uploadDialog = ProgressDialog.show(this, "File Server",
                "Uploading", true);
    }
    // Give notice to user when server replies
    private void makeToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG)
                .show();
    }
    // AsyncTask class for sending file to fileserver
    private class SendFile extends AsyncTask<URL,Integer,String> {
        protected String doInBackground(URL... urls) {
            // Set up filepath to use for servercall
            String filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()+"/"+files.get(filePosition).toString();
            try {
                // return reply from server
                return fileClient.uploadFile(filepath,app.getUsername(),app.getPwd()).toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        protected void onPostExecute(String result) {
            // Send notice through toast and System.out.println to user and log
            System.out.println("Reply from server!: " + result);

            // Give result back to calling Activity
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result",result);
            setResult(Activity.RESULT_OK,returnIntent);
            if(result != null){
                if(result.contains("[")){
                    makeToast("File Uploaded");
                }
            }
            // Close dialog
            uploadDialog.dismiss();
            // End Activity
            finish();
        }
    }
}
