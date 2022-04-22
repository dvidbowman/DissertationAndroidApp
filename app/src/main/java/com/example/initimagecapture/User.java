package com.example.initimagecapture;

public class User {

    private static int userId;
    private static int noImages;
    private static byte[] byteArray;
    private static byte[] croppedReactiveByteArray;
    private static byte[] croppedNonReactiveByteArray;
    private static boolean cameFromCamera;
    private static boolean loggedIn;

    // resetUser called upon LogOut
    public static void resetUser() {
        userId = -1;
        noImages = -1;
        byteArray = null;
        croppedReactiveByteArray = null;
        croppedNonReactiveByteArray = null;
    }

    public static int getUserId() {
        return userId;
    }

    public static void setUserId(int uId) {
        userId = uId;
    }

    public static int getUserImageNo() { return noImages; }

    public static void setUserImageNo(int numberImages) { noImages = numberImages; }

    public static byte[] getUserByteArray() {
        return byteArray;
    }

    public static void setUserByteArray(byte[] arr) {
        byteArray = arr;
    }

    public static byte[] getCroppedReactiveByteArray() { return croppedReactiveByteArray; }

    public static void setCroppedReactiveByteArray(byte[] arr) { croppedReactiveByteArray = arr; }

    public static byte[] getCroppedNonReactiveByteArray() { return croppedNonReactiveByteArray; }

    public static void setCroppedNonReactiveByteArray(byte[] arr) { croppedNonReactiveByteArray = arr; }

    public static boolean getCameFromCamera() { return cameFromCamera; }

    public static void setCameFromCamera(boolean fromCamera) { cameFromCamera = fromCamera; }

    public static boolean getLoggedIn() { return loggedIn; }

    public static void setLoggedIn(boolean isLoggedIn) { loggedIn = isLoggedIn; }

}
