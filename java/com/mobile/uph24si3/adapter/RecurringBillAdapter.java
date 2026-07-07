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
import com.mobile.uph24si3.model.RecurringBill;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class RecurringBillAdapter extends RecyclerView.Adapter<RecurringBillAdapter.ViewHolder> {

    public interface OnDeleteListener {
        void onDelete(RecurringBill bill);
    }

    private List<RecurringBill> bills;
    private Context context;
    private OnDeleteListener deleteListener;
    private NumberFormat currencyFormat;

    public RecurringBillAdapter(Context context, List<RecurringBill> bills) {
        this.context = context;
        this.bills = bills;
        this.currencyFormat = NumberFormat.getInstance(new Locale("id", "ID"));
    }

    public void setOnDeleteListener(OnDeleteListener listener) {
        this.deleteListener = listener;
    }

    public void updateData(List<RecurringBill> newBills) {
        this.bills = newBills;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recurring_bill, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecurringBill bill = bills.get(position);

        holder.tvName.setText(bill.getName());
        holder.tvAmount.setText("Rp " + currencyFormat.format(bill.getAmount()));
        holder.tvCategory.setText(bill.getCategory() != null ? bill.getCategory() : "Tagihan");
        holder.tvDueDay.setText("Jatuh tempo: tgl " + bill.getDueDay());
        holder.tvFrequency.setText(getFrequencyLabel(bill.getFrequency()));

        if (bill.isPaid()) {
            holder.tvPaidStatus.setText("✅ Lunas bulan ini");
            holder.tvPaidStatus.setTextColor(Color.parseColor("#52C41A"));
        } else {
            holder.tvPaidStatus.setText("⏳ Belum dibayar");
            holder.tvPaidStatus.setTextColor(Color.parseColor("#FAAD14"));
        }

        holder.itemView.setOnLongClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(bill);
            return true;
        });
    }

    private String getFrequencyLabel(String freq) {
        if (freq == null) return "Bulanan";
        switch (freq) {
            case "MONTHLY": return "📅 Bulanan";
            case "WEEKLY": return "📅 Mingguan";
            case "YEARLY": return "📅 Tahunan";
            default: return "📅 Bulanan";
        }
    }

    @Override
    public int getItemCount() { return bills != null ? bills.size() : 0; }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAmount, tvCategory, tvDueDay, tvFrequency, tvPaidStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvBillName);
            tvAmount = itemView.findViewById(R.id.tvBillAmount);
            tvCategory = itemView.findViewById(R.id.tvBillCategory);
            tvDueDay = itemView.findViewById(R.id.tvBillDueDay);
            tvFrequency = itemView.findViewById(R.id.tvBillFrequency);
            tvPaidStatus = itemView.findViewById(R.id.tvBillPaidStatus);
        }
    }
}
