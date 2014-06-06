package com.physicaloid.app.alcoholmeter;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;

import com.physicaloid.lib.Boards;
import com.physicaloid.lib.Physicaloid;

public class AlcoholMeterActivity extends Activity {
    private static final String TAG = AlcoholMeterActivity.class.getSimpleName();

    SurfaceView mSvBeer;
    AlcoholMeterSurfaceView mAlcoholeMeterSV;

    Physicaloid mPhysicaloid;
    AlcoholMeter mAlcohol;

    private boolean mDemo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alcohol_meter);

        mSvBeer = (SurfaceView) findViewById(R.id.svBeer);
        mAlcoholeMeterSV = new AlcoholMeterSurfaceView(this, mSvBeer, "Please tap to start!");

        mPhysicaloid = new Physicaloid(getApplicationContext());
//        mPhysicaloid = new Physicaloid(this);
        mAlcohol = new AlcoholMeter(mPhysicaloid);
        mAlcohol.loadZeroPoint(getApplicationContext());

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeDevice();
        unregisterReceiver(mUsbReceiver);
    }

    private static final int MENU_SET_ZERO_POINT = 0;
    private static final int MENU_UPLOAD = 1;
    private static final int MENU_DEMO = 2;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.alcohol_meter, menu);
        menu.add(Menu.NONE, MENU_DEMO, Menu.NONE, "Demo");
        menu.add(Menu.NONE, MENU_UPLOAD, Menu.NONE, "Upload Firmware");
        menu.add(Menu.NONE, MENU_SET_ZERO_POINT, Menu.NONE, "Set Zero Point");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret = true;
        switch (item.getItemId()) {
        default:
            ret = super.onOptionsItemSelected(item);
            break;
        case MENU_SET_ZERO_POINT:
            mAlcohol.setZeroPoint();
            mAlcohol.saveZeroPoint(getApplicationContext());
            ret = true;
            break;
        case MENU_UPLOAD:
            upload();
            break;
        case MENU_DEMO:
            mDemo = !mDemo;
            demo();
            break;
        }
        return ret;
    }

    private void upload() {
        try {
            mPhysicaloid.upload(Boards.POCKETDUINO, getResources().getAssets().open("AlcoholSensor.hex"), null);
        } catch (RuntimeException e) {
            Log.e(TAG, e.toString());
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    private Timer mTimer2;
    private void openDevice() {
        if(!mPhysicaloid.isOpened()) {
            if(mAlcohol.init()) {
                mAlcoholeMeterSV.showPercentage();

                mTimer2 = new Timer();
                mTimer2.schedule( new TimerTask(){
                    @Override
                    public void run() {
                        if(!mDemo) {
                            mAlcoholeMeterSV.setAlcoholPercentage(mAlcohol.getPercentage());
                        }
                    }
                }, 100, 1000);
            } else {
                mAlcoholeMeterSV.setText("Please attach PocketDuino");
                mAlcoholeMeterSV.showText();
            }
        }
    }

    private void closeDevice() {
        mPhysicaloid.close();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        openDevice();
        return true;
    }

    // デモ用のランダムパーセンテージ表示
    Timer mTimer;
    Handler mHandler;
    Random rnd;
    private void demo() {
        mTimer = new Timer(true);
        mHandler = new Handler();
        rnd = new Random();

        mAlcoholeMeterSV.showPercentage();

        mTimer.schedule( new TimerTask(){
        @Override
        public void run() {
            mHandler.post( new Runnable() {
                public void run() {
                    int ran = rnd.nextInt(4);
                    switch (ran) {
                    case 0:
                        mAlcoholeMeterSV.setAlcoholPercentage(50);
                        break;

                    case 1:
                        mAlcoholeMeterSV.setAlcoholPercentage(80);
                        break;

                    case 2:
                        mAlcoholeMeterSV.setAlcoholPercentage(90);
                        break;

                    case 3:
                        mAlcoholeMeterSV.setAlcoholPercentage(100);
                        break;

                    default:
                        break;
                    }
                }
            });
        }
    }, 1000, 2000);
    }

    //****************************************************************
    // get intent when a USB device attached
    protected void onNewIntent(Intent intent) {
        String action = intent.getAction();

        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
            openDevice();
        }
    };
    //****************************************************************

    //****************************************************************
    // get intent when a USB device detached
    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                closeDevice();
            }
        }
    };
    //****************************************************************
}
