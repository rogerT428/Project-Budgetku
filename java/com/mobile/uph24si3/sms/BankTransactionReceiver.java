package com.mobile.uph24si3.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.mobile.uph24si3.database.DatabaseHelper;
import com.mobile.uph24si3.model.Transaction;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * BankTransactionReceiver - Menerima notifikasi transaksi dari DummyBank App
 * secara real-time dan langsung mencatat ke BudgetKu.
 *
 * Ini mensimulasikan sistem Open Banking seperti yang digunakan oleh
 * Jenius, Flip, dan aplikasi fintech lainnya di Indonesia.
 */
public class BankTransactionReceiver extends BroadcastReceiver {

    public static final String ACTION = "com.budgetku.BANK_TRANSACTION";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ACTION.equals(intent.getAction())) return;

        try {
            String type      = intent.getStringExtra("type");        // INCOME / EXPENSE
            double amount    = intent.getDoubleExtra("amount", 0);
            String desc      = intent.getStringExtra("description");
            String category  = intent.getStringExtra("category");
            String bankName  = intent.getStringExtra("bank_name");

            if (type == null || amount <= 0) return;

            // Format ke kategori yang ada di BudgetKu
            String mappedCategory = mapCategory(category, type);
            
            // Real-time timing dari Bank
            long timestamp = intent.getLongExtra("timestamp", System.currentTimeMillis());
            Date txDateObj = new Date(timestamp);
            String txDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(txDateObj);
            String txTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(txDateObj);
            
            String description = (bankName != null ? "[" + bankName + "] " : "") +
                                 (desc != null ? desc : "Transaksi Bank");

            DatabaseHelper db = DatabaseHelper.getInstance(context);

            // Cek duplikat
            if (!db.isTransactionExists(amount, txDate)) {
                Transaction t = new Transaction(type, amount, mappedCategory, description, txDate, txTime, 0);
                db.addTransaction(t);

                // Notifikasi user
                NumberFormat fmt = NumberFormat.getInstance(new Locale("id", "ID"));
                String typeLabel = "INCOME".equals(type) ? "💚 Pemasukan" : "❤️ Pengeluaran";
                Toast.makeText(context,
                    "🏦 " + typeLabel + " dari " + bankName + "\n" +
                    "Rp " + fmt.format(amount) + " otomatis dicatat!",
                    Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String mapCategory(String category, String type) {
        if (category == null) return "INCOME".equals(type) ? "Lainnya" : "Lainnya";

        String lower = category.toLowerCase();
        if (lower.contains("makan") || lower.contains("food")) return "Makanan & Minum";
        if (lower.contains("transportasi") || lower.contains("transport")) return "Transportasi";
        if (lower.contains("belanja") || lower.contains("shopping")) return "Belanja";
        if (lower.contains("tagihan") || lower.contains("listrik") || lower.contains("utilitas")) return "Tagihan";
        if (lower.contains("kesehatan") || lower.contains("health")) return "Kesehatan";
        if (lower.contains("hiburan") || lower.contains("entertainment")) return "Hiburan";
        if (lower.contains("pendidikan") || lower.contains("education")) return "Pendidikan";
        if (lower.contains("gaji") || lower.contains("salary")) return "Gaji";
        if (lower.contains("freelance")) return "Freelance";
        if (lower.contains("investasi") || lower.contains("investment")) return "Investasi";
        return "Lainnya";
    }
}
