package com.example.initimagecapture;

public class User {
    private static User user;

    private int userId = -1;
    private int noImages = 0;
    private boolean loggedIn = false;

    private User() {}

    public static User getInstance() {
        if (user == null) {
            user = new User();
        }

        return user;
    }

    // resetUser called upon LogOut
    public void resetUser() {
        user.setUserId(-1);
        user.setUserImageNo(0);
        user.setLoggedIn(false);
    }

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int userId) {
        if (userId > 0) {
            this.userId = userId;
        }
    }

    public int getUserImageNo() {
        return this.noImages;
    }

    public void setUserImageNo(int noImages) {
        if (noImages >= 0) {
            this.noImages = noImages;
        }
    }

    public boolean getLoggedIn() {
        return this.loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

}
