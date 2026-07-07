package com.mobile.uph24si3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.Executor;

/**
 * LoginActivity — Halaman Login Aplikasi BudgetKu.
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilNama, tilPassword;
    private TextInputEditText etNama, etPassword;
    private Button btnLogin, btnBiometricLogin;
    private TextView tvToRegister;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = getSharedPreferences("BudgetKuPrefs", MODE_PRIVATE);

        initViews();
        setupClickListeners();
        checkPremiumBiometric();
    }

    private void initViews() {
        tilNama     = findViewById(R.id.tilNama);
        tilPassword = findViewById(R.id.tilPassword);
        etNama      = findViewById(R.id.etNama);
        etPassword  = findViewById(R.id.etPassword);
        btnLogin    = findViewById(R.id.btnLogin);
        btnBiometricLogin = findViewById(R.id.btnBiometricLogin);
        tvToRegister = findViewById(R.id.tvToRegister);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> prosesLogin());
        btnBiometricLogin.setOnClickListener(v -> showBiometricPrompt());
        tvToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void checkPremiumBiometric() {
        boolean isPremium = prefs.getBoolean("is_premium", false);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);

        if (isPremium && isLoggedIn) {
            btnBiometricLogin.setVisibility(View.VISIBLE);
            showBiometricPrompt();
        } else {
            btnBiometricLogin.setVisibility(View.GONE);
        }
    }

    private void showBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(LoginActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(), "Login Berhasil!", Toast.LENGTH_SHORT).show();
                navigateToDashboard();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Login Premium BudgetKu")
                .setSubtitle("Gunakan sidik jari Anda untuk masuk")
                .setNegativeButtonText("Gunakan Password")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void prosesLogin() {
        String nama = etNama.getText() != null ? etNama.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        tilNama.setError(null);
        tilPassword.setError(null);

        if (TextUtils.isEmpty(nama)) {
            tilNama.setError("Nama tidak boleh kosong");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password tidak boleh kosong");
            return;
        }

        // Cek data di SharedPreferences (data pendaftaran)
        String savedName = prefs.getString("reg_name", "");
        String savedPass = prefs.getString("reg_password", "");

        if (nama.equalsIgnoreCase(savedName) && password.equals(savedPass)) {
            // Berhasil
            prefs.edit()
                    .putString("user_name", savedName)
                    .putBoolean("is_logged_in", true)
                    .apply();
            
            Toast.makeText(this, "Selamat Datang, " + savedName, Toast.LENGTH_SHORT).show();
            navigateToDashboard();
        } else {
            Toast.makeText(this, "Nama atau Password salah!", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
