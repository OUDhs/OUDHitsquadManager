package com.pressy4pie.oudhs.manager;

/**
 * Created by pressy4pie on 8/9/2014.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import com.pressy4pie.oudhs.manager.root_tools;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class SplashScreen extends Activity {
    String device = root_tools.DeviceName();
    public int check = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Log.d("Device Name", "Device is: " + device);

        //start the fun stuff here.
        new PrefetchData().execute();
    }
        private class PrefetchData extends AsyncTask<Void, Void, Void> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... arg0) {
                //print device name to log.
                Log.d("Device Check", "Checking Device: " + device);
                parseJson();

                return null;
            }

            //get the json file
            public String getJson(){
                StringBuilder builder = new StringBuilder();
                HttpClient client = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet("http://pressy4pie.com/devices/devices.json");
                try {
                    HttpResponse response = client.execute(httpGet);
                    StatusLine statusLine = response.getStatusLine();
                    int statusCode = statusLine.getStatusCode();
                    if (statusCode == 200) {
                        HttpEntity entity = response.getEntity();
                        InputStream content = entity.getContent();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                        }
                    } else {
                        Log.e("Json get", "Failed to download file");
                    }
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return builder.toString();
            }

            //parse the json file and return the data too a string
            //and check if the device exists on the server
            public boolean parseJson(){
                String jsonData = null;
                String get = getJson();

                try {
                    JSONArray jArr = new JSONArray(get);
                    for (int i=0; i < jArr.length(); i++) {
                        Log.i("Device List", jArr.getString(i));
                        //giving it a string for debug purposes
                        jsonData = jArr.getString(i);
                    }


                    if(jArr.toString().contains("\"device\":\""+device+"\"")){
                        check = 1;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if ( check == 1) {
                    return true;
                }
                else return false;

            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                    Intent i = new Intent(SplashScreen.this, Main.class);
                i.putExtra("deviceCheck", check);
                    startActivity(i);
                // close this activity
                finish();
            }

        }
    }



