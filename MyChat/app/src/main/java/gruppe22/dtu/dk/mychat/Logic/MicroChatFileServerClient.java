package gruppe22.dtu.dk.mychat.Logic;
/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
/*
 Mainly left uncommented to stay true to Authors version,
 only altered code was to fit Android program
  */
import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.logging.Level;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

/**
 * MicroChatFileServerClient is a client application to interface with the MicroFileChatServer
 * @author www.codejava.net
 * Adapted to microChatFileServerClient by:
 * @author s072250 Tobias German Jørgensen adapted to microChatFileServerClient
 * @modified s974489 Charles Mathiesen to fit the Android App
 */
public class MicroChatFileServerClient extends AppCompatActivity{
    //private static final String REST_SERVICE = "http://localhost:8081/microChatFileServer/rest/files/";
    private static final String REST_SERVICE = "http://microchatfileserver.wdk.dk:8080/microChatFileServer/rest/files/";
    private static final String USER_AGENT = "microChatFileServerClient";
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 200;

    public MicroChatFileServerClient(){
    }

    /**
     *
     * @param pathToFile
     * @param username
     * @param password
     * @return
     * @throws IOException
     */
    public List<String> uploadFile(String pathToFile, String username, String password) throws IOException {
        final String boundary = "---" + System.currentTimeMillis() + "---";
        final String CHARSET = "UTF-8";
        final String LINE_FEED = "\r\n";
        File fileToUpload = new File(pathToFile);
        String filename = fileToUpload.getName();

        /*
        */
        URL url = new URL(REST_SERVICE + username + "?username=" + username +"&password=" + password);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn = buildHttpRequest(httpConn, "POST", true);

        /*
        */
        httpConn.setRequestProperty("Content-Type","multipart/form-data; boundary=" + boundary);
        httpConn.setRequestProperty("User-Agent", "microChatFileServerClient");
        OutputStream outputStream = httpConn.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, CHARSET),true);
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"").append(LINE_FEED);
        writer.append("Content-Type: application/octet-stream").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        /*
        */
        FileInputStream inputStream = new FileInputStream(fileToUpload);
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();

        //
        writer.append(LINE_FEED).flush();
        writer.append("--" + boundary + "--").append(LINE_FEED);
        writer.close();

        //
        return parseHttpResponse(httpConn);
    }
    /**
     * Small alteration to return server address
     */
    public String getRestService(){
        return REST_SERVICE;
    }
    /**
     *
     * @param owner
     * @param username
     * @param password
     * @return
     * @throws IOException
     */
    public List<String> listFiles(String owner, String username, String password) throws IOException {
        URL url = new URL(REST_SERVICE + owner + "?username=" + username +"&password=" + password);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn = buildHttpRequest(httpConn, "GET", false);
        return parseHttpResponse(httpConn);
    }

    /**
     *
     * @param owner
     * @param filename
     * @param fileDestination
     * @param username
     * @param password
     * @return
     * @throws IOException
     */
    public int downloadFile(String owner, String filename, String fileDestination, String username, String password) throws IOException {
        int status = 0;
        URL url = new URL(REST_SERVICE + owner + "/" + filename + "/" + "?username=" + username +"&password=" + password);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn = buildHttpRequest(httpConn, "GET", false);

        // checks server's status code first
        status = httpConn.getResponseCode();

        if (status == HttpURLConnection.HTTP_OK) {
            InputStream downloadedFile = httpConn.getInputStream();
            String filePath = fileDestination  + filename;
            File file = new File(filePath);
            FileOutputStream out = new FileOutputStream(file);
            int read = 0;
            byte[] bytes = new byte[1024];
            out = new FileOutputStream(new File(filePath));
            while ((read = downloadedFile.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();

            httpConn.disconnect();
        } else {
            throw new IOException("Server returned non-OK status: " + status);
        }
        return status;
    }

    /**
     *
     * @param owner
     * @param filename
     * @param username
     * @param password
     * @return
     * @throws IOException
     */
    public List<String> deleteFile(String owner, String filename, String username, String password) throws IOException{
        URL url = new URL(REST_SERVICE + owner + "/" + filename + "/" + "?username=" + username +"&password=" + password);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn = buildHttpRequest(httpConn, "DELETE", false);
        return parseHttpResponse(httpConn);
    }

    /**
     *
     * @param httpConn
     * @return
     * @throws IOException
     */
    private HttpURLConnection buildHttpRequest(HttpURLConnection httpConn, String method, boolean doOutput) throws IOException{
        httpConn.setRequestMethod(method);
        httpConn.setUseCaches(false);
        httpConn.setRequestProperty("User-Agent", USER_AGENT);
        httpConn.addRequestProperty("Accept", "*/*");
        httpConn.addRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
        httpConn.setDoOutput(doOutput); //TRUE if POST
        httpConn.setDoInput(true);
        return httpConn;
    }

    /**
     *
     * @param httpConn
     * @return
     * @throws IOException
     */
    private List<String> parseHttpResponse(HttpURLConnection httpConn) throws IOException{
        JsonObject jsonObject;
        // checks server's status code first
        int status = httpConn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
            JsonReader jsonReader = Json.createReader(reader);
            jsonObject = jsonReader.readObject();
            reader.close();
            jsonReader.close();
            httpConn.disconnect();
        } else {
            throw new IOException("Server returned non-OK status: " + status);
        }

        List<String> response = new ArrayList<>();
        response.add(jsonObject.getString("username"));
        JsonArray jsonArray = jsonObject.getJsonArray("filename");
        for(JsonValue value : jsonArray){
            response.add(value.toString());
        }
        return response;
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        String owner = "s072250";
        String downloadFile = "tomtom";
        String fileDestination = "/home/tgj/Downloads/test/";
        String username = "s072250";
        String password = "oathkeeper";
        String fileToUpload = "/home/tgj/Downloads/tomtom";

        MicroChatFileServerClient mpfu = new MicroChatFileServerClient();
        try {
            System.out.println("LIST_FILES: " + mpfu.listFiles(owner,username,password));
            System.out.println("UPLOAD_FILE: " + mpfu.uploadFile(fileToUpload,username,password));
            System.out.println("LIST_FILES: " + mpfu.listFiles(owner,username,password));
            System.out.println("DOWNLOAD_FILE: " + mpfu.downloadFile(owner,downloadFile,fileDestination,username,password));
            System.out.println("DELETE_FILES: " + mpfu.deleteFile(owner,downloadFile,username,password));
            System.out.println("LIST_FILES: " + mpfu.listFiles(owner,username,password));

        } catch (IOException ex) {
            Logger.getLogger(MicroChatFileServerClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}