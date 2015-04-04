package com.pressy4pie.oudhs.manager;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class OtherActivity extends Activity {
    public String working_dir_sh = Environment.getExternalStorageDirectory() + "/OudHSManager/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.other, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void ClearData(View view){
        //todo: give this an alert, make it prettier, take it off the main thread, etc
        //this empties the dir on the sdcard
        Log.i("SDCARD", "All data in  \" + working_dir_sh + \" Will be deleted");
        root_tools.logger("All data in  " + working_dir_sh + " Will be deleted");
        root_tools.execute("rm -rf  " + working_dir_sh);
    }
}
