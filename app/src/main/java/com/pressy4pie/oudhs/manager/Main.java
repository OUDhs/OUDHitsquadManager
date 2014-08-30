package com.pressy4pie.oudhs.manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity {
    public String device = root_tools.DeviceName();
    private WelcomeAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdapter = new WelcomeAdapter(this);
        mAdapter.add(new WelcomeItem(R.string.RecoveryOptions, R.string.RecoveryOptions_description));
        mAdapter.add(new WelcomeItem(R.string.DumpOptions, R.string.DumpOptions_description));
        mAdapter.add(new WelcomeItem(R.string.ContactOptions, R.string.ContactOptions_description));
        mAdapter.add(new WelcomeItem(R.string.OtherOptions, R.string.OtherOptions_description));
        mAdapter.add(new WelcomeItem(R.string.LogOptions, R.string.LogOptions_description));
        mAdapter.add(new WelcomeItem(R.string.Info, R.string.Info_description));

        ListView lv = (ListView) findViewById(R.id.welcome_list);
        lv.setAdapter(mAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        //recovery options
                        if(is_in_devices()) {
                            Intent recovery = new Intent(getApplicationContext(), RecoveryInstallerActivity.class);
                            startActivity(recovery);
                        }
                        else Toast.makeText(getApplicationContext(), "Your device is unsupported or could not reach Pressy4pie.com", Toast.LENGTH_LONG).show();
                        break;
                    case 1:
                        //image dumping options
                        Intent dump = new Intent(getApplicationContext(), ImageDumpingActivity.class);
                        startActivity(dump);
                        break;
                    case 2:
                        //contact
                        Uri uri = Uri.parse("http://www.pressy4pie.com/devices/mobilesite");
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                        break;
                    case 3:
                        //other options
                        //Intent login = new Intent(getApplicationContext(), LoginActivity.class);
                        //startActivity(login);
                        //Toast.makeText(getApplicationContext(), "This is not ready yet.", Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        //log
                        Intent test = new Intent(getApplicationContext(), LogActivity.class);
                        startActivity(test);
                        break;
                    case 5:
                        //info
                        info();
                        break;
                }

            }
        });
    }

    //check to see if it is in devices
    public final boolean is_in_devices()
    {
        //grab the int from SplashScreen
        Bundle extras = getIntent().getExtras();
        int deviceChecker = extras.getInt("deviceCheck");

        //Check and return values
        if (deviceChecker == 1){
            Log.d("Device Check", device + " Is a supported Device.");
            root_tools.logger(device + " Is a supported Device.");
            return true;
        }
        else {
            Log.d("Device Check", device + " Is not a supported Device.");
            root_tools.logger(device + " Is not a supported Device.");
            return false;
        }
    }

    //display a cute little credits thing
    public void info() {
        AlertDialog.Builder about = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        about.setTitle("Developer Info");
        about.setView(inflater.inflate(R.layout.activity_info, null));
        about.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        about.show();
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
