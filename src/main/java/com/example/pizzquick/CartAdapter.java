package com.example.pizzquick;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItem> cartItems;
    private CartItemListener listener;

    public interface CartItemListener {
        void onQuantityChanged(String pizzaId, int newQuantity);
        void onItemRemoved(String pizzaId);
    }

    public CartItemListener getListener() {
        return listener;
    }

    public CartAdapter(List<CartItem> cartItems, CartItemListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView pizzaName, pizzaPrice, quantityText;
        Button decreaseButton, increaseButton, removeButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            pizzaName = itemView.findViewById(R.id.cart_item_name);
            pizzaPrice = itemView.findViewById(R.id.cart_item_price);
            quantityText = itemView.findViewById(R.id.cart_item_quantity);
            decreaseButton = itemView.findViewById(R.id.cart_item_decrease);
            increaseButton = itemView.findViewById(R.id.cart_item_increase);
            removeButton = itemView.findViewById(R.id.cart_item_remove);
        }

        public void bind(CartItem item, CartItemListener listener) {
            pizzaName.setText(item.getPizzaName());
            pizzaPrice.setText(item.getPrice() + " ₽ x " + item.getQuantity() + " = " + item.getTotalPrice() + " ₽");
            quantityText.setText(String.valueOf(item.getQuantity()));

            decreaseButton.setOnClickListener(v -> {
                int newQuantity = item.getQuantity() - 1;
                if (newQuantity > 0) {
                    listener.onQuantityChanged(item.getPizzaId(), newQuantity);
                } else {
                    listener.onItemRemoved(item.getPizzaId());
                }
            });

            increaseButton.setOnClickListener(v -> {
                listener.onQuantityChanged(item.getPizzaId(), item.getQuantity() + 1);
            });

            removeButton.setOnClickListener(v -> {
                listener.onItemRemoved(item.getPizzaId());
            });
        }
    }
}
