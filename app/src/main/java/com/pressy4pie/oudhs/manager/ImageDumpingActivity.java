package com.pressy4pie.oudhs.manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;


public class ImageDumpingActivity extends Activity {
    private WelcomeAdapter mAdapter;
    public String working_dir_sh = "/sdcard/OudHSManager/downloads";

    //recovery images & stuff
    public String recovery_location = "/dev/block/msm_sdcc.1/by-name/recovery";
    public String recovery_backup = "dd if=" + recovery_location + " of=" + working_dir_sh + "/recovery_backup.img";

    //boot images
    public String boot_location = "/dev/block/msm_sdcc.1/by-name/boot";
    public String boot_backup = "dd if=" + boot_location + " of=" + working_dir_sh + "/boot_backup.img";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_dumping);

        mAdapter = new WelcomeAdapter(this);
        mAdapter.add(new WelcomeItem(R.string.dump_recovery, R.string.dump_recovery_desc));
        mAdapter.add(new WelcomeItem(R.string.dump_boot, R.string.dump_boot_desc));
        mAdapter.add(new WelcomeItem(R.string.dump_custom, R.string.dump_custom_desc));
        mAdapter.add(new WelcomeItem(R.string.dump_buildprop, R.string.dump_buildprop_desc));

        ListView lv = (ListView) findViewById(R.id.dump_list);
        lv.setAdapter(mAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        //prompt for recovery image
                        AlertDialog.Builder prompt1 = new AlertDialog.Builder(ImageDumpingActivity.this);
                        //stylized in html cause i suck
                        prompt1.setMessage(Html.fromHtml
                                ("This is going to read: " + "<br>" +
                                "<b>" +recovery_location + "</b>" + " and create an image at: " +
                                "<br>" + "<b>" +
                                working_dir_sh + "/recovery_backup.img" + "</b>"))

                                .setPositiveButton("Yes", RecoverybackupPrompt)
                                .setNegativeButton("No", RecoverybackupPrompt).show();
                        break;

                    case 1:
                        //boot image
                        AlertDialog.Builder prompt2 = new AlertDialog.Builder(ImageDumpingActivity.this);
                        prompt2.setMessage(Html.fromHtml
                                ("This is going to read: " + "<br>" +
                                        "<b>" +boot_location + "</b>" + " and create an image at: " +
                                        "<br>" + "<b>" +
                                        working_dir_sh + "/boot_backup.img" + "</b>"))

                                .setPositiveButton("Yes", BootbackupPrompt)
                                .setNegativeButton("No", BootbackupPrompt).show();
                        break;

                    case 2:
                        //custom
                        Toast.makeText(getApplicationContext(), "THIS ISN'T READY YET, SORRY", Toast.LENGTH_LONG).show();
                        break;

                    case 3:
                        //build.prop stuff
                        Toast.makeText(getApplicationContext(), "THIS ISN'T READY YET, SORRY", Toast.LENGTH_LONG).show();
                        break;
                }

            }
        });



    }

    DialogInterface.OnClickListener RecoverybackupPrompt = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                   //if the user agrees
                    //Toast.makeText(getApplicationContext(), "Executing: " + recovery_backup, Toast.LENGTH_LONG).show();
                    root_tools.execute(recovery_backup);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    DialogInterface.OnClickListener BootbackupPrompt = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //if the user agrees
                    root_tools.execute(boot_backup);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.image_dumping, menu);
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
