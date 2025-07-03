package com.example.pizzquick;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CartActivity extends AppCompatActivity {
    private CartManager cartManager;
    private String userId;
    private CartAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        userId = getIntent().getStringExtra("USER_ID");
        cartManager = CartManager.getInstance(this, userId);

        setupRecyclerView();
        updateTotalPrice();

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        Button checkoutButton = findViewById(R.id.checkout_button);
        checkoutButton.setOnClickListener(v -> {
            if (cartManager.getCartItems().isEmpty()) {
                Toast.makeText(this, "Корзина пуста!", Toast.LENGTH_SHORT).show();
                return;
            }

            ProgressDialog progress = new ProgressDialog(this);
            progress.setMessage("Оформление заказа...");
            progress.setCancelable(false);
            progress.show();

            cartManager.checkoutOrder((success, message) -> {
                progress.dismiss();
                runOnUiThread(() -> {
                    if (success) {

                        Intent paymentIntent = new Intent(CartActivity.this, PaymentActivity.class);
                        paymentIntent.putExtra("USER_ID", userId);
                        startActivityForResult(paymentIntent, 1);
                    } else {
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    }
                });
            });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {

            cartManager.clearCart();
            refreshCart();
            Toast.makeText(this, "Заказ успешно оплачен", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.cart_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CartAdapter(cartManager.getCartItems(), new CartAdapter.CartItemListener() {
            @Override
            public void onQuantityChanged(String pizzaId, int newQuantity) {
                cartManager.updateQuantity(pizzaId, newQuantity);
                updateTotalPrice();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onItemRemoved(String pizzaId) {
                cartManager.removeFromCart(pizzaId);
                updateTotalPrice();
                refreshCart();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void refreshCart() {

        CartAdapter.CartItemListener listener = adapter != null ? adapter.getListener() : null;

        adapter = new CartAdapter(cartManager.getCartItems(), listener);
        RecyclerView recyclerView = findViewById(R.id.cart_recycler_view);
        recyclerView.setAdapter(adapter);
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        TextView totalPriceTextView = findViewById(R.id.total_price_text);
        totalPriceTextView.setText("Итого: " + cartManager.getTotalPrice() + " ₽");
    }
}