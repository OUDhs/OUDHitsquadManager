package com.pressy4pie.oudhs.manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;


public class ImageDumpingActivity extends Activity {
    private WelcomeAdapter mAdapter;
    public String working_dir_sh = "/sdcard/OudHSManager/downloads";
    public String hardware = detect_location();

    //recovery images & stuff
    public String recovery_location;
    public String recovery_backup;

    //boot images
    public String boot_location;
    public String boot_backup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_dumping);

        SetEnvironment();
        root_tools.logger(hardware);

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
                            if(!hardware.equals("couldnotdetect")) {
                                //prompt for recovery image
                                AlertDialog.Builder prompt1 = new AlertDialog.Builder(ImageDumpingActivity.this);
                                //stylized in html cause i suck
                                prompt1.setMessage(Html.fromHtml
                                        ("This is going to read: " + "<br>" +
                                                "<b>" + recovery_location + "</b>" + " and create an image at: " +
                                                "<br>" + "<b>" +
                                                working_dir_sh + "/recovery_backup.img" + "</b>"))

                                        .setPositiveButton("Yes", RecoverybackupPrompt)
                                        .setNegativeButton("No", RecoverybackupPrompt).show();
                            }
                            else Toast.makeText(getApplicationContext(), "YOUR DEVICE IS UNSUPPORTED", Toast.LENGTH_LONG).show();
                            break;

                        case 1:
                            if(!hardware.equals("couldnotdetect")) {
                                //boot image
                                AlertDialog.Builder prompt2 = new AlertDialog.Builder(ImageDumpingActivity.this);
                                prompt2.setMessage(Html.fromHtml
                                        ("This is going to read: " + "<br>" +
                                                "<b>" + boot_location + "</b>" + " and create an image at: " +
                                                "<br>" + "<b>" +
                                                working_dir_sh + "/boot_backup.img" + "</b>"))

                                        .setPositiveButton("Yes", BootbackupPrompt)
                                        .setNegativeButton("No", BootbackupPrompt).show();
                            }
                            else Toast.makeText(getApplicationContext(), "YOUR DEVICE IS UNSUPPORTED", Toast.LENGTH_LONG).show();
                            break;

                        case 2:
                            //custom
                            //this is basically a DD wrapper

                            AlertDialog.Builder alert = new AlertDialog.Builder(ImageDumpingActivity.this);

                            alert.setTitle("Type A Custom Image Location");
                            alert.setMessage("This shouldn't Cause any harm to your device, but use caution anyway. if you try to dump a huge partition or something big it will take a while.");

                            // Set an EditText view to get user input
                            final EditText input = new EditText(getApplicationContext());
                            alert.setView(input);
                            input.setTextColor(Color.BLACK);

                            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    String value = String.valueOf(input.getText());
                                    // Do something with value!
                                    root_tools.logger(value);
                                    if (!root_tools.fileExists(value)){
                                        Toast.makeText(getApplicationContext(), value + " does not appear to exist, sorry", Toast.LENGTH_LONG).show();
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(), "This could take a while...", Toast.LENGTH_LONG).show();
                                        String CustomDumpCmd = "dd if=" + value + " of=" + working_dir_sh + "/custom.img";
                                        root_tools.execute(CustomDumpCmd);
                                        Toast.makeText(getApplicationContext(), "DONE", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // Canceled.
                                }
                            });

                            alert.show();

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

    public String detect_location(){
        String location = null;

        if(root_tools.fileExists("/dev/block/platform/msm_sdcc.1/by-name/recovery")){
            location = "msm";
        }
        else if(root_tools.fileExists("/dev/block/platform/omap/omap_hsmmc.0/by-name/recovery")){
            location = "omap";
        }
        else location = "couldnotdetect";
        return location;

    }

    public void SetEnvironment(){
        if(detect_location().equals("msm")) {
            root_tools.logger("Found Hardware as MSM");
            recovery_location = "/dev/block/msm_sdcc.1/by-name/recovery";
            recovery_backup = "dd if=" + recovery_location + " of=" + working_dir_sh + "/recovery_backup.img";
            boot_location = "/dev/block/msm_sdcc.1/by-name/boot";
            boot_backup = "dd if=" + boot_location + " of=" + working_dir_sh + "/boot_backup.img";
        }

        else if(detect_location().equals("omap")){
            root_tools.logger("Found Hardware as OMAP");
            recovery_location = "/dev/block/platform/omap/omap_hsmmc.0/by-name/recovery";
            recovery_backup = "dd if=" + recovery_location + " of=" + working_dir_sh + "/recovery_backup.img";
            boot_location = "/dev/block/platform/omap/omap_hsmmc.0/by-name/boot";
            boot_backup = "dd if=" + boot_location + " of=" + working_dir_sh + "/boot_backup.img";
        }

        else if(detect_location().equals("couldnotdetect")){
            root_tools.logger("Could not detect hardware");
        }

        else root_tools.logger("something went extremely wrong");
        root_tools.logger("Environment Initialized for Image Dumper");

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
