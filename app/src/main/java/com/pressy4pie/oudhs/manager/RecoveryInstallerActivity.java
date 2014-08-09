package com.pressy4pie.oudhs.manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by pressy4pie on 8/9/2014.
 */

public class RecoveryInstallerActivity extends Activity {
    public boolean backup = false;
    public String device = root_tools.DeviceName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery_installer);
    }

    DialogInterface.OnClickListener recoveryprompt = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    new PrefetchData().execute();
                    Log.d("Recovery Install", "Starting download and install");
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

    private class PrefetchData extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            if(root_tools.fileExists(Environment.getExternalStorageDirectory() + "/cwm.img")){
                Log.d("Recovery Install", "Cwm image found, deleting.");
                root_tools.execute("busybox rm " + Environment.getExternalStorageDirectory() + "/cwm.img");

            }
            else {
                Log.d("Recovery Install", "Cwm image not found");
                backup = true;
                downloadFiles("http://pressy4pie.com/devices/" + device + "/cwm.img", "cwm.img");
            }
            return null;
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

                //set the path where we want to save the file
                //in this case, going to save it on the root directory of the
                //sd card.
                File SDCardDir = Environment.getExternalStorageDirectory();
                //create a new file, specifying the path, and the filename
                //which we want to save the file as.
                File file = new File(SDCardDir + "/" + filename);

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
                Log.i("File Download", "Downloading " + filename);
                while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                    //add the data in the buffer to the file in the file output stream (the file on the sd card
                    fileOutput.write(buffer, 0, bufferLength);
                    //add up the size so we know how much is downloaded
                    downloadedSize += bufferLength;
                    //updateProgress(downloadedSize, totalSize, filename);
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

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if(backup) {
                root_tools.execute("dd if=/dev/block/platform/msm_sdcc.1/by-name/recovery of= " + Environment.getExternalStorageDirectory() + "/backup.img");
            }

            root_tools.execute("dd if= "+ Environment.getExternalStorageDirectory() + "/cwm.img of=/dev/block/platform/msm_sdcc.1/by-name/recovery");
        }
}
}
