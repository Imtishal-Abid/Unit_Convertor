package com.example;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme settings before creating view
        applySavedTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Bind Views
        ImageView imgLogo = findViewById(R.id.img_splash_logo);
        TextView txtTitle = findViewById(R.id.txt_splash_title);
        TextView txtTagline = findViewById(R.id.txt_splash_tagline);
        ProgressBar progressBar = findViewById(R.id.splash_progress);

        // Initialize state for animations (Alpha = 0, scale = 0.8)
        imgLogo.setAlpha(0f);
        imgLogo.setScaleX(0.8f);
        imgLogo.setScaleY(0.8f);

        txtTitle.setAlpha(0f);
        txtTitle.setTranslationY(20f);

        txtTagline.setAlpha(0f);
        txtTagline.setTranslationY(20f);

        progressBar.setAlpha(0f);

        // Execute beautiful staggered spring-like animations
        imgLogo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .setInterpolator(new android.view.animation.OvershootInterpolator(1.2f))
                .start();

        txtTitle.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(700)
                .setStartDelay(200)
                .start();

        txtTagline.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(700)
                .setStartDelay(350)
                .start();

        progressBar.animate()
                .alpha(1f)
                .setDuration(500)
                .setStartDelay(500)
                .start();

        // Navigate to MainActivity after splash animation finishes
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            // Apply a nice cross-fade transition between activities
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 2500);
    }

    private void applySavedTheme() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        // Default to MODE_NIGHT_FOLLOW_SYSTEM (which is value -1)
        int savedTheme = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedTheme);
    }
}
