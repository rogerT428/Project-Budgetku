package com.mobile.uph24si3.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.uph24si3.R;
import com.mobile.uph24si3.model.BankAccount;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BankAccountAdapter extends RecyclerView.Adapter<BankAccountAdapter.ViewHolder> {

    public interface OnAccountLongClickListener {
        void onLongClick(BankAccount account);
    }

    private List<BankAccount> accounts;
    private Context context;
    private OnAccountLongClickListener listener;
    private NumberFormat currencyFormat;

    public BankAccountAdapter(Context context, List<BankAccount> accounts) {
        this.context = context;
        this.accounts = accounts;
        this.currencyFormat = NumberFormat.getInstance(new Locale("id", "ID"));
    }

    public void setOnLongClickListener(OnAccountLongClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<BankAccount> newAccounts) {
        this.accounts = newAccounts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bank_account, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BankAccount account = accounts.get(position);

        holder.tvBankName.setText(account.getBankName());
        holder.tvAccountNumber.setText(account.getMaskedAccountNumber());
        holder.tvAccountHolder.setText(account.getAccountHolder());
        holder.tvBalance.setText("Rp " + currencyFormat.format(account.getBalance()));

        // Set bank type emoji/icon
        holder.tvBankIcon.setText(getBankIcon(account.getBankType()));

        // Set card gradient color based on bank color
        try {
            int color = Color.parseColor(account.getColor() != null ? account.getColor() : "#1890FF");
            holder.cardView.setCardBackgroundColor(color);
        } catch (Exception e) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#1890FF"));
        }

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onLongClick(account);
            return true;
        });
    }

    private String getBankIcon(String bankType) {
        if (bankType == null) return "🏦";
        switch (bankType.toUpperCase()) {
            case "BCA": return "🏦";
            case "MANDIRI": return "🏧";
            case "BNI": return "🏛️";
            case "BRI": return "💳";
            case "GOPAY": return "💚";
            case "OVO": return "💜";
            case "DANA": return "💙";
            case "SHOPEEPAY": return "🧡";
            default: return "💳";
        }
    }

    @Override
    public int getItemCount() { return accounts != null ? accounts.size() : 0; }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvBankIcon, tvBankName, tvAccountNumber, tvAccountHolder, tvBalance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardBankAccount);
            tvBankIcon = itemView.findViewById(R.id.tvBankIcon);
            tvBankName = itemView.findViewById(R.id.tvBankName);
            tvAccountNumber = itemView.findViewById(R.id.tvAccountNumber);
            tvAccountHolder = itemView.findViewById(R.id.tvAccountHolder);
            tvBalance = itemView.findViewById(R.id.tvAccountBalance);
        }
    }
}
