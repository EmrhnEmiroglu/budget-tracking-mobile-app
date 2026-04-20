package com.example.proje.model;

public class Transaction {
    private int id;
    private String title;
    private double amount;
    private String category;
    private String type;
    private String note;
    private String date;

    public Transaction(int id, String title, double amount, String category, String type, String note, String date) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.type = type;
        this.note = note;
        this.date = date;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public String getType() { return type; }
    public String getNote() { return note; }
    public String getDate() { return date; }
}