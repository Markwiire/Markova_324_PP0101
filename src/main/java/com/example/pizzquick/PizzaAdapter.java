package com.example.pizzquick;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PizzaAdapter extends RecyclerView.Adapter<PizzaAdapter.PizzaViewHolder> {
    private List<Pizza> pizzaList;
    private OnPizzaClickListener listener;

    public interface OnPizzaClickListener {
        void onPizzaClick(Pizza pizza);
    }

    public PizzaAdapter(List<Pizza> pizzaList, OnPizzaClickListener listener) {
        this.pizzaList = pizzaList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PizzaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pizza, parent, false);
        return new PizzaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PizzaViewHolder holder, int position) {
        Pizza pizza = pizzaList.get(position);
        holder.bind(pizza, listener);
    }

    @Override
    public int getItemCount() {
        return pizzaList.size();
    }

    static class PizzaViewHolder extends RecyclerView.ViewHolder {
        ImageView pizzaImage;
        TextView pizzaName, pizzaPrice, pizzaDescription;

        public PizzaViewHolder(@NonNull View itemView) {
            super(itemView);
            pizzaImage = itemView.findViewById(R.id.pizzaImage);
            pizzaName = itemView.findViewById(R.id.pizzaName);
            pizzaPrice = itemView.findViewById(R.id.pizzaPrice);
            pizzaDescription = itemView.findViewById(R.id.pizzaDescription);
        }

        public void bind(Pizza pizza, OnPizzaClickListener listener) {
            pizzaImage.setImageResource(pizza.getImageRes());
            pizzaName.setText(pizza.getName());
            pizzaDescription.setText(pizza.getDescription());
            pizzaPrice.setText(pizza.getPrice() + " ₽");

            itemView.setOnClickListener(v -> listener.onPizzaClick(pizza));
        }
    }
}