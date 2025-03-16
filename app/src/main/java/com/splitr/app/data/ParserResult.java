package com.splitr.app.data;

import java.util.List;

public class ParserResult {
    private String name;
    private String date;
    private double totalAmount;
    private List<Item> items;

    public ParserResult(String name, String date, double totalAmount, List<Item> items) {
        this.name = name;
        this.date = date;
        this.totalAmount = totalAmount;
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public List<Item> getItems() {
        return items;
    }
}