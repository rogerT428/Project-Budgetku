package com.mobile.uph24si3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_simple);

        SharedPreferences prefs = getSharedPreferences("BudgetKuPrefs", MODE_PRIVATE);
        String username = prefs.getString("user_name", "User");

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        tvWelcome.setText("Selamat Datang, " + username + "!");

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            prefs.edit().putBoolean("is_logged_in", false).apply();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        });
    }
}
