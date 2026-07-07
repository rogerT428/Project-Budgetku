package com.mobile.uph24si3.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.uph24si3.R;
import com.mobile.uph24si3.model.SplitBill;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class SplitBillAdapter extends RecyclerView.Adapter<SplitBillAdapter.ViewHolder> {

    public interface OnClickListener {
        void onClick(SplitBill splitBill);
        void onLongClick(SplitBill splitBill);
    }

    private List<SplitBill> splitBills;
    private Context context;
    private OnClickListener listener;
    private NumberFormat currencyFormat;

    public SplitBillAdapter(Context context, List<SplitBill> splitBills) {
        this.context = context;
        this.splitBills = splitBills;
        this.currencyFormat = NumberFormat.getInstance(new Locale("id", "ID"));
    }

    public void setOnClickListener(OnClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<SplitBill> newSplitBills) {
        this.splitBills = newSplitBills;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_split_bill, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SplitBill sb = splitBills.get(position);

        holder.tvTitle.setText(sb.getTitle());
        holder.tvTotal.setText("Total: Rp " + currencyFormat.format(sb.getTotalAmount()));
        holder.tvPerPerson.setText("Per orang: Rp " + currencyFormat.format(sb.getAmountPerPerson()));
        holder.tvMembers.setText(sb.getMemberCount() + " orang");
        holder.tvPaidCount.setText(sb.getPaidCount() + "/" + sb.getMemberCount() + " sudah bayar");
        holder.tvDate.setText(sb.getDate() != null ? sb.getDate() : "");

        if (sb.isSettled()) {
            holder.tvSettled.setText("✅ Selesai");
            holder.tvSettled.setVisibility(View.VISIBLE);
        } else {
            holder.tvSettled.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(sb);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onLongClick(sb);
            return true;
        });
    }

    @Override
    public int getItemCount() { return splitBills != null ? splitBills.size() : 0; }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTotal, tvPerPerson, tvMembers, tvPaidCount, tvDate, tvSettled;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSplitTitle);
            tvTotal = itemView.findViewById(R.id.tvSplitTotal);
            tvPerPerson = itemView.findViewById(R.id.tvSplitPerPerson);
            tvMembers = itemView.findViewById(R.id.tvSplitMembers);
            tvPaidCount = itemView.findViewById(R.id.tvSplitPaidCount);
            tvDate = itemView.findViewById(R.id.tvSplitDate);
            tvSettled = itemView.findViewById(R.id.tvSplitSettled);
        }
    }
}
