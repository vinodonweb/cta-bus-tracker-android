package com.vinodsharma.ctabustracker.models;

import java.io.Serializable;

public class Routes implements Serializable {

    private String rNum;
    private String rName;
    private String rColor;

    public Routes(String rNum, String rName, String rColor) {
        this.rNum = rNum;
        this.rName = rName;
        this.rColor = rColor;
    }

    public String getrNum() {
        return rNum;
    }

    public String getrName() {
        return rName;
    }

    public String getrColor() {
        return rColor;
    }

}
