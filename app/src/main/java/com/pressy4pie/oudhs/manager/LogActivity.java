package com.pressy4pie.oudhs.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class LogActivity extends Activity {
    public String working_dir = "/sdcard/OudHSManager/";
    public File mFileErrorLog= new File(working_dir + "/log.log");
    private File mFileErrorLogOld = new File(working_dir + "/log.old");
    private File mFileErrorLogOldOld = new File(working_dir + "/log.old.old");
    private static final int MAX_LOG_SIZE = 2*1024*1024; // 2 MB
    private TextView mTxtLog;
    private ScrollView mSVLog;
    private HorizontalScrollView mHSVLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        mTxtLog = (TextView) this.findViewById(R.id.txtLog);
        mSVLog = (ScrollView) this.findViewById(R.id.svLog);
        mHSVLog = (HorizontalScrollView) this.findViewById(R.id.hsvLog);
        reloadErrorLog();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_logs, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                reloadErrorLog();
                return true;
            case R.id.menu_save:
                save();
                return true;
            case R.id.menu_clear:
                clear();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void reloadErrorLog() {
        StringBuilder logContent = new StringBuilder(15 * 1024);
        try {
            FileInputStream fis = new FileInputStream(mFileErrorLog);
            long skipped = skipLargeFile(fis, mFileErrorLog.length());
            if (skipped > 0) {
                logContent.append("-----------------\n");
                logContent.append(getResources().getString(R.string.log_too_large, MAX_LOG_SIZE / 1024, skipped / 1024));
                logContent.append("\n-----------------\n\n");
            }
            Reader reader = new InputStreamReader(fis);
            char[] temp = new char[1024];
            int read;
            while ((read = reader.read(temp)) > 0) {
                logContent.append(temp, 0, read);
            }
            reader.close();
        } catch (IOException e) {
            logContent.append(getResources().getString(R.string.logs_load_failed));
            logContent.append('\n');
            logContent.append(e.getMessage());
        }

        if (logContent.length() > 0)
            mTxtLog.setText(logContent.toString());
        else
            mTxtLog.setText(R.string.log_is_empty);

        mSVLog.post(new Runnable() {
            @Override
            public void run() {
                mSVLog.scrollTo(0, mTxtLog.getHeight());
            }
        });
        mHSVLog.post(new Runnable() {
            @Override
            public void run() {
                mHSVLog.scrollTo(0, 0);
            }
        });
    }

    private void clear() {
        try {
            new FileOutputStream(mFileErrorLog).close();;
            mFileErrorLogOld.delete();
            Toast.makeText(this, R.string.logs_cleared, Toast.LENGTH_SHORT).show();
            reloadErrorLog();
        } catch (IOException e) {
            Toast.makeText(this,
                    getResources().getString(R.string.logs_clear_failed) + "\n" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            return;
        }
    }

    @SuppressLint("DefaultLocale")
    private void save() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, R.string.sdcard_not_writable, Toast.LENGTH_LONG).show();
            return;
        }

        Calendar now = Calendar.getInstance();
        String filename = String.format("OUDM_%s_%04d%02d%02d_%02d%02d%02d.log", "error",
                now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH),
                now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), now.get(Calendar.SECOND));
        File targetFile = new File(this.getExternalFilesDir(null), filename);

        try {
            FileInputStream in = new FileInputStream(mFileErrorLog);
            FileOutputStream out = new FileOutputStream(targetFile);

            long skipped = skipLargeFile(in, mFileErrorLog.length());
            if (skipped > 0) {
                StringBuilder logContent = new StringBuilder(512);
                logContent.append("-----------------\n");
                logContent.append(getResources().getString(R.string.log_too_large, MAX_LOG_SIZE / 1024, skipped / 1024));
                logContent.append("\n-----------------\n\n");
                out.write(logContent.toString().getBytes());
            }

            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            in.close();
            out.close();
        } catch (IOException e) {
            Toast.makeText(this,
                    getResources().getString(R.string.logs_save_failed) + "\n" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, targetFile.toString(), Toast.LENGTH_LONG).show();
    }

    private long skipLargeFile(InputStream is, long length) throws IOException {
        if (length < MAX_LOG_SIZE)
            return 0;

        long skipped = length - MAX_LOG_SIZE;
        long yetToSkip = skipped;
        do {
            yetToSkip -= is.skip(yetToSkip);
        } while (yetToSkip > 0);

        int c;
        do {
            c = is.read();
            if (c == -1)
                break;
            skipped++;
        } while (c != '\n');

        return skipped;
    }
}