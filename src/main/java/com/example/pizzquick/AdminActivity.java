package com.example.pizzquick;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AdminActivity extends AppCompatActivity implements AdminOrdersAdapter.OnStatusChangeListener {

    private static final String TAG = "AdminActivity";
    private static final String SUPABASE_URL = "https://rrmyhvqaluvfommjwmrk.supabase.co";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJybXlodnFhbHV2Zm9tbWp3bXJrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTA3OTg2OTgsImV4cCI6MjA2NjM3NDY5OH0.mUTm223epek4-2yCeK2EF6k8r5FfJbRBMtAZVafT6gI";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private Spinner statusFilterSpinner;
    private AdminOrdersAdapter adapter;
    private List<Order> allOrders = new ArrayList<>();
    private List<Order> filteredOrders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        findViewById(R.id.btn_back1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        recyclerView = findViewById(R.id.admin_orders_recycler_view);
        progressBar = findViewById(R.id.admin_progress_bar);
        statusFilterSpinner = findViewById(R.id.status_filter_spinner);

        setupRecyclerView();
        setupStatusFilter();
        loadAllOrders();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminOrdersAdapter(filteredOrders, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupStatusFilter() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.order_statuses,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusFilterSpinner.setAdapter(adapter);

        statusFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedStatus = parent.getItemAtPosition(position).toString();
                filterOrders(selectedStatus);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                filterOrders("Все");
            }
        });
    }

    private void filterOrders(String selectedStatus) {
        filteredOrders.clear();

        if ("Все".equals(selectedStatus)) {
            filteredOrders.addAll(allOrders);
        } else {
            String englishStatus = convertStatusToEnglish(selectedStatus);
            for (Order order : allOrders) {
                if (englishStatus.equals(order.getStatus())) {
                    filteredOrders.add(order);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void loadAllOrders() {
        progressBar.setVisibility(View.VISIBLE);
        allOrders.clear();
        filteredOrders.clear();
        adapter.notifyDataSetChanged();

        String ordersUrl = SUPABASE_URL + "/rest/v1/orders?select=id,user_id,total_price,status,created_at";

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
                    Toast.makeText(AdminActivity.this, "Ошибка загрузки заказов: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        throw new IOException("HTTP ошибка: " + response.code());
                    }

                    String responseData = response.body() != null ? response.body().string() : null;
                    Log.d(TAG, "Ответ заказов: " + responseData);

                    if (responseData == null || responseData.isEmpty() || responseData.equals("[]")) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(AdminActivity.this, "Заказы не найдены", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(AdminActivity.this, "Ошибка обработки данных: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
                allOrders.addAll(ordersList);
                filteredOrders.addAll(ordersList);
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
                Log.e(TAG, "Ошибка загрузки товаров", e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    allOrders.addAll(ordersList);
                    filteredOrders.addAll(ordersList);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(AdminActivity.this, "Загружены заказы без деталей", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        throw new IOException("HTTP ошибка: " + response.code());
                    }

                    String responseData = response.body() != null ? response.body().string() : null;
                    Log.d(TAG, "Ответ товаров: " + responseData);

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
                        allOrders.addAll(ordersList);
                        filteredOrders.addAll(ordersList);
                        adapter.notifyDataSetChanged();
                    });

                } catch (Exception e) {
                    Log.e(TAG, "Ошибка обработки товаров", e);
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        allOrders.addAll(ordersList);
                        filteredOrders.addAll(ordersList);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(AdminActivity.this, "Загружены заказы с частичными деталями", Toast.LENGTH_SHORT).show();
                    });
                } finally {
                    response.close();
                }
            }
        });
    }

    @Override
    public void onStatusChanged(String orderId, String newStatus) {
        updateOrderStatus(orderId, newStatus);
    }

    private void updateOrderStatus(String orderId, String newStatus) {
        progressBar.setVisibility(View.VISIBLE);

        String englishStatus = convertStatusToEnglish(newStatus);

        JSONObject statusJson = new JSONObject();
        try {
            statusJson.put("status", englishStatus);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = SUPABASE_URL + "/rest/v1/orders?id=eq." + orderId;

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(
                statusJson.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .patch(body)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AdminActivity.this, "Ошибка обновления статуса", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        Toast.makeText(AdminActivity.this, "Статус обновлен", Toast.LENGTH_SHORT).show();

                        for (Order order : allOrders) {
                            if (order.getId().equals(orderId)) {
                                order.setStatus(englishStatus);
                                break;
                            }
                        }

                        String selectedStatus = statusFilterSpinner.getSelectedItem().toString();
                        filterOrders(selectedStatus);
                    } else {
                        Toast.makeText(AdminActivity.this, "Ошибка: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private String convertStatusToEnglish(String russianStatus) {
        if (russianStatus == null) return "pending";

        switch (russianStatus) {
            case "Ожидает": return "pending";
            case "В обработке": return "processing";
            case "Завершен": return "completed";
            case "Отменен": return "cancelled";
            default: return "pending";
        }
    }

    private Date parseDateSafe(String dateStr) {
        try {
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            Log.w(TAG, "Не удалось распарсить дату: " + dateStr);
            return new Date();
        }
    }
}