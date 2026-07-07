package com.mobile.uph24si3.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.uph24si3.R;
import com.mobile.uph24si3.model.Budget;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {

    public interface OnDeleteListener {
        void onDelete(Budget budget);
    }

    private List<Budget> budgets;
    private Context context;
    private OnDeleteListener deleteListener;
    private NumberFormat currencyFormat;

    public BudgetAdapter(Context context, List<Budget> budgets) {
        this.context = context;
        this.budgets = budgets;
        this.currencyFormat = NumberFormat.getInstance(new Locale("id", "ID"));
    }

    public void setOnDeleteListener(OnDeleteListener listener) {
        this.deleteListener = listener;
    }

    public void updateData(List<Budget> newBudgets) {
        this.budgets = newBudgets;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_budget_goal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Budget budget = budgets.get(position);

        holder.tvCategory.setText(budget.getCategory());
        holder.tvLimit.setText("Batas: Rp " + currencyFormat.format(budget.getMonthlyLimit()));
        holder.tvUsed.setText("Rp " + currencyFormat.format(budget.getUsedAmount()));
        holder.tvRemaining.setText("Sisa: Rp " + currencyFormat.format(budget.getRemainingAmount()));

        int progress = (int) Math.min(budget.getProgressPercent(), 100);
        holder.progressBar.setProgress(progress);

        String percentStr = String.format(Locale.getDefault(), "%.0f%%", budget.getProgressPercent());
        holder.tvPercent.setText(percentStr);

        // Color based on status
        if (budget.isOverBudget()) {
            holder.progressBar.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#FF4D4F")));
            holder.tvUsed.setTextColor(Color.parseColor("#FF4D4F"));
            holder.tvStatus.setText("⚠️ Melebihi Budget!");
            holder.tvStatus.setTextColor(Color.parseColor("#FF4D4F"));
            holder.tvStatus.setVisibility(View.VISIBLE);
        } else if (budget.isNearLimit()) {
            holder.progressBar.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#FAAD14")));
            holder.tvUsed.setTextColor(Color.parseColor("#FAAD14"));
            holder.tvStatus.setText("⚡ Mendekati Batas");
            holder.tvStatus.setTextColor(Color.parseColor("#FAAD14"));
            holder.tvStatus.setVisibility(View.VISIBLE);
        } else {
            holder.progressBar.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#00D4AA")));
            holder.tvUsed.setTextColor(Color.parseColor("#F0F6FC"));
            holder.tvStatus.setVisibility(View.GONE);
        }

        holder.itemView.setOnLongClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(budget);
            return true;
        });
    }

    @Override
    public int getItemCount() { return budgets != null ? budgets.size() : 0; }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvLimit, tvUsed, tvRemaining, tvPercent, tvStatus;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvBudgetCategory);
            tvLimit = itemView.findViewById(R.id.tvBudgetLimit);
            tvUsed = itemView.findViewById(R.id.tvBudgetUsed);
            tvRemaining = itemView.findViewById(R.id.tvBudgetRemaining);
            tvPercent = itemView.findViewById(R.id.tvBudgetPercent);
            tvStatus = itemView.findViewById(R.id.tvBudgetStatus);
            progressBar = itemView.findViewById(R.id.progressBudget);
        }
    }
}
