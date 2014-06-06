package com.physicaloid.lib.sensor;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.physicaloid.app.alcoholmeter.AlcoholMeterActivity;
import com.physicaloid.app.alcoholmeter.AlcoholMeterSurfaceView;
import com.physicaloid.lib.Boards;
import com.physicaloid.lib.Physicaloid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.util.Log;

public class PDuino implements PDuinoListener{
    private static final String TAG = PDuino.class.getSimpleName();

    public static final int ALCOHOL_METER = 1;
    Physicaloid mPhysicaloid;

    public PDuino(Context context) {
    	mPhysicaloid = new Physicaloid(context);

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        context.registerReceiver(mUsbReceiver, filter);
    }

    public Physicaloid getPhysicaloid() {
    	return mPhysicaloid;
    }

    public void init(Context context) {
    }

    public void onDestroy(Context context) {
        closeDevice();
        context.unregisterReceiver(mUsbReceiver);
    }

    private Timer mTimer;
    private boolean openDevice() {
        if(!mPhysicaloid.isOpened()) {
        	return true;
        } else {
        	return false;
        }
    }

    private void closeDevice() {
        mPhysicaloid.close();
    }

    public void upload(Context context) {
        try {
			mPhysicaloid.upload(Boards.POCKETDUINO, context.getResources().getAssets().open("AlcoholSensor.hex"), null);
		} catch (RuntimeException e) {
			Log.e(TAG,e.toString());
		} catch (IOException e) {
			Log.e(TAG,e.toString());
		}
    }
    /**
     * 
     * @param intent
     */
    protected void onNewIntent(Intent intent) {
        String action = intent.getAction();

        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
            openDevice();
        }
    };

    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                closeDevice();
            }
        }
    };

	/**
	 * Converts byte array to hex string
	 * @param b byte array
	 * @param length byte length
	 * @return hex string
	 */
	public String ByteArray2HexStr(byte[] b, int length) {
        String str="";
        for(int i=0; i<length; i++) {
            str += String.format("%02x ", b[i]);
        }
        return str;
    }

	@Override
	public void onReadAlcohol(int percent) {
	}
}
