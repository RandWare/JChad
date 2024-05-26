package net.jchad.server.model.command.commands.about;

import com.google.gson.Gson;
import net.jchad.server.model.command.commands.BaseCommand;
import net.jchad.server.model.server.Server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.*;

public class AboutCommand extends BaseCommand {
    private static final URL repoURL;
    static {
        try {
                repoURL = URI.create("https://api.github.com/repos/randware/jchad/releases").toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Special mega ultra rare exception! The repo url in the about command is malformed", e);
        }
    }

    @Override
    public void execute(Server server, ArrayList<String> args) {

        Thread apiRequest = new Thread(new AboutAPIcall(repoURL, server.getMessageHandler(), server.getVersion()));
        apiRequest.start();

    }










    //DONT DELETE! This methode serves as an example to demonstrate on how to get a github repo
    /*private static Release[] getRepositoryReleases(URL urlTOReleases) {
        try {
            //Opens  the connection ot read the github json response
            HttpURLConnection connection = (HttpURLConnection) urlTOReleases.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");

            //Reads the response
            BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String currentLine;
            while ((currentLine = input.readLine()) != null) {
                response.append(currentLine);
            }
            //System.out.println(response);
            Gson gson = new Gson();
            return gson.fromJson(response.toString(), Release[].class);
        } catch (Exception e) {
            return null;
        }
    }*/
}
