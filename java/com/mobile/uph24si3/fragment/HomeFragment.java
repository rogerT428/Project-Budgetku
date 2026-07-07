package com.mobile.uph24si3.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.uph24si3.R;
import com.mobile.uph24si3.adapter.BankAccountAdapter;
import com.mobile.uph24si3.adapter.TransactionAdapter;
import com.mobile.uph24si3.database.DatabaseHelper;
import com.mobile.uph24si3.model.BankAccount;
import com.mobile.uph24si3.model.Category;
import com.mobile.uph24si3.model.Transaction;
import com.mobile.uph24si3.sms.SmsAutoReader;
import com.mobile.uph24si3.view.PieChartView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private DatabaseHelper db;
    private TextView tvTotalBalance, tvIncomeAmount, tvExpenseAmount;
    private RecyclerView rvBankAccounts, rvRecentTransactions;
    private BankAccountAdapter bankAdapter;
    private TransactionAdapter transactionAdapter;
    private PieChartView pieChart;
    private LinearLayout btnAddIncome, btnAddExpense, btnLinkBank;
    private LinearLayout tvNoTransactions;
    private LinearLayout llBankEmptyHint, llBankSection, llPremiumInsights;
    private TextView tvFinancialHealthTitle, tvFinancialTip;
    private SharedPreferences prefs;
    private NumberFormat currencyFormat;

    // SMS permission launcher
    private ActivityResultLauncher<String[]> smsPermissionLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        smsPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean readSms = result.getOrDefault(Manifest.permission.READ_SMS, false);
                Boolean receiveSms = result.getOrDefault(Manifest.permission.RECEIVE_SMS, false);
                if (Boolean.TRUE.equals(readSms)) {
                    // Read existing SMS from inbox
                    int imported = SmsAutoReader.readInboxSms(requireContext(), db);
                    if (imported > 0) {
                        refreshData();
                        Toast.makeText(requireContext(),
                            "✅ " + imported + " transaksi berhasil diimpor dari SMS!",
                            Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(requireContext(),
                            "📭 Tidak ada SMS transaksi bank ditemukan",
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(),
                        "⚠️ Izin SMS diperlukan untuk fitur ini",
                        Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = DatabaseHelper.getInstance(requireContext());
        db.seedInitialData(); // Isi data riwayat otomatis mulai dari tanggal 1
        prefs = requireActivity().getSharedPreferences("BudgetKuPrefs", requireActivity().MODE_PRIVATE);
        currencyFormat = NumberFormat.getInstance(new Locale("id", "ID"));

        initViews(view);
        setupRecyclerViews();
        setupClickListeners(view);
        refreshData();

        return view;
    }

    private void initViews(View view) {
        tvTotalBalance = view.findViewById(R.id.tvTotalBalance);
        tvIncomeAmount = view.findViewById(R.id.tvIncomeAmount);
        tvExpenseAmount = view.findViewById(R.id.tvExpenseAmount);
        rvBankAccounts = view.findViewById(R.id.rvBankAccounts);
        rvRecentTransactions = view.findViewById(R.id.rvRecentTransactions);
        pieChart = view.findViewById(R.id.pieChart);
        btnAddIncome = view.findViewById(R.id.btnQuickIncome);
        btnAddExpense = view.findViewById(R.id.btnQuickExpense);
        btnLinkBank = view.findViewById(R.id.btnLinkBank);
        tvNoTransactions = view.findViewById(R.id.tvNoTransactions);
        llBankEmptyHint = view.findViewById(R.id.llBankEmptyHint);
        llBankSection = view.findViewById(R.id.llBankSection);
        llPremiumInsights = view.findViewById(R.id.llPremiumInsights);
        tvFinancialHealthTitle = view.findViewById(R.id.tvFinancialHealthTitle);
        tvFinancialTip = view.findViewById(R.id.tvFinancialTip);
    }

    private void setupRecyclerViews() {
        // Bank accounts - horizontal
        bankAdapter = new BankAccountAdapter(requireContext(), new ArrayList<>());
        rvBankAccounts.setLayoutManager(new LinearLayoutManager(requireContext(),
                LinearLayoutManager.HORIZONTAL, false));
        rvBankAccounts.setAdapter(bankAdapter);
        bankAdapter.setOnLongClickListener(account -> showUnlinkBankDialog(account));

        // Recent transactions - vertical
        transactionAdapter = new TransactionAdapter(requireContext(), new ArrayList<>());
        rvRecentTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecentTransactions.setNestedScrollingEnabled(false);
        rvRecentTransactions.setAdapter(transactionAdapter);
        transactionAdapter.setOnItemClickListener(new TransactionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Transaction transaction) {}
            @Override
            public void onItemLongClick(Transaction transaction) {
                showDeleteTransactionDialog(transaction);
            }
        });
    }

    private void setupClickListeners(View view) {
        btnAddIncome.setOnClickListener(v -> showAddTransactionDialog(Transaction.TYPE_INCOME));
        btnAddExpense.setOnClickListener(v -> showAddTransactionDialog(Transaction.TYPE_EXPENSE));
        btnLinkBank.setOnClickListener(v -> {
            Intent intent = requireContext().getPackageManager().getLaunchIntentForPackage("com.budgetku.dummybank");
            if (intent != null) {
                startActivity(intent);
            } else {
                showLinkBankDialog();
            }
        });

        // "+ Hubungkan" text button
        View btnLinkAlt = view.findViewById(R.id.btnLinkBankAlt);
        if (btnLinkAlt != null) btnLinkAlt.setOnClickListener(v -> showLinkBankDialog());

        // History Search
        View btnOpenHistory = view.findViewById(R.id.btnOpenHistorySearch);
        if (btnOpenHistory != null) {
            btnOpenHistory.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), com.mobile.uph24si3.HistorySearchActivity.class);
                startActivity(intent);
            });
        }
    }

    public void refreshData() {
        if (!isAdded()) return;

        String currentMonth = db.getCurrentMonth();

        double totalBalance = db.getTotalBalance();
        double income = db.getTotalIncome(currentMonth);
        double expense = db.getTotalExpense(currentMonth);

        tvTotalBalance.setText("Rp " + currencyFormat.format(totalBalance));
        tvIncomeAmount.setText("Rp " + currencyFormat.format(income));
        tvExpenseAmount.setText("Rp " + currencyFormat.format(expense));

        // Bank accounts
        List<BankAccount> accounts = db.getAllBankAccounts();
        bankAdapter.updateData(accounts);
        
        // Sembunyikan seluruh section jika tidak ada bank (Termasuk Mandiri dll)
        if (accounts.isEmpty()) {
            llBankSection.setVisibility(View.GONE);
        } else {
            llBankSection.setVisibility(View.VISIBLE);
            llBankEmptyHint.setVisibility(View.GONE);
            rvBankAccounts.setVisibility(View.VISIBLE);
        }

        // Recent transactions (5)
        List<Transaction> recentTx = db.getRecentTransactions(5);
        transactionAdapter.updateData(recentTx);
        tvNoTransactions.setVisibility(recentTx.isEmpty() ? View.VISIBLE : View.GONE);

        // Pie chart
        updatePieChart(currentMonth, expense);

        // Premium Insights
        updatePremiumInsights(income, expense);
    }

    private void updatePremiumInsights(double income, double expense) {
        boolean isPremium = prefs.getBoolean("is_premium", false);
        if (!isPremium) {
            llPremiumInsights.setVisibility(View.GONE);
            return;
        }

        llPremiumInsights.setVisibility(View.VISIBLE);
        
        if (income <= 0) {
            tvFinancialHealthTitle.setText("Kesehatan: Perlu Perhatian");
            tvFinancialTip.setText("Belum ada pemasukan bulan ini. Yuk mulai mencatat!");
            return;
        }

        double savingsRate = ((income - expense) / income) * 100;
        
        if (savingsRate >= 30) {
            tvFinancialHealthTitle.setText("Kesehatan: Sangat Baik (A+)");
            tvFinancialTip.setText("Keren! Anda menabung " + (int)savingsRate + "% bulan ini. Anda berada di jalur aman.");
        } else if (savingsRate >= 10) {
            tvFinancialHealthTitle.setText("Kesehatan: Baik");
            tvFinancialTip.setText("Anda menabung " + (int)savingsRate + "%. Coba kurangi jajan untuk mencapai target 30%.");
        } else if (savingsRate > 0) {
            tvFinancialHealthTitle.setText("Kesehatan: Waspada");
            tvFinancialTip.setText("Tabungan Anda hanya " + (int)savingsRate + "%. Hati-hati dengan pengeluaran tak terduga.");
        } else {
            tvFinancialHealthTitle.setText("Kesehatan: Bahaya!");
            tvFinancialTip.setText("Pengeluaran lebih besar dari pemasukan! Segera evaluasi gaya hidup Anda.");
        }
    }

    private void updatePieChart(String month, double totalExpense) {
        List<String[]> expenseByCategory = db.getExpenseByCategory(month);
        List<PieChartView.PieSlice> slices = new ArrayList<>();
        int[] chartColors = {
            Color.parseColor("#00D4AA"), Color.parseColor("#1890FF"),
            Color.parseColor("#FAAD14"), Color.parseColor("#FF4D4F"),
            Color.parseColor("#7C3AED"), Color.parseColor("#FF8C42"),
            Color.parseColor("#52C41A"), Color.parseColor("#EB2F96")
        };

        for (int i = 0; i < Math.min(expenseByCategory.size(), chartColors.length); i++) {
            String[] item = expenseByCategory.get(i);
            String cat = item[0] != null ? item[0] : "Lainnya";
            float val = Float.parseFloat(item[1]);
            slices.add(new PieChartView.PieSlice(cat, val, chartColors[i]));
        }

        String centerText = "Rp " + currencyFormat.format(totalExpense);
        pieChart.setData(slices, centerText, "Pengeluaran");
    }

    // ============================================================
    // SMS AUTO-READ FEATURE
    // ============================================================
    public void requestSmsAutoRead() {
        boolean hasRead = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;
        boolean hasReceive = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;

        if (hasRead && hasReceive) {
            int imported = SmsAutoReader.readInboxSms(requireContext(), db);
            refreshData();
            String msg = imported > 0
                    ? "✅ " + imported + " transaksi diimpor dari SMS bank!"
                    : "📭 Tidak ada SMS transaksi bank baru";
            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
        } else {
            showSmsPermissionDialog();
        }
    }

    private void showSmsPermissionDialog() {
        new AlertDialog.Builder(requireContext(), R.style.Widget_BudgetKu_Dialog)
            .setTitle("📱 Auto-Baca Transaksi Bank")
            .setMessage("BudgetKu perlu akses SMS untuk membaca notifikasi transaksi dari:\n\n" +
                    "🏦 BCA  •  🏛️ Mandiri  •  💳 BRI  •  🏦 BNI\n" +
                    "💚 GoPay  •  💜 OVO  •  💙 DANA\n\n" +
                    "SMS tidak dikirim ke mana pun — hanya dibaca secara lokal di perangkat Anda.\n\n" +
                    "Izinkan akses SMS?")
            .setPositiveButton("✅ Izinkan", (d, w) -> {
                smsPermissionLauncher.launch(new String[]{
                    Manifest.permission.READ_SMS,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.RECEIVE_MMS
                });
            })
            .setNegativeButton("Nanti", null)
            .show();
    }

    // ============================================================
    // DIALOGS
    // ============================================================
    private void showAddTransactionDialog(String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.Widget_BudgetKu_Dialog);
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_transaction, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_card);

        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        EditText etAmount = dialogView.findViewById(R.id.etTransactionAmount);
        EditText etDescription = dialogView.findViewById(R.id.etTransactionDescription);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        Button btnSave = dialogView.findViewById(R.id.btnSaveTransaction);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelTransaction);

        tvDialogTitle.setText(Transaction.TYPE_INCOME.equals(type) ? "💚 Tambah Pemasukan" : "❤️ Tambah Pengeluaran");

        List<Category> categories = db.getCategoriesByType(type);
        List<String> catNames = new ArrayList<>();
        for (Category c : categories) catNames.add(c.getIcon() + " " + c.getName());
        spinnerCategory.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, catNames));

        btnSave.setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString().trim();
            if (TextUtils.isEmpty(amountStr)) { etAmount.setError("Masukkan jumlah"); return; }
            try {
                double amount = Double.parseDouble(amountStr);
                String selectedCat = catNames.isEmpty() ? "Lainnya" :
                        categories.get(spinnerCategory.getSelectedItemPosition()).getName();
                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                db.addTransaction(new Transaction(type, amount, selectedCat,
                        etDescription.getText().toString().trim(), today, 0));
                refreshData();
                dialog.dismiss();
                Toast.makeText(requireContext(), "✅ Transaksi ditambahkan!", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                etAmount.setError("Jumlah tidak valid");
            }
        });
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }


    private void showLinkBankDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.Widget_BudgetKu_Dialog);
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_link_bank, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_card);

        Spinner spinnerBank = dialogView.findViewById(R.id.spinnerBankType);
        EditText etAccountNumber = dialogView.findViewById(R.id.etAccountNumber);
        EditText etAccountHolder = dialogView.findViewById(R.id.etAccountHolder);
        EditText etInitialBalance = dialogView.findViewById(R.id.etInitialBalance);
        TextView tvValidation = dialogView.findViewById(R.id.tvAccountValidation);
        Button btnLink = dialogView.findViewById(R.id.btnLinkAccount);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelLink);

        // Data bank: nama, emoji, panjang rekening min-max, warna card
        String[][] bankData = {
            {"BCA",       "🏦", "10", "10"},
            {"Mandiri",   "🏛️", "13", "13"},
            {"BNI",       "🏦", "10", "10"},
            {"BRI",       "💳", "15", "15"},
            {"GoPay",     "💚", "10", "13"},
            {"OVO",       "💜", "10", "13"},
            {"DANA",      "💙", "10", "13"},
            {"ShopeePay", "🛍️", "10", "13"},
            {"BSI",       "🕌", "13", "13"},
            {"CIMB",      "🏦", "14", "14"}
        };

        String[] bankNames = new String[bankData.length];
        for (int i = 0; i < bankData.length; i++) {
            bankNames[i] = bankData[i][1] + "  " + bankData[i][0];
        }
        spinnerBank.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, bankNames));

        // Validasi nomor rekening real-time sesuai bank yang dipilih
        android.text.TextWatcher validationWatcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                int idx = spinnerBank.getSelectedItemPosition();
                if (idx < 0 || idx >= bankData.length) return;
                int minLen = Integer.parseInt(bankData[idx][2]);
                int maxLen = Integer.parseInt(bankData[idx][3]);
                int len = s.toString().trim().length();
                String bankName = bankData[idx][0];

                if (len == 0) {
                    tvValidation.setText("");
                } else if (len < minLen) {
                    tvValidation.setText("⚠️ Nomor rekening " + bankName + " harus " + minLen + " digit");
                    tvValidation.setTextColor(0xFFFAAD14);
                } else if (len > maxLen) {
                    tvValidation.setText("❌ Nomor rekening " + bankName + " maks " + maxLen + " digit");
                    tvValidation.setTextColor(0xFFFF4D4F);
                } else {
                    tvValidation.setText("✅ Format nomor rekening valid");
                    tvValidation.setTextColor(0xFF00D4AA);
                }
            }
        };
        etAccountNumber.addTextChangedListener(validationWatcher);

        btnLink.setOnClickListener(v -> {
            // Validasi
            String accNum = etAccountNumber.getText().toString().trim();
            String holder = etAccountHolder.getText().toString().trim();
            String balanceStr = etInitialBalance.getText().toString().trim();

            if (TextUtils.isEmpty(holder)) { etAccountHolder.setError("Nama pemilik wajib diisi"); return; }
            if (TextUtils.isEmpty(accNum)) { etAccountNumber.setError("Nomor rekening wajib diisi"); return; }
            if (TextUtils.isEmpty(balanceStr)) { etInitialBalance.setError("Masukkan saldo saat ini"); return; }

            int idx = spinnerBank.getSelectedItemPosition();
            int minLen = Integer.parseInt(bankData[idx][2]);
            int maxLen = Integer.parseInt(bankData[idx][3]);
            if (accNum.length() < minLen || accNum.length() > maxLen) {
                etAccountNumber.setError("Nomor rekening tidak valid untuk bank ini");
                return;
            }

            double balance;
            try {
                // Dukung format angka bebas: 1500000, 1500000.50, 1.500.000
                balance = Double.parseDouble(balanceStr.replace(",", ".").replaceAll("[^0-9.]", ""));
            } catch (Exception e) {
                etInitialBalance.setError("Format saldo tidak valid");
                return;
            }

            String bankName = bankData[idx][0];

            // Simulasi "connecting" dengan progress
            btnLink.setEnabled(false);
            btnLink.setText("⏳ Menghubungkan...");

            // Simulasi delay koneksi 1.5 detik
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                if (!isAdded()) return;
                // Simpan ke database
                db.addBankAccount(new BankAccount(bankName, accNum, holder, balance, bankName));
                refreshData();
                dialog.dismiss();

                // Tampilkan saldo yang tercatat
                NumberFormat fmt = NumberFormat.getInstance(new Locale("id", "ID"));
                Toast.makeText(requireContext(),
                    "✅ " + bankName + " berhasil terhubung!\nSaldo: Rp " + fmt.format(balance),
                    Toast.LENGTH_LONG).show();
            }, 1500);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showUnlinkBankDialog(BankAccount account) {
        new AlertDialog.Builder(requireContext(), R.style.Widget_BudgetKu_Dialog)
            .setTitle("Putuskan Akun")
            .setMessage("Hapus " + account.getBankName() + " dari daftar?")
            .setPositiveButton("Ya", (d, w) -> { db.deleteBankAccount(account.getId()); refreshData(); })
            .setNegativeButton("Tidak", null).show();
    }

    private void showDeleteTransactionDialog(Transaction transaction) {
        new AlertDialog.Builder(requireContext(), R.style.Widget_BudgetKu_Dialog)
            .setTitle("Hapus Transaksi")
            .setMessage("Hapus transaksi Rp " + currencyFormat.format(transaction.getAmount()) + "?")
            .setPositiveButton("Hapus", (d, w) -> { db.deleteTransaction(transaction.getId()); refreshData(); })
            .setNegativeButton("Batal", null).show();
    }
}
