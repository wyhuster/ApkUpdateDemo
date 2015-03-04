package com.gtja.tonywang.yyzupdate;

import android.app.Application;
import android.os.Environment;

public class MyApp extends Application {

	private boolean isDownload;
	private String apk_save_path;
	private String apk_save_name;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		isDownload = false;
		apk_save_path = getSavePath("/yyzApkDownload/");
		apk_save_name = apk_save_path + "YYZ_AppUpdate.apk";
	}

	public boolean isDownload() {
		return isDownload;
	}

	public void setDownload(boolean isDownload) {
		this.isDownload = isDownload;
	}

	public String getApk_save_path() {
		return apk_save_path;
	}

	public String getApk_save_name() {
		return apk_save_name;
	}

	private String getSavePath(String directory) {

		String state = Environment.getExternalStorageState();
		System.out.println("state:" + state);
		if (state.equals(Environment.MEDIA_MOUNTED)) {
			String path = Environment.getExternalStorageDirectory()
					.getAbsolutePath();
			return path + directory;
		} else {
			String path = Environment.getDataDirectory().getAbsolutePath();
			return path + directory;
		}

	}

}
