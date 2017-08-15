package com.gwplayer.utils;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;

/**
 * 网络工具类
 * 
 * @author Wxcily
 */
public class NetworkUtil {
	public static String NETWORK_TYPE_WIFI = "WIFI";
	public static String NETWORK_TYPE_MOBILE = "MOBILE";
	public static String NETWORK_TYPE_ERROR = "ERROR";

	/**
	 * 返回网络是否可用。需要权限：
	 * <p>
	 * <b> < uses-permission
	 * android:name="android.permission.ACCESS_NETWORK_STATE" /> </b>
	 * </p>
	 * 
	 * @param context
	 *            上下文
	 * @return 网络可用则返回true，否则返回false
	 */
	public static boolean isAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		return info != null && info.isAvailable();
	}

	/**
	 * 判断网络连接状态
	 * 
	 * @param context
	 * @return
	 */
	public static String getNetType(Context context) {

		try {
			ConnectivityManager connectivity = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {

				NetworkInfo info = connectivity.getActiveNetworkInfo();
				if (info != null && info.isConnected()) {

					if (info.getState() == NetworkInfo.State.CONNECTED) {
						if (info.getType() == ConnectivityManager.TYPE_WIFI) {
							// wifi
							return NETWORK_TYPE_WIFI;
						} else {
							// 手机网络
							return NETWORK_TYPE_MOBILE;
						}
					}
				}
			}
		} catch (Exception e) {
			// 网络错误
			return NETWORK_TYPE_ERROR;
		}
		// 网络错误
		return NETWORK_TYPE_ERROR;

	}

	/**
	 * 返回Wifi是否启用
	 * 
	 * @param context
	 *            上下文
	 * @return Wifi网络可用则返回true，否则返回false
	 */
	public static boolean isWIFIActivate(Context context) {
		return ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).isWifiEnabled();
	}

	/**
	 * 修改Wifi状态
	 * 
	 * @param context
	 *            上下文
	 * @param status
	 *            true为开启Wifi，false为关闭Wifi
	 */
	public static void changeWIFIStatus(Context context, boolean status) {
		((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).setWifiEnabled(status);
	}

	public static void startNetSettingActivity(Context context) {
		Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
		context.startActivity(intent);
	}
}
