package com.example.initimagecapture;

import android.util.Base64;

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

public class makeRequest extends Thread {

    // Url to make request to, the HTTP method to use, and the purpose of request (getUserID, getUserImages etc.)
    private String url, httpMethod, action;
    private String username, imgToString, currentRowNumber;
    String[] data, field;

    // Result from the HTTP Request
    String result_data = "Empty";

    // Constructor for getUserImages
    public makeRequest(String url, String httpMethod, String action, String currentRowNumber) {
        this.url = url;
        this.httpMethod = httpMethod;
        this.action = action;
        this.currentRowNumber = currentRowNumber;
    }

    // Constructor for getUserId
    /*
    public makeRequest(String url, String httpMethod, String action, String username) {
        this.url = url;
        this.httpMethod = httpMethod;
        this.action = action;
        this.username = username;
    }
     */

    // Constructor for signUp
    public makeRequest(String url, String httpMethod, String action, String[] field, String[] data) {
        this.url = url;
        this.httpMethod = httpMethod;
        this.action = action;
        this.data = new String[data.length];
        this.field = new String[field.length];
        System.arraycopy(field, 0, this.field, 0, field.length);
        System.arraycopy(data, 0, this.data, 0, data.length);
    }

    // Constructor for imageUpload
    public makeRequest(String url, String httpMethod, String action, byte[] imgByteArray) {
        this.url = url;
        this.httpMethod = httpMethod;
        this.action = action;
        this.imgToString = Base64.encodeToString(imgByteArray, Base64.DEFAULT);
    }

    @Override
    public void run() {
        try {

            String UTF8 = "UTF-8", iso = "iso-8859-1";
            URL url = new URL(this.url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod(this.httpMethod);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, UTF8));
            StringBuilder post_data = new StringBuilder();

            switch(action) {
                case "signUp":
                case "logIn":
                    for (int i = 0; i < this.field.length; i++) {
                        post_data.append(URLEncoder.encode(this.field[i], "UTF-8")).append("=").append(URLEncoder.encode(this.data[i], UTF8)).append("&");
                    }
                    break;
                case "getUserImages":
                    post_data.append(URLEncoder.encode("user_id", "UTF-8")).append("=").append(URLEncoder.encode(String.valueOf(User.getUserId()), UTF8)).append("&");
                    post_data.append(URLEncoder.encode("currentRowNumber", "UTF-8")).append("=").append(URLEncoder.encode(String.valueOf(this.currentRowNumber), UTF8));
                    break;
                case "imageUpload":
                    post_data.append(URLEncoder.encode("user_id", "UTF-8")).append("=").append(URLEncoder.encode(String.valueOf(User.getUserId()), UTF8)).append("&");
                    post_data.append(URLEncoder.encode("image", "UTF-8")).append("=").append(URLEncoder.encode(this.imgToString, UTF8));
                    break;
                default:
                    break;
            }

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


        } catch(IOException e) {
            setData(e.toString());
        }
    }

    public Boolean startRequest() {
        makeRequest.this.start();
        return true;
    }

    public Boolean onComplete() {
        while(true) {
            if(!this.isAlive()) {
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
