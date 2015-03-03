package com.gtja.tonywang.yyzupdate.activity;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import com.alibaba.fastjson.JSON;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.gtja.tonywang.yyzupdate.Constants;
import com.gtja.tonywang.yyzupdate.MyApp;
import com.gtja.tonywang.yyzupdate.R;
import com.gtja.tonywang.yyzupdate.UpdateType;
import com.gtja.tonywang.yyzupdate.model.UpdateModel;
import com.gtja.tonywang.yyzupdate.service.DownloadService.DownloadBinder;
import com.gtja.tonywang.yyzupdate.service.DownloadService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class UpdateActivity extends Activity {

	private MyApp app;
	private boolean isDestroy = true;

	private String currentVersion;
	private String check_url = "http://dd.gtja.com/download/yyz/YyzplusVer.json";
	private int update_type = -1;
	private ProgressDialog dialog;

	private DownloadBinder binder;
	private boolean isBinded;
	private SharedPreferences sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update);
		app = (MyApp) getApplication();
		update_type = getIntent().getIntExtra("update_type", -1);
		sp = PreferenceManager.getDefaultSharedPreferences(this);
	}

	/**
	 * 检查是否需要更新
	 * 
	 * @param url
	 */
	private void checkUpdate(String url) {

		// 当前版本
		currentVersion = null;
		try {
			PackageManager packageManager = getPackageManager();
			// getPackageName()是当前类的包名，0代表是获取版本信息
			PackageInfo packInfo = packageManager.getPackageInfo(
					getPackageName(), 0);
			currentVersion = packInfo.versionName;
			// int versioncode = packInfo.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (currentVersion == null)
			return;

		// 创建RequestQueue对象
		RequestQueue mRequestQueue = Volley.newRequestQueue(this);
		// 创建JsonObjectRequest对象
		JsonObjectRequest mJsonObjectRequest = new JsonObjectRequest(url, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						System.out.println("app update请求结果:"
								+ response.toString());
						try {
							String android_update = response
									.getString("android");
							UpdateModel model = JSON.parseObject(
									android_update, UpdateModel.class);
							String newVersion = model.getVersion();
							if (newVersion != null) {
								if (newVersion.compareTo(currentVersion) > 0) {
									boolean needDownload = false;
									File local_file = new File(
											Constants.APK_SAVE_FILE_NAME);
									if (local_file.exists()) {
										String local_apk_version = sp
												.getString(
														Constants.LOCAL_APK_VERSION,
														"");
										if (newVersion
												.compareTo(local_apk_version) > 0) {
											needDownload = true;
										} else {
											showInstallApkDialog(local_file);
										}
									} else {
										needDownload = true;
									}
									if (needDownload) {
										switch (update_type) {
										case UpdateType.UPDATE_NORMAL:
											updateNormal(model);
											break;
										case UpdateType.UPDATE_FORCE:
											updateForce(model);
											break;
										case UpdateType.UPDATE_SILENCE:
											updateSilence(model);
											break;
										}
									}

								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						System.out
								.println("app update请求错误:" + error.toString());
						Toast.makeText(getApplicationContext(),
								error.toString(), Toast.LENGTH_SHORT).show();
					}
				});

		// 将JsonObjectRequest添加到RequestQueue
		mRequestQueue.add(mJsonObjectRequest);

	}

	private void showInstallApkDialog(final File local_file) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("应用更新")
				.setMessage("已准备好新版本，是否安装？")
				.setCancelable(false)
				.setPositiveButton("是", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// 安装本地apk
						Intent i = new Intent(Intent.ACTION_VIEW);
						i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						i.setDataAndType(
								Uri.parse("file://" + local_file.toString()),
								"application/vnd.android.package-archive");
						startActivity(i);

					}
				})
				.setNegativeButton("否", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				}).create().show();

	}

	/**
	 * 普通更新模式，通知栏通知
	 * 
	 * @param model
	 */
	private void updateNormal(UpdateModel model) {
		final String url = model.getLoadAddress();
		final String versionname = model.getVersion();
		// notify user to download
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("检测到新版本,是否下载？")
				.setMessage(model.getNoticeText())
				.setCancelable(false)
				.setPositiveButton("是", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						if (!app.isDownload()) {

							Intent it = new Intent(UpdateActivity.this,
									DownloadService.class);
							it.putExtra("download_url", url);
							it.putExtra("versionname", versionname);
							it.putExtra("update_type", UpdateType.UPDATE_NORMAL);
							startService(it);
							bindService(it, conn, Context.BIND_AUTO_CREATE);
						}
					}
				})
				.setNegativeButton("否", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				}).create().show();
	}

	/**
	 * 强制更新模式，对话框强制更新
	 * 
	 * @param model
	 */
	private void updateForce(UpdateModel model) {
		final String url = model.getLoadAddress();
		final String versionname = model.getVersion();
		// notify user to download
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("检测到新版本,请更新").setMessage(model.getNoticeText())
				.setCancelable(false)
				.setPositiveButton("更新", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						if (!app.isDownload()) {
							startForceDownload(url, versionname);
						}
					}
				}).create().show();
	}

	/**
	 * 开始强制更新下载
	 * 
	 * @param url
	 */
	private void startForceDownload(String url, String versionname) {
		dialog = new ProgressDialog(this);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// 设置水平进度条
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setIcon(R.drawable.ic_launcher);// 设置提示的title的图标，默认是没有的
		dialog.setTitle("更新");
		dialog.setMax(100);
		dialog.show();
		if (!app.isDownload()) {
			Intent it = new Intent(UpdateActivity.this, DownloadService.class);
			it.putExtra("download_url", url);
			it.putExtra("versionname", versionname);
			it.putExtra("update_type", UpdateType.UPDATE_FORCE);
			startService(it);
			bindService(it, conn, Context.BIND_AUTO_CREATE);
		}
	}

	/**
	 * 静默更新模式，有更新直接后台下载，下载完成再通知用户安装
	 * 
	 * @param model
	 */
	private void updateSilence(UpdateModel model) {
		String url = model.getLoadAddress();
		String versionname = model.getVersion();
		if (!app.isDownload()) {
			Intent it = new Intent(UpdateActivity.this, DownloadService.class);
			it.putExtra("download_url", url);
			it.putExtra("versionname", versionname);
			it.putExtra("update_type", UpdateType.UPDATE_SILENCE);
			startService(it);
			bindService(it, conn, Context.BIND_AUTO_CREATE);
		}
	}

	ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			isBinded = false;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = (DownloadBinder) service;
			System.out.println("服务启动!!!");
			// 开始下载
			isBinded = true;
			if (update_type == UpdateType.UPDATE_FORCE)
				binder.addCallback(callback);
			binder.start();

		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		if (isDestroy && !app.isDownload()) {
			checkUpdate(check_url);
		}
		System.out.println(" notification  onresume");
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (isDestroy && !app.isDownload()) {
			checkUpdate(check_url);
		}
		System.out.println(" notification  onNewIntent");
	}

	@Override
	protected void onStart() {
		super.onStart();

	}

	@Override
	protected void onPause() {
		super.onPause();
		System.out.println(" notification  onPause");
	}

	@Override
	protected void onStop() {
		super.onStop();
		isDestroy = false;
		System.out.println(" notification  onStop");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isBinded) {
			System.out.println(" onDestroy   unbindservice");
			unbindService(conn);
		}
		if (binder != null && binder.isCanceled()) {
			System.out.println(" onDestroy  stopservice");
			Intent it = new Intent(this, DownloadService.class);
			stopService(it);
		}
	}

	// 回调接口，强制更新策略中用于更改对话框进度条
	private ICallbackResult callback = new ICallbackResult() {

		@Override
		public void OnBackResult(Object result) {
			if ("finish".equals(result)) {
				dialog.cancel();
				return;
			}
			int i = (Integer) result;
			dialog.setProgress(i);
		}

	};

	public interface ICallbackResult {
		public void OnBackResult(Object result);
	}
}