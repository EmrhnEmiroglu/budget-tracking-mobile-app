package com.example.proje.model;

public class Goal {
    private int id;
    private String title;
    private double targetAmount;
    private double currentAmount;
    private String note;

    public Goal(int id, String title, double targetAmount, double currentAmount, String note) {
        this.id = id;
        this.title = title;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.note = note;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public double getTargetAmount() { return targetAmount; }
    public double getCurrentAmount() { return currentAmount; }
    public String getNote() { return note; }
}