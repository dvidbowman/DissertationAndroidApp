package com.example.initimagecapture;

// Used to handle retrieval and filtering of User Images from Database

public class UserImage {
    private String imgId, imgDate, imgValue, avgRed, pco2;

    public UserImage(String imgId, String imgDate, String imgValue, String avgRed, String pco2) {
        this.imgId = imgId;
        this.imgDate = imgDate;
        this.imgValue = imgValue;
        this.avgRed = avgRed;
        this.pco2 = pco2;
    }

    public String getImgDate() {
        return imgDate;
    }

    public String getImgId() {
        return imgId;
    }

    public String getImgValue() {
        return imgValue;
    }

    public String getAvgRed() { return avgRed; }

    public String getPco2() { return pco2; }
}
