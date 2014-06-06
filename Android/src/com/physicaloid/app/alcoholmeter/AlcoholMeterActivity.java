package com.physicaloid.app.alcoholmeter;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;

import com.physicaloid.lib.sensor.AlcoholMeter;
import com.physicaloid.lib.sensor.PDuino;

public class AlcoholMeterActivity extends Activity {
    private static final String TAG = AlcoholMeterActivity.class.getSimpleName();

    AlcoholMeterSurfaceView mAlcoholeMeterSV;
    SurfaceView mSvBeer;

    PDuino mSensor;
    AlcoholMeter mAlcohol;
    private boolean mDemo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alcohol_meter);

        mSvBeer = (SurfaceView) findViewById(R.id.svBeer);
        mAlcoholeMeterSV = new AlcoholMeterSurfaceView(this, mSvBeer, "Please tap to start!");

        mSensor = new PDuino(getApplicationContext());

    	mAlcohol = new AlcoholMeter(mSensor);
    	mAlcohol.loadZeroPoint(getApplicationContext());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSensor.onDestroy(getApplicationContext());
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

}
