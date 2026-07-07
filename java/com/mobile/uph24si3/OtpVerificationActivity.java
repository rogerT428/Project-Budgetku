package com.mobile.uph24si3;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class OtpVerificationActivity extends AppCompatActivity {

    private TextView tvOtpMessage, tvResend;
    private EditText etOtpCode;
    private Button btnVerify;
    private SharedPreferences prefs;

    private String correctOtp;
    private String name, contact, password;

    // Izin Notifikasi untuk Android 13+
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                showOtpNotification();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        prefs = getSharedPreferences("BudgetKuPrefs", MODE_PRIVATE);

        // Get data from intent
        Intent intent = getIntent();
        name = intent.getStringExtra("reg_name");
        contact = intent.getStringExtra("reg_contact");
        password = intent.getStringExtra("reg_password");
        correctOtp = intent.getStringExtra("otp_code");

        initViews();
        setupClickListeners();
        
        // Cek Izin Notifikasi sebelum mengirim
        checkNotificationPermission();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                showOtpNotification();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            showOtpNotification();
        }
    }

    private void initViews() {
        tvOtpMessage = findViewById(R.id.tvOtpMessage);
        tvResend = findViewById(R.id.tvResendOtp);
        etOtpCode = findViewById(R.id.etOtpCode);
        btnVerify = findViewById(R.id.btnVerifyOtp);

        if (contact != null) {
            tvOtpMessage.setText("Kami telah mengirimkan kode ke\n" + contact);
        }
    }

    private void setupClickListeners() {
        btnVerify.setOnClickListener(v -> {
            String inputCode = etOtpCode.getText().toString().trim();
            if (inputCode.equals(correctOtp)) {
                completeRegistration();
            } else {
                Toast.makeText(this, "Kode OTP salah!", Toast.LENGTH_SHORT).show();
            }
        });

        tvResend.setOnClickListener(v -> {
            Toast.makeText(this, "Mengirim ulang kode...", Toast.LENGTH_SHORT).show();
            showOtpNotification();
        });
    }

    private void completeRegistration() {
        // Simpan data pendaftaran ke SharedPreferences
        prefs.edit()
                .putString("reg_name", name)
                .putString("reg_password", password)
                .putBoolean("has_account", true)
                .apply();

        Toast.makeText(this, "Verifikasi Berhasil! Silakan Login.", Toast.LENGTH_LONG).show();

        // Ke Login dan bersihkan stack
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showOtpNotification() {
        // Tampilkan juga di Toast sebagai cadangan jika notifikasi mati
        Toast.makeText(this, "SIMULASI OTP: " + correctOtp, Toast.LENGTH_LONG).show();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "otp_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "OTP Notification", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel untuk pengiriman simulasi kode OTP");
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentTitle("BudgetKu OTP Code")
                .setContentText("Kode verifikasi Anda adalah: " + correctOtp)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[]{0, 500, 100, 500})
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }
}
