package com.vinodsharma.ctabustracker.models;

public class Stops {
    private String stpid;
    private String stpnm;
    private String lat;
    private String lon;

    public Stops(String stpid, String stpnm, String lat, String lon) {
        this.stpid = stpid;
        this.stpnm = stpnm;
        this.lat = lat;
        this.lon = lon;
    }

    public String getStpid() {
        return stpid;
    }

    public String getStpnm() {
        return stpnm;
    }


    public String getLat() {
        return lat;
    }


    public String getLon() {
        return lon;
    }

}
