package com.pressy4pie.oudhs.manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
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
import android.widget.Toast;

import java.util.Scanner;


public class Main extends ListActivity {
    public String device = root_tools.DeviceName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle extras = getIntent().getExtras();



        //String[] mainList = getResources().getStringArray(R.array.adobe_products);


        setListAdapter(ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.list_items, R.layout.list_item));

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.d("list view", String.valueOf(position + id));
                switch (position){
                    case 0:
                        //recovery options
                        if(is_in_devices()) {
                            Intent recovery = new Intent(getApplicationContext(), RecoveryInstallerActivity.class);
                            startActivity(recovery);
                        }
                        else Toast.makeText(getApplicationContext(), "Your device is unsupported.", Toast.LENGTH_LONG).show();
                        break;
                    case 1:
                        //image dumping options
                        Toast.makeText(getApplicationContext(), "This is not ready yet.", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        //contact
                        Toast.makeText(getApplicationContext(), "This is not ready yet.", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        //nothing
                        Toast.makeText(getApplicationContext(), "This is not ready yet.", Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        //nothing
                        Toast.makeText(getApplicationContext(), "This is not ready yet.", Toast.LENGTH_SHORT).show();
                        break;
                    case 5:
                        //nothing
                        Toast.makeText(getApplicationContext(), "This is not ready yet.", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });



        //determine what buttons to show, depending on if device is supported
        TextView top_support = (TextView) findViewById(R.id.text_top_support);
        TextView top_nosupport = (TextView) findViewById(R.id.text_top_nosupport);
        top_nosupport.setVisibility(View.GONE);

        if(!is_in_devices())
        {
            top_support.setVisibility(View.GONE);
            top_nosupport.setVisibility(View.VISIBLE);
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

    //display a cute little credits thing
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

    //probably going to change this around a bit
    public void recoveryInstaller(View view){
        //goto the recovery installer intent
        Intent intent = new Intent(this, RecoveryInstallerActivity.class);
        startActivity(intent);
    }

    //this doesnt exist yet. get over it.
    public void dump(View view){
        //goto the recovery installer intent
        Intent intent = new Intent(this, dump.class);
        startActivity(intent);
    }

}
