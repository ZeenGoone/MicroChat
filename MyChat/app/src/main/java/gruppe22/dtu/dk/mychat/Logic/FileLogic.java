package gruppe22.dtu.dk.mychat.Logic;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import gruppe22.dtu.dk.mychat.Logic.Message;

/**
 * Created by zeeng on 30/04/2016.
 */
public class FileLogic extends AppCompatActivity {

    public void writeFile(String currentRoom, ArrayList<Message> chat){
        //If storage can be written to
        if(isExternalStorageWritable()) {
            //instantiate storage directory
            String fileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();

            //Setup filename for chat-file saving
            String fname = "chatlog_from_" + currentRoom + ".chat";
            File file = new File(fileDir, fname).getAbsoluteFile();
            System.out.println("From file logic: " + file.toString());
            String data = "";
            try {
                //Try and write to the file
                FileOutputStream outputFileWrite = new FileOutputStream(file);
                /*
                Iterate through chat-list and write messages to string
                with newline after each message
                 */
                for (Message message : chat) {
                    data += message.getName() + ": " + message.getMessage() + "\n";
                }
                //Write output to file and flush + close output
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputFileWrite);
                outputStreamWriter.write(data);
                outputStreamWriter.close();
                outputFileWrite.flush();
                outputFileWrite.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            System.out.println("EXTERNAL MEDIA IS NOT MOUNTED!");
        }
    }
    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
