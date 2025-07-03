package com.example.pizzquick;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PaymentActivity extends AppCompatActivity {
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);


        userId = getIntent().getStringExtra("USER_ID");
        if (userId == null) {
            Toast.makeText(this, "Ошибка: пользователь не распознан", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        EditText cardNumberInput = findViewById(R.id.card_number_input);
        EditText expiryInput = findViewById(R.id.expiry_input);
        EditText cvvInput = findViewById(R.id.cvv_input);
        Button payButton = findViewById(R.id.pay_button);

            expiryInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 2 && before == 0) {
                    expiryInput.setText(s + "/");
                    expiryInput.setSelection(3);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        payButton.setOnClickListener(v -> {
            String cardNumber = cardNumberInput.getText().toString().trim();
            String expiry = expiryInput.getText().toString().trim();
            String cvv = cvvInput.getText().toString().trim();

            if (cardNumber.isEmpty() || expiry.isEmpty() || cvv.isEmpty()) {
                Toast.makeText(this, "Заполните все поля!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (cardNumber.length() != 16) {
                Toast.makeText(this, "Номер карты должен содержать 16 цифр", Toast.LENGTH_SHORT).show();
                return;
            }

              if (!expiry.matches("(0[1-9]|1[0-2])/[0-9]{2}")) {
                Toast.makeText(this, "Введите срок в формате MM/YY", Toast.LENGTH_SHORT).show();
                return;
            }

            if (cvv.length() != 3) {
                Toast.makeText(this, "CVV должен содержать 3 цифры", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Оплата прошла успешно!")
                    .setMessage("Спасибо за покупку!")
                    .setPositiveButton("OK", (dialog, which) -> {

                        setResult(RESULT_OK);
                        finish();
                    })
                    .setCancelable(false)
                    .show();
        });
    }
}
