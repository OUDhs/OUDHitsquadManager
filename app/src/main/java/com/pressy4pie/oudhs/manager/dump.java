package com.pressy4pie.oudhs.manager;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;

import java.util.Scanner;

/**
 * Created by pressy4pie on 8/9/2014.
 */
public class dump extends Activity {
    public String device = root_tools.DeviceName();
    public final boolean is_in_devices()
    {
        String deviceFile = Environment.getExternalStorageDirectory() + "/devices.txt";
        boolean is = false;
        Scanner input = new Scanner (deviceFile);
        while(input.hasNextLine()){
            String lineFromFile = input.nextLine();
            if(lineFromFile.contains(device)) {
                is = true;
            }
        }
        if (is = true){
            return true;
        }
        else return false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dump);
    }
}
