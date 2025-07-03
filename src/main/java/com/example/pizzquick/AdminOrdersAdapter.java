package com.example.pizzquick;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AdminOrdersAdapter extends RecyclerView.Adapter<AdminOrdersAdapter.AdminOrderViewHolder> {

    public interface OnStatusChangeListener {
        void onStatusChanged(String orderId, String newStatus);
    }

    private List<Order> orders;
    private OnStatusChangeListener listener;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public AdminOrdersAdapter(List<Order> orders, OnStatusChangeListener listener) {
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_order, parent, false);
        return new AdminOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminOrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order, listener);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class AdminOrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderId, orderDate, orderTotal, orderUser, orderItems;
        Spinner statusSpinner;

        public AdminOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.admin_order_id);
            orderDate = itemView.findViewById(R.id.admin_order_date);
            orderTotal = itemView.findViewById(R.id.admin_order_total);
            orderUser = itemView.findViewById(R.id.admin_order_user);
            orderItems = itemView.findViewById(R.id.admin_order_items);
            statusSpinner = itemView.findViewById(R.id.admin_order_status_spinner);
        }

        public void bind(Order order, OnStatusChangeListener listener) {
            String displayId = order.getId().length() > 8
                    ? "Заказ #" + order.getId().substring(0, 8)
                    : "Заказ #" + order.getId();

            orderId.setText(displayId);
            orderDate.setText(dateFormat.format(order.getCreatedAt()));
            orderTotal.setText("Итого: " + order.getTotalPrice() + " ₽");
            orderUser.setText("ID пользователя: " + order.getUserId());

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    itemView.getContext(),
                    R.array.order_statuses,
                    android.R.layout.simple_spinner_item
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            statusSpinner.setAdapter(adapter);

            int position = getStatusPosition(convertStatusToRussian(order.getStatus()));
            statusSpinner.setSelection(position);

            statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String newStatus = parent.getItemAtPosition(position).toString();
                    String currentStatus = convertStatusToRussian(order.getStatus());
                    if (!newStatus.equals(currentStatus)) {
                        listener.onStatusChanged(order.getId(), newStatus);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            if (order.getItems() != null && !order.getItems().isEmpty()) {
                StringBuilder itemsText = new StringBuilder("Товары:\n");
                for (OrderItem item : order.getItems()) {
                    itemsText.append("- ")
                            .append(item.getPizzaId())
                            .append(" (Кол-во: ")
                            .append(item.getQuantity())
                            .append(", Цена: ")
                            .append(item.getPrice())
                            .append(" ₽)\n");
                }
                orderItems.setText(itemsText.toString());
            } else {
                orderItems.setText("Нет информации о товарах");
            }
        }

        private int getStatusPosition(String status) {
            switch (status) {
                case "Ожидает": return 1;
                case "В обработке": return 2;
                case "Завершен": return 3;
                case "Отменен": return 4;
                default: return 0;
            }
        }
        private String convertStatusToRussian(String englishStatus) {
            if (englishStatus == null) return "Ожидает";

            switch (englishStatus.toLowerCase()) {
                case "pending": return "Ожидает";
                case "processing": return "В обработке";
                case "completed": return "Завершен";
                case "cancelled": return "Отменен";
                default: return "Ожидает";
            }
        }
    }
}