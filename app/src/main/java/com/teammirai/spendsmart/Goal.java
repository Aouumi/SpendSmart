package com.teammirai.spendsmart.model;

public class Goal {
    private String title;
    private String description;
    private double targetAmount;
    private double currentAmount;
    private String goalName;
    private String date;
    private String time;
    private String amount;

    // Default constructor required for calls to DataSnapshot.getValue(Goal.class)
    public Goal() {
    }

    public Goal(String title, String description, double targetAmount, double currentAmount, String goalName, String date, String time, String amount) {
        this.title = title;
        this.description = description;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.goalName = goalName;
        this.date = date;
        this.time = time;
        this.amount = amount;
    }

    // Getters and setters

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
    }

    public String getGoalName() {
        return goalName;
    }

    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    // Serialization method
    public String toString() {
        return title + "," + description + "," + targetAmount + "," + currentAmount + "," + goalName + "," + date + "," + time + "," + amount;
    }

    // Deserialization method
    public static Goal fromString(String goalString) {
        String[] parts = goalString.split(",");
        if (parts.length == 8) {
            String title = parts[0];
            String description = parts[1];
            double targetAmount = Double.parseDouble(parts[2]);
            double currentAmount = Double.parseDouble(parts[3]);
            String goalName = parts[4];
            String date = parts[5];
            String time = parts[6];
            String amount = parts[7];
            return new Goal(title, description, targetAmount, currentAmount, goalName, date, time, amount);
        }
        return null;
    }
}

