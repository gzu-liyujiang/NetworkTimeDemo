package cn.qqtheme.NetworkTimeDemo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * 简单的定位服务
 * <p/>
 * Author:李玉江[QQ:1032694760]
 * DateTime:2016/7/11 23:05
 * Builder:Android Studio
 */
public class LocationService extends Service {
    private LocationListener listener;
    private LocationManager lm;
    private static Location location;

    public static Location getLocation() {
        return location;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        listener = new MyLocationListener();
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location == null) {
            location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, listener);
        }
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, listener);
        }
        return START_STICKY_COMPATIBILITY;
    }

    @Override
    public void onDestroy() {
        if (lm != null) {
            lm.removeUpdates(listener);
        }
        super.onDestroy();
    }

    private class MyLocationListener implements LocationListener {

        public void onLocationChanged(Location loc) {
            //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
            if (loc != null) {
                location = loc;
            }
        }

        public void onProviderDisabled(String provider) {
            // Provider被disable时触发此函数，比如GPS被关闭
        }

        public void onProviderEnabled(String provider) {
            //  Provider被enable时触发此函数，比如GPS被打开
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // Provider的转态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        }

    }

}
