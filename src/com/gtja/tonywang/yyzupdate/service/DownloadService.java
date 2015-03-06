package com.gtja.tonywang.yyzupdate.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;

import com.gtja.tonywang.yyzupdate.Constants;
import com.gtja.tonywang.yyzupdate.MyApp;
import com.gtja.tonywang.yyzupdate.R;
import com.gtja.tonywang.yyzupdate.UpdateType;
import com.gtja.tonywang.yyzupdate.activity.UpdateActivity.ICallbackResult;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

public class DownloadService extends Service {
	private static final int NOTIFY_ID = 0;
	private static final int DOWNLOAD_SUCCESS = 0;
	private static final int DOWNLOAD_FAIL = 1;
	private static final int DOWNLOAD_PROGRESS = 2;
	private static final int DOWNLOAD_CANCEL = 3;

	private int progress;
	private NotificationManager mNotificationManager;
	private Notification mNotification;
	private boolean canceled;
	private String apkUrl = null;
	private int update_type = -1;
	private String versionname;

	private String savePath;
	private String saveFileName;
	private String tempSaveFileName;
	private ICallbackResult callback;
	private DownloadBinder binder;
	private MyApp app;
	private boolean serviceIsDestroy = false;

	private Context mContext = this;
	private SharedPreferences sp;
	// handler处理下载过程更新，下载结束和取消等
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case DOWNLOAD_SUCCESS:
				// 下载成功
				app.setDownload(false);
				if (update_type == UpdateType.UPDATE_NORMAL) {
					mNotificationManager.cancel(NOTIFY_ID);
				}
				Editor editor = sp.edit();
				editor.putString(Constants.PREF_LOCAL_APK_VERSION, versionname);
				editor.commit();
				serviceIsDestroy = true;
				stopSelf();

