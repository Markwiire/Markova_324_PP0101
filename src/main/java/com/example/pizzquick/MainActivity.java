package com.example.pizzquick;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String userId;
    private CartManager cartManager;
    private Button btnMyOrders, btn_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        Button btnMyOrders = findViewById(R.id.btn_my_orders);
        btnMyOrders.setOnClickListener(v -> {
            try {
                if (userId == null || userId.isEmpty()) {
                    Toast.makeText(this, "Ошибка: пользователь не распознан", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(MainActivity.this, OrdersActivity.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            } catch (Exception e) {
                Log.e("MainActivity", "Ошибка открытия заказов", e);
                Toast.makeText(this, "Ошибка открытия заказов", Toast.LENGTH_SHORT).show();
            }
        });

        userId = getIntent().getStringExtra("USER_ID");
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Ошибка: пользователь не идентифицирован", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cartManager = CartManager.getInstance(this, userId);


        List<Pizza> pizzas = new ArrayList<>();
        pizzas.add(new Pizza("1", "Маргарита", "Моцарелла, томатный соус, помидоры, свежий базилик", 450, R.drawable.pizza_margherita));
        pizzas.add(new Pizza("2", "Пепперони", "Пипперони, моцарелла, томатный соус", 550, R.drawable.pizza_pepperoni));
        pizzas.add(new Pizza("3", "Гавайская", "Курица, ананасы, томатный соус, итальянские травы ", 550, R.drawable.pizza_hawaiian));
        pizzas.add(new Pizza("4", "Песто", "Моцарелла, томаты, пармезан, базилик, соус песто", 550, R.drawable.pizza_pesto));
        pizzas.add(new Pizza("5", "Сырная", "Моцарелла, пармезан, базилик, чеснок, сырный соус", 550, R.drawable.pizza_cheese));
        pizzas.add(new Pizza("6", "Мясная", "Бекон, пепперони, курица, моцарелла, томатный соус", 550, R.drawable.pizza_meat));
        pizzas.add(new Pizza("7", "Двойной цыпленок", "Курица, моцарелла, томаты, томатный соус", 550, R.drawable.pizza_double));



        RecyclerView recyclerView = findViewById(R.id.pizzaRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        PizzaAdapter adapter = new PizzaAdapter(pizzas, pizza -> {
            PizzaDialog.showPizzaDetails(this, pizza, () -> {
                cartManager.addToCart(pizza, 1);
                Toast.makeText(this, pizza.getName() + " добавлена в корзину", Toast.LENGTH_SHORT).show();
            });
        });

        recyclerView.setAdapter(adapter);


        ImageView cartButton = findViewById(R.id.cart_button);
        cartButton.setOnClickListener(v -> {
            Log.d("DEBUG", "Текущий userId: " + userId);

            if (userId == null || userId.isEmpty()) {
                Toast.makeText(this, "Ошибка: пользователь не распознан", Toast.LENGTH_LONG).show();
                return;
            }

            try {
                Intent intent = new Intent(this, CartActivity.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            } catch (Exception e) {
                Log.e("ERROR", "Ошибка открытия корзины", e);
                Toast.makeText(this, "Ошибка открытия: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
