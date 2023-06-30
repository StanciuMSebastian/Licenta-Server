package org.example.entities;

public class Rating {
    private int ratingId;
    private User tester;
    private String message;
    private double value;

    public int getRatingId() {
        return ratingId;
    }

    public User getTester() {
        return tester;
    }

    public String getMessage() {
        return message;
    }

    public double getValue() {
        return value;
    }

    public Rating(int ratingId, User tester, String message, double value) {
        this.ratingId = ratingId;
        this.tester = tester;
        this.message = message;
        this.value = value;
    }
}
