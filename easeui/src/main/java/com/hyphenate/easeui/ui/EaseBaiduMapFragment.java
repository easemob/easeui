package com.hyphenate.easeui.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
import com.hyphenate.easeui.ui.base.EaseBaseFragment;


public class EaseBaiduMapFragment extends EaseBaseFragment {
    protected MapView mapView;
    protected double latitude;
    protected double longtitude;
    protected String address;
    private BaiduMap baiduMap;
    private LocationClient mLocClient;
    private BaiduSDKReceiver mBaiduReceiver;
    protected BDLocation lastLocation;
    private OnBDLocationListener listener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initialize SDK with context, should call this before setContentView
        SDKInitializer.initialize(requireActivity().getApplicationContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ease_fragment_baidumap, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initArguments();
        initView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    protected void initArguments() {
        Bundle bundle = getArguments();
        if(bundle != null) {
            latitude = bundle.getDouble("latitude", 0);
            longtitude = bundle.getDouble("longtitude", 0);
            address = bundle.getString("address");
        }
    }

    protected void initView() {
        mapView = findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        baiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(15.0f));
        mapView.setLongClickable(true);
    }

    protected void initData() {
        if(latitude == 0) {
            mapView = new MapView(mContext, new BaiduMapOptions());
            baiduMap.setMyLocationConfigeration(
                    new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, null));
            showMapWithLocationClient();
        }else {
            LatLng lng = new LatLng(latitude, longtitude);
            mapView = new MapView(mContext,
                    new BaiduMapOptions().mapStatus(new MapStatus.Builder().target(lng).build()));
            showMap(latitude, longtitude);
        }
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        mBaiduReceiver = new BaiduSDKReceiver();
        mContext.registerReceiver(mBaiduReceiver, iFilter);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        if (mLocClient != null) {
            mLocClient.start();
        }
    }

    @Override
    public void onPause() {
        mapView.onPause();
        if (mLocClient != null) {
            mLocClient.stop();
        }
        super.onPause();
        lastLocation = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mContext.isFinishing()) {
            if (mLocClient != null)
                mLocClient.stop();
            mapView.onDestroy();
            mContext.unregisterReceiver(mBaiduReceiver);
        }
    }

    protected void showMapWithLocationClient() {
        mLocClient = new LocationClient(mContext);
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
        mLocClient.setLocOption(option);
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

    /**
     * on receive baidu location
     * @param bdLocation
     */
    protected void onReceiveBDLocation(BDLocation bdLocation) {
        if(bdLocation == null) {
            return;
        }
        if(listener != null) {
            listener.onReceiveBDLocation(bdLocation);
        }
        lastLocation = bdLocation;
        baiduMap.clear();
        showMap(lastLocation.getLatitude(), lastLocation.getLongitude());
    }

    /**
     * show error message
     * @param message
     */
    protected void showErrorToast(String message) {

    }

    public class EaseBDLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            onReceiveBDLocation(bdLocation);
        }
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

    public void setOnBDLocationListener(OnBDLocationListener listener) {
        this.listener = listener;
    }

    public interface OnBDLocationListener {
        /**
         * 获取到定位信息
         * @param bdLocation
         */
        void onReceiveBDLocation(BDLocation bdLocation);
    }

}
