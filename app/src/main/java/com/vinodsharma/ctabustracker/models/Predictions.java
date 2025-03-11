package com.vinodsharma.ctabustracker.models;

public class Predictions {

    private String vid;  // Vehicle ID
    private String rtDir; // Route Direction
    private String des;  // Route Destination
    private String prdtm; // Prediction arrival date /time at the stop
    private String dly; //indicates if the bus is delayed
    private String prdctdm; //Number of minutes untill the bus arrives at bus stop

    public Predictions(String vid, String rtDir, String des, String prdtm, String dly, String prdctdm) {
        this.vid = vid;
        this.rtDir = rtDir;
        this.des = des;
        this.prdtm = prdtm;
        this.dly = dly;
        this.prdctdm = prdctdm;
    }

    public String getVid() {
        return vid;
    }

    public String getRtDir() {
        return rtDir;
    }

    public String getDes() {
        return des;
    }

    public String getPrdtm() {
        return prdtm;
    }

    public String getDly() {
        return dly;
    }

    public String getPrdctdm() {
        return prdctdm;
    }
}
