package com.physicaloid.lib.pocketduino;

public class PocketDuino {

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
}
