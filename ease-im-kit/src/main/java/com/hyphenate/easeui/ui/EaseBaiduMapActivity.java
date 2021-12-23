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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.ui.base.EaseBaseActivity;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.widget.EaseTitleBar;
import com.hyphenate.util.EMLog;

public class EaseBaiduMapActivity extends EaseBaseActivity implements EaseTitleBar.OnBackPressListener,
																		EaseTitleBar.OnRightClickListener{
	private EaseTitleBar titleBarMap;
	private MapView mapView;
	private BaiduMap baiduMap;
	private BDLocation lastLocation;
	protected double latitude;
	protected double longtitude;
	protected String address;
	private BaiduSDKReceiver mBaiduReceiver;
	private LocationClient mLocClient;

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
		//initialize SDK with context, should call this before setContentView
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.ease_activity_baidumap);
		setFitSystemForTheme(false, R.color.transparent, true);
		initIntent();
		initView();
		initListener();
		initData();
	}

	private void initIntent() {
		latitude = getIntent().getDoubleExtra("latitude", 0);
		longtitude = getIntent().getDoubleExtra("longtitude", 0);
		address = getIntent().getStringExtra("address");
	}

	private void initView() {
		titleBarMap = findViewById(R.id.title_bar_map);
		mapView = findViewById(R.id.bmapView);
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

		baiduMap = mapView.getMap();
		baiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(15.0f));
		mapView.setLongClickable(true);
	}

	private void initListener() {
		titleBarMap.setOnBackPressListener(this);
		titleBarMap.setOnRightClickListener(this);
	}

	private void initData() {
		if(latitude == 0) {
			mapView = new MapView(this, new BaiduMapOptions());
			baiduMap.setMyLocationConfigeration(
					new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, null));
			showMapWithLocationClient();
		}else {
			LatLng lng = new LatLng(latitude, longtitude);
			mapView = new MapView(this,
					new BaiduMapOptions().mapStatus(new MapStatus.Builder().target(lng).build()));
			showMap(latitude, longtitude);
		}
		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
		iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
		mBaiduReceiver = new BaiduSDKReceiver();
		registerReceiver(mBaiduReceiver, iFilter);
	}

	protected void showMapWithLocationClient() {
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(new EaseBDLocationListener());
		LocationClientOption option = new LocationClientOption();
		// open gps
		option.setOpenGps(true);
		// option.setCoorType("bd09ll");
		// Johnson change to use gcj02 coordination. chinese national standard
		// so need to conver to bd09 everytime when draw on baidu map
		option.setCoorType("gcj02");
		option.setScanSpan(30000);
		option.setAddrType("all");
		option.setIgnoreKillProcess(true);
		mLocClient.setLocOption(option);
		if(!mLocClient.isStarted()) {
			mLocClient.start();
		}
	}

	protected void showMap(double latitude, double longtitude) {
		LatLng lng = new LatLng(latitude, longtitude);
		CoordinateConverter converter = new CoordinateConverter();
		converter.coord(lng);
		converter.from(CoordinateConverter.CoordType.COMMON);
		LatLng convertLatLng = converter.convert();
		OverlayOptions ooA = new MarkerOptions().position(convertLatLng).icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ease_icon_marka))
				.zIndex(4).draggable(true);
		baiduMap.addOverlay(ooA);
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(convertLatLng, 17.0f);
		baiduMap.animateMapStatus(u);
	}

	@Override
	public void onBackPress(View view) {
		onBackPressed();
	}

	@Override
	public void onRightClick(View view) {
		sendLocation();
	}

	public void onReceiveBDLocation(BDLocation bdLocation) {
		if(bdLocation == null) {
			return;
		}
		if (lastLocation != null) {
			if (lastLocation.getLatitude() == bdLocation.getLatitude() && lastLocation.getLongitude() == bdLocation.getLongitude()) {
				Log.d("map", "same location, skip refresh");
				// mMapView.refresh(); //need this refresh?
				return;
			}
		}
		titleBarMap.getRightLayout().setClickable(true);
		lastLocation = bdLocation;
		baiduMap.clear();
		showMap(lastLocation.getLatitude(), lastLocation.getLongitude());
	}

	private void sendLocation() {
		Intent intent = getIntent();
		intent.putExtra("latitude", lastLocation.getLatitude());
		intent.putExtra("longitude", lastLocation.getLongitude());
		intent.putExtra("address", lastLocation.getAddrStr());
		intent.putExtra("buildingName", lastLocation.getBuildingName() == null ? "" : lastLocation.getBuildingName());
		this.setResult(RESULT_OK, intent);
		finish();
	}

	@Override
	protected void onResume() {
		mapView.onResume();
		if (mLocClient != null) {
			if(!mLocClient.isStarted()) {
				mLocClient.start();
			}
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		mapView.onPause();
		if (mLocClient != null) {
			mLocClient.stop();
		}
		super.onPause();
		lastLocation = null;
	}

	@Override
	protected void onDestroy() {
		if (mLocClient != null)
			mLocClient.stop();
		mapView.onDestroy();
		unregisterReceiver(mBaiduReceiver);
		super.onDestroy();
	}

	public class BaiduSDKReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(TextUtils.equals(action, SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
				showErrorToast(getResources().getString(R.string.please_check));
			}else if(TextUtils.equals(action, SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
				showErrorToast(getResources().getString(R.string.Network_error));
			}
		}
	}

	public class EaseBDLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation bdLocation) {
			onReceiveBDLocation(bdLocation);
		}
	}

	/**
	 * show error message
	 * @param message
	 */
	protected void showErrorToast(String message) {
		Toast.makeText(EaseBaiduMapActivity.this, message, Toast.LENGTH_SHORT).show();
	}
}
