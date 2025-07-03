package com.example.pizzquick;

public class Pizza {
    private String id;
    private String name;
    private String description;
    private int price;
    private int imageRes;

    public Pizza(String id, String name, String description, int price, int imageRes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageRes = imageRes;
    }

    // Геттеры
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getPrice() { return price; }
    public int getImageRes() { return imageRes; }
}
