package gruppe22.dtu.dk.mychat.Logic;

import android.support.v7.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by zeeng on 24/04/2016.
 */
public class HttpGetToken extends AppCompatActivity {
    //Initialize variables
    private static URL url;
    private static String token;

    //Method to change password on authenticationserver
    public static String changePassword(String username, String oldpwd, String newpwd){
        //Instantiate variables
        URL relayServer = null;
        String inputLine = null, reponse = null;
        try {
            //Set relayserver URL for passwordchange
            relayServer = new URL("http://85.11.31.36:8080/RelayServer/account/changepsw?user="+username+"&oldPsw="+oldpwd+"&newPsw="+newpwd);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            //Open connection, buffer and read inputstream
            URLConnection tokenConnection = null;
            tokenConnection = relayServer.openConnection();
            BufferedReader input = null;
            input = new BufferedReader(new InputStreamReader(
                    tokenConnection.getInputStream()));
            while ((inputLine = input.readLine()) != null)
                if(inputLine.length() > 2)
                    reponse = inputLine;
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return reponse;
    }

    public static String getToken(String username, String pwd) {
        //Initiate variables
        URL relayServer = null;
        String inputLine = null, tempToken = null;
        try {
            //Set relayserver URL for token generation authentication
            relayServer = new URL("http://85.11.31.36:8080/RelayServer/account/auth?user=" + username + "&psw=" + pwd);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            //Setup connection, buffer the inputstream and return token
            URLConnection tokenConnection = null;
            tokenConnection = relayServer.openConnection();
            BufferedReader input = null;
            input = new BufferedReader(new InputStreamReader(
                    tokenConnection.getInputStream()));
            while ((inputLine = input.readLine()) != null)
                if(inputLine.length() > 10)
                    tempToken = inputLine;
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempToken;
    }
}
