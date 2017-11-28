package com.example.skyworthclub.visible_light_communication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity  implements LocationSource, AMapLocationListener,
        TextWatcher, AdapterView.OnItemClickListener, Inputtips.InputtipsListener{

    private MapView mMapView;
    //初始化地图控制器对象
    private AMap aMap;
    private CameraUpdate cameraUpdate;

    private EditText editText;
    private ListView listView;
    private TextView textView;
    private SearchAdapter searchAdapter;
    List<HashMap<String,String>> searchList;
    private String currentCity;


    OnLocationChangedListener onLocationChangedListener;
    AMapLocationClient mlocationClient;
    AMapLocationClientOption mLocationOption;
    //经纬度地点依次是正佳，天河城，太古汇
    private double[] position = {23.1315797200,113.3195285800,23.1322190000,113.3226170000,23.1342510000,113.3324550000};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.xyj_main);

        init();//初始化

        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);

        // 设置定位监听，必须放在前面才能实现监听
        aMap.setLocationSource(this);
        MyLocationStyle myLocationStyle;
        //初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
        // 连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）
        // 如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle = new MyLocationStyle();
        //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
//        myLocationStyle.interval(4000);

        //定位一次，且将视角移动到地图中心点。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);

        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        // 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        aMap.setMyLocationEnabled(true);

        //隐藏左下角的"高德地图"logo
        UiSettings uiSettings = aMap.getUiSettings();
        uiSettings.setLogoBottomMargin(-50);
//        uiSettings.setScaleControlsEnabled(true);
//        Log.e("TAG", "缩放功能"+uiSettings.isScaleControlsEnabled()+"");

