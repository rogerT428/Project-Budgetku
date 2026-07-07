package com.mobile.uph24si3.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.uph24si3.R;
import com.mobile.uph24si3.adapter.TransactionAdapter;
import com.mobile.uph24si3.database.DatabaseHelper;
import com.mobile.uph24si3.model.Category;
import com.mobile.uph24si3.model.Transaction;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionFragment extends Fragment {

    private DatabaseHelper db;
    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;
    private EditText etSearch;
    private Button btnFilterAll, btnFilterIncome, btnFilterExpense;
    private TextView tvEmpty;
    private NumberFormat currencyFormat;

    private List<Transaction> allTransactions = new ArrayList<>();
    private String currentFilter = "ALL";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);

        db = DatabaseHelper.getInstance(requireContext());
        currencyFormat = NumberFormat.getInstance(new Locale("id", "ID"));

        initViews(view);
        setupRecyclerView();
        setupSearch();
        setupFilters();
        loadTransactions();

        return view;
    }

    private void initViews(View view) {
        rvTransactions = view.findViewById(R.id.rvTransactions);
        etSearch = view.findViewById(R.id.etSearchTransaction);
        btnFilterAll = view.findViewById(R.id.btnFilterAll);
        btnFilterIncome = view.findViewById(R.id.btnFilterIncome);
        btnFilterExpense = view.findViewById(R.id.btnFilterExpense);
        tvEmpty = view.findViewById(R.id.tvEmptyTransactions);

        // FAB for adding transaction
        view.findViewById(R.id.fabAddTransaction).setOnClickListener(v ->
                showAddTransactionDialog());
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(requireContext(), new ArrayList<>());
        rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTransactions.setAdapter(adapter);

        adapter.setOnItemClickListener(new TransactionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Transaction transaction) {
                showTransactionDetail(transaction);
            }

            @Override
            public void onItemLongClick(Transaction transaction) {
                showDeleteDialog(transaction);
            }
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAndSearch(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilters() {
        btnFilterAll.setOnClickListener(v -> {
            currentFilter = "ALL";
            updateFilterButtons();
            filterAndSearch(etSearch.getText().toString());
        });
        btnFilterIncome.setOnClickListener(v -> {
            currentFilter = "INCOME";
            updateFilterButtons();
            filterAndSearch(etSearch.getText().toString());
        });
        btnFilterExpense.setOnClickListener(v -> {
            currentFilter = "EXPENSE";
            updateFilterButtons();
            filterAndSearch(etSearch.getText().toString());
        });

        updateFilterButtons();
    }

    private void updateFilterButtons() {
        int activeColor = getResources().getColor(R.color.accent_green, null);
        int inactiveColor = getResources().getColor(R.color.bg_card_elevated, null);
        int activeTextColor = getResources().getColor(R.color.bg_primary, null);
        int inactiveTextColor = getResources().getColor(R.color.text_secondary, null);

        btnFilterAll.setBackgroundColor("ALL".equals(currentFilter) ? activeColor : inactiveColor);
        btnFilterAll.setTextColor("ALL".equals(currentFilter) ? activeTextColor : inactiveTextColor);

        btnFilterIncome.setBackgroundColor("INCOME".equals(currentFilter) ? activeColor : inactiveColor);
        btnFilterIncome.setTextColor("INCOME".equals(currentFilter) ? activeTextColor : inactiveTextColor);

        btnFilterExpense.setBackgroundColor("EXPENSE".equals(currentFilter) ? activeColor : inactiveColor);
        btnFilterExpense.setTextColor("EXPENSE".equals(currentFilter) ? activeTextColor : inactiveTextColor);
    }

    private void filterAndSearch(String query) {
        List<Transaction> filtered = new ArrayList<>();
        for (Transaction t : allTransactions) {
            // Apply type filter
            if ("INCOME".equals(currentFilter) && !t.isIncome()) continue;
            if ("EXPENSE".equals(currentFilter) && !t.isExpense()) continue;

            // Apply search
            if (!TextUtils.isEmpty(query)) {
                String q = query.toLowerCase();
                boolean matches = (t.getCategory() != null && t.getCategory().toLowerCase().contains(q))
                        || (t.getDescription() != null && t.getDescription().toLowerCase().contains(q));
                if (!matches) continue;
            }

            filtered.add(t);
        }

        adapter.updateData(filtered);
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void loadTransactions() {
        allTransactions = db.getAllTransactions();
        filterAndSearch(etSearch.getText().toString());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTransactions();
    }

    private void showAddTransactionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.Widget_BudgetKu_Dialog);
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_transaction, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_card);
        }

        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        EditText etAmount = dialogView.findViewById(R.id.etTransactionAmount);
        EditText etDescription = dialogView.findViewById(R.id.etTransactionDescription);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        Button btnSave = dialogView.findViewById(R.id.btnSaveTransaction);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelTransaction);
        Button btnToggleType = dialogView.findViewById(R.id.btnToggleType);

        final String[] selectedType = {Transaction.TYPE_EXPENSE};
        tvDialogTitle.setText("❤️ Tambah Pengeluaran");
        btnToggleType.setText("Ganti ke Pemasukan");

        // Helper to refresh categories
        List<Category>[] categoriesHolder = new List[1];
        String[][] catNamesHolder = new String[1][0];

        Runnable refreshCategories = () -> {
            categoriesHolder[0] = db.getCategoriesByType(selectedType[0]);
            catNamesHolder[0] = new String[categoriesHolder[0].size()];
            for (int i = 0; i < categoriesHolder[0].size(); i++) {
                Category c = categoriesHolder[0].get(i);
                catNamesHolder[0][i] = c.getIcon() + " " + c.getName();
            }
            ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, catNamesHolder[0]);
            spinnerCategory.setAdapter(catAdapter);
        };
        refreshCategories.run();

        btnToggleType.setOnClickListener(v -> {
            if (Transaction.TYPE_EXPENSE.equals(selectedType[0])) {
                selectedType[0] = Transaction.TYPE_INCOME;
                tvDialogTitle.setText("💚 Tambah Pemasukan");
                btnToggleType.setText("Ganti ke Pengeluaran");
            } else {
                selectedType[0] = Transaction.TYPE_EXPENSE;
                tvDialogTitle.setText("❤️ Tambah Pengeluaran");
                btnToggleType.setText("Ganti ke Pemasukan");
            }
            refreshCategories.run();
        });

        btnSave.setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString().trim();
            String desc = etDescription.getText().toString().trim();

            if (TextUtils.isEmpty(amountStr)) {
                etAmount.setError("Masukkan jumlah");
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                String selectedCat = categoriesHolder[0].isEmpty() ? "Lainnya" :
                        categoriesHolder[0].get(spinnerCategory.getSelectedItemPosition()).getName();
                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                Transaction t = new Transaction(selectedType[0], amount, selectedCat, desc, today, 0);
                db.addTransaction(t);
                loadTransactions();
                dialog.dismiss();
                Toast.makeText(requireContext(), "✅ Transaksi ditambahkan!", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                etAmount.setError("Jumlah tidak valid");
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showTransactionDetail(Transaction t) {
        String detail = "Tanggal: " + t.getDate() + "\n" +
                "Tipe: " + (t.isIncome() ? "Pemasukan" : "Pengeluaran") + "\n" +
                "Kategori: " + t.getCategory() + "\n" +
                "Jumlah: Rp " + currencyFormat.format(t.getAmount()) + "\n" +
                "Deskripsi: " + (t.getDescription() != null ? t.getDescription() : "-");

        new AlertDialog.Builder(requireContext(), R.style.Widget_BudgetKu_Dialog)
                .setTitle("📋 Detail Transaksi")
                .setMessage(detail)
                .setPositiveButton("OK", null)
                .setNegativeButton("Hapus", (d, w) -> showDeleteDialog(t))
                .show();
    }

    private void showDeleteDialog(Transaction t) {
        new AlertDialog.Builder(requireContext(), R.style.Widget_BudgetKu_Dialog)
                .setTitle("Hapus Transaksi")
                .setMessage("Hapus transaksi ini?\nRp " + currencyFormat.format(t.getAmount()) + " - " + t.getCategory())
                .setPositiveButton("Hapus", (d, w) -> {
                    db.deleteTransaction(t.getId());
                    loadTransactions();
                    Toast.makeText(requireContext(), "Transaksi dihapus", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}
