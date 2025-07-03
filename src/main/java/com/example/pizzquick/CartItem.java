package com.example.pizzquick;

public class CartItem {
    private String pizzaId;
    private String pizzaName;
    private int quantity;
    private int price;

    public CartItem(String pizzaId, String pizzaName, int quantity, int price) {
        this.pizzaId = pizzaId;
        this.pizzaName = pizzaName;
        this.quantity = quantity;
        this.price = price;
    }

    public String getPizzaId() { return pizzaId; }
    public String getPizzaName() { return pizzaName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getPrice() { return price; }
    public int getTotalPrice() { return price * quantity; }
}