//        aMap.moveCamera(CameraUpdateFactory.zoomTo(15));

        getAdress(position[0], position[1]);
        getAdress(position[2], position[3]);
        getAdress(position[4],position[5]);


    }

    private void init(){
        mMapView = (MapView) findViewById(R.id.map);
        editText = (EditText)findViewById(R.id.xyj_editText);
        listView = (ListView)findViewById(R.id.xyj_listView);
        textView = (TextView)findViewById(R.id.xyj_currentCity);
        if (aMap == null) {
            aMap = mMapView.getMap();
        }

        editText.addTextChangedListener(this);
        listView.setOnItemClickListener(this);
    }


    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener listener) {

//        System.out.println("开始工作");
        onLocationChangedListener = listener;
        if (mlocationClient == null) {
            //初始化定位
            mlocationClient = new AMapLocationClient(this);
            //初始化定位参数
            mLocationOption = new AMapLocationClientOption();
            //设置定位回调监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();//启动定位
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        onLocationChangedListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {

//        System.out.println("开始工作");
        if (onLocationChangedListener != null && amapLocation != null) {
            if (amapLocation != null && amapLocation.getErrorCode() == 0) {
                // 显示系统小蓝点
                onLocationChangedListener.onLocationChanged(amapLocation);
                //获取当前城市
                currentCity = amapLocation.getCity();
                textView.setText(currentCity);
                Log.e("TAG","当前城市："+currentCity);
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode()+ ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr",errText);
            }
        }
    }


    /*
    解释指定坐标的地址
    @param x 经度
    @param y 纬度
     */
    public void getAdress(final double x, final double y){
        Log.e("TAG", "调用getAdress");

        //地址查询器
        GeocodeSearch geocodeSearch = new GeocodeSearch(this);
        //设置查询参数,
        //三个参数依次为坐标，范围多少米，坐标系
        RegeocodeQuery regeocodeQuery = new RegeocodeQuery(new LatLonPoint(x, y), 200, GeocodeSearch.AMAP);
        //设置查询结果监听
        geocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            //根据坐标获取地址信息调用
            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
                String result = regeocodeResult.getRegeocodeAddress().getFormatAddress();
                Log.e("Shunxu","获得请求结果");
                makepoint(x, y, result);
            }
            //根据地址获取坐标信息是调用
            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

            }
        });
        //发起异步查询请求
        geocodeSearch.getFromLocationAsyn(regeocodeQuery);

    }

    /*
    在地图上绘制相应的点
    @param x       经度
    @param y       纬度
    @param result  地点名称
     */
    public void makepoint(double x, double y, String result){
        Log.e("Shunxu","开始绘图");
        //北纬39.22，东经116.39，为负则表示相反方向
        LatLng latLng=new LatLng(x, y);
        Log.e("地址",result);

        //使用默认点标记
        Marker marker = aMap.addMarker(new MarkerOptions().position(latLng).title(x+"").snippet(result));
        //改变可视区域为指定位置
        //CameraPosition4个参数分别为位置，缩放级别，目标可视区域倾斜度，可视区域指向方向（正北逆时针算起，0-360）
        cameraUpdate= CameraUpdateFactory.newCameraPosition(new CameraPosition(latLng,12,0,30));
        aMap.moveCamera(cameraUpdate);//地图移向指定区域

        aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String temp = marker + "";
                Log.e("TAG", "这个是什么"+marker.getTitle()+"你爱我"+temp.length());
                Toast.makeText(MainActivity.this,"点击指定位置",Toast.LENGTH_SHORT).show();
                return false;//false 点击marker marker会移动到地图中心，true则不会
            }
        });

        aMap.setOnInfoWindowClickListener(new AMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Toast.makeText(MainActivity.this,"点击了我的地点",Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
//        Log.e("TAG", "你看见了吗");
        //获取自动提示输入框的内容
        String content = s.toString().trim();

        //初始化一个输入提示搜索对象，并传入参数
        InputtipsQuery inputtipsQuery = new InputtipsQuery(content,currentCity);
        //将获取到的结果进行城市限制筛选
        inputtipsQuery.setCityLimit(true);
        //定义一个输入提示对象，传入当前上下文和搜索对象
        Inputtips inputtips=new Inputtips(this,inputtipsQuery);
        //设置输入提示查询的监听，实现输入提示的监听方法onGetInputtips()
        inputtips.setInputtipsListener(this);
        //输入查询提示的异步接口实现
        inputtips.requestInputtipsAsyn();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onGetInputtips(List<Tip> list, int returnCode) {
        //如果输入提示搜索成功
        if(returnCode == AMapException.CODE_AMAP_SUCCESS){
            searchList = new ArrayList<HashMap<String, String>>();
            for (int i=0;i<list.size();i++){
                HashMap<String,String> hashMap = new HashMap<String, String>();
                hashMap.put("name",list.get(i).getName());
                //将地址信息取出放入HashMap中
                hashMap.put("address",list.get(i).getDistrict());
                Log.e("TAG", list.get(i).getPoint().toString());
                //解析返回的经纬度
                String latlonPoint = list.get(i).getPoint().toString();
                //经度
                String x = latlonPoint.substring(0, latlonPoint.indexOf(","));
                //纬度
                String y = latlonPoint.substring(latlonPoint.indexOf(",")+1, latlonPoint.length());
                //详细地址
                String detailAddress = list.get(i).getAddress();
                hashMap.put("x", x);
                hashMap.put("y", y);
                hashMap.put("detailAddress", detailAddress);
                //将HashMap放入表中
                searchList.add(hashMap);

            }
            //新建一个适配器
            searchAdapter = new SearchAdapter(this, searchList);
            //为listview适配
            listView.setAdapter(searchAdapter);

        }else{
            //清空原来的所有item
            searchList.clear();
            searchAdapter.notifyDataSetChanged();
            Log.e("TAG", "没错，这个是错的返回码"+returnCode);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        double x = Double.parseDouble(searchList.get(position).get("x"));
        double y = Double.parseDouble(searchList.get(position).get("y"));
        String detailAddress = searchList.get(position).get("detailAddress");
        Log.e("TAG","经度"+x+"纬度"+y);
        //在地图上显示我点击的地点
        makepoint(x, y, detailAddress);
        //把listView清空
        searchList.clear();
        searchAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
        if(null != mlocationClient){
            mlocationClient.onDestroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }
}
