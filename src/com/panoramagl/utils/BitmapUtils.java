/*
 * PanoramaGL library
 * Version 0.2 beta
 * Copyright (c) 2010 Javier Baez <javbaezga@gmail.com>
 *
 * mayakun add 2016/5/4
 */

package com.panoramagl.utils;

import java.io.InputStream;


public class BitmapUtils {
	/**
	 * ��InputStreamת����byte[] 
	 * @param is
	 * @return
	 */
	public static byte[] inputStream2Bytes(InputStream is) {  
        String str = "";  
        byte[] readByte = new byte[1024];  
        int readCount = -1;  
        try {  
            while ((readCount = is.read(readByte, 0, 1024)) != -1) {  
                str += new String(readByte).trim();  
            }  
            return str.getBytes();  
        } catch (Exception e) {  
            e.printStackTrace();
        }  
        return null;  
    }
}