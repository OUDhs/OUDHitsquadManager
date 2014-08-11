package com.pressy4pie.oudhs.manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Scanner;


public class Main extends Activity {
    public String device = root_tools.DeviceName();
    /*
    TODO: Parse device info from a json stored server side about supported devices..
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle extras = getIntent().getExtras();

        //determine what buttons to show, depending on if device is supported
        TextView top_support = (TextView) findViewById(R.id.text_top_support);
        TextView top_nosupport = (TextView) findViewById(R.id.text_top_nosupport);
        top_nosupport.setVisibility(View.GONE);

        Button recoveryButton, dumpbutton, three, four, five, six;
        recoveryButton = (Button) findViewById(R.id.btn_main_recovery);
        dumpbutton = (Button) findViewById(R.id.btn_main_dump);
        //these are unused for now
        three = (Button) findViewById(R.id.btn_main_3);
        four = (Button) findViewById(R.id.btn_main_4);
        five = (Button) findViewById(R.id.btn_main_5);
        six = (Button) findViewById(R.id.btn_main_6);

        //these are unused for now
        three.setVisibility(View.GONE);
        four.setVisibility(View.GONE);
        five.setVisibility(View.GONE);
        six.setVisibility(View.GONE);


        if(!is_in_devices())
        {
            top_support.setVisibility(View.GONE);
            top_nosupport.setVisibility(View.VISIBLE);
            recoveryButton.setVisibility(View.GONE);
        }
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
            return true;
        }
        else {
            Log.d("Device Check", device + " Is not a supported Device.");
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void info(View view) {
        AlertDialog.Builder about = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        about.setTitle("Developer Info");
        //about.setIcon(R.drawable.apple);
        about.setView(inflater.inflate(R.layout.activity_info, null));
        about.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        about.show();
    }

    public void recoveryInstaller(View view){
        //goto the recovery installer intent
        Intent intent = new Intent(this, RecoveryInstallerActivity.class);
        startActivity(intent);
    }

    public void dump(View view){
        //goto the recovery installer intent
        Intent intent = new Intent(this, dump.class);
        startActivity(intent);
    }

}
