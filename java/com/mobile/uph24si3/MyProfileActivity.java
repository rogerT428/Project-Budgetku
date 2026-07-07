package com.mobile.uph24si3;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.mobile.uph24si3.database.DatabaseHelper;
import com.mobile.uph24si3.model.Budget;
import com.mobile.uph24si3.model.Category;
import com.mobile.uph24si3.model.SavingsGoal;
import com.mobile.uph24si3.model.Transaction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * MyProfileActivity — Halaman profil lengkap pengguna.
 * Fitur: avatar, info pribadi (nama, email, no HP), statistik,
 *        toggle notifikasi & dark mode, dan tombol logout.
 */
public class MyProfileActivity extends AppCompatActivity {

    // ---- Views ----
    private TextView tvProfileName, tvProfileBio, tvAvatarInitial;
    private TextView tvInfoName, tvInfoEmail, tvInfoPhone, tvInfoJoinDate;
    private TextView tvStatTransactions, tvStatSavings, tvStatBudgets;
    private Switch switchNotification, switchDarkMode;
    private Button btnLogout;

    // ---- Clickable rows ----
    private View rowEditName, rowEditEmail, rowEditPhone;
    private View rowChangePIN, rowManageCategories, rowExportReport, rowAbout;
    private View btnBack, flAvatar;

    // ---- Data ----
    private DatabaseHelper db;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        db = DatabaseHelper.getInstance(this);
        prefs = getSharedPreferences("BudgetKuPrefs", MODE_PRIVATE);

