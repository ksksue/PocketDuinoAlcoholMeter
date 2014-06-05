package com.physicaloid.app.alcoholmeter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.physicaloid.lib.Physicaloid;
import com.physicaloid.lib.usb.driver.uart.ReadLisener;

public class AlcoholMeter {
	private static final String TAG = AlcoholMeter.class.getSimpleName();
    private int mAdcVal;
    private int mAdcZeroPoint;
    private int mPercentage;
    private boolean mDemo = false;

    Physicaloid mPhysicaloid;

    private static final int ALCOHOL_ZERO_ADC_VALUE = 300;
    private static final int ALCOHOL_DIFF_ADC_VALUE = 350;
    private static final int PREFERENCE_DEF_ZERO_POINT = 600;

	public AlcoholMeter(Physicaloid physicaloid) {
        mPhysicaloid = physicaloid;
        mAdcZeroPoint = ALCOHOL_ZERO_ADC_VALUE;
        mAdcVal = 0;
	}

	public boolean init() {
		if(mPhysicaloid.open()) {
			mPhysicaloid.addReadListener(new ReadLisener() {
                // callback when reading one or more size buffer
                @Override
                public void onRead(int size) {
                    byte[] buf = new byte[size];

                    int readSize = mPhysicaloid.read(buf, size);
                    Log.d(TAG, String.format("%02d ", size));
                    if(readSize > 2) {
                        mAdcVal = decodePacket(buf);
                        mPercentage = getPercentageFromAdc(mAdcVal);
                    }
                }
            });
			return true;
		} else {
			return false;
		}
	}

	public int getPercentage() {
		return mPercentage;
	}

	public void setZeroPoint() {
	    mAdcZeroPoint = mAdcVal;
	}
	// TODO: パケット構造をより強固に
	final static byte STX = 's';
    final static byte ETX = '\r';

    private int decodePacket(byte[] buf) {
        boolean existStx = false;
        int result = 0;

        for(int i=0; i<buf.length; i++) {
            Log.d(TAG, String.format("%02x ", buf[i]));
            if(!existStx) {
                if(buf[i] == STX) {
                    existStx = true;
                }
            } else {
                if(buf[i] == ETX) {
                    return result;
                } else {
                    if('0' <= buf[i] && buf[i] <= '9') {
                        result = result*10 + (buf[i]-'0');
                    } else {
                        return -1;
                    }
                }
            }
        }

        return -1;
    }

    private int getPercentageFromAdc(int val) {
        if(val < mAdcZeroPoint) {
            val = mAdcZeroPoint;
        } else if(val > mAdcZeroPoint+ALCOHOL_DIFF_ADC_VALUE) {
            val = mAdcZeroPoint+ALCOHOL_DIFF_ADC_VALUE;
        }

        return (int)((val-mAdcZeroPoint)*100/ALCOHOL_DIFF_ADC_VALUE);
    }

    // 0%時のADの値を保存
    public void saveZeroPoint(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt("ZeroPoint", mAdcZeroPoint).commit();
    }

    // 0%時のADの値をロード
    public void loadZeroPoint(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        mAdcZeroPoint = sp.getInt("ZeroPoint", PREFERENCE_DEF_ZERO_POINT);
    }
}
