package com.mobile.uph24si3.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.uph24si3.R;
import com.mobile.uph24si3.model.Transaction;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Transaction transaction);
        void onItemLongClick(Transaction transaction);
    }

    private List<Transaction> transactions;
    private Context context;
    private OnItemClickListener listener;
    private NumberFormat currencyFormat;

    public TransactionAdapter(Context context, List<Transaction> transactions) {
        this.context = context;
        this.transactions = transactions;
        this.currencyFormat = NumberFormat.getInstance(new Locale("id", "ID"));
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<Transaction> newTransactions) {
        this.transactions = newTransactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction t = transactions.get(position);

        holder.tvCategory.setText(t.getCategory() != null ? t.getCategory() : "Lainnya");
        holder.tvDescription.setText(t.getDescription() != null && !t.getDescription().isEmpty()
                ? t.getDescription() : t.getCategory());
        
        // Show Date + Time for real-time feel
        String dateTime = formatDate(t.getDate());
        if (t.getTime() != null && t.getTime().length() >= 5) {
            dateTime += " • " + t.getTime().substring(0, 5); // Ambil HH:mm
        }
        holder.tvDate.setText(dateTime);

        // Amount with + or -
        String prefix = t.isIncome() ? "+" : "-";
        String amountStr = prefix + "Rp " + currencyFormat.format(t.getAmount());
        holder.tvAmount.setText(amountStr);
        holder.tvAmount.setTextColor(t.isIncome()
                ? Color.parseColor("#52C41A")
                : Color.parseColor("#FF4D4F"));

        // Category icon (first letter or emoji)
        String category = t.getCategory() != null ? t.getCategory() : "L";
        holder.tvIcon.setText(getCategoryEmoji(category));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(t);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onItemLongClick(t);
            return true;
        });
    }

    private String getCategoryEmoji(String category) {
        String lower = category.toLowerCase();
        if (lower.contains("makan") || lower.contains("food")) return "🍔";
        if (lower.contains("transport")) return "🚗";
        if (lower.contains("belanja") || lower.contains("shop")) return "🛍️";
        if (lower.contains("kesehatan") || lower.contains("health")) return "💊";
        if (lower.contains("hiburan") || lower.contains("entertain")) return "🎮";
        if (lower.contains("pendidikan") || lower.contains("edu")) return "📚";
        if (lower.contains("tagihan") || lower.contains("bill")) return "💡";
        if (lower.contains("rumah") || lower.contains("home")) return "🏠";
        if (lower.contains("gaji") || lower.contains("salary")) return "💼";
        if (lower.contains("freelance")) return "💻";
        if (lower.contains("invest")) return "📈";
        return "📦";
    }

    private String formatDate(String date) {
        if (date == null || date.length() < 10) return date;
        String[] parts = date.split("-");
        if (parts.length >= 3) {
            String[] months = {"", "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
                    "Jul", "Agu", "Sep", "Okt", "Nov", "Des"};
            try {
                int month = Integer.parseInt(parts[1]);
                return parts[2] + " " + months[month] + " " + parts[0];
            } catch (Exception e) { return date; }
        }
        return date;
    }

    @Override
    public int getItemCount() { return transactions != null ? transactions.size() : 0; }

    public Transaction getItem(int position) { return transactions.get(position); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvCategory, tvDescription, tvAmount, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvTransactionIcon);
            tvCategory = itemView.findViewById(R.id.tvTransactionCategory);
            tvDescription = itemView.findViewById(R.id.tvTransactionDescription);
            tvAmount = itemView.findViewById(R.id.tvTransactionAmount);
            tvDate = itemView.findViewById(R.id.tvTransactionDate);
        }
    }
}
