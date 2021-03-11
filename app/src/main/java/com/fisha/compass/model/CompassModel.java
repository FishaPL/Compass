package com.fisha.compass.model;

public class CompassModel
{
    private float[] gravity;
    private float[] geomagnetic;
    private float azimuth;
    private float currentAzimuth;
    private float bearing;
    private float currentBearing;
    private int distance;

    public CompassModel()
    {
        gravity = new float[3];
        geomagnetic = new float[3];
        azimuth = 0f;
        currentAzimuth = 0f;
        bearing = 0f;
        currentBearing = 0f;
        distance = 0;
    }

    public void updateCurrentAzimuth() {
        this.currentAzimuth = this.azimuth;
    }

    public void updateCurrentBearing() {
        this.currentBearing = this.bearing;
    }

    public float getAzimuth() {
        return azimuth;
    }

    public float getCurrentAzimuth() {
        return currentAzimuth;
    }

    public float getBearing() {
        return bearing;
    }

    public float getCurrentBearing() {
        return currentBearing;
    }

    public float[] getGravity() {
        return gravity;
    }

    public float[] getGeomagnetic() {
        return geomagnetic;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distanceTo) {
        this.distance = distanceTo;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public void setAzimuth(float azimuth) {
        this.azimuth = azimuth;
    }

    public void setGravity(float[] gravity) {
        this.gravity = gravity;
    }

    public void setGeomagnetic(float[] geomagnetic) {
        this.geomagnetic = geomagnetic;
    }
}
