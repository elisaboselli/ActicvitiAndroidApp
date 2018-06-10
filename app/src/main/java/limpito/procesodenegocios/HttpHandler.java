package limpito.procesodenegocios;

import android.util.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by elisa on 28/5/18.
 */

public class HttpHandler {

    private final String POST = "POST";

    public HttpHandler() {
    }

    public String makeServiceCallAuth(String reqUrl, String user, String pass, String method, String body) {

        URL url;
        HttpURLConnection conn;
        BufferedReader rd;
        String line;
        String result = "";

        try {
            url = new URL(reqUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod(method);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("charset", "UTF-8");

            String userCredentials = user + ":" + pass;
            String basicAuth = "Basic " + android.util.Base64.encodeToString(userCredentials.getBytes(), Base64.NO_WRAP);
            conn.setRequestProperty("Authorization", basicAuth);

            if (POST.equals(method)) {
                //String input = "{ \"action\":\"complete\"} ";
                //String input = "{\"action\":\"complete\",\"variables\":[{\"name\":\"id_boolvalue\",\"value\":\"false\"}]}";
                String input = body;
                OutputStream os = conn.getOutputStream();
                os.write(input.getBytes());
                os.flush();
                os.close();
            }

            rd = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            while ((line = rd.readLine()) != null) {
                result += line;
            }
            rd.close();

            return result;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}