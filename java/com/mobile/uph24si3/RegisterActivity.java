package com.mobile.uph24si3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Random;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilNama, tilContact, tilPassword;
    private TextInputEditText etNama, etContact, etPassword;
    private Button btnRegister;
    private TextView tvToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        tilNama = findViewById(R.id.tilRegNama);
        tilContact = findViewById(R.id.tilRegContact);
        tilPassword = findViewById(R.id.tilRegPassword);
        etNama = findViewById(R.id.etRegNama);
        etContact = findViewById(R.id.etRegContact);
        etPassword = findViewById(R.id.etRegPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvToLogin = findViewById(R.id.tvToLogin);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> prosesRegister());
        tvToLogin.setOnClickListener(v -> {
            finish(); // Kembali ke LoginActivity
        });
    }

    private void prosesRegister() {
        String nama = etNama.getText() != null ? etNama.getText().toString().trim() : "";
        String contact = etContact.getText() != null ? etContact.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        tilNama.setError(null);
        tilContact.setError(null);
        tilPassword.setError(null);

        boolean isValid = true;

        if (TextUtils.isEmpty(nama)) {
            tilNama.setError("Nama tidak boleh kosong");
            isValid = false;
        }

        if (TextUtils.isEmpty(contact)) {
            tilContact.setError("Kontak tidak boleh kosong");
            isValid = false;
        }

        if (password.length() < 6) {
            tilPassword.setError("Password minimal 6 karakter");
            isValid = false;
        }

        if (isValid) {
            // Generate simulated OTP code
            String generatedOtp = String.format("%04d", new Random().nextInt(10000));
            
            // Go to OTP Verification screen
            Intent intent = new Intent(this, OtpVerificationActivity.class);
            intent.putExtra("reg_name", nama);
            intent.putExtra("reg_contact", contact);
            intent.putExtra("reg_password", password);
            intent.putExtra("otp_code", generatedOtp);
            startActivity(intent);
        }
    }
}
