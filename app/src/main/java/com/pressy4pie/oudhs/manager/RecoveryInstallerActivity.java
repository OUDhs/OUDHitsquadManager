package com.pressy4pie.oudhs.manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
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

/**
 * Created by pressy4pie on 8/9/2014.
 */


//TODO this will be completely revised :(

public class RecoveryInstallerActivity extends Activity {
    private ProgressBar pbM;
    private Handler mHandler = new Handler();
    public File sdCard = Environment.getExternalStorageDirectory();
    public String working_dir = sdCard + "/OudHSManager/downloads";
    //this is so dirty your parents will ground you for a week
    public String working_dir_sh = "/sdcard/OudHSManager/downloads";
    public String device = root_tools.DeviceName();
    //1 = cwm. 2 = stock
    public int choose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery_installer);

        File dir = new File (working_dir);
        dir.mkdirs();
        File file = new File(dir, "filename");
        Log.d("working dir", working_dir);

        //Progress Bar
        pbM = (ProgressBar) findViewById( R.id.pbDefault);
    }

    DialogInterface.OnClickListener recoveryprompt = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    Log.d("Recovery Install", "Starting download and install");
                    choose = 1;
                    pbM.setVisibility(View.VISIBLE);
                    new PrefetchData().execute();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };


    public void install(View view) {
        //prompt asking to download and install recovery
        AlertDialog.Builder prompt = new AlertDialog.Builder(RecoveryInstallerActivity.this);
        prompt.setMessage("Ok to download and install recovery for device: " + device).setPositiveButton("Yes", recoveryprompt)
                .setNegativeButton("No", recoveryprompt).show();
    }

    DialogInterface.OnClickListener stockprompt = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    Log.d("Recovery Install", "Starting download and install");
                    choose = 2;
                    pbM.setVisibility(View.VISIBLE);
                    new PrefetchData().execute();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };


    public void restore(View view) {
        //prompt asking to download and install recovery
        AlertDialog.Builder prompt = new AlertDialog.Builder(RecoveryInstallerActivity.this);
        prompt.setMessage("Ok to download and install  stock recovery for device: " + device).setPositiveButton("Yes", stockprompt)
                .setNegativeButton("No", stockprompt).show();
    }

    //prompt to reboot to recovery
    DialogInterface.OnClickListener rebootPrompt = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    root_tools.execute("reboot recovery");
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    public void reboot(View view) {
        //prompt asking to download and install recovery
        AlertDialog.Builder prompt = new AlertDialog.Builder(RecoveryInstallerActivity.this);
        prompt.setMessage("Reboot to Recovery?").setPositiveButton("Yes", rebootPrompt)
                .setNegativeButton("No", rebootPrompt).show();
    }


    private class PrefetchData extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           //no pre execute for now
        }

        @Override
        protected Void doInBackground(Void... arg0)
        {
            //first we must parse the json file to see what the recovery this need to be done in a more
            //effiecent way to be honest.

            String RecoveryInstallLocation = installLocation();
            String RecoveryImageLocation =  imageName();
            String StockImageLocation = StockName();

                switch (choose)
                {
                    case 1:
                        Log.d("Check Files", "case 1");
                        //cwm
                        if (remote_file_exists("http://pressy4pie.com/devices/" + device + "/" + RecoveryImageLocation)) {
                            Log.d("Check Files", "cwm Image found");
                            downloadFiles("http://pressy4pie.com/devices/" + device +  "/" + RecoveryImageLocation, working_dir + "/AfterMarket.img");
                        }
                        else {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "CWM Image cannot be found on the server ",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        break;
                    case 2:
                        Log.d("Check Files", "case 2");
                        //stock
                        Log.d("Check Files", "Checking for " + "http://pressy4pie.com/devices/" + device + "/" +  StockImageLocation);
                        if (remote_file_exists("http://pressy4pie.com/devices/" + device + "/" +  StockImageLocation)) {
                            Log.d("Check Files", "stock Image found");
                            downloadFiles("http://pressy4pie.com/devices/" + device + "/" +  StockImageLocation, working_dir + "/stock.img");
                        }
                        else {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Stock Image cannot be found on the server ",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                        break;
                }
                return null;
         }

        //get the json file
        public String getJson(){
            StringBuilder builder = new StringBuilder();
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet("http://pressy4pie.com/devices/" + device + "/device.json");
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

        //TODO make it so i dont have to download the file three times lol

        String installLocation(){
            String JsonFile = getJson();
            String FileName = null;
            try {
                JSONObject jObj = new JSONObject(JsonFile);
                FileName = jObj.getString("RecoveryImageName");
                Log.d("ImageName", FileName);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return FileName;
        }

        String imageName(){
            String JsonFile = getJson();
            String Location = null;
            try {
                JSONObject jObj = new JSONObject(JsonFile);
                Location = jObj.getString("RecoveryPartition");
                Log.d("ImageName", Location);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return Location;
        }

        String StockName(){
            String JsonFile = getJson();
            String StockLocation = null;
            try {
                JSONObject jObj = new JSONObject(JsonFile);
                StockLocation = jObj.getString("StockImageName");
                Log.d("ImageName", StockLocation);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return StockLocation;
        }

        public boolean remote_file_exists(String URLName){
            try {
                HttpURLConnection.setFollowRedirects(false);
                // note : you may also need
                //        HttpURLConnection.setInstanceFollowRedirects(false)
                HttpURLConnection con =
                        (HttpURLConnection) new URL(URLName).openConnection();
                con.setRequestMethod("HEAD");
                return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        public void downloadFiles(String file2get, String filename){
            try {
                //set the download URL, a url that points to a file on the internet
                //this is the file to be downloaded
                URL url = new URL(file2get);

                //create the new connection
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                //set up some things on the connection
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(true);

                //and connect!
                urlConnection.connect();

                //create a new file, specifying the path, and the filename
                //which we want to save the file as.
                File file = new File(filename);

                //this will be used to write the downloaded data into the file we created
                FileOutputStream fileOutput = new FileOutputStream(file);

                //this will be used in reading the data from the internet
                InputStream inputStream = urlConnection.getInputStream();

                //this is the total size of the file
                int totalSize = urlConnection.getContentLength();
                //variable to store total downloaded bytes
                int downloadedSize = 0;

                //create a buffer...
                byte[] buffer = new byte[1024];
                int bufferLength = 0; //used to store a temporary size of the buffer

                //now, read through the input buffer and write the contents to the file
                while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                    //add the data in the buffer to the file in the file output stream (the file on the sd card
                    fileOutput.write(buffer, 0, bufferLength);
                    //add up the size so we know how much is downloaded
                    downloadedSize += bufferLength;
                    //updateProgress(downloadedSize, totalSize, filename);
                    float percentage = ((float)downloadedSize / (float)totalSize) * 100;
                    updateProgress(percentage);
                    if((percentage % 10 == 0)) {
                        Log.d("download", String.valueOf(percentage));
                    }
                }
                Log.i("File Download", "Download of " + filename + " has completed");
                //close the output stream when done
                fileOutput.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void updateProgress(final float per){
            mHandler.post(new Runnable() {
                public void run() {
                    pbM.setProgress((int)per);
                }
            });
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            //todo make this less shitty
            //todo also add loki support
                //there is the potential of both files existing. which did the user select?
                switch (choose){
                    case 1:
                        //cwm
                        if(root_tools.fileExists(working_dir_sh + "/cwm.img")) {
                            String dd_install = "dd if=" + working_dir_sh + "/cwm.img of=/dev/block/platform/msm_sdcc.1/by-name/recovery";
                            Log.d("DD", "install: " + dd_install);
                           // root_tools.execute(dd_install);
                            Log.d("DD", "Install appears to have completed!");
                        }

                        break;
                    case 2:
                        //stock
                        if(root_tools.fileExists(working_dir_sh + "/stock.img")) {
                            String dd_restore = "dd if=" + working_dir_sh + "/stock.img of=/dev/block/platform/msm_sdcc.1/by-name/recovery";
                            Log.d("DD", "install: " + dd_restore);
                           // root_tools.execute(dd_restore);
                            Log.d("DD", "restore appears to have completed!");
                        }
                        break;
                }
            pbM.setVisibility(View.INVISIBLE);
            }
        }
}

