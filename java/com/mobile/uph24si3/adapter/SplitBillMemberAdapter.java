package com.mobile.uph24si3.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.uph24si3.R;
import com.mobile.uph24si3.model.SplitBillMember;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class SplitBillMemberAdapter extends RecyclerView.Adapter<SplitBillMemberAdapter.ViewHolder> {

    public interface OnPaidStatusChangeListener {
        void onPaidStatusChanged(SplitBillMember member, boolean isPaid);
    }

    private List<SplitBillMember> members;
    private Context context;
    private OnPaidStatusChangeListener listener;
    private NumberFormat currencyFormat;
    private boolean isEditable = true;

    public SplitBillMemberAdapter(Context context, List<SplitBillMember> members) {
        this.context = context;
        this.members = members;
        this.currencyFormat = NumberFormat.getInstance(new Locale("id", "ID"));
    }

    public void setOnPaidStatusChangeListener(OnPaidStatusChangeListener listener) {
        this.listener = listener;
    }

    public void setEditable(boolean editable) {
        this.isEditable = editable;
    }

    public void updateData(List<SplitBillMember> newMembers) {
        this.members = newMembers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_split_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SplitBillMember member = members.get(position);

        holder.tvName.setText(member.getName());
        holder.tvAmount.setText("Rp " + currencyFormat.format(member.getAmountOwed()));

        // Avatar initials
        String name = member.getName();
        holder.tvAvatar.setText(name.length() > 0 ? String.valueOf(name.charAt(0)).toUpperCase() : "?");

        if (member.isPaid()) {
            holder.tvStatus.setText("✅ Sudah Bayar");
            holder.tvStatus.setTextColor(Color.parseColor("#52C41A"));
            holder.btnTogglePaid.setText("Batal");
            holder.btnTogglePaid.setBackgroundColor(Color.parseColor("#21262D"));
        } else {
            holder.tvStatus.setText("⏳ Belum Bayar");
            holder.tvStatus.setTextColor(Color.parseColor("#FAAD14"));
            holder.btnTogglePaid.setText("Tandai Lunas");
            holder.btnTogglePaid.setBackgroundColor(Color.parseColor("#00D4AA"));
        }

        if (isEditable) {
            holder.btnTogglePaid.setVisibility(View.VISIBLE);
            holder.btnTogglePaid.setOnClickListener(v -> {
                boolean newStatus = !member.isPaid();
                if (listener != null) listener.onPaidStatusChanged(member, newStatus);
            });
        } else {
            holder.btnTogglePaid.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return members != null ? members.size() : 0; }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvName, tvAmount, tvStatus;
        Button btnTogglePaid;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvMemberAvatar);
            tvName = itemView.findViewById(R.id.tvMemberName);
            tvAmount = itemView.findViewById(R.id.tvMemberAmount);
            tvStatus = itemView.findViewById(R.id.tvMemberStatus);
            btnTogglePaid = itemView.findViewById(R.id.btnTogglePaid);
        }
    }
}
