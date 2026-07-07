package com.mobile.uph24si3.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;


import com.mobile.uph24si3.ProfilActivity;
import com.mobile.uph24si3.R;
import com.mobile.uph24si3.database.DatabaseHelper;
import com.mobile.uph24si3.model.Category;

import java.util.List;

public class ProfileFragment extends Fragment {

    private DatabaseHelper db;
    private SharedPreferences prefs;
    private TextView tvUserName, tvUserEmail;
    private Button btnEditProfile, btnChangePIN, btnExportReport, btnManageCategories, btnAbout, btnSmsAutoRead, btnOpenMyProfile, btnLogout, btnGoPremium;
    private View llPremiumBadge;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        db = DatabaseHelper.getInstance(requireContext());
        prefs = requireActivity().getSharedPreferences("BudgetKuPrefs", requireActivity().MODE_PRIVATE);

        initViews(view);
        checkPremiumStatus();
        loadProfile();
        setupClickListeners();

        return view;
    }

    private void checkPremiumStatus() {
        boolean isPremium = prefs.getBoolean("is_premium", false);
        if (isPremium) {
            btnGoPremium.setVisibility(View.GONE);
            llPremiumBadge.setVisibility(View.VISIBLE);
        } else {
            btnGoPremium.setVisibility(View.VISIBLE);
            llPremiumBadge.setVisibility(View.GONE);
        }
    }

    private void initViews(View view) {
        tvUserName = view.findViewById(R.id.tvProfileName);
        tvUserEmail = view.findViewById(R.id.tvProfileSubtitle);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnChangePIN = view.findViewById(R.id.btnChangePIN);
        btnExportReport = view.findViewById(R.id.btnExportReport);
        btnManageCategories = view.findViewById(R.id.btnManageCategories);
        btnAbout = view.findViewById(R.id.btnAbout);
        btnSmsAutoRead = view.findViewById(R.id.btnSmsAutoRead);
        btnOpenMyProfile = view.findViewById(R.id.btnOpenMyProfile);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnGoPremium = view.findViewById(R.id.btnGoPremium);
        llPremiumBadge = view.findViewById(R.id.llPremiumBadge);

        // Stats cards
        TextView tvTotalTransactions = view.findViewById(R.id.tvStatTransactions);
        TextView tvTotalSavings = view.findViewById(R.id.tvStatSavings);
        TextView tvTotalBudgets = view.findViewById(R.id.tvStatBudgets);

        List<com.mobile.uph24si3.model.Transaction> all = db.getAllTransactions();
        tvTotalTransactions.setText(String.valueOf(all.size()));

        List<com.mobile.uph24si3.model.SavingsGoal> savings = db.getAllSavingsGoals();
        tvTotalSavings.setText(String.valueOf(savings.size()));

        List<com.mobile.uph24si3.model.Budget> budgets = db.getBudgetsByMonth(db.getCurrentMonth());
        tvTotalBudgets.setText(String.valueOf(budgets.size()));
    }

    private void loadProfile() {
        String name = prefs.getString("user_name", "Pengguna BudgetKu");
        String subtitle = prefs.getString("user_subtitle", "Kelola keuangan dengan bijak 💪");
        tvUserName.setText(name);
        tvUserEmail.setText(subtitle);
    }

    private void setupClickListeners() {
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        btnChangePIN.setOnClickListener(v -> changePIN());
        btnExportReport.setOnClickListener(v -> exportReport());
        btnManageCategories.setOnClickListener(v -> showManageCategoriesDialog());
        btnAbout.setOnClickListener(v -> showAboutDialog());
        btnSmsAutoRead.setOnClickListener(v -> triggerSmsAutoRead());
        // Buka ProfilActivity (halaman profil sesuai soal UTS)
        btnOpenMyProfile.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), com.mobile.uph24si3.ProfilActivity.class);
            startActivity(intent);
        });
        btnLogout.setOnClickListener(v -> logout());
        btnGoPremium.setOnClickListener(v -> showPremiumDialog());
    }

    private void showPremiumDialog() {
        String[] options = {
            "Mingguan - Rp 15.000",
            "Bulanan - Rp 49.000",
            "Tahunan - Rp 399.000 (Hemat 30%)"
        };
        final int[] selectedOption = {1}; // Default to Monthly

        new AlertDialog.Builder(requireContext(), R.style.Widget_BudgetKu_Dialog)
                .setTitle("⭐ Pilih Paket Premium")
                .setSingleChoiceItems(options, selectedOption[0], (dialog, which) -> {
                    selectedOption[0] = which;
                })
                .setPositiveButton("Beli Sekarang", (dialog, which) -> {
                    // Simulasi Pembelian Berhasil
                    prefs.edit().putBoolean("is_premium", true).apply();
                    checkPremiumStatus();
                    
                    String selectedPackage = options[selectedOption[0]].split(" - ")[0];
                    Toast.makeText(requireContext(), 
                        "🎉 Pembelian Berhasil!\nPaket " + selectedPackage + " diaktifkan.", 
                        Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void triggerSmsAutoRead() {
        // Delegate ke HomeFragment yang memiliki permission launcher
        if (getActivity() != null) {
            androidx.fragment.app.Fragment homeFragment =
                getActivity().getSupportFragmentManager().findFragmentByTag("home");
            if (homeFragment instanceof HomeFragment) {
                ((HomeFragment) homeFragment).requestSmsAutoRead();
            } else {
                // Fallback: switch ke home tab dulu
                Toast.makeText(requireContext(),
                    "📱 Buka tab Beranda lalu coba lagi", Toast.LENGTH_SHORT).show();
                if (getActivity() instanceof com.mobile.uph24si3.DashboardActivity) {
                    ((com.mobile.uph24si3.DashboardActivity) getActivity())
                        .switchToHome();
                }
            }
        }
    }

    private void logout() {
        new AlertDialog.Builder(requireContext(), R.style.Widget_BudgetKu_Dialog)
                .setTitle("Keluar Akun")
                .setMessage("Apakah Anda yakin ingin keluar dari akun BudgetKu?")
                .setPositiveButton("Keluar", (dialog, which) -> {
                    // Hapus status login
                    prefs.edit().putBoolean("is_logged_in", false).apply();

                    // Kembali ke LoginActivity
                    Intent intent = new Intent(requireActivity(), com.mobile.uph24si3.LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.Widget_BudgetKu_Dialog);
        View dialogView = LayoutInflater.from(requireContext()).inflate(android.R.layout.activity_list_item, null);

        // Simple dialog with EditText for name
        EditText etName = new EditText(requireContext());
        etName.setText(prefs.getString("user_name", ""));
        etName.setHint("Nama Anda");
        etName.setTextColor(getResources().getColor(R.color.text_primary, null));
        etName.setPadding(48, 32, 48, 32);

        builder.setTitle("✏️ Edit Profil")
                .setView(etName)
                .setPositiveButton("Simpan", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    if (!TextUtils.isEmpty(name)) {
                        prefs.edit().putString("user_name", name).apply();
                        loadProfile();
                        Toast.makeText(requireContext(), "✅ Profil diperbarui!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void changePIN() {
        // Keamanan dikelola melalui form login (Nama, NIM, Password)
        Toast.makeText(requireContext(),
                "🔒 Keamanan dikelola melalui form Login",
                Toast.LENGTH_SHORT).show();
    }

    private void exportReport() {
        boolean isPremium = prefs.getBoolean("is_premium", false);
        String report;
        
        if (isPremium) {
            report = generatePremiumReport();
        } else {
            report = db.generateFullReport(db.getCurrentMonth());
        }

        // Show in dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.Widget_BudgetKu_Dialog);
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_export_report, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_card);

        TextView tvReport = dialogView.findViewById(R.id.tvReportContent);
        Button btnShare = dialogView.findViewById(R.id.btnShareReport);
        Button btnClose = dialogView.findViewById(R.id.btnCloseReport);

        tvReport.setText(report);

        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Laporan Keuangan BudgetKu");
            shareIntent.putExtra(Intent.EXTRA_TEXT, report);
            startActivity(Intent.createChooser(shareIntent, "Bagikan Laporan"));
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        Toast.makeText(requireContext(), "📊 " + getString(R.string.report_exported), Toast.LENGTH_SHORT).show();
        dialog.show();
    }

    private String generatePremiumReport() {
        String currentMonth = db.getCurrentMonth();
        double income = db.getTotalIncome(currentMonth);
        double expense = db.getTotalExpense(currentMonth);
        double savings = income - expense;
        
        StringBuilder sb = new StringBuilder();
        sb.append("🌟 LAPORAN EKSEKUTIF PREMIUM 🌟\n");
        sb.append("Bulan: ").append(currentMonth).append("\n");
        sb.append("---------------------------------\n\n");
        
        sb.append("📊 RINGKASAN FINANSIAL:\n");
        sb.append("• Total Pemasukan : Rp ").append(String.format("%,.0f", income)).append("\n");
        sb.append("• Total Pengeluaran: Rp ").append(String.format("%,.0f", expense)).append("\n");
        sb.append("• Surplus/Defisit  : Rp ").append(String.format("%,.0f", savings)).append("\n\n");

        if (income > 0) {
            double rate = (savings / income) * 100;
            sb.append("📈 ANALISIS TABUNGAN:\n");
            sb.append("Anda berhasil menyisihkan ").append((int)rate).append("% dari pendapatan Anda.\n");
            if (rate > 20) sb.append("Status: SANGAT SEHAT (Pertahankan gaya hidup ini!)\n\n");
            else sb.append("Status: PERLU EVALUASI (Cobalah metode 50/30/20)\n\n");
        }

        sb.append("💡 REKOMENDASI PREMIUM:\n");
        List<String[]> categories = db.getExpenseByCategory(currentMonth);
        if (!categories.isEmpty()) {
            sb.append("Pengeluaran terbesar Anda ada pada kategori '").append(categories.get(0)[0])
              .append("'. Coba kurangi di sektor ini bulan depan untuk tabungan lebih maksimal.\n");
        }
        
        return sb.toString();
    }

    private void showManageCategoriesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.Widget_BudgetKu_Dialog);
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_manage_categories, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_card);


        EditText etNewCatName = dialogView.findViewById(R.id.etNewCategoryName);
        EditText etNewCatIcon = dialogView.findViewById(R.id.etNewCategoryIcon);
        Button btnAdd = dialogView.findViewById(R.id.btnAddCategory);
        Button btnClose = dialogView.findViewById(R.id.btnCloseCategories);

        List<Category> categories = db.getAllCategories();

        // Simple list display
        android.widget.ArrayAdapter<String> catDisplayAdapter = new android.widget.ArrayAdapter<>(
                requireContext(), android.R.layout.simple_list_item_1);
        for (Category c : categories) {
            catDisplayAdapter.add(c.getIcon() + " " + c.getName() + " [" + c.getType() + "]");
        }

        android.widget.ListView lvCats = dialogView.findViewById(R.id.lvCategories);
        if (lvCats != null) {
            lvCats.setAdapter(catDisplayAdapter);
            lvCats.setOnItemLongClickListener((parent, v, position, id) -> {
                Category cat = categories.get(position);
                if (!cat.isDefault()) {
                    new AlertDialog.Builder(requireContext(), R.style.Widget_BudgetKu_Dialog)
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
                    Toast.makeText(requireContext(), "Kategori default tidak bisa dihapus", Toast.LENGTH_SHORT).show();
                }
                return true;
            });
        }

        btnAdd.setOnClickListener(v -> {
            String name = etNewCatName.getText().toString().trim();
            String icon = etNewCatIcon.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                etNewCatName.setError("Wajib diisi");
                return;
            }
            if (TextUtils.isEmpty(icon)) icon = "📦";
            Category newCat = new Category(name, icon, "#8B949E", "BOTH");
            db.addCategory(newCat);
            Toast.makeText(requireContext(), "✅ Kategori ditambahkan!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(requireContext(), R.style.Widget_BudgetKu_Dialog)
                .setTitle("💰 BudgetKu")
                .setMessage("Versi 1.0.0\n\n" +
                        "Aplikasi manajemen keuangan pribadi dengan fitur:\n" +
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
}
