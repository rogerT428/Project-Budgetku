package com.mobile.uph24si3.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
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
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.mobile.uph24si3.R;
import com.mobile.uph24si3.adapter.BudgetAdapter;
import com.mobile.uph24si3.adapter.RecurringBillAdapter;
import com.mobile.uph24si3.adapter.SavingsAdapter;
import com.mobile.uph24si3.database.DatabaseHelper;
import com.mobile.uph24si3.model.Budget;
import com.mobile.uph24si3.model.Category;
import com.mobile.uph24si3.model.RecurringBill;
import com.mobile.uph24si3.model.SavingsGoal;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BudgetFragment extends Fragment {

    private DatabaseHelper db;
    private TabLayout tabLayout;

    // Budget Goals views
    private RecyclerView rvBudgets;
    private BudgetAdapter budgetAdapter;
    private Button btnAddBudget;
    private TextView tvNoBudgets;

    // Savings views
    private RecyclerView rvSavings;
    private SavingsAdapter savingsAdapter;
    private Button btnAddSavings;
    private TextView tvNoSavings;

    // Recurring Bills views
    private RecyclerView rvRecurring;
    private RecurringBillAdapter recurringAdapter;
    private Button btnAddRecurring;
    private TextView tvNoRecurring;

    // Tab containers
    private View tabBudget, tabSavings, tabRecurring;

    private NumberFormat currencyFormat;
    private int currentTab = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        db = DatabaseHelper.getInstance(requireContext());
        currencyFormat = NumberFormat.getInstance(new Locale("id", "ID"));

        tabLayout = view.findViewById(R.id.tabLayoutBudget);

        // Tab containers
        tabBudget = view.findViewById(R.id.tabContentBudget);
        tabSavings = view.findViewById(R.id.tabContentSavings);
        tabRecurring = view.findViewById(R.id.tabContentRecurring);

        setupBudgetGoals(view);
        setupSavings(view);
        setupRecurring(view);
        setupTabs();
        loadAllData();

        return view;
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("🎯 Budget"));
        tabLayout.addTab(tabLayout.newTab().setText("💰 Tabungan"));
        tabLayout.addTab(tabLayout.newTab().setText("📅 Tagihan"));

        showTab(0);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                showTab(currentTab);
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void showTab(int tab) {
        tabBudget.setVisibility(tab == 0 ? View.VISIBLE : View.GONE);
        tabSavings.setVisibility(tab == 1 ? View.VISIBLE : View.GONE);
        tabRecurring.setVisibility(tab == 2 ? View.VISIBLE : View.GONE);
    }

    private void setupBudgetGoals(View view) {
        rvBudgets = view.findViewById(R.id.rvBudgets);
        btnAddBudget = view.findViewById(R.id.btnAddBudget);
        tvNoBudgets = view.findViewById(R.id.tvNoBudgets);

        budgetAdapter = new BudgetAdapter(requireContext(), new ArrayList<>());
        rvBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBudgets.setNestedScrollingEnabled(false);
        rvBudgets.setAdapter(budgetAdapter);

        budgetAdapter.setOnDeleteListener(budget -> {
            new AlertDialog.Builder(requireContext(), R.style.Widget_BudgetKu_Dialog)
                    .setTitle("Hapus Budget")
                    .setMessage("Hapus budget " + budget.getCategory() + "?")
                    .setPositiveButton("Hapus", (d, w) -> {
                        db.deleteBudget(budget.getId());
                        loadBudgets();
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        btnAddBudget.setOnClickListener(v -> showAddBudgetDialog());
    }

    private void setupSavings(View view) {
        rvSavings = view.findViewById(R.id.rvSavings);
        btnAddSavings = view.findViewById(R.id.btnAddSavings);
        tvNoSavings = view.findViewById(R.id.tvNoSavings);

        savingsAdapter = new SavingsAdapter(requireContext(), new ArrayList<>());
        rvSavings.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSavings.setNestedScrollingEnabled(false);
        rvSavings.setAdapter(savingsAdapter);

        savingsAdapter.setListener(new SavingsAdapter.OnSavingsListener() {
            @Override
            public void onAddMoney(SavingsGoal goal) {
                showAddMoneyToSavingsDialog(goal);
            }
            @Override
            public void onDelete(SavingsGoal goal) {
                new AlertDialog.Builder(requireContext(), R.style.Widget_BudgetKu_Dialog)
                        .setTitle("Hapus Tabungan")
                        .setMessage("Hapus tabungan \"" + goal.getName() + "\"?")
                        .setPositiveButton("Hapus", (d, w) -> {
                            db.deleteSavingsGoal(goal.getId());
                            loadSavings();
                        })
                        .setNegativeButton("Batal", null)
                        .show();
            }
        });

        btnAddSavings.setOnClickListener(v -> showAddSavingsDialog());
    }

    private void setupRecurring(View view) {
        rvRecurring = view.findViewById(R.id.rvRecurring);
        btnAddRecurring = view.findViewById(R.id.btnAddRecurring);
        tvNoRecurring = view.findViewById(R.id.tvNoRecurring);

        recurringAdapter = new RecurringBillAdapter(requireContext(), new ArrayList<>());
        rvRecurring.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecurring.setNestedScrollingEnabled(false);
        rvRecurring.setAdapter(recurringAdapter);

        recurringAdapter.setOnDeleteListener(bill -> {
            new AlertDialog.Builder(requireContext(), R.style.Widget_BudgetKu_Dialog)
                    .setTitle("Hapus Tagihan")
                    .setMessage("Hapus tagihan \"" + bill.getName() + "\"?")
                    .setPositiveButton("Hapus", (d, w) -> {
                        db.deleteRecurringBill(bill.getId());
                        loadRecurring();
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        btnAddRecurring.setOnClickListener(v -> showAddRecurringDialog());
    }

    private void loadAllData() {
        loadBudgets();
        loadSavings();
        loadRecurring();
    }

    private void loadBudgets() {
        List<Budget> budgets = db.getBudgetsByMonth(db.getCurrentMonth());
        budgetAdapter.updateData(budgets);
        tvNoBudgets.setVisibility(budgets.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void loadSavings() {
        List<SavingsGoal> goals = db.getAllSavingsGoals();
        savingsAdapter.updateData(goals);
        tvNoSavings.setVisibility(goals.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void loadRecurring() {
        List<RecurringBill> bills = db.getAllRecurringBills();
        recurringAdapter.updateData(bills);
        tvNoRecurring.setVisibility(bills.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showAddBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.Widget_BudgetKu_Dialog);
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_budget, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_card);

        Spinner spinnerCat = dialogView.findViewById(R.id.spinnerBudgetCategory);
        EditText etLimit = dialogView.findViewById(R.id.etBudgetLimit);
        Button btnSave = dialogView.findViewById(R.id.btnSaveBudget);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelBudget);

        List<Category> categories = db.getCategoriesByType("EXPENSE");
        List<String> catNames = new ArrayList<>();
        for (Category c : categories) catNames.add(c.getIcon() + " " + c.getName());
        spinnerCat.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, catNames));

        btnSave.setOnClickListener(v -> {
            String limitStr = etLimit.getText().toString().trim();
            if (TextUtils.isEmpty(limitStr)) { etLimit.setError("Wajib diisi"); return; }
            try {
                double limit = Double.parseDouble(limitStr);
                String catName = categories.isEmpty() ? "Lainnya" :
                        categories.get(spinnerCat.getSelectedItemPosition()).getName();
                Budget budget = new Budget(catName, limit, db.getCurrentMonth());
                db.addBudget(budget);
                loadBudgets();
                dialog.dismiss();
                Toast.makeText(requireContext(), "✅ Budget ditambahkan!", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) { etLimit.setError("Nilai tidak valid"); }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showAddSavingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.Widget_BudgetKu_Dialog);
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_savings, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_card);

        EditText etName = dialogView.findViewById(R.id.etSavingsName);
        EditText etTarget = dialogView.findViewById(R.id.etSavingsTarget);
        EditText etCurrent = dialogView.findViewById(R.id.etSavingsCurrent);
        EditText etDeadline = dialogView.findViewById(R.id.etSavingsDeadline);
        EditText etIcon = dialogView.findViewById(R.id.etSavingsIcon);
        Button btnSave = dialogView.findViewById(R.id.btnSaveSavings);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelSavings);

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String targetStr = etTarget.getText().toString().trim();
            String currentStr = etCurrent.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(targetStr)) {
                Toast.makeText(requireContext(), "Nama dan target wajib diisi", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                double target = Double.parseDouble(targetStr);
                double current = TextUtils.isEmpty(currentStr) ? 0 : Double.parseDouble(currentStr);
                String icon = etIcon.getText().toString().trim();
                if (TextUtils.isEmpty(icon)) icon = "🎯";
                String deadline = etDeadline.getText().toString().trim();

                SavingsGoal goal = new SavingsGoal(name, target, current, deadline);
                goal.setIcon(icon);
                db.addSavingsGoal(goal);
                loadSavings();
                dialog.dismiss();
                Toast.makeText(requireContext(), "✅ Target tabungan ditambahkan!", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Nilai tidak valid", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showAddMoneyToSavingsDialog(SavingsGoal goal) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(
                android.R.layout.select_dialog_item, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.Widget_BudgetKu_Dialog);
        EditText input = new EditText(requireContext());
        input.setHint("Jumlah yang ditambahkan");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setTextColor(getResources().getColor(R.color.text_primary, null));
        input.setPadding(48, 24, 48, 24);

        builder.setTitle("Tambah Uang ke " + goal.getName())
                .setView(input)
                .setPositiveButton("Tambah", (d, w) -> {
                    String amtStr = input.getText().toString().trim();
                    if (!TextUtils.isEmpty(amtStr)) {
                        try {
                            double addAmount = Double.parseDouble(amtStr);
                            double newAmount = goal.getCurrentAmount() + addAmount;
                            db.updateSavingsAmount(goal.getId(), newAmount);
                            loadSavings();
                            Toast.makeText(requireContext(), "✅ Ditambahkan!", Toast.LENGTH_SHORT).show();
                        } catch (NumberFormatException e) {
                            Toast.makeText(requireContext(), "Nilai tidak valid", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showAddRecurringDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.Widget_BudgetKu_Dialog);
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_recurring, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_card);

        EditText etName = dialogView.findViewById(R.id.etRecurringName);
        EditText etAmount = dialogView.findViewById(R.id.etRecurringAmount);
        EditText etDueDay = dialogView.findViewById(R.id.etRecurringDueDay);
        Spinner spinnerFreq = dialogView.findViewById(R.id.spinnerFrequency);
        Spinner spinnerCat = dialogView.findViewById(R.id.spinnerRecurringCategory);
        Button btnSave = dialogView.findViewById(R.id.btnSaveRecurring);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelRecurring);

        String[] freqs = {"Bulanan", "Mingguan", "Tahunan"};
        spinnerFreq.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, freqs));

        List<Category> cats = db.getCategoriesByType("EXPENSE");
        List<String> catNames = new ArrayList<>();
        for (Category c : cats) catNames.add(c.getName());
        spinnerCat.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, catNames));

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String amtStr = etAmount.getText().toString().trim();
            String dayStr = etDueDay.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(amtStr)) {
                Toast.makeText(requireContext(), "Nama dan jumlah wajib diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amtStr);
                int dueDay = TextUtils.isEmpty(dayStr) ? 1 : Integer.parseInt(dayStr);
                String[] freqCodes = {RecurringBill.FREQ_MONTHLY, RecurringBill.FREQ_WEEKLY, RecurringBill.FREQ_YEARLY};
                String freq = freqCodes[spinnerFreq.getSelectedItemPosition()];
                String cat = cats.isEmpty() ? "Tagihan" : cats.get(spinnerCat.getSelectedItemPosition()).getName();

                RecurringBill bill = new RecurringBill(name, amount, cat, dueDay, freq);
                db.addRecurringBill(bill);
                loadRecurring();
                dialog.dismiss();
                Toast.makeText(requireContext(), "✅ Tagihan rutin ditambahkan!", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Nilai tidak valid", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAllData();
    }
}
