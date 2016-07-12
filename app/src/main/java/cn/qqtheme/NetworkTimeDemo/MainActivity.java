package cn.qqtheme.NetworkTimeDemo;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * 通过网络及定位获取真实的本地时间
     */
    public void onRealTime(View view) {
        final int TIMEOUT = 10;
        final Intent service = new Intent(this, LocationService.class);
        startService(service);
        new Thread() {
            @Override
            public void run() {
                long gmtTime = 0;
                try {
                    URL url = new URL("https://www.baidu.com");
                    URLConnection connection = url.openConnection();
                    connection.setUseCaches(false);
                    connection.setConnectTimeout(TIMEOUT * 1000);
                    connection.setReadTimeout(TIMEOUT * 1000);
                    String gmtStr = connection.getHeaderField("Date");
                    Log.i("liyujiang", "get time from network succeed: " + gmtStr);
                    SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss", Locale.UK);
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));//0时区时间
                    gmtTime = sdf.parse(gmtStr).getTime();
                    Log.i("liyujiang", "gmt timestamp: " + gmtTime);
                } catch (Exception e) {
                    Log.e("liyujiang", "get time from network error: " + e);
                    return;
                }
                Location location = null;
                int i = 0;
                while (true) {
                    if (i >= TIMEOUT) {
                        Log.w("liyujiang", "location timeout");
                        break;
                    }
                    location = LocationService.getLocation();
                    if (location != null) {
                        Log.i("liyujiang", "location succeed i=" + i);
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        Log.e("liyujiang", "location InterruptedException");
                    }
                    i++;
                }
                int timeZoneInt;
                int deviceZone = Calendar.getInstance(Locale.getDefault())
                        .get(Calendar.ZONE_OFFSET) / 60 / 60 / 1000;//本机设置的时区
                if (location != null) {
                    double longitude = location.getLongitude();//经度
                    timeZoneInt = (int) Math.round(longitude / 15);//基于经度算出的实际时区
                    Log.i("liyujiang", "provider=" + location.getProvider() + ",longitude=" + longitude
                            + ",location zone=" + timeZoneInt + ",device zone=" + deviceZone);
                } else {
                    Log.w("liyujiang", "location failed, use device timezone");
                    timeZoneInt = deviceZone;
                }
                long realTime = gmtTime + timeZoneInt * 60 * 60 * 1000;
                long beijingTime = gmtTime + 8 * 60 * 60 * 1000;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
                String timeReal = sdf.format(realTime);//当地真实时间
                String timeBeijing = sdf.format(beijingTime);//北京时间
                sdf.setTimeZone(TimeZone.getDefault());
                String timeDevice = sdf.format(System.currentTimeMillis());//本机时间
                Log.i("liyujiang", "timeReal=" + timeReal + ",timeBeijing=" + timeBeijing + ",timeDevice=" + timeDevice);
                stopService(service);
            }
        }.start();
    }

}
