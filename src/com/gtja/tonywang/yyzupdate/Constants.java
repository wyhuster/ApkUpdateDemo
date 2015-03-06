package com.gtja.tonywang.yyzupdate;

public class Constants {
	/** 查询update信息的api */
	public static final String URL_CHECK_UPDATE = "";

	/** 下载到sdcard中新建的文件夹 */
	public static final String APK_SAVE_PATH = "/yyzApkDownload/";
	/** 下载的apk文件名 */
	public static final String APK_SAVE_FILE_NAME = "YYZ_AppUpdate.apk";

	/** 本地sharedpreference中存储的apk版本key */
	public static final String PREF_LOCAL_APK_VERSION = "local_apk_version";

	/** 网络连接状态 无网络连接 */
	public static final int NETWORK_NONE = 0;
	/** 网络连接状态 wifi连接 */
	public static final int NETWORK_WIFI = 1;
	/** 网络连接状态 数据流量连接 */
	public static final int NETWORK_MOBILE = 2;

}