				// 从temp文件拷贝内容到apk文件中
				fileChannelCopy(tempSaveFileName, saveFileName);
				// if (update_type != UpdateType.UPDATE_SILENCE) {
				app.installApk();
				// }
				if (callback != null) {
					// 关闭强制下载时候的对话框
					callback.OnBackResult("finish");
				}
				break;
			case DOWNLOAD_FAIL:
				// 下载失败
				app.setDownload(false);
				if (update_type == UpdateType.UPDATE_NORMAL) {
					System.out.println("下载失败!!!!!!!!!!!");
					mNotification.flags = Notification.FLAG_AUTO_CANCEL;
					mNotification.contentView = null;
					mNotification.setLatestEventInfo(mContext, "易阳指", "下载失败",
							null);
					mNotificationManager.notify(NOTIFY_ID, mNotification);
				}
				serviceIsDestroy = true;
				stopSelf();
				break;
			case DOWNLOAD_CANCEL:
				// 取消下载
				app.setDownload(false);
				if (update_type == UpdateType.UPDATE_NORMAL) {
					mNotificationManager.cancel(NOTIFY_ID);
				}
				serviceIsDestroy = true;
				stopSelf();
				break;
			case DOWNLOAD_PROGRESS:
				// 下载过程
				int rate = msg.arg1;
				app.setDownload(true);
				if (rate < 100) {
					RemoteViews contentview = mNotification.contentView;
					contentview.setTextViewText(R.id.tv_progress, rate + "%");
					contentview.setProgressBar(R.id.progressbar, 100, rate,
							false);
				} else {
					System.out.println("下载成功!!!!!!!!!!!");
					mNotification.flags = Notification.FLAG_AUTO_CANCEL;
					mNotification.contentView = null;
					mNotification.setLatestEventInfo(mContext, "易阳指", "下载成功",
							null);

					serviceIsDestroy = true;
					stopSelf();
				}
				mNotificationManager.notify(NOTIFY_ID, mNotification);
				break;
			}
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		binder = new DownloadBinder();
		mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		// setForeground(true);
		app = (MyApp) getApplication();
		savePath = app.getApkSavePath();
		saveFileName = app.getApkSaveName();
		tempSaveFileName = savePath + "temp";

		sp = PreferenceManager.getDefaultSharedPreferences(this);
	}

	// @Override
	// public int onStartCommand(Intent intent, int flags, int startId) {
	// // TODO Auto-generated method stub
	// return START_STICKY;
	// }

	@Override
	public IBinder onBind(Intent intent) {
		if (intent != null) {
			update_type = intent.getIntExtra("update_type", -1);
			apkUrl = intent.getStringExtra("download_url");
			versionname = intent.getStringExtra("versionname");
			// apkUrl =
			// "http://softfile.3g.qq.com:8080/msoft/179/24659/43549/qq_hd_mini_1.4.apk";
		}
		System.out.println("onBind");
		return binder;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		System.out.println("downloadservice ondestroy");
		app.setDownload(false);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		System.out.println("downloadservice onUnbind");
		return super.onUnbind(intent);
	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
		System.out.println("downloadservice onRebind");
	}

	/**
	 * 复制文件
	 * 
	 * @param s
	 * @param t
	 */
	public void fileChannelCopy(String s, String t) {
		FileInputStream fi = null;
		FileOutputStream fo = null;
		FileChannel in = null;
		FileChannel out = null;
		try {
			if (app.getStoreInSdcard()) {
				fi = new FileInputStream(s);
				fo = new FileOutputStream(t);
			} else {
				fi = openFileInput(s);
				fo = openFileOutput(t, Context.MODE_PRIVATE);
			}
			in = fi.getChannel();// 得到对应的文件通道
			out = fo.getChannel();// 得到对应的文件通道
			in.transferTo(0, in.size(), out);// 连接两个通道，并且从in通道读取，然后写入out通道
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fi.close();
				in.close();
				fo.close();
				out.close();
				// 删除临时文件
				if (app.getStoreInSdcard()) {
					new File(s).delete();
				} else {
					deleteFile(s);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	// 下载线程
	private Thread downLoadThread;

	// binder 将service和activity联系起来
	public class DownloadBinder extends Binder {
		public void start() {
			if (downLoadThread == null || !downLoadThread.isAlive()) {

				progress = 0;
				if (update_type == UpdateType.UPDATE_NORMAL) {
					setUpNotification();
				}
				canceled = false;
				downLoadThread = new Thread(new Runnable() {
					@Override
					public void run() {
						downloadApk();
					}
				});
				downLoadThread.start();
			}
		}

		public void cancel() {
			canceled = true;
		}

		public int getProgress() {
			return progress;
		}

		public boolean isCanceled() {
			return canceled;
		}

		public boolean serviceIsDestroy() {
			return serviceIsDestroy;
		}

		public void cancelNotification() {
			mHandler.sendEmptyMessage(DOWNLOAD_CANCEL);
		}

		public void addCallback(ICallbackResult callback) {
			DownloadService.this.callback = callback;
		}
	}

	/**
	 * 设置下载通知栏
	 */
	private void setUpNotification() {
		int icon = R.drawable.ic_launcher;
		CharSequence tickerText = "开始下载";
		long when = System.currentTimeMillis();
		mNotification = new Notification(icon, tickerText, when);
		mNotification.flags = Notification.FLAG_ONGOING_EVENT;

		RemoteViews contentView = new RemoteViews(getPackageName(),
				R.layout.download_notification_layout);
		contentView.setTextViewText(R.id.name, "易阳指 正在下载...");
		mNotification.contentView = contentView;

		mNotificationManager.notify(NOTIFY_ID, mNotification);
	}

	/**
	 * apk下载函数
	 */
	private void downloadApk() {
		int length = 0;
		int count = 0;
		boolean fail = false;
		try {
			URL url = new URL(apkUrl);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(20000);
			conn.connect();
			length = conn.getContentLength();
			InputStream is = conn.getInputStream();

			FileOutputStream fos = null;

			System.out.println("download path:" + tempSaveFileName);

			if (!app.getStoreInSdcard()) {
				// 没有内存卡，则存储到手机内部/data/data/{package}/files/
				fos = openFileOutput(tempSaveFileName, Context.MODE_PRIVATE);
			} else {
				// 存储到内存卡
				File file = new File(savePath);
				if (!file.exists()) {
					file.mkdirs();
				}
				fos = new FileOutputStream(tempSaveFileName);
			}

			int lastRate = 0;
			byte buf[] = new byte[1024];
			do {
				int numread = is.read(buf);
				count += numread;
				progress = (int) (((float) count / length) * 100);
				Message msg = mHandler.obtainMessage();
				msg.what = DOWNLOAD_PROGRESS;
				msg.arg1 = progress;
				if (progress >= lastRate + 1) {
					// 普通更新模式才需要更新notification的进度
					if (update_type == UpdateType.UPDATE_NORMAL) {
						mHandler.sendMessage(msg);
					}
					lastRate = progress;
					if (callback != null)
						callback.OnBackResult(progress);
				}
				if (numread <= 0) {
					canceled = true;
					mHandler.sendEmptyMessage(DOWNLOAD_SUCCESS);
					break;
				}
				fos.write(buf, 0, numread);
			} while (!canceled);

			fos.close();
			is.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail = true;
		} catch (IOException e) {
			e.printStackTrace();
			fail = true;
		}
		if (fail) {
			mHandler.sendEmptyMessage(DOWNLOAD_FAIL);
		}
	}
}
