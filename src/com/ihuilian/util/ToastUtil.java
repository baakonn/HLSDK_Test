package com.ihuilian.util;


import android.content.Context;
import android.widget.Toast;

/**
 * @author hwk
 * UpdateDate: Apr 1, 2015
 * Version: 1
 * Introduce: 
 */
public class ToastUtil {

	public static void show(Context context, int no_result) {
		Toast.makeText(context, no_result, Toast.LENGTH_LONG).show();
	}

	public static void show(Context context, String no_result) {
		Toast.makeText(context, no_result, Toast.LENGTH_LONG).show();
	}
	
	public static void showShort(Context context, String no_result) {
		Toast.makeText(context, no_result, Toast.LENGTH_SHORT).show();
	}
}
