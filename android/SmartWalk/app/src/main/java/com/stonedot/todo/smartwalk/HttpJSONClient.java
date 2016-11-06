package com.stonedot.todo.smartwalk;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Created by goto on 2016/10/29.
 */

public class HttpJSONClient extends AsyncTask<Void, Void, HttpJSONClient.ResponseObject> {

    public class ResponseObject {
        public int code;
        public String statusMessage;
        public String content;

        public ResponseObject(int code, String statusMessage, String content) {
            this.code = code;
            this.statusMessage = statusMessage;
            this.content = content;
        }
    }

    public interface Responded {
        public void responded(int code, String statusMessage, String content);
    }

    private URL url;
    private String sendData;
    private Responded responseCallback;

    public HttpJSONClient(URL url, Map<String, String> data) {
        this.url = url;
        this.sendData = new JSONObject(data).toString();
    }

    public void post(Responded responseCallback) {
        this.responseCallback = responseCallback;
        execute();
    }

    @Override
    protected ResponseObject doInBackground(Void... params) {
        StringBuilder sb = new StringBuilder();
        int code = 500;
        String statusMessage = "";
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setInstanceFollowRedirects(false);
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            con.connect();
            try (OutputStream os = con.getOutputStream();
                 PrintStream ps = new PrintStream(os)) {
                ps.print(sendData);
            }
            code = con.getResponseCode();
            statusMessage = con.getResponseMessage();
            try (InputStreamReader isr = new InputStreamReader(con.getInputStream());
                 BufferedReader br = new BufferedReader(isr)) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }
            con.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("HttpJSONClient", "Failed to send json data.");
        }
        return new ResponseObject(code, statusMessage, sb.toString());
    }

    @Override
    protected void onPostExecute(ResponseObject obj) {
        if(responseCallback != null) {
            responseCallback.responded(obj.code, obj.statusMessage, obj.content);
        }
    }
}
