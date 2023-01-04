package com.cci.fs.utils;

import io.pkts.buffer.Buffer;

public class ConvertUtils {


    static public String convertToHex(Buffer buff){
        StringBuilder hexStr = new StringBuilder();
        if (!buff.isEmpty()){
                byte[] bytes = buff.getArray();

                for (int ind = bytes.length-1; ind >= 0; ind--) {
                        hexStr.append(String.format("%02x", bytes[ind]));
                }

                //buff.readByte();
        }

        return hexStr.toString();
    }

}