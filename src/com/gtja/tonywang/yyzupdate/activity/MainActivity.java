package com.gtja.tonywang.yyzupdate.activity;

import java.io.File;
import java.io.IOException;

import com.gtja.tonywang.yyzupdate.R;
import com.gtja.tonywang.yyzupdate.UpdateType;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.btn_update_normal).setOnClickListener(listener);
		findViewById(R.id.btn_update_force).setOnClickListener(listener);
		findViewById(R.id.btn_update_silence).setOnClickListener(listener);
	}

	private OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_update_normal:
				gotoUpdateActivity(UpdateType.UPDATE_NORMAL);
				break;
			case R.id.btn_update_force:
				gotoUpdateActivity(UpdateType.UPDATE_FORCE);
				break;
			case R.id.btn_update_silence:
				gotoUpdateActivity(UpdateType.UPDATE_SILENCE);
				break;
			}

		}
	};

	private void gotoUpdateActivity(int update_type) {
		Intent i = new Intent(MainActivity.this, UpdateActivity.class);
		i.putExtra("update_type", update_type);
		startActivity(i);
	}

}
