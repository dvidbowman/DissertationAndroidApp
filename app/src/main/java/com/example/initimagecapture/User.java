package com.example.initimagecapture;

public class User {

    private static int userId;
    private static String username;
    private static int noImages;

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

}
