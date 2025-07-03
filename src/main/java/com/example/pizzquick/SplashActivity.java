package com.example.pizzquick;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private volatile boolean isActive = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        progressBar = findViewById(R.id.progressBar);

        new Thread(() -> {
            for (int progress = 0; progress <= 100 && isActive; progress++) {
                int finalProgress = progress;
                runOnUiThread(() -> progressBar.setProgress(finalProgress));
                SystemClock.sleep(45);
            }
            if (isActive) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        isActive = false;
        super.onDestroy();
    }
}
