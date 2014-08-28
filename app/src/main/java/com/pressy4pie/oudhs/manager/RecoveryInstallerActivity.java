package com.pressy4pie.oudhs.manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import java.util.List;

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
    public String RecoveryInstallLocation = null;
    public String selected = null;
    //1 = cwm. 2 = stock
    public int choose;
    private WelcomeAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery_installer);

        mAdapter = new WelcomeAdapter(this);
        mAdapter.add(new WelcomeItem(R.string.Install, R.string.Install_description));
        mAdapter.add(new WelcomeItem(R.string.Restore, R.string.Restore_description));
        mAdapter.add(new WelcomeItem(R.string.Custom, R.string.Custom_description));
        mAdapter.add(new WelcomeItem(R.string.nothing, R.string.nothing_description));
        mAdapter.add(new WelcomeItem(R.string.Reboot, R.string.Reboot_description));

        ListView lv = (ListView) findViewById(R.id.recovery_list);
        lv.setAdapter(mAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        //prompt asking to download and install recovery
                        AlertDialog.Builder prompt1 = new AlertDialog.Builder(RecoveryInstallerActivity.this);
                        prompt1.setMessage("Ok to download and install recovery for device: " + device).setPositiveButton("Yes", recoveryprompt)
                                .setNegativeButton("No", recoveryprompt).show();
                        break;
                    case 1:
                        //prompt asking to download and install recovery
                        AlertDialog.Builder prompt2 = new AlertDialog.Builder(RecoveryInstallerActivity.this);
                        prompt2.setMessage("Ok to download and install  stock recovery for device: " + device).setPositiveButton("Yes", stockprompt)
                                .setNegativeButton("No", stockprompt).show();
                        break;
                    case 2:
                        //start the file browser activity
                        Intent i = new Intent(getApplicationContext(),file_browserActivity.class);
                        startActivityForResult(i, 1);
                        break;
                    case 3:
                        //nothing
                        Toast.makeText(getApplicationContext(), "This is not ready yet.", Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        //prompt asking to download and install recovery
                        AlertDialog.Builder prompt5 = new AlertDialog.Builder(RecoveryInstallerActivity.this);
                        prompt5.setMessage("Reboot to Recovery?").setPositiveButton("Yes", rebootPrompt)
                                .setNegativeButton("No", rebootPrompt).show();
                        break;
                }

            }
        });

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


    DialogInterface.OnClickListener customprompt = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    Log.d("Recovery Install", "Custom Image Flash Started!");
                    if(root_tools.fileExists("/dev/block/platform/msm_sdcc.1/by-name/recovery")) {
                        String dd_custom_install = "dd if=" + selected + " of= /dev/block/msm_sdcc.1/by-name/recovery";
                        Log.d("DD", "Custom Image Install: " + dd_custom_install);
                        root_tools.execute(dd_custom_install);
                        Log.d("DD", "Custom install appears to have completed!");
                    }

                    else if(!root_tools.fileExists("/dev/block/platform/msm_sdcc.1/by-name/recovery")){
                        Log.d("DD", "Custom Image Install: " + "This only works on msm devices");
                    }

                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                String result=data.getStringExtra("FILENAME");
                selected = Environment.getExternalStorageDirectory().toString() + "/" + result;
                Toast.makeText(getApplicationContext(), "You selected: " + selected, Toast.LENGTH_SHORT).show();

                //prompt asking to download and install recovery
                AlertDialog.Builder prompt = new AlertDialog.Builder(RecoveryInstallerActivity.this);
                prompt.setMessage("Ok to write: " + "\"" + selected + "\"" + " to " + device).setPositiveButton("Yes", customprompt)
                        .setNegativeButton("No", customprompt).show();
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }//onActivityResult

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
            RecoveryInstallLocation = installLocation();
            String RecoveryImageLocation =  imageName();
            String StockImageLocation = StockName();

                switch (choose)
                {
                    case 1:
                        Log.d("Check Files", "case 1");
                        //cwm
                        if (remote_file_exists("http://pressy4pie.com/devices/" + device + "/" + RecoveryImageLocation)) {
                            Log.d("Check Files", "Recovery Image found");
                            downloadFiles("http://pressy4pie.com/devices/" + device +  "/" + RecoveryImageLocation, working_dir + "/AfterMarket.img");
                        }
                        else {
                            Log.d("Check Files", "Recovery Image not found");
                            runOnUiThread(new Runnable() {
                                //toast for showing a failure
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
                            Log.d("Check Files", "Stock Recovery Image found");
                            Log.d("Check Files", "stock Image found");
                            downloadFiles("http://pressy4pie.com/devices/" + device + "/" +  StockImageLocation, working_dir + "/stock.img");
                        }
                        else {
                            Log.d("Check Files", "Stock Recovery Image not found");
                            //toast for showing a failure
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

        String imageName(){
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
                Log.d("File Download", "Download of " + filename + " has completed");
                root_tools.logger("Download of " + filename + " has completed");

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
                    //switch choose again to see what the user originally chose
                    case 1:
                        //cwm
                        if(root_tools.fileExists(working_dir_sh + "/AfterMarket.img")) {
                            String dd_install = "dd if=" + working_dir_sh + "/AfterMarket.img of=" + RecoveryInstallLocation;
                            Log.d("DD", "install: " + dd_install);
                            root_tools.logger("install: " + dd_install);

                            //this is the actual install
                            //it is commented so i dont acidentally write over my recovery
                            //root_tools.execute(dd_install);
                            Log.d("DD", "Install appears to have completed!");
                            root_tools.logger("Install appears to have completed!");
                        }
                        else {
                            Log.d("DD", "Something went wrong with install");
                            root_tools.logger("Something went wrong with install");
                        }

                        break;
                    case 2:
                        //stock
                        if(root_tools.fileExists(working_dir_sh + "/stock.img")) {
                            String dd_restore = "dd if=" + working_dir_sh + "/stock.img of=" + RecoveryInstallLocation;
                            Log.d("DD", "Restore: " + dd_restore);
                            root_tools.logger("restore appears to have completed!");
                            //root_tools.execute(dd_restore);
                            Log.d("DD", "restore appears to have completed!");
                            root_tools.logger("restore appears to have completed!");
                        }
                        else{
                            Log.d("DD", "Something went wrong with restore");
                            root_tools.logger("Something went wrong with restore");
                        }
                        break;
                }
            //make the progress bar invisible
            pbM.setVisibility(View.INVISIBLE);
            }
        }

    class WelcomeAdapter extends ArrayAdapter<WelcomeItem> {
        public WelcomeAdapter(Context context) {
            super(context, R.layout.list_item_welcome, android.R.id.text1);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            WelcomeItem item = getItem(position);

            TextView description = (TextView) view.findViewById(android.R.id.text2);
            description.setText(item.description);
            return view;
        }
    }

    class WelcomeItem {
        public final String title;
        public final String description;

        protected WelcomeItem(int titleResId, int descriptionResId) {
            this.title = getString(titleResId);
            this.description = getString(descriptionResId);
        }

        @Override
        public String toString() {
            return title;
        }
    }
}

