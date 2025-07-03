package com.example.pizzquick;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OrdersActivity extends AppCompatActivity {

    private static final String TAG = "OrdersActivity";
    private static final String SUPABASE_URL = "https://rrmyhvqaluvfommjwmrk.supabase.co";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJybXlodnFhbHV2Zm9tbWp3bXJrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTA3OTg2OTgsImV4cCI6MjA2NjM3NDY5OH0.mUTm223epek4-2yCeK2EF6k8r5FfJbRBMtAZVafT6gI";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private OrdersAdapter adapter;
    private List<Order> orders = new ArrayList<>();
    private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        Button btnBackToOrders = findViewById(R.id.btn_back_to_orders);
        btnBackToOrders.setOnClickListener(v -> {
            finish();
        });

        recyclerView = findViewById(R.id.orders_recycler_view);
        progressBar = findViewById(R.id.progress_bar);

        userId = getIntent().getStringExtra("USER_ID");
        if (userId == null || userId.isEmpty()) {
            showErrorAndFinish("Ошибка: пользователь не распознан");
            return;
        }

        Log.d(TAG, "Начало загрузки заказов для user: " + userId);

        setupRecyclerView();
        loadOrders();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrdersAdapter(orders);
        recyclerView.setAdapter(adapter);
    }

    private void loadOrders() {
        progressBar.setVisibility(View.VISIBLE);
        orders.clear();
        adapter.notifyDataSetChanged();


        String ordersUrl = SUPABASE_URL + "/rest/v1/orders?user_id=eq." + userId + "&select=id,user_id,total_price,status,created_at";

        OkHttpClient client = new OkHttpClient();
        Request ordersRequest = new Request.Builder()
                .url(ordersUrl)
                .get()
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(ordersRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Ошибка загрузки заказов", e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(OrdersActivity.this, "Ошибка загрузки заказов: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        throw new IOException("HTTP ошибка: " + response.code());
                    }

                    String responseData = response.body() != null ? response.body().string() : null;
                    Log.d(TAG, "Ответ orders: " + responseData);

                    if (responseData == null || responseData.isEmpty() || responseData.equals("[]")) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(OrdersActivity.this, "У вас пока нет заказов", Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }

                    JSONArray ordersArray = new JSONArray(responseData);
                    List<Order> loadedOrders = new ArrayList<>();


                    for (int i = 0; i < ordersArray.length(); i++) {
                        JSONObject orderJson = ordersArray.getJSONObject(i);
                        Order order = new Order(
                                orderJson.getString("id"),
                                orderJson.getString("user_id"),
                                orderJson.getInt("total_price"),
                                orderJson.getString("status"),
                                parseDateSafe(orderJson.getString("created_at"))
                        );
                        loadedOrders.add(order);
                    }


                    loadOrderItems(loadedOrders);

                } catch (Exception e) {
                    Log.e(TAG, "Ошибка обработки заказов", e);
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(OrdersActivity.this, "Ошибка обработки данных: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                } finally {
                    response.close();
                }
            }
        });
    }

    private void loadOrderItems(List<Order> ordersList) {
        if (ordersList.isEmpty()) {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                this.orders.addAll(ordersList);
                adapter.notifyDataSetChanged();
            });
            return;
        }


        StringBuilder orderIds = new StringBuilder();
        for (Order order : ordersList) {
            if (orderIds.length() > 0) orderIds.append(",");
            orderIds.append(order.getId());
        }

        String itemsUrl = SUPABASE_URL + "/rest/v1/order_items?order_id=in.(" + orderIds + ")";

        OkHttpClient client = new OkHttpClient();
        Request itemsRequest = new Request.Builder()
                .url(itemsUrl)
                .get()
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(itemsRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Ошибка загрузки items", e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);

                    OrdersActivity.this.orders.addAll(ordersList);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(OrdersActivity.this, "Загружены заказы, но не удалось загрузить детали", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        throw new IOException("HTTP ошибка: " + response.code());
                    }

                    String responseData = response.body() != null ? response.body().string() : null;
                    Log.d(TAG, "Ответ order_items: " + responseData);

                    if (responseData != null && !responseData.isEmpty() && !responseData.equals("[]")) {
                        JSONArray itemsArray = new JSONArray(responseData);


                        for (int i = 0; i < itemsArray.length(); i++) {
                            JSONObject itemJson = itemsArray.getJSONObject(i);
                            String orderId = itemJson.getString("order_id");

                            for (Order order : ordersList) {
                                if (order.getId().equals(orderId)) {
                                    OrderItem item = new OrderItem(
                                            itemJson.getString("pizza_id"),
                                            itemJson.getInt("quantity"),
                                            itemJson.getInt("price")
                                    );
                                    if (order.getItems() == null) {
                                        order.setItems(new ArrayList<>());
                                    }
                                    order.getItems().add(item);
                                    break;
                                }
                            }
                        }
                    }

                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        OrdersActivity.this.orders.addAll(ordersList);
                        adapter.notifyDataSetChanged();
                    });

                } catch (Exception e) {
                    Log.e(TAG, "Ошибка обработки items", e);
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);

                        OrdersActivity.this.orders.addAll(ordersList);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(OrdersActivity.this, "Загружены заказы, но с ошибкой деталей", Toast.LENGTH_SHORT).show();
                    });
                } finally {
                    response.close();
                }
            }
        });
    }



    private List<Order> parseOrders(JSONArray ordersArray) throws JSONException {
        List<Order> result = new ArrayList<>();

        for (int i = 0; i < ordersArray.length(); i++) {
            try {
                JSONObject orderJson = ordersArray.getJSONObject(i);


                String id = orderJson.optString("id", "unknown");
                String userId = orderJson.optString("user_id", "unknown");
                int totalPrice = orderJson.optInt("total_price", 0);
                String status = orderJson.optString("status", "unknown");

                Date createdAt;
                try {
                    createdAt = dateFormat.parse(orderJson.getString("created_at"));
                } catch (ParseException e) {
                    createdAt = new Date();
                }

                Order order = new Order(id, userId, totalPrice, status, createdAt);
                result.add(order);
            } catch (JSONException e) {
                Log.e(TAG, "Ошибка парсинга заказа #" + i, e);
            }
        }
        return result;
    }
    private Date parseDateSafe(String dateStr) {
        try {
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            Log.w(TAG, "Не удалось распарсить дату: " + dateStr);
            return new Date();
        }
    }

    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }
}
