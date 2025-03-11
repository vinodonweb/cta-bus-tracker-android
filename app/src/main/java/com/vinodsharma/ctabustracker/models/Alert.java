package com.vinodsharma.ctabustracker.models;

public class Alert {
    private final String alertId;
    private final String headline;
    private final String shortDescription;

    public Alert(String alertId, String headline, String shortDescription) {
        this.alertId = alertId;
        this.headline = headline;
        this.shortDescription = shortDescription;
    }

    public String getAlertId() {
        return alertId;
    }

    public String getHeadline() {
        return headline;
    }

    public String getShortDescription() {
        return shortDescription;
    }
}
