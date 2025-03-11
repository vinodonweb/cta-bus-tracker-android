package com.vinodsharma.ctabustracker.models;

public class Vehicles {
    private double lat;
    private double lon;

    public Vehicles(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }
}
