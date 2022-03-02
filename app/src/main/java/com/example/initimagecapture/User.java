package com.example.initimagecapture;

public class User {

    private static int userId;
    private static String username;
    private static int noImages;
    private static byte[] byteArray;
    private static byte[] croppedImageByteArray;
    private static boolean cameFromCamera;

    // resetUser called upon LogOut
    public static void resetUser() {
        userId = -1;
        username = "";
        noImages = -1;
        byteArray = null;
        croppedImageByteArray = null;
    }

    public static int getUserId() {
        return userId;
    }

    public static void setUserId(int uId) {
        userId = uId;
    }

    public static int getUserImageNo() { return noImages; }

    public static void setUserImageNo(int numberImages) { noImages = numberImages; }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String uname) {
        username = uname;
    }

    public static byte[] getUserByteArray() {
        return byteArray;
    }

    public static void setUserByteArray(byte[] arr) {
        byteArray = arr;
    }

    public static byte[] getCroppedImageByteArray() { return croppedImageByteArray; }

    public static void setCroppedImageByteArray(byte[] arr) { croppedImageByteArray = arr; }

    public static boolean getCameFromCamera() { return cameFromCamera; }

    public static void setCameFromCamera(boolean fromCamera) { cameFromCamera = fromCamera; }

}
