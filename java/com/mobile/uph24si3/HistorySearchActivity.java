package com.mobile.uph24si3;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.uph24si3.adapter.TransactionAdapter;
import com.mobile.uph24si3.database.DatabaseHelper;
import com.mobile.uph24si3.model.Transaction;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistorySearchActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private RecyclerView rvHistory;
    private TransactionAdapter adapter;
    private TextView tvIncome, tvExpense, tvDateDisplay, tvEmpty;
    private Button btnSelectDate;
    private NumberFormat currencyFormat;
    private String selectedDate; // yyyy-MM-dd

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_search);

        db = DatabaseHelper.getInstance(this);
        currencyFormat = NumberFormat.getInstance(new Locale("id", "ID"));

        initViews();
        setupRecyclerView();
        
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        updateDateDisplay();
        loadData();
    }

    private void initViews() {
        rvHistory = findViewById(R.id.rvHistorySearch);
        tvIncome = findViewById(R.id.tvHistoryIncome);
        tvExpense = findViewById(R.id.tvHistoryExpense);
        tvDateDisplay = findViewById(R.id.tvSelectedDateDisplay);
        tvEmpty = findViewById(R.id.tvHistoryEmpty);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        ImageButton btnBack = findViewById(R.id.btnBackHistory);

        btnBack.setOnClickListener(v -> finish());
        btnSelectDate.setOnClickListener(v -> showDatePicker());
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(this, new ArrayList<>());
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(adapter);
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date d = sdf.parse(selectedDate);
            if (d != null) c.setTime(d);
        } catch (Exception ignored) {}

        DatePickerDialog dpd = new DatePickerDialog(this, R.style.Widget_BudgetKu_Dialog,
                (view, year, month, dayOfMonth) -> {
                    Calendar sel = Calendar.getInstance();
                    sel.set(year, month, dayOfMonth);
                    selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(sel.getTime());
                    updateDateDisplay();
                    loadData();
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    private void updateDateDisplay() {
        String formatted = formatDateDisplay(selectedDate);
        tvDateDisplay.setText("Riwayat transaksi pada " + formatted);
        btnSelectDate.setText("📅  " + formatted);
    }

    private String formatDateDisplay(String dateStr) {
        try {
            SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat to = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
            Date d = from.parse(dateStr);
            return d != null ? to.format(d) : dateStr;
        } catch (Exception e) {
            return dateStr;
        }
    }

    private void loadData() {
        List<Transaction> all = db.getAllTransactions();
        List<Transaction> filtered = new ArrayList<>();
        double totalIncome = 0;
        double totalExpense = 0;

        for (Transaction t : all) {
            if (selectedDate.equals(t.getDate())) {
                filtered.add(t);
                if (t.isIncome()) totalIncome += t.getAmount();
                else totalExpense += t.getAmount();
            }
        }

        // Sort newest time first
        filtered.sort((t1, t2) -> {
            String time1 = t1.getTime() != null ? t1.getTime() : "00:00:00";
            String time2 = t2.getTime() != null ? t2.getTime() : "00:00:00";
            return time2.compareTo(time1);
        });

        adapter.updateData(filtered);
        tvIncome.setText("Rp " + currencyFormat.format(totalIncome));
        tvExpense.setText("Rp " + currencyFormat.format(totalExpense));
        
        if (filtered.isEmpty()) {
            tvEmpty.setText("Tidak ada transaksi pada " + formatDateDisplay(selectedDate));
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }
}
