package com.gtja.tonywang.yyzupdate.activity;

import com.gtja.tonywang.yyzupdate.R;
import com.gtja.tonywang.yyzupdate.UpdateType;
import com.gtja.tonywang.yyzupdate.R.id;
import com.gtja.tonywang.yyzupdate.R.layout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

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

	/**
	 * @Override public boolean onCreateOptionsMenu(Menu menu) { // Inflate the
	 *           menu; this adds items to the action bar if it is present.
	 *           getMenuInflater().inflate(R.menu.main, menu); return true; }
	 * @Override public boolean onOptionsItemSelected(MenuItem item) { // Handle
	 *           action bar item clicks here. The action bar will //
	 *           automatically handle clicks on the Home/Up button, so long //
	 *           as you specify a parent activity in AndroidManifest.xml. int id
	 *           = item.getItemId(); if (id == R.id.action_settings) { return
	 *           true; } return super.onOptionsItemSelected(item); }
	 */

	private void gotoUpdateActivity(int update_type) {
		Intent i = new Intent(MainActivity.this, UpdateActivity.class);
		i.putExtra("update_type", update_type);
		startActivity(i);
	}
}