        initViews();
        loadProfileData();
        loadStats();
        setupClickListeners();
        applyPreferences();
    }

    // ---------------------------------------------------------------
    // Init
    // ---------------------------------------------------------------
    private void initViews() {
        tvProfileName   = findViewById(R.id.tvProfileName);
        tvProfileBio    = findViewById(R.id.tvProfileBio);
        tvAvatarInitial = findViewById(R.id.tvAvatarInitial);

        tvInfoName      = findViewById(R.id.tvInfoName);
        tvInfoEmail     = findViewById(R.id.tvInfoEmail);
        tvInfoPhone     = findViewById(R.id.tvInfoPhone);
        tvInfoJoinDate  = findViewById(R.id.tvInfoJoinDate);

        tvStatTransactions = findViewById(R.id.tvStatTransactions);
        tvStatSavings      = findViewById(R.id.tvStatSavings);
        tvStatBudgets      = findViewById(R.id.tvStatBudgets);

        switchNotification = findViewById(R.id.switchNotification);
        switchDarkMode     = findViewById(R.id.switchDarkMode);
        btnLogout          = findViewById(R.id.btnLogout);

        // Clickable rows
        btnBack             = findViewById(R.id.btnBack);
        flAvatar            = findViewById(R.id.flAvatar);
        rowEditName         = findViewById(R.id.rowEditName);
        rowEditEmail        = findViewById(R.id.rowEditEmail);
        rowEditPhone        = findViewById(R.id.rowEditPhone);
        rowChangePIN        = findViewById(R.id.rowChangePIN);
        rowManageCategories = findViewById(R.id.rowManageCategories);
        rowExportReport     = findViewById(R.id.rowExportReport);
        rowAbout            = findViewById(R.id.rowAbout);
    }

    // ---------------------------------------------------------------
    // Load & display profile data from SharedPreferences
    // ---------------------------------------------------------------
    private void loadProfileData() {
        String name      = prefs.getString("user_name", "Pengguna BudgetKu");
        String email     = prefs.getString("user_email", "user@budgetku.app");
        String phone     = prefs.getString("user_phone", "");
        String joinDate  = prefs.getString("user_join_date", "");

        // Set join date on first launch
        if (joinDate.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", new Locale("id", "ID"));
            joinDate = sdf.format(new Date());
            prefs.edit().putString("user_join_date", joinDate).apply();
        }

        // Header
        tvProfileName.setText(name);
        tvProfileBio.setText(prefs.getString("user_subtitle", "Kelola keuangan dengan bijak 💪"));

        // Avatar initial (first character)
        if (!name.isEmpty()) {
            tvAvatarInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
        } else {
            tvAvatarInitial.setText("👤");
        }

        // Info rows
        tvInfoName.setText(name);
        tvInfoEmail.setText(email.isEmpty() ? "Belum diisi" : email);
        tvInfoPhone.setText(phone.isEmpty() ? "Belum diisi" : phone);
        tvInfoJoinDate.setText(joinDate);
    }

    // ---------------------------------------------------------------
    // Load stats from database
    // ---------------------------------------------------------------
    private void loadStats() {
        List<Transaction> transactions = db.getAllTransactions();
        tvStatTransactions.setText(String.valueOf(transactions.size()));

        List<SavingsGoal> savings = db.getAllSavingsGoals();
        tvStatSavings.setText(String.valueOf(savings.size()));

        List<Budget> budgets = db.getBudgetsByMonth(db.getCurrentMonth());
        tvStatBudgets.setText(String.valueOf(budgets.size()));
    }

    // ---------------------------------------------------------------
    // Apply saved preferences (switches state)
    // ---------------------------------------------------------------
    private void applyPreferences() {
        boolean notifEnabled = prefs.getBoolean("notif_enabled", true);
        boolean darkMode     = prefs.getBoolean("dark_mode", true);

        switchNotification.setChecked(notifEnabled);
        switchDarkMode.setChecked(darkMode);
    }

    // ---------------------------------------------------------------
    // Click Listeners
    // ---------------------------------------------------------------
    private void setupClickListeners() {

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Avatar tap — show emoji picker dialog
        flAvatar.setOnClickListener(v -> showAvatarEmojiPicker());

        // Edit info rows
        rowEditName.setOnClickListener(v -> showEditFieldDialog(
                "✏️ Ubah Nama", "Nama Lengkap", "user_name",
                tvInfoName, tvProfileName));

        rowEditEmail.setOnClickListener(v -> showEditFieldDialog(
                "📧 Ubah Email", "Email", "user_email",
                tvInfoEmail, null));

        rowEditPhone.setOnClickListener(v -> showEditFieldDialog(
                "📱 Ubah Nomor HP", "Nomor HP", "user_phone",
                tvInfoPhone, null));

        // Notification toggle
        switchNotification.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean("notif_enabled", isChecked).apply();
            String msg = isChecked ? "🔔 Notifikasi diaktifkan" : "🔕 Notifikasi dimatikan";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        // Dark mode toggle
        switchDarkMode.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(
                    isChecked
                            ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO);
            String msg = isChecked ? "🌙 Tema gelap aktif" : "☀️ Tema terang aktif";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        // Change PIN
        rowChangePIN.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("mode", "change");
            startActivity(intent);
        });

        // Manage Categories
        rowManageCategories.setOnClickListener(v ->
                showManageCategoriesDialog());

        // Export Report
        rowExportReport.setOnClickListener(v -> exportReport());

        // About
        rowAbout.setOnClickListener(v -> showAboutDialog());

        // Logout
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    // ---------------------------------------------------------------
    // Avatar Emoji Picker
    // ---------------------------------------------------------------
    private void showAvatarEmojiPicker() {
        String[] emojis = {"👤", "😊", "🤩", "😎", "🧑‍💻", "👨‍💼", "👩‍💼", "🦁", "🐯", "🐼", "🦊", "🐸"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Widget_BudgetKu_Dialog);
        builder.setTitle("🎨 Pilih Avatar")
                .setItems(emojis, (dialog, which) -> {
                    String selected = emojis[which];
                    tvAvatarInitial.setText(selected);
                    prefs.edit().putString("user_avatar", selected).apply();
                    Toast.makeText(this, "Avatar diperbarui!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    // ---------------------------------------------------------------
    // Generic Edit Field Dialog
    // ---------------------------------------------------------------
    private void showEditFieldDialog(String title, String hint, String prefKey,
                                     TextView targetView, TextView secondaryView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Widget_BudgetKu_Dialog);

        EditText etInput = new EditText(this);
        String current = prefs.getString(prefKey, "");
        etInput.setText(current);
        etInput.setHint(hint);
        etInput.setTextColor(getResources().getColor(R.color.text_primary, null));
        etInput.setHintTextColor(getResources().getColor(R.color.text_secondary, null));
        etInput.setPadding(48, 32, 48, 32);

        builder.setTitle(title)
                .setView(etInput)
                .setPositiveButton("Simpan", (d, w) -> {
                    String value = etInput.getText().toString().trim();
                    if (!TextUtils.isEmpty(value)) {
                        prefs.edit().putString(prefKey, value).apply();
                        targetView.setText(value);
                        if (secondaryView != null) secondaryView.setText(value);
                        // Refresh avatar initial if name changed
                        if ("user_name".equals(prefKey)) {
                            tvAvatarInitial.setText(
                                    prefs.getString("user_avatar",
                                            String.valueOf(value.charAt(0)).toUpperCase()));
                        }
                        Toast.makeText(this, "✅ Berhasil diperbarui!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    // ---------------------------------------------------------------
    // Manage Categories Dialog (reused from ProfileFragment)
    // ---------------------------------------------------------------
    private void showManageCategoriesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Widget_BudgetKu_Dialog);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_manage_categories, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_card);

        EditText etNewCatName = dialogView.findViewById(R.id.etNewCategoryName);
        EditText etNewCatIcon = dialogView.findViewById(R.id.etNewCategoryIcon);
        Button btnAdd         = dialogView.findViewById(R.id.btnAddCategory);
        Button btnClose       = dialogView.findViewById(R.id.btnCloseCategories);

        List<Category> categories = db.getAllCategories();

        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1);
        for (Category c : categories) {
            adapter.add(c.getIcon() + " " + c.getName() + " [" + c.getType() + "]");
        }

        android.widget.ListView lv = dialogView.findViewById(R.id.lvCategories);
        if (lv != null) {
            lv.setAdapter(adapter);
            lv.setOnItemLongClickListener((parent, v, pos, id) -> {
                Category cat = categories.get(pos);
                if (!cat.isDefault()) {
                    new AlertDialog.Builder(this, R.style.Widget_BudgetKu_Dialog)
                            .setTitle("Hapus Kategori")
                            .setMessage("Hapus kategori \"" + cat.getName() + "\"?")
                            .setPositiveButton("Hapus", (d2, w) -> {
                                db.deleteCategory(cat.getId());
                                dialog.dismiss();
                                showManageCategoriesDialog();
                            })
                            .setNegativeButton("Batal", null)
                            .show();
                } else {
                    Toast.makeText(this, "Kategori default tidak bisa dihapus", Toast.LENGTH_SHORT).show();
                }
                return true;
            });
        }

        btnAdd.setOnClickListener(v -> {
            String name = etNewCatName.getText().toString().trim();
            String icon = etNewCatIcon.getText().toString().trim();
            if (TextUtils.isEmpty(name)) { etNewCatName.setError("Wajib diisi"); return; }
            if (TextUtils.isEmpty(icon)) icon = "📦";
            Category newCat = new Category(name, icon, "#8B949E", "BOTH");
            db.addCategory(newCat);
            Toast.makeText(this, "✅ Kategori ditambahkan!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // ---------------------------------------------------------------
    // Export Report
    // ---------------------------------------------------------------
    private void exportReport() {
        String report = db.generateFullReport(db.getCurrentMonth());

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Widget_BudgetKu_Dialog);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_export_report, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_card);

        TextView tvReport = dialogView.findViewById(R.id.tvReportContent);
        Button btnShare   = dialogView.findViewById(R.id.btnShareReport);
        Button btnClose   = dialogView.findViewById(R.id.btnCloseReport);

        tvReport.setText(report);

        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Laporan Keuangan BudgetKu");
            shareIntent.putExtra(Intent.EXTRA_TEXT, report);
            startActivity(Intent.createChooser(shareIntent, "Bagikan Laporan"));
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // ---------------------------------------------------------------
    // About Dialog
    // ---------------------------------------------------------------
    private void showAboutDialog() {
        new AlertDialog.Builder(this, R.style.Widget_BudgetKu_Dialog)
                .setTitle("💰 BudgetKu")
                .setMessage("Versi 1.0.0\n\n" +
                        "Aplikasi manajemen keuangan pribadi:\n" +
                        "✅ Dashboard & statistik\n" +
                        "✅ Koneksi akun bank digital\n" +
                        "✅ Riwayat transaksi\n" +
                        "✅ Budget goals\n" +
                        "✅ Split bill\n" +
                        "✅ Tabungan\n" +
                        "✅ Tagihan rutin\n" +
                        "✅ Grafik keuangan\n" +
                        "✅ Export laporan\n" +
                        "✅ Keamanan PIN\n\n" +
                        "Dibuat dengan ❤️ untuk Mobile24SI3")
                .setPositiveButton("OK", null)
                .show();
    }

    // ---------------------------------------------------------------
    // Logout Confirmation
    // ---------------------------------------------------------------
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this, R.style.Widget_BudgetKu_Dialog)
                .setTitle("🚪 Keluar")
                .setMessage("Apakah kamu yakin ingin keluar dari BudgetKu?\n\nData lokal tetap tersimpan.")
                .setPositiveButton("Keluar", (d, w) -> {
                    // Clear session / PIN lock
                    prefs.edit()
                            .remove("is_logged_in")
                            .apply();
                    Toast.makeText(this, "👋 Sampai jumpa!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning from another screen
        loadProfileData();
        loadStats();
    }
}
