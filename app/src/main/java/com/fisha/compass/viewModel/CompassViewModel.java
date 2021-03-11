package com.fisha.compass.viewModel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.hardware.Sensor;
import android.location.Location;
import android.view.Display;
import android.view.Surface;

import com.fisha.compass.R;
import com.fisha.compass.model.CompassModel;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class CompassViewModel extends AndroidViewModel
{
    private final CompassModel model;
    private Location location;
    private Location target;
    private MutableLiveData<String> liveDistance = new MutableLiveData<>();
    @SuppressLint("StaticFieldLeak")
    private final Context context;

    public CompassViewModel(Application context) {
        super(context);
        model = new CompassModel();
        location = new Location("A");
        target = new Location("B");
        setTarget(50.081554, 19.863006);
        model.setDistance( (int) location.distanceTo(target));
        liveDistance.setValue(context.getString(R.string.distance_from_the_destination, 0));
        this.context = context;
    }

    public LiveData<String> getLiveDistance() {
        return liveDistance;
    }

    public Location getLocation() {
        return location;
    }
    public void setLocation(Location location) {
        this.location = location;
    }

    public void setTarget(double latitude, double longitude) {
        target.setLatitude(latitude);
        target.setLongitude(longitude);
    }

    public void updateNewDistance() {
        int distance = (int) location.distanceTo(target);
        if (model.getDistance() != distance)
        {
            model.setDistance(distance);
            liveDistance.setValue(context.getString(R.string.distance_from_the_destination, distance));
        }
    }

    public void countNewBearing() {
        float bearing = location.bearingTo(target);
        bearing = (bearing + 360) % 360;
        bearing = (bearing - model.getAzimuth());
        model.setBearing(bearing);
    }

    public void countNewAzimuth(float orientation, Display display) {
        float azimuth = (float) Math.toDegrees(orientation);

        int rotation = display.getRotation();
        if (rotation == Surface.ROTATION_90)
            azimuth += 90;
        else if (rotation == Surface.ROTATION_180)
            azimuth += 180;
        else if (rotation == Surface.ROTATION_270)
            azimuth += 270;

        azimuth = (azimuth + 360) % 360;
        model.setAzimuth(azimuth);
    }

    public void countNewSensorValues(final int type, final float[] sensorValues)
    {
        final float alpha = 0.97f;

        if (type == Sensor.TYPE_ACCELEROMETER) {
            // Low-pass filter  α * x[i] + (1-α) * y[i-1]
            float[] gravity = model.getGravity();
            gravity[0] = alpha * gravity[0] + (1 - alpha) * sensorValues[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * sensorValues[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * sensorValues[2];
            model.setGravity(gravity);
        }
        else if (type == Sensor.TYPE_MAGNETIC_FIELD) {
            float[] geomagnetic = model.getGeomagnetic();
            geomagnetic[0] = alpha * geomagnetic[0] + (1 - alpha) * sensorValues[0];
            geomagnetic[1] = alpha * geomagnetic[1] + (1 - alpha) * sensorValues[1];
            geomagnetic[2] = alpha * geomagnetic[2] + (1 - alpha) * sensorValues[2];
            model.setGeomagnetic(geomagnetic);
        }
    }

    public void updateCurrentAzimuth() {
        model.updateCurrentAzimuth();
    }

    public void updateCurrentBearing() {
        model.updateCurrentBearing();
    }

    public float[] getGravity() {
        return model.getGravity();
    }

    public float[] getGeomagnetic() {
        return model.getGeomagnetic();
    }

    public float getCurrentAzimuth() {
        return model.getCurrentAzimuth();
    }

    public float getAzimuth() {
        return model.getAzimuth();
    }

    public float getCurrentBearing() {
        return model.getCurrentBearing();
    }

    public float getBearing() {
        return model.getBearing();
    }
}