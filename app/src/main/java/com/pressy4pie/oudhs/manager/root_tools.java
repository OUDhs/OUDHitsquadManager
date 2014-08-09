package com.pressy4pie.oudhs.manager;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by pressy4pie on 8/9/2014.
 */

/*
Class with useful tools for a root environment
*/

public class root_tools {

    public static String DeviceName() {
    return readProp("ro.product.device");
    }
    /*
	 * executes a command as super user in an interactive shell
	 */
    public static BufferedReader execute(String cmd) {
        BufferedReader reader = null;
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(
                    process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            reader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            String err = (new BufferedReader(new InputStreamReader(
                    process.getErrorStream()))).readLine();
            os.flush();
            if (process.waitFor() != 0 || (!"".equals(err) && null != err && containsIllegals(err) != true) ) {
                Log.e("Root Error", err);
                return null;
            }

            return reader;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /*
     * Executes command as sh (not r00t) in an interactive shell
     */
    public static BufferedReader executeAsSH(String shcmd) {
        BufferedReader reader = null;
        try {
            Process process = Runtime.getRuntime().exec("/system/bin/sh");
            DataOutputStream os = new DataOutputStream(
                    process.getOutputStream());
            os.writeBytes(shcmd + "\n");
            os.writeBytes("exit\n");
            reader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            String err = (new BufferedReader(new InputStreamReader(
                    process.getErrorStream()))).readLine();
            os.flush();

            if (process.waitFor() != 0 || (!"".equals(err) && null != err)) {
                Log.e("SH error", err);
                return null;
            }

            return reader;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /*
     * works by using getprop to retrieve android prop
     */
    public static String readProp(String prop) {
        //this reads a prop as SH (not root) into a string
        Process p = null;
        String line = "";
        String propRead = "";


        try {
            p = new ProcessBuilder("/system/bin/getprop", prop).redirectErrorStream(true).start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

            while ((line=br.readLine()) != null){
                propRead = line;
            }
            p.destroy();

        } catch (IOException e) {
            Log.e("Get Prop", "Failed to read prop");
        }
        return propRead;

    }

    /*
     * Checks if is integer returns true if is
     */
    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

    /*
     * Checks to see if file FileToCheck exists
     */
    public static boolean fileExists(String FileToCheck){
        File f = new File(FileToCheck);
        if(f.exists()){
            return true;
        }
        else return false;
    }

    /*
     * checks for illegal characters that would throw an error in root_tools.execute
     */
    public static boolean containsIllegals(String toExamine) {
        Pattern pattern = Pattern.compile("[+]");
        Matcher matcher = pattern.matcher(toExamine);
        return matcher.find();
    }

    /*
     * Calculate checksum of a File using MD5 algorithm
     */
    public static String checkSum(String path){
        String checksum = null;
        try {
            FileInputStream fis = new FileInputStream(path);
            MessageDigest md = MessageDigest.getInstance("MD5");

            //Using MessageDigest update() method to provide input
            byte[] buffer = new byte[8192];
            int numOfBytesRead;
            while( (numOfBytesRead = fis.read(buffer)) > 0){
                md.update(buffer, 0, numOfBytesRead);
            }
            byte[] hash = md.digest();
            checksum = new BigInteger(1, hash).toString(16); //don't use this, truncates leading zero
        } catch (IOException ex) {
            //no
        } catch (NoSuchAlgorithmException ex) {
            //no
        }

        return checksum;
    }

    public static String readLine(String fileName, int toread)
    {
        String line = null;
        java.io.BufferedReader br = null;
        try
        {
            br = new java.io.BufferedReader(new java.io.FileReader(fileName));

            for (int i = 0; i < toread; i++)
            {
                line = br.readLine();
            }
        }catch(Exception e){
        }finally{
            if(br != null)
            {
                try{br.close();}catch(Exception e){}
            }
        }
        return line;
    }

    public boolean is_in_devices()
    {
        String device = root_tools.DeviceName();
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
}


