package com.example.pizzquick;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order {
    private String id;
    private String userId;
    private int totalPrice;
    private String status;
    private Date createdAt;
    private List<OrderItem> items;

    public Order(String id, String userId, int totalPrice, String status, Date createdAt) {
        this.id = id != null ? id : "unknown";
        this.userId = userId != null ? userId : "unknown";
        this.totalPrice = totalPrice;
        this.status = status != null ? status : "unknown";
        this.createdAt = createdAt != null ? createdAt : new Date();
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public int getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }
    public Date getCreatedAt() { return createdAt; }
    public List<OrderItem> getItems() { return items; }

    public void setItems(List<OrderItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }
}