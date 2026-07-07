package com.mobile.uph24si3.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.uph24si3.R;
import com.mobile.uph24si3.model.SavingsGoal;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class SavingsAdapter extends RecyclerView.Adapter<SavingsAdapter.ViewHolder> {

    public interface OnSavingsListener {
        void onAddMoney(SavingsGoal goal);
        void onDelete(SavingsGoal goal);
    }

    private List<SavingsGoal> goals;
    private Context context;
    private OnSavingsListener listener;
    private NumberFormat currencyFormat;

    public SavingsAdapter(Context context, List<SavingsGoal> goals) {
        this.context = context;
        this.goals = goals;
        this.currencyFormat = NumberFormat.getInstance(new Locale("id", "ID"));
    }

    public void setListener(OnSavingsListener listener) {
        this.listener = listener;
    }

    public void updateData(List<SavingsGoal> newGoals) {
        this.goals = newGoals;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_savings, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SavingsGoal goal = goals.get(position);

        holder.tvIcon.setText(goal.getIcon() != null ? goal.getIcon() : "🎯");
        holder.tvName.setText(goal.getName());
        holder.tvCurrent.setText("Rp " + currencyFormat.format(goal.getCurrentAmount()));
        holder.tvTarget.setText("/ Rp " + currencyFormat.format(goal.getTargetAmount()));
        holder.tvRemaining.setText("Kurang: Rp " + currencyFormat.format(goal.getRemainingAmount()));
        holder.tvDeadline.setText("Target: " + (goal.getTargetDate() != null ? goal.getTargetDate() : "-"));

        int progress = (int) goal.getProgressPercent();
        holder.progressBar.setProgress(progress);
        holder.tvPercent.setText(progress + "%");

        if (goal.isCompleted() || progress >= 100) {
            holder.tvStatus.setText("✅ Tercapai!");
            holder.tvStatus.setVisibility(View.VISIBLE);
        } else {
            holder.tvStatus.setVisibility(View.GONE);
        }

        holder.btnAddMoney.setOnClickListener(v -> {
            if (listener != null) listener.onAddMoney(goal);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onDelete(goal);
            return true;
        });
    }

    @Override
    public int getItemCount() { return goals != null ? goals.size() : 0; }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvName, tvCurrent, tvTarget, tvRemaining, tvDeadline, tvPercent, tvStatus;
        ProgressBar progressBar;
        View btnAddMoney;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvSavingsIcon);
            tvName = itemView.findViewById(R.id.tvSavingsName);
            tvCurrent = itemView.findViewById(R.id.tvSavingsCurrent);
            tvTarget = itemView.findViewById(R.id.tvSavingsTarget);
            tvRemaining = itemView.findViewById(R.id.tvSavingsRemaining);
            tvDeadline = itemView.findViewById(R.id.tvSavingsDeadline);
            tvPercent = itemView.findViewById(R.id.tvSavingsPercent);
            tvStatus = itemView.findViewById(R.id.tvSavingsStatus);
            progressBar = itemView.findViewById(R.id.progressSavings);
            btnAddMoney = itemView.findViewById(R.id.btnAddMoney);
        }
    }
}
