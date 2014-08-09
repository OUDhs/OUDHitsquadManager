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
    public File sdCard = Environment.getExternalStorageDirectory();
    public String working_dir = sdCard + "/OudHSManager/downloads";
    //this is so dirty your parents will ground you for a week
    public String working_dir_sh = "/sdcard/OudHSManager/downloads";
    public boolean backup = false;
    public String device = root_tools.DeviceName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery_installer);

        File dir = new File (working_dir);
        dir.mkdirs();
        File file = new File(dir, "filename");

        Log.d("working dir", working_dir);
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
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            if(root_tools.fileExists(Environment.getExternalStorageDirectory() + "/cwm.img")){
                Log.d("Recovery Install", "Cwm image found, deleting.");
                String rm_recovery = "busybox rm " + working_dir_sh + "/cwm.img";
                root_tools.execute(rm_recovery);

            }
            else {
                Log.d("Recovery Install", "Cwm image not found");
                backup = true;

            }
            downloadFiles("http://pressy4pie.com/devices/" + device + "/cwm.img", working_dir + "/cwm.img");
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
                Log.i("File Download", "Downloading file too: " + filename);
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
            String dd_backup = "dd if=/dev/block/platform/msm_sdcc.1/by-name/recovery of= " + working_dir_sh + "/backup.img";
            String dd_install = "dd if="+ working_dir_sh + "/cwm.img of=/dev/block/platform/msm_sdcc.1/by-name/recovery";

            if(backup) {
                Log.d("backup?", "yes");
                Log.d("DD", "backup: " +  dd_backup);
                root_tools.execute(dd_backup);
            }

            Log.d("backup?", "no");
            Log.d("DD", "install: " + dd_install);
            root_tools.execute(dd_install);
            Log.d("DD", "Install appears to have completed!");
        }
}
}
