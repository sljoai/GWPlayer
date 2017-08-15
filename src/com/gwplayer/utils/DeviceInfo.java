package com.gwplayer.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class DeviceInfo {
	/**
	 * 得到手机IMEI
	 * 
	 * @param context
	 * @return String
	 */
	public static String getImei(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getDeviceId();
	}

	/**
	 * 判断是否是平板电脑
	 * 
	 * @param context
	 * @return boolean
	 */
	public static boolean isTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	/**
	 * 获得设备型号
	 * 
	 * @return String
	 */
	public static String getDeviceModel() {
		return StringUtils.trim(Build.MODEL);
	}

	/** 检测是否魅族手机 */
	public static boolean isMeizu() {
		return getDeviceModel().toLowerCase().indexOf("meizu") != -1;
	}

	/** 检测是否HTC手机 */
	public static boolean isHTC() {
		return getDeviceModel().toLowerCase().indexOf("htc") != -1;
	}

	/** 检测是否小米手机 */
	public static boolean isXiaomi() {
		return getDeviceModel().toLowerCase().indexOf("xiaomi") != -1;
	}

	/**
	 * 获得设备制造商
	 * 
	 * @return String
	 */
	public static String getManufacturer() {
		return StringUtils.trim(Build.MANUFACTURER);
	}

	/**
	 * 获取最大音量
	 * 
	 * @param context
	 * @return
	 */
	public static int getMaxVolume(Context context) {
		return ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE))
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	}

	/**
	 * 获取当前音量
	 * 
	 * @param context
	 * @return
	 */
	public static int getCurVolume(Context context) {
		return ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE))
				.getStreamVolume(AudioManager.STREAM_MUSIC);
	}

	/**
	 * 设置当前音量
	 * 
	 * @param context
	 * @param index
	 */
	public static void setCurVolume(Context context, int index) {
		((AudioManager) context.getSystemService(Context.AUDIO_SERVICE)).setStreamVolume(AudioManager.STREAM_MUSIC,
				index, 0);
	}

	/**
	 * 获取屏幕宽高
	 * 
	 * @return int[widthPixels,heightPixels]
	 */
	public static int[] getScreenSize(Context context) {
		int[] screens;
		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getResources().getDisplayMetrics();
		screens = new int[] { dm.widthPixels, dm.heightPixels };
		return screens;
	}

	/** 获取屏幕高度 */

	public static int getScreenHeight(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getResources().getDisplayMetrics();
		return dm.heightPixels;
	}

	/** 获取屏幕宽度 */
	public static int getScreenWidth(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getResources().getDisplayMetrics();
		return dm.widthPixels;
	}

	/**
	 * 获得设备屏幕密度
	 */
	public static float getScreenDensity(Context context) {
		DisplayMetrics metrics = context.getApplicationContext().getResources().getDisplayMetrics();
		return metrics.density;
	}

	/** 隐藏软键盘 */
	public static void hideSoftInput(Context ctx, View view) {
		InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
		// 这个方法可以实现输入法在窗口上切换显示，如果输入法在窗口上已经显示，则隐藏，如果隐藏，则显示输入法到窗口上
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	/** 显示软键盘 */
	public static void showSoftInput(Context ctx) {
		InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);// (v,
		// InputMethodManager.SHOW_FORCED);
	}

	/**
	 * 软键盘是否已经打开
	 */
	public boolean isHardKeyboardOpen(Context ctx) {
		return ctx.getResources().getConfiguration().hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO;
	}

}
