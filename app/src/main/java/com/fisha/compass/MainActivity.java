package com.fisha.compass;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Toast;

import com.fisha.compass.databinding.ActivityMainBinding;
import com.fisha.compass.viewModel.CompassViewModel;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener
{
    private SensorManager sensorManager;
    private LocationManager locationManager;
    int PLACE_PICKER_REQUEST = 1;

    private CompassViewModel viewModel;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(CompassViewModel.class);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO - permission allow
            Toast.makeText(this, "Accept location permission!", Toast.LENGTH_SHORT).show();
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, this);
        if (isLocationEnabled())
            getMyLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
    }

    public void setDestinationPlacePicker(View view)
    {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(MainActivity.this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    private boolean isLocationEnabled() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Enable Location");
            alertDialog.setMessage("Your locations setting is not enabled. Please enabled it in settings menu.");
            alertDialog.setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alert = alertDialog.create();
            alert.show();
            return false;
        }
        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        synchronized (this) {
            viewModel.countNewSensorValues(sensorEvent.sensor.getType(), sensorEvent.values);

            float[] R = new float[9];
            float[] I = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, viewModel.getGravity(), viewModel.getGeomagnetic());

            if (success) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);

                Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                viewModel.countNewAzimuth(orientation[0], display);

                if (viewModel.getLocation() != null) {
                    viewModel.countNewBearing();
                    viewModel.updateNewDistance();
                }

                float currentAzimuth = viewModel.getCurrentAzimuth();
                float aziumth = viewModel.getAzimuth();
                if (currentAzimuth != aziumth) {
                    Animation animation = new RotateAnimation(-currentAzimuth, -aziumth, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    viewModel.updateCurrentAzimuth();
                    animation.setDuration(500);
                    animation.setRepeatCount(0);
                    animation.setFillAfter(true);
                    binding.imageCompass.startAnimation(animation);
                }

                float currentBearing = viewModel.getCurrentBearing();
                float bearing = viewModel.getBearing();
                if (currentBearing != bearing) {
                    Animation animationArrow = new RotateAnimation(currentBearing, bearing, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    viewModel.updateCurrentBearing();
                    animationArrow.setDuration(500);
                    animationArrow.setRepeatCount(0);
                    animationArrow.setFillAfter(true);
                    binding.imageArrow.startAnimation(animationArrow);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    // LocationListener
    @Override
    public void onLocationChanged(Location location) {
        viewModel.setLocation(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Toast.makeText(this, "onStatusChanged", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String s) {
        Toast.makeText(this, "onProviderEnabled\n" + s, Toast.LENGTH_SHORT).show();
        getMyLocation();
    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(this, "onProviderDisabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                double lat = place.getLatLng().latitude;
                double lon = place.getLatLng().longitude;
                viewModel.setTarget(lat, lon);
            }
        }
    }

    public void getMyLocation()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO - permission allow
            Toast.makeText(this, "Accept location permission!", Toast.LENGTH_SHORT).show();
            return;
        }

        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, false);
        viewModel.setLocation(locationManager.getLastKnownLocation(bestProvider));
    }

}