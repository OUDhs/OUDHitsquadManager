package com.pressy4pie.oudhs.manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class ImageDumpingActivity extends Activity {
    private WelcomeAdapter mAdapter;
    public String working_dir_sh = "/sdcard/OudHSManager";
    public String hardware = detect_location();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_dumping);

        //print this for debugging && logging
        root_tools.logger("===Entering Dumping Menu===");
        root_tools.logger("Hardware is: " + hardware);

        //must be final for the list view.
        final String[] PartitionList;
        PartitionList = MakePartitionArray();

        mAdapter = new WelcomeAdapter(this);
        //build the list view. The first two will always be the same. items 3-PartitionLIst.lengeth will be different
        mAdapter.add(new WelcomeItem("Dump Build.prop", "This prints the value of build.prop to be saved or simply viewed.")); //case 0
        mAdapter.add(new WelcomeItem("Do Something Custom", "Be very careful of this.")); //case 1
        for(int i = 0; i < PartitionList.length; i++){
            mAdapter.add(new WelcomeItem(String.valueOf(PartitionList[i]), "Dump " + String.valueOf(PartitionList[i]) + " partition to img on storage."));
        }

        //list view adapter
        ListView lv = (ListView) findViewById(R.id.dump_list);
        lv.setAdapter(mAdapter);

        //execute what gets clicked.
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //headerEntries is the number of entries that are NOT actual partitions. Right now it is only build.prop, and the custom dumper.
                //just in case we add something else.
                int headerEntries = 2;
                    switch (position) {
                        case 0:
                            //case 0 is always the build prop dumper.
                            Toast.makeText(getApplicationContext(), "Build.prop dumping is still a Work In Progress, sorry...", Toast.LENGTH_LONG).show();
                            break;

                        case 1:
                            //case 1 is always the custom image maker.
                            //build the alert.
                            AlertDialog.Builder alert = new AlertDialog.Builder(ImageDumpingActivity.this);

                            alert.setTitle("Type A Custom Image Location");
                            alert.setMessage("This shouldn't Cause any harm to your device, but use caution anyway. if you try to dump a huge partition or something big it will take a while.");

                            // Set an EditText view to get user input
                            final EditText input = new EditText(getApplicationContext());
                            alert.setView(input);
                            //set color because it shows up as white on my white background.
                            input.setTextColor(Color.BLACK);

                            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    String value = String.valueOf(input.getText());
                                    // Do something with value!
                                    root_tools.logger(value);
                                    if (!root_tools.fileExists(value)){
                                        //check to see if the value typed exists.
                                        Toast.makeText(getApplicationContext(), value + " does not appear to exist, sorry", Toast.LENGTH_LONG).show();
                                    }
                                    else {
                                        //it exists
                                        Toast.makeText(getApplicationContext(), "This could take a while...", Toast.LENGTH_LONG).show();
                                        String CustomDumpCmd = "dd if=" + value + " of=" + working_dir_sh + "/custom.img";
                                        //dump it.
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
                        default:
                            //Determine which partition user clicked
                            final String clicked = String.valueOf(PartitionList[position - headerEntries]);
                            AlertDialog.Builder dumpBuilder = new AlertDialog.Builder(ImageDumpingActivity.this);

                            //the user knows best:
                            dumpBuilder.setTitle("Dumping the Partition: "+ String.valueOf(PartitionList[position - headerEntries]));
                            dumpBuilder.setMessage("Is this right?");

                            dumpBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int which) {
                                    //yes button clicked. Continue with dump
                                    String dumpCmd = null;
                                    if(hardware.equals("msm")) {
                                       dumpCmd = "dd if=/dev/block/platform/msm_sdcc.1/by-name/" + clicked + " of=" + working_dir_sh + "/" + clicked + ".img";
                                    }
                                    else if(hardware.equals("omap")) {
                                        dumpCmd = "dd if=/dev/block/platform/omap/omap_hsmmc.0/by-name/" + clicked + " of=" + working_dir_sh + "/" + clicked + ".img";
                                    }
                                    else {
                                        dumpCmd = "dd if=/dev/block/" + clicked + "of=" + working_dir_sh + "/" + clicked + ".img";
                                    }
                                    // log it, do it.
                                    root_tools.logger("\ndd cmd to execute: \n" + dumpCmd);
                                    root_tools.execute(dumpCmd);
                                }
                            });

                            dumpBuilder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //no button clicked...
                                    dialog.dismiss();
                                }
                            });
                            AlertDialog DumpAlert = dumpBuilder.create();
                            DumpAlert.show();
                    }
                }
        });
    }

    public String detect_location(){
        String location = null;

        //recovery is just used for testing because all devices should have that.
        //boot or system would be just as effective.
        if(root_tools.fileExists("/dev/block/platform/msm_sdcc.1/by-name/recovery")){
            location = "msm";
        }
        else if(root_tools.fileExists("/dev/block/platform/omap/omap_hsmmc.0/by-name/recovery")){
            location = "omap";
        }
        else location = "couldnotdetect";
        return location;
    }

    public String[] MakePartitionArray() {
        File listTxt = new File(working_dir_sh + "/partitions.txt");
        if(!listTxt.exists()){
            //make sure it exists
            //and fill it with content
            //we use root to do this because we get permission errors otherwise
            try {
                listTxt.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //list the partition depending on hardware
            if(hardware.equals("msm")) {
                //where current msm devices list partitions
                root_tools.execute("ls /dev/block/platform/msm_sdcc.1/by-name/ > " + String.valueOf(listTxt));
            }
            else if(hardware.equals("omap")){
                //where current omap devices list partitions
                root_tools.execute("ls /dev/block/platform/omap/omap_hsmmc.0/by-name/ > " + String.valueOf(listTxt));
            }
            else if(hardware.equals("couldnotdetect")){
                //not a known partition scheme
                // we will list the /dev/block directory and filter "mmc"
                //this does NOT show names of anything. just the raq partition numbers.
                root_tools.execute("ls /dev/block/* > " + String.valueOf(listTxt));
            }
        }

        int totalCountedPartitions = 0;
        try {
            totalCountedPartitions = countPartitions();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] returnme = new String[totalCountedPartitions];

        BufferedReader br = null;
        //the try/catch shit here is annoying, but it cant be put in the signature, because of the array needing to be final when called.
        try {
            br = new BufferedReader(new FileReader(String.valueOf(listTxt)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            String line = null;
            for (int i = 0; i < returnme.length; i++) {
                line = br.readLine();
                returnme[i] = line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnme;
    }

    public int countPartitions() throws IOException {
        //use the text file to count # of partitions

        BufferedReader counter = new BufferedReader(new FileReader(working_dir_sh + "/partitions.txt"));
        int counted = 0;
        while (counter.readLine() != null){
            counted++;
        }
        counter.close();

        //print the number of possible partitions to log
        root_tools.logger(String.valueOf("Number of partions detected: " + counted));
        //return it
        return counted;


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

        protected WelcomeItem(String Title, String Desc) {
            this.title = Title;
            this.description = Desc;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}
