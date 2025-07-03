package com.example.pizzquick;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class PizzaDialog {
    public static void showPizzaDetails(Context context, Pizza pizza, Runnable onAddToCart) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_pizza_details);
        dialog.setCancelable(true);

        ImageView image = dialog.findViewById(R.id.dialog_pizza_image);
        TextView name = dialog.findViewById(R.id.dialog_pizza_name);
        TextView price = dialog.findViewById(R.id.dialog_pizza_price);
        TextView description = dialog.findViewById(R.id.dialog_pizza_desc);
        Button closeButton = dialog.findViewById(R.id.dialog_close_btn);
        Button addToCartButton = dialog.findViewById(R.id.dialog_add_to_cart_btn);

        image.setImageResource(pizza.getImageRes());
        name.setText(pizza.getName());
        price.setText(pizza.getPrice() + " ₽");
        description.setText(pizza.getDescription());

        closeButton.setOnClickListener(v -> dialog.dismiss());
        addToCartButton.setOnClickListener(v -> {
            onAddToCart.run();
            dialog.dismiss();
        });

        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}
