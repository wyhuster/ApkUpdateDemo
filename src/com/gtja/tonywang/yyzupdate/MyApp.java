package com.gtja.tonywang.yyzupdate;

import java.io.File;
import java.io.IOException;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

public class MyApp extends Application {

	private boolean is_download;
	private String apk_save_path;
	private String apk_save_name;
	private boolean store_in_sdcard;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		is_download = false;
		apk_save_path = getSavePath();
		apk_save_name = apk_save_path + Constants.APK_SAVE_FILE_NAME;
	}

	public boolean isDownload() {
		return is_download;
	}

	public void setDownload(boolean isDownload) {
		this.is_download = isDownload;
	}

	public String getApkSavePath() {
		return apk_save_path;
	}

	public String getApkSaveName() {
		return apk_save_name;
	}

	public boolean getStoreInSdcard() {
		return store_in_sdcard;
	}

	private String getSavePath() {

		String state = Environment.getExternalStorageState();
		// System.out.println("state:" + state);
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			String sdcard = Environment.getExternalStorageDirectory()
					.getAbsolutePath();
			store_in_sdcard = true;
			return sdcard + Constants.APK_SAVE_PATH;
		} else {
			// 内存卡不可用
			store_in_sdcard = false;
			return "";
		}

	}

	/**
	 * 安装apk
	 */
	public void installApk() {
		File apkfile = null;
		if (store_in_sdcard) {
			apkfile = new File(apk_save_name);
		} else {
			apkfile = new File(getFilesDir().getPath() + "/" + apk_save_name);
			String cmd = "chmod 777 " + apkfile.toString();
			try {
				Runtime.getRuntime().exec(cmd);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (apkfile != null && apkfile.exists()) {
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.setDataAndType(Uri.parse("file://" + apkfile),
					"application/vnd.android.package-archive");
			startActivity(i);
		} else {
			Toast.makeText(getApplicationContext(), "apk file not exists",
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 判断本地是否已下载好apk
	 * 
	 * @return
	 */
	public boolean apkFileExists() {
		File apkfile = null;
		if (store_in_sdcard) {
			apkfile = new File(apk_save_name);
		} else {
			apkfile = new File(getFilesDir().getPath() + "/" + apk_save_name);
		}
		if (apkfile != null && apkfile.exists()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 返回当前网络连接情况 未连接，wifi，mobile
	 * 
	 * @return
	 */
	public int getNetworkInfo() {
		ConnectivityManager connectMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectMgr.getActiveNetworkInfo();
		if (info != null && info.isConnected()) {
			switch (info.getType()) {
			case ConnectivityManager.TYPE_WIFI:
				return Constants.NETWORK_WIFI;
			case ConnectivityManager.TYPE_MOBILE:
				return Constants.NETWORK_MOBILE;
			default:
				return Constants.NETWORK_NONE;
			}
		} else {
			return Constants.NETWORK_NONE;
		}
	}
}
