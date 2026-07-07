package com.mobile.uph24si3;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * ProfilActivity — Halaman profil pengguna.
 * Menampilkan informasi lengkap user: Avatar, Nama Lengkap, Username,
 * NIM, Tempat Lahir, Tanggal Lahir, Hobi, dan Bio.
 * Data diambil dari SharedPreferences yang diisi saat login.
 */
public class ProfilActivity extends AppCompatActivity {

    // ---- Views: Header ----
    private ImageButton btnBackProfil;
    private TextView tvAvatarInitialProfil;
    private ImageView ivAvatarProfil;
    private FloatingActionButton btnChangePhoto;
    private TextView tvProfilNama, tvProfilUsername, tvProfilNim;

    // ---- Views: Info Detail ----
    private TextView tvInfoNama, tvInfoUsername;
    private TextView tvInfoTempatLahir, tvInfoTanggalLahir;
    private TextView tvInfoHobi, tvInfoBio;

    // ---- Views: Tombol ----
    private Button btnEditProfil;

    // ---- Data ----
    private SharedPreferences prefs;

    // ---- Image Picker ----
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    ivAvatarProfil.setImageURI(uri);
                    // Ambil hak akses persisten jika memungkinkan
                    try {
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    prefs.edit().putString("user_avatar_uri", uri.toString()).apply();
                    Toast.makeText(this, "✅ Foto profil diperbarui!", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        prefs = getSharedPreferences("BudgetKuPrefs", MODE_PRIVATE);

        initViews();
        tampilkanDataProfil();
        setupClickListeners();
    }

    // ---------------------------------------------------------------
    // Inisialisasi semua view dari layout
    // ---------------------------------------------------------------
    private void initViews() {
        btnBackProfil           = findViewById(R.id.btnBackProfil);
        tvAvatarInitialProfil   = findViewById(R.id.tvAvatarInitialProfil);
        ivAvatarProfil          = findViewById(R.id.ivAvatarProfil);
        btnChangePhoto          = findViewById(R.id.btnChangePhoto);
        tvProfilNama            = findViewById(R.id.tvProfilNama);
        tvProfilUsername        = findViewById(R.id.tvProfilUsername);
        tvProfilNim             = findViewById(R.id.tvProfilNim);

        tvInfoNama              = findViewById(R.id.tvInfoNama);
        tvInfoUsername          = findViewById(R.id.tvInfoUsername);
        tvInfoTempatLahir       = findViewById(R.id.tvInfoTempatLahir);
        tvInfoTanggalLahir      = findViewById(R.id.tvInfoTanggalLahir);
        tvInfoHobi              = findViewById(R.id.tvInfoHobi);
        tvInfoBio               = findViewById(R.id.tvInfoBio);

        btnEditProfil           = findViewById(R.id.btnEditProfil);
    }

    // ---------------------------------------------------------------
    // Ambil dan tampilkan data profil dari SharedPreferences
    // ---------------------------------------------------------------
    private void tampilkanDataProfil() {
        // Ambil data dari SharedPreferences (diisi saat login & edit)
        String nama         = prefs.getString("user_name", "Pengguna BudgetKu");
        String nim          = prefs.getString("user_nim", "00000000000");
        String username     = prefs.getString("user_username", "@pengguna");
        String tempatLahir  = prefs.getString("user_tempat_lahir", "Belum diisi");
        String tanggalLahir = prefs.getString("user_tanggal_lahir", "Belum diisi");
        String hobi         = prefs.getString("user_hobi", "Belum diisi");
        String bio          = prefs.getString("user_bio", "Senang mengelola keuangan dengan bijak 💪");
        String avatarUriStr = prefs.getString("user_avatar_uri", null);

        // --- Atur header avatar ---
        if (avatarUriStr != null) {
            ivAvatarProfil.setImageURI(Uri.parse(avatarUriStr));
            tvAvatarInitialProfil.setVisibility(View.GONE);
            ivAvatarProfil.setVisibility(View.VISIBLE);
        } else if (!nama.isEmpty()) {
            tvAvatarInitialProfil.setText(String.valueOf(nama.charAt(0)).toUpperCase());
            tvAvatarInitialProfil.setVisibility(View.VISIBLE);
            ivAvatarProfil.setVisibility(View.GONE);
        } else {
            tvAvatarInitialProfil.setText("👤");
            tvAvatarInitialProfil.setVisibility(View.VISIBLE);
            ivAvatarProfil.setVisibility(View.GONE);
        }

        // --- Atur teks header ---
        tvProfilNama.setText(nama);
        tvProfilUsername.setText(username);
        tvProfilNim.setText("NIM: " + nim);

        // --- Atur info detail card ---
        tvInfoNama.setText(nama);
        tvInfoUsername.setText(username);
        tvInfoTempatLahir.setText(tempatLahir);
        tvInfoTanggalLahir.setText(tanggalLahir);
        tvInfoHobi.setText(hobi);
        tvInfoBio.setText(bio);
    }

    // ---------------------------------------------------------------
    // Setup listener untuk tombol-tombol
    // ---------------------------------------------------------------
    private void setupClickListeners() {
        // Tombol back: kembali ke halaman sebelumnya (Dashboard)
        btnBackProfil.setOnClickListener(v -> finish());

        // Tombol Edit Profil: tampilkan dialog edit data
        btnEditProfil.setOnClickListener(v -> tampilkanDialogEditProfil());

        // Tombol Ubah Foto
        btnChangePhoto.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        ivAvatarProfil.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
    }

    // ---------------------------------------------------------------
    // Dialog Edit Profil: user bisa mengubah data profil tambahan
    // ---------------------------------------------------------------
    private void tampilkanDialogEditProfil() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Widget_BudgetKu_Dialog);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profil, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_card);
        }

        // Ambil field dalam dialog
        EditText etTempatLahir  = dialogView.findViewById(R.id.etEditTempatLahir);
        EditText etTanggalLahir = dialogView.findViewById(R.id.etEditTanggalLahir);
        EditText etHobi         = dialogView.findViewById(R.id.etEditHobi);
        EditText etBio          = dialogView.findViewById(R.id.etEditBio);
        Button btnSimpan        = dialogView.findViewById(R.id.btnSimpanEditProfil);
        Button btnBatal         = dialogView.findViewById(R.id.btnBatalEditProfil);

        // Isi field dengan data yang sudah tersimpan
        etTempatLahir.setText(prefs.getString("user_tempat_lahir", ""));
        etTanggalLahir.setText(prefs.getString("user_tanggal_lahir", ""));
        etHobi.setText(prefs.getString("user_hobi", ""));
        etBio.setText(prefs.getString("user_bio", ""));

        // Simpan perubahan
        btnSimpan.setOnClickListener(v -> {
            String tempatLahir  = etTempatLahir.getText().toString().trim();
            String tanggalLahir = etTanggalLahir.getText().toString().trim();
            String hobi         = etHobi.getText().toString().trim();
            String bio          = etBio.getText().toString().trim();

            // Validasi: minimal tempat & tanggal lahir harus diisi
            if (TextUtils.isEmpty(tempatLahir)) {
                etTempatLahir.setError("Wajib diisi");
                return;
            }
            if (TextUtils.isEmpty(tanggalLahir)) {
                etTanggalLahir.setError("Wajib diisi");
                return;
            }

            // Simpan ke SharedPreferences
            prefs.edit()
                    .putString("user_tempat_lahir", tempatLahir)
                    .putString("user_tanggal_lahir", tanggalLahir)
                    .putString("user_hobi", TextUtils.isEmpty(hobi) ? "Belum diisi" : hobi)
                    .putString("user_bio", TextUtils.isEmpty(bio) ? "Belum diisi" : bio)
                    .apply();

            Toast.makeText(this, "✅ Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();

            // Refresh tampilan data
            tampilkanDataProfil();
        });

        btnBatal.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // ---------------------------------------------------------------
    // Refresh data saat kembali ke activity ini
    // ---------------------------------------------------------------
    @Override
    protected void onResume() {
        super.onResume();
        tampilkanDataProfil();
    }
}
