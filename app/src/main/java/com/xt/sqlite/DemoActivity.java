package com.xt.sqlite;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.xt.sqlite.bean.SettingDbBean;
import com.xt.sqlite.main.SelectTableActivity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class DemoActivity extends Activity implements View.OnClickListener {

    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        findViewById(R.id.copy).setOnClickListener(this);
        findViewById(R.id.jump).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.copy) {
            startThreadCopy();
        } else if (v.getId() == R.id.jump) {
            jump();
        }
    }


    private void startThreadCopy() {
        ((TextView) findViewById(R.id.copy)).setText("拷贝中");
        ((TextView) findViewById(R.id.copy)).setEnabled(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                copyAllDB();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.copy)).setText("拷贝完成");
                    }
                });
            }
        }).start();
    }

    private boolean copyAllDB() {
        try {
            File folder = new File(SettingDbBean.getDbSourceLocation(DemoActivity.this));
            if (!folder.exists()) {
                if (!folder.mkdirs()) {
                    return false;
                }
            }

            checkPerssion(folder);

            final AssetManager assets = getResources().getAssets();
            final String[] list = assets.list("");
            for (String filename : list) {
                copyOneDB(assets, filename);
            }
            Log.i("", "");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //文件夹权限检查
    public void checkPerssion(File folder) {
        folder.setExecutable(true, false);
    }

    private boolean copyOneDB(AssetManager assets, String filename) {
        if (!filename.endsWith("db")) {
            return false;
        }
        boolean isCopyed = true;
        File dbFile = null;
        File dbFileTemp = null;
        if (Build.VERSION.SDK_INT >= 16) {
            dbFile = new File(this.getDatabasePath(filename).getAbsolutePath());
            dbFileTemp = new File(this.getDatabasePath(filename + ".tmp").getAbsolutePath());
        } else {
            dbFile = new File(SettingDbBean.DB_PATH + "/" + filename);
            dbFileTemp = new File(SettingDbBean.DB_PATH + "/" + filename + ".tmp");
        }
        FileOutputStream fos = null;
        try {
            InputStream in = assets.open(filename);
            BufferedInputStream bin = new BufferedInputStream(in);
            int len = bin.available();
            byte[] data = new byte[len];
            bin.read(data);
            fos = new FileOutputStream(dbFileTemp);
            fos.write(data);
            fos.close();
            dbFileTemp.renameTo(dbFile);
            dbFile.setExecutable(true, false);
            dbFile.setReadable(true, false);
            dbFile.setWritable(true, false);
        } catch (IOException e) {
            isCopyed = false;
        } finally {
            if (dbFileTemp.exists()) {
                dbFileTemp.delete();
            }
        }
        return isCopyed;
    }

    private void jump() {
        Intent intent = new Intent();
        intent.setClass(this, SelectTableActivity.class);
        startActivity(intent);
    }
}
