package com.example.pizzquick;

public class OrderItem {
    private String pizzaId;
    private int quantity;
    private double price;

    public OrderItem(String pizzaId, int quantity, double price) {
        this.pizzaId = pizzaId;
        this.quantity = quantity;
        this.price = price;
    }
    public String getPizzaId() { return pizzaId; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
}