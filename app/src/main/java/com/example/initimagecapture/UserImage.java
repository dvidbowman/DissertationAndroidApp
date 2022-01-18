package com.example.initimagecapture;

// Used to handle retrieval and filtering of User Images from Database

public class UserImage {
    private String imgId, imgDate, imgValue;

    public UserImage(String imgId, String imgDate, String imgValue) {
        this.imgId = imgId;
        this.imgDate = imgDate;
        this.imgValue = imgValue;
    }

    public String getImgDate() {
        return imgDate;
    }

    public void setImgDate(String imgDate) {
        this.imgDate = imgDate;
    }

    public String getImgId() {
        return imgId;
    }

    public void setImgId(String imgId) {
        this.imgId = imgId;
    }

    public String getImgValue() {
        return imgValue;
    }

    public void setImgValue(String imgValue) {
        this.imgValue = imgValue;
    }
}
