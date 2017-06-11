package com.hoangthien.hackernews.utils;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;


public final class HttpUtils {

    private HttpUtils() {

    }

    public static String requestHttpGET(String url) {

        String response = null;
        try {
            URL uUrl = new URL(url);

            HttpURLConnection conn = (HttpURLConnection) uUrl.openConnection();
            conn.setReadTimeout(TConstants.TIME_OUT_WAIT);
            conn.setConnectTimeout(TConstants.TIME_OUT_WAIT);
            conn.setDoInput(true);

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                InputStream streamContent = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(streamContent));
                StringBuilder str = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    str.append(line);
                }
                response = str.toString();
            }

        } catch (IOException e) {
            Log.e("request error", " " + e.getMessage());
        }
        return response;

    }

    public static String requestHttpGETGzip(String url) {

        String response = null;
        try {
            URL uUrl = new URL(url);

            HttpURLConnection conn = (HttpURLConnection) uUrl.openConnection();
            conn.setRequestProperty("Accept-Encoding", "gzip");
            conn.setReadTimeout(TConstants.TIME_OUT_WAIT);
            conn.setConnectTimeout(TConstants.TIME_OUT_WAIT);
            conn.setDoInput(true);

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                InputStream streamContent = new GZIPInputStream(conn.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(streamContent));
                StringBuilder str = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    str.append(line);
                }
                response = str.toString();
            }

        } catch (IOException e) {
            Log.e("request error", " " + e.getMessage());
        }
        return response;

    }


    public static String requestHttpPOSTWithHeadrer(String url,
                                                    JSONObject params) {
        String response = null;
        try {
            URL uUrl = new URL(url);

            HttpURLConnection conn = (HttpURLConnection) uUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            DataOutputStream printout = new DataOutputStream(conn.getOutputStream());
            printout.write(params.toString().getBytes("UTF8"));
            printout.flush();
            printout.close();

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                InputStream streamContent = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(streamContent));
                StringBuilder str = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    str.append(line);
                }
                response = str.toString();
            }

        } catch (IOException e) {
            Log.e("request error", " " + e.getMessage());
        }
        return response;

    }


}
