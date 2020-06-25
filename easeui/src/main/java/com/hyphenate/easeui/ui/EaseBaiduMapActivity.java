/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hyphenate.easeui.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.baidu.location.BDLocation;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.ui.base.EaseBaseActivity;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.widget.EaseTitleBar;

public class EaseBaiduMapActivity extends EaseBaseActivity implements EaseTitleBar.OnBackPressListener,
																		EaseTitleBar.OnRightClickListener,
																		EaseBaiduMapFragment.OnBDLocationListener {
	private EaseTitleBar titleBarMap;
	private BDLocation lastLocation;

	public static void actionStartForResult(Fragment fragment, int requestCode) {
		Intent intent = new Intent(fragment.getContext(), EaseBaiduMapActivity.class);
		fragment.startActivityForResult(intent, requestCode);
	}

	public static void actionStartForResult(Activity activity, int requestCode) {
		Intent intent = new Intent(activity, EaseBaiduMapActivity.class);
		activity.startActivityForResult(intent, requestCode);
	}

	public static void actionStart(Context context, double latitude, double longtitude, String address) {
		Intent intent = new Intent(context, EaseBaiduMapActivity.class);
		intent.putExtra("latitude", latitude);
		intent.putExtra("longtitude", longtitude);
		intent.putExtra("address", address);
		context.startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ease_activity_baidumap);
		setFitSystemForTheme(false, R.color.transparent, true);
		initView();
		initListener();
		initData();
	}

	private void initView() {
		titleBarMap = findViewById(R.id.title_bar_map);
		titleBarMap.setRightTitleResource(R.string.button_send);
		double latitude = getIntent().getDoubleExtra("latitude", 0);
		if(latitude != 0) {
			titleBarMap.getRightLayout().setVisibility(View.GONE);
		}else {
			titleBarMap.getRightLayout().setVisibility(View.VISIBLE);
			titleBarMap.getRightLayout().setClickable(false);
		}
		ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) titleBarMap.getLayoutParams();
		params.topMargin = (int) EaseCommonUtils.dip2px(this, 24);
		titleBarMap.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));
		titleBarMap.getRightText().setTextColor(ContextCompat.getColor(this, R.color.white));
		titleBarMap.getRightText().setBackgroundResource(R.drawable.ease_title_bar_right_selector);
		int left = (int) EaseCommonUtils.dip2px(this, 10);
		int top = (int) EaseCommonUtils.dip2px(this, 5);
		titleBarMap.getRightText().setPadding(left, top, left, top);
		ViewGroup.LayoutParams layoutParams = titleBarMap.getRightLayout().getLayoutParams();
		if(layoutParams instanceof ViewGroup.MarginLayoutParams) {
		    ((ViewGroup.MarginLayoutParams) layoutParams).setMargins(0, 0, left, 0);
		}
	}

	private void initListener() {
		titleBarMap.setOnBackPressListener(this);
		titleBarMap.setOnRightClickListener(this);
	}

	private void initData() {
		EaseBaiduMapFragment fragment = new EaseBaiduMapFragment();
		Bundle bundle = new Bundle();
		bundle.putDouble("latitude", getIntent().getDoubleExtra("latitude", 0));
		bundle.putDouble("longtitude", getIntent().getDoubleExtra("longtitude", 0));
		bundle.putString("address", getIntent().getStringExtra("address"));
		fragment.setArguments(bundle);
		getSupportFragmentManager().beginTransaction().replace(R.id.fl_fragment, fragment).commit();

		fragment.setOnBDLocationListener(this);
	}

	@Override
	public void onBackPress(View view) {
		onBackPressed();
	}

	@Override
	public void onRightClick(View view) {
		sendLocation();
	}

	@Override
	public void onReceiveBDLocation(BDLocation bdLocation) {
		lastLocation = bdLocation;
		if(bdLocation != null) {
			titleBarMap.getRightLayout().setClickable(true);
		}
	}

	private void sendLocation() {
		Intent intent = getIntent();
		intent.putExtra("latitude", lastLocation.getLatitude());
		intent.putExtra("longitude", lastLocation.getLongitude());
		intent.putExtra("address", lastLocation.getAddrStr());
		this.setResult(RESULT_OK, intent);
		finish();
	}
}
