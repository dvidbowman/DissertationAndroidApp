package com.example.initimagecapture;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class getUserId extends Thread {
    private String url, method, usernameData;
    String result_data = "Empty";

    public getUserId(String url, String method, String username) {
        this.url = url;
        this.method = method;
        this.usernameData = username;

    }

    @Override
    public void run() {
        try {
            String UTF8 = "UTF-8", iso = "iso-8859-1";
            URL url = new URL(this.url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod(this.method);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, UTF8));
            StringBuilder post_data = new StringBuilder();


            post_data.append(URLEncoder.encode("username", "UTF-8")).append("=").append(URLEncoder.encode(usernameData, UTF8)).append("&");

            bufferedWriter.write(post_data.toString());
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();

            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, iso));
            StringBuilder result = new StringBuilder();
            String result_line;

            while ((result_line = bufferedReader.readLine()) != null) {
                result.append(result_line);
            }

            bufferedReader.close();
            inputStream.close();
            httpURLConnection.disconnect();
            setData(result.toString());

        } catch (IOException e) {
            setData(e.toString());
        }
    }

    public boolean startPut() {
        getUserId.this.start();
        return true;
    }

    public boolean onComplete() {
        while (true) {
            if (!this.isAlive()) {
                return true;
            }
        }
    }

    public String getResult() {
        return this.getData();
    }

    public void setData(String result_data) {
        this.result_data = result_data;
    }


    public String getData() {
        return result_data;
    }
}
