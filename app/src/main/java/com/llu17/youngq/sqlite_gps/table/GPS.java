package com.llu17.youngq.sqlite_gps.table;

/**
 * Created by youngq on 17/3/28.
 */

public class GPS {
    private String id;
    private long timestamp;
    private double latitude;
    private double longitude;
    private double bearing;
    private double speed;

    public GPS(String id, long timestamp, double latitude, double longitude, double bearing, double speed){
        this.id = id;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.bearing = bearing;
        this.speed = speed;
    }

    public GPS() {}

    public double getBearing() {
        return bearing;
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString(){
        return "GPS: {" + " id: " + id + " timestamp: " + timestamp + " latitude: "
                + latitude + " longitude: " + longitude + " bearing: " + bearing + " speed: " + speed + " }";
    }
}
