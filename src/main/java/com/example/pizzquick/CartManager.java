package com.example.pizzquick;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CartManager {
    private static CartManager instance;
    private List<CartItem> cartItems = new ArrayList<>();
    private String userId;
    private OkHttpClient client = new OkHttpClient();


    private static final String ORDERS_URL = "https://rrmyhvqaluvfommjwmrk.supabase.co/rest/v1/orders";
    private static final String ORDER_ITEMS_URL = "https://rrmyhvqaluvfommjwmrk.supabase.co/rest/v1/order_items";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJybXlodnFhbHV2Zm9tbWp3bXJrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTA3OTg2OTgsImV4cCI6MjA2NjM3NDY5OH0.mUTm223epek4-2yCeK2EF6k8r5FfJbRBMtAZVafT6gI";

    private CartManager(Context context, String userId) {
        this.userId = userId;
    }

    public static synchronized CartManager getInstance(Context context, String userId) {
        if (instance == null || !instance.userId.equals(userId)) {
            instance = new CartManager(context, userId);
        }
        return instance;
    }


    public void addToCart(Pizza pizza, int i) {
        for (CartItem item : cartItems) {
            if (item.getPizzaId().equals(pizza.getId())) {
                item.setQuantity(item.getQuantity() + 1);
                return;
            }
        }
        cartItems.add(new CartItem(pizza.getId(), pizza.getName(), 1, pizza.getPrice()));
    }


    public void removeFromCart(String pizzaId) {
        cartItems.removeIf(item -> item.getPizzaId().equals(pizzaId));
    }


    public void updateQuantity(String pizzaId, int quantity) {
        for (CartItem item : cartItems) {
            if (item.getPizzaId().equals(pizzaId)) {
                item.setQuantity(quantity);
                return;
            }
        }
    }


    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }


    public int getTotalPrice() {
        int total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }


    public void clearCart() {
        cartItems.clear();
    }


    public void checkoutOrder(CheckoutCallback callback) {
        if (cartItems.isEmpty()) {
            callback.onComplete(false, "Корзина пуста");
            return;
        }

        try {

            JSONObject orderJson = new JSONObject();
            orderJson.put("user_id", userId);
            orderJson.put("total_price", getTotalPrice());
            orderJson.put("status", "pending");


            JSONArray itemsArray = new JSONArray();
            for (CartItem item : cartItems) {
                JSONObject itemJson = new JSONObject();
                itemJson.put("pizza_id", item.getPizzaId());
                itemJson.put("quantity", item.getQuantity());
                itemJson.put("price", item.getPrice());
                itemsArray.put(itemJson);
            }


            String url = "https://rrmyhvqaluvfommjwmrk.supabase.co/rest/v1/orders";

            Log.d("API_REQUEST", "URL: " + url);

            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(orderJson.toString(), MediaType.get("application/json")))
                    .addHeader("apikey", SUPABASE_API_KEY)
                    .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onComplete(false, "Ошибка сети: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    Log.d("API_RESPONSE", "Code: " + response.code() + ", Body: " + responseBody);

                    if (!response.isSuccessful()) {
                        callback.onComplete(false, "Ошибка сервера: " + response.code());
                        return;
                    }

                    try {
                        JSONArray result = new JSONArray(responseBody);
                        if (result.length() > 0) {
                            long orderId = result.getJSONObject(0).getLong("id");
                            saveOrderItems(orderId, callback);
                        } else {
                            callback.onComplete(false, "Не удалось создать заказ");
                        }
                    } catch (Exception e) {
                        callback.onComplete(false, "Ошибка обработки данных");
                    }
                }
            });
        } catch (JSONException e) {
            callback.onComplete(false, "Ошибка формирования заказа");
        }
    }

    private void saveOrderItems(long orderId, CheckoutCallback callback) {
        try {
            JSONArray itemsArray = new JSONArray();
            for (CartItem item : cartItems) {
                JSONObject itemJson = new JSONObject();
                itemJson.put("order_id", orderId);
                itemJson.put("pizza_id", item.getPizzaId());
                itemJson.put("quantity", item.getQuantity());
                itemJson.put("price", item.getPrice());
                itemsArray.put(itemJson);
            }

            Request itemsRequest = new Request.Builder()
                    .url(ORDER_ITEMS_URL)
                    .post(RequestBody.create(itemsArray.toString(), MediaType.get("application/json")))
                    .addHeader("apikey", SUPABASE_API_KEY)
                    .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(itemsRequest).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onComplete(false, "Ошибка добавления товаров");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        clearCart();
                        callback.onComplete(true, "Заказ успешно оформлен!");
                    } else {
                        callback.onComplete(false, "Ошибка сохранения товаров");
                    }
                }
            });
        } catch (Exception e) {
            callback.onComplete(false, "Ошибка оформления заказа");
        }
    }

    public interface CheckoutCallback {
        void onComplete(boolean success, String message);
    }
}