package com.game.cookingspree;

import java.util.ArrayList;
import java.util.List;

//our users are called "Chef" like if username is "Bob" then they will be called "Chef Bob"
public class Chef {
    private String uid;
    private String email;
    private String chefName;
    private String chefCode;
    private String photoUrl;
    private int highScore;
    private int gamesPlayed;
    private double averageScore;
    private List<String> following;

    public Chef() {
        // Required by Firestore
    }

    public Chef(String uid, String email, String chefName, String chefCode, String photoUrl) {
        this.uid = uid;
        this.email = email;
        this.chefName = chefName;
        this.chefCode = chefCode;
        this.photoUrl = photoUrl;
        this.highScore = 0;
        this.gamesPlayed = 0;
        this.averageScore = 0;
        this.following = new ArrayList<>();
    }

    // Getters and Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getChefName() { return chefName; }
    public void setChefName(String chefName) { this.chefName = chefName; }

    public String getChefCode() { return chefCode; }
    public void setChefCode(String chefCode) { this.chefCode = chefCode; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public int getHighScore() { return highScore; }
    public void setHighScore(int highScore) { this.highScore = highScore; }

    public int getGamesPlayed() { return gamesPlayed; }
    public void setGamesPlayed(int gamesPlayed) { this.gamesPlayed = gamesPlayed; }

    public double getAverageScore() { return averageScore; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }

    public List<String> getFollowing() { return following; }
    public void setFollowing(List<String> friends) { this.following = following; }

    public void follow(String uid) {
        if (!following.contains(uid)) {
            following.add(uid);
        }
    }
}
