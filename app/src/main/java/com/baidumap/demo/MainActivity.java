package com.baidumap.demo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements SensorEventListener, LocationListener {

    private CompassView compassView;
    private TextView tvDirection;
    private TextView tvDegree;
    private TextView tvLat;
    private TextView tvLng;
    private TextView tvAltitude;
    private TextView tvSpeed;
    private TextView tvAccuracy;
    private TextView tvAddress;
    private TextView tvStatus;
    private Button btnLocate;

    private SensorManager sensorManager;
    private LocationManager locationManager;
    private float[] gravity = new float[3];
    private float[] geomagnetic = new float[3];
    private float[] rotation = new float[9];
    private float[] orientation = new float[3];
    private boolean hasGravity = false;
    private boolean hasMagnetic = false;

    private static final int PERMISSION_REQUEST_CODE = 100;
    private int locateClickCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        compassView = findViewById(R.id.compassView);
        tvDirection = findViewById(R.id.tvDirection);
        tvDegree = findViewById(R.id.tvDegree);
        tvLat = findViewById(R.id.tvLat);
        tvLng = findViewById(R.id.tvLng);
        tvAltitude = findViewById(R.id.tvAltitude);
        tvSpeed = findViewById(R.id.tvSpeed);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        tvAddress = findViewById(R.id.tvAddress);
        tvStatus = findViewById(R.id.tvStatus);
        btnLocate = findViewById(R.id.btnLocate);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        btnLocate.setOnClickListener(v -> {
            locateClickCount++;
            doLocate();
        });

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                }, PERMISSION_REQUEST_CODE);
            } else {
                doLocate();
            }
        } else {
            doLocate();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            doLocate();
        }
    }

    private void doLocate() {
        tvStatus.setText("定位中...");

        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!gpsEnabled && !networkEnabled) {
            tvStatus.setText("请开启GPS或网络定位");
            Toast.makeText(this, "请在设置中开启定位服务", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            // 先尝试获取上次已知位置，立即显示
            Location lastGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location lastNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location best = pickBest(lastGps, lastNet);
            if (best != null) {
                onLocationChanged(best);
                tvStatus.setText("已显示缓存位置，等待精确GPS...");
            }

            // 注册GPS监听
            if (gpsEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);
            }
            // 注册网络监听（作为GPS的补充，尤其在室内）
            if (networkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
            }

            if (best == null && !gpsEnabled) {
                tvStatus.setText("等待网络定位...");
            } else if (best == null) {
                tvStatus.setText("等待GPS信号...");
            }
        } catch (SecurityException e) {
            tvStatus.setText("无定位权限");
        }
    }

    private Location pickBest(Location a, Location b) {
        if (a == null) return b;
        if (b == null) return a;
        if (a.getAccuracy() <= b.getAccuracy()) return a;
        return b;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Sensor accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (accel != null) sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_UI);
        if (magnetic != null) sensorManager.registerListener(this, magnetic, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        try {
            locationManager.removeUpdates(this);
        } catch (SecurityException ignored) {}
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravity, 0, 3);
            hasGravity = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, geomagnetic, 0, 3);
            hasMagnetic = true;
        }

        if (hasGravity && hasMagnetic) {
            SensorManager.getRotationMatrix(rotation, null, gravity, geomagnetic);
            SensorManager.getOrientation(rotation, orientation);

            float azimuth = (float) Math.toDegrees(orientation[0]);
            if (azimuth < 0) azimuth += 360;

            compassView.setDegree(azimuth);
            tvDegree.setText(String.format("%.1f°", azimuth));
            tvDirection.setText(getDirection(azimuth));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onLocationChanged(Location location) {
        tvLat.setText(String.format("%.6f", location.getLatitude()));
        tvLng.setText(String.format("%.6f", location.getLongitude()));

        double alt = location.hasAltitude() ? location.getAltitude() : 0;
        tvAltitude.setText(String.format("%.1f m", alt));

        float speedMs = location.hasSpeed() ? location.getSpeed() : 0;
        float speedKmh = speedMs * 3.6f;
        tvSpeed.setText(String.format("%.1f km/h", speedKmh));

        float acc = location.getAccuracy();
        tvAccuracy.setText(String.format("%.1f m", acc));

        final float finalAcc = acc;
        // 更新状态
        String provider = location.getProvider();
        if ("gps".equals(provider)) {
            tvStatus.setText("GPS定位 | 精度" + String.format("%.0f", finalAcc) + "m");
        } else {
            tvStatus.setText("网络定位 | 精度" + String.format("%.0f", finalAcc) + "m");
        }

        // 反向地理编码获取地址
        reverseGeocode(location.getLatitude(), location.getLongitude());
    }

    private void reverseGeocode(double lat, double lng) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address addr = addresses.get(0);
                    StringBuilder sb = new StringBuilder();
                    // 省/市/区/街道
                    if (addr.getAdminArea() != null) sb.append(addr.getAdminArea());
                    if (addr.getLocality() != null) sb.append(addr.getLocality());
                    if (addr.getSubLocality() != null) sb.append(addr.getSubLocality());
                    if (addr.getThoroughfare() != null) sb.append(addr.getThoroughfare());
                    if (addr.getSubThoroughfare() != null) sb.append(" ").append(addr.getSubThoroughfare());

                    String addressText = sb.toString();
                    if (addressText.isEmpty()) {
                        addressText = addr.getAddressLine(0) != null ? addr.getAddressLine(0) : "未知位置";
                    }
                    final String fullAddress = addressText;

                    runOnUiThread(() -> tvAddress.setText(fullAddress));
                } else {
                    runOnUiThread(() -> tvAddress.setText("未知位置"));
                }
            } catch (IOException e) {
                runOnUiThread(() -> tvAddress.setText("获取地址失败"));
            }
        }).start();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}

    private String getDirection(float degree) {
        if (degree >= 337.5 || degree < 22.5) return "北 N";
        if (degree >= 22.5 && degree < 67.5) return "东北 NE";
        if (degree >= 67.5 && degree < 112.5) return "东 E";
        if (degree >= 112.5 && degree < 157.5) return "东南 SE";
        if (degree >= 157.5 && degree < 202.5) return "南 S";
        if (degree >= 202.5 && degree < 247.5) return "西南 SW";
        if (degree >= 247.5 && degree < 292.5) return "西 W";
        return "西北 NW";
    }
}
