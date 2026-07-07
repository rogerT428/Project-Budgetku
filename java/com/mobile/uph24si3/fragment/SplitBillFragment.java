package com.mobile.uph24si3.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.uph24si3.R;
import com.mobile.uph24si3.adapter.SplitBillAdapter;
import com.mobile.uph24si3.adapter.SplitBillMemberAdapter;
import com.mobile.uph24si3.database.DatabaseHelper;
import com.mobile.uph24si3.model.SplitBill;
import com.mobile.uph24si3.model.SplitBillMember;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SplitBillFragment extends Fragment {

    private DatabaseHelper db;
    private RecyclerView rvSplitBills;
    private SplitBillAdapter adapter;
    private Button btnNewSplit;
    private TextView tvNoSplits;
    private NumberFormat currencyFormat;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_split_bill, container, false);

        db = DatabaseHelper.getInstance(requireContext());
        currencyFormat = NumberFormat.getInstance(new Locale("id", "ID"));

        rvSplitBills = view.findViewById(R.id.rvSplitBills);
        btnNewSplit = view.findViewById(R.id.btnNewSplit);
        tvNoSplits = view.findViewById(R.id.tvNoSplits);

        adapter = new SplitBillAdapter(requireContext(), new ArrayList<>());
        rvSplitBills.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSplitBills.setAdapter(adapter);

        adapter.setOnClickListener(new SplitBillAdapter.OnClickListener() {
            @Override
            public void onClick(SplitBill splitBill) {
                showSplitBillDetail(splitBill);
            }

            @Override
            public void onLongClick(SplitBill splitBill) {
                showDeleteSplitDialog(splitBill);
            }
        });

        btnNewSplit.setOnClickListener(v -> showCreateSplitDialog());

        loadSplitBills();
        return view;
    }

    private void loadSplitBills() {
        List<SplitBill> splits = db.getAllSplitBills();
        adapter.updateData(splits);
        tvNoSplits.setVisibility(splits.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showCreateSplitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.Widget_BudgetKu_Dialog);
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_split_bill, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_card);

        EditText etTitle = dialogView.findViewById(R.id.etSplitTitle);
        EditText etTotal = dialogView.findViewById(R.id.etSplitTotal);
        EditText etNotes = dialogView.findViewById(R.id.etSplitNotes);
        EditText etMembers = dialogView.findViewById(R.id.etMemberNames);
        TextView tvPerPerson = dialogView.findViewById(R.id.tvCalculatedPerPerson);
        Button btnCreate = dialogView.findViewById(R.id.btnCreateSplit);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelSplit);

        // Auto-calculate per person as user types
        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    String totalStr = etTotal.getText().toString().trim();
                    String membersStr = etMembers.getText().toString().trim();
                    if (!totalStr.isEmpty() && !membersStr.isEmpty()) {
                        double total = Double.parseDouble(totalStr);
                        String[] names = membersStr.split(",");
                        int count2 = 0;
                        for (String n : names) if (!n.trim().isEmpty()) count2++;
                        if (count2 > 0) {
                            double perPerson = total / count2;
                            tvPerPerson.setText("Per orang: Rp " + currencyFormat.format(perPerson));
                            tvPerPerson.setVisibility(View.VISIBLE);
                        }
                    }
                } catch (Exception e) {}
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        };
        etTotal.addTextChangedListener(watcher);
        etMembers.addTextChangedListener(watcher);

        btnCreate.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String totalStr = etTotal.getText().toString().trim();
            String notes = etNotes.getText().toString().trim();
            String membersStr = etMembers.getText().toString().trim();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(totalStr) || TextUtils.isEmpty(membersStr)) {
                Toast.makeText(requireContext(), "Judul, total, dan anggota wajib diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double total = Double.parseDouble(totalStr);
                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                SplitBill split = new SplitBill(title, total, today);
                split.setNotes(notes);

                // Parse member names (comma-separated)
                String[] memberNames = membersStr.split(",");
                List<SplitBillMember> members = new ArrayList<>();
                int validCount = 0;
                for (String n : memberNames) {
                    if (!n.trim().isEmpty()) validCount++;
                }
                if (validCount == 0) {
                    Toast.makeText(requireContext(), "Masukkan minimal 1 anggota", Toast.LENGTH_SHORT).show();
                    return;
                }
                double perPerson = total / validCount;
                for (String n : memberNames) {
                    String name = n.trim();
                    if (!name.isEmpty()) {
                        members.add(new SplitBillMember(0, name, perPerson));
                    }
                }
                split.setMembers(members);

                db.addSplitBill(split);
                loadSplitBills();
                dialog.dismiss();
                Toast.makeText(requireContext(), "✅ Split bill dibuat!", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Total tidak valid", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showSplitBillDetail(SplitBill splitBill) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.Widget_BudgetKu_Dialog);
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_split_detail, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_card);

        TextView tvTitle = dialogView.findViewById(R.id.tvDetailSplitTitle);
        TextView tvTotal = dialogView.findViewById(R.id.tvDetailSplitTotal);
        TextView tvPerPerson = dialogView.findViewById(R.id.tvDetailPerPerson);
        TextView tvPaidStatus = dialogView.findViewById(R.id.tvDetailPaidStatus);
        RecyclerView rvMembers = dialogView.findViewById(R.id.rvDetailMembers);
        Button btnClose = dialogView.findViewById(R.id.btnDetailClose);

        tvTitle.setText(splitBill.getTitle());
        tvTotal.setText("Total: Rp " + currencyFormat.format(splitBill.getTotalAmount()));
        tvPerPerson.setText("Per orang: Rp " + currencyFormat.format(splitBill.getAmountPerPerson()));
        tvPaidStatus.setText(splitBill.getPaidCount() + "/" + splitBill.getMemberCount() + " sudah bayar");

        SplitBillMemberAdapter memberAdapter = new SplitBillMemberAdapter(requireContext(), splitBill.getMembers());
        memberAdapter.setOnPaidStatusChangeListener((member, isPaid) -> {
            db.updateMemberPaidStatus(member.getId(), isPaid);
            // Reload
            SplitBill updated = db.getAllSplitBills().stream()
                    .filter(s -> s.getId() == splitBill.getId())
                    .findFirst().orElse(null);
            if (updated != null) {
                memberAdapter.updateData(updated.getMembers());
                tvPaidStatus.setText(updated.getPaidCount() + "/" + updated.getMemberCount() + " sudah bayar");
            }
        });

        rvMembers.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMembers.setAdapter(memberAdapter);

        btnClose.setOnClickListener(v -> {
            loadSplitBills();
            dialog.dismiss();
        });
        dialog.show();
    }

    private void showDeleteSplitDialog(SplitBill splitBill) {
        new AlertDialog.Builder(requireContext(), R.style.Widget_BudgetKu_Dialog)
                .setTitle("Hapus Split Bill")
                .setMessage("Hapus \"" + splitBill.getTitle() + "\"?")
                .setPositiveButton("Hapus", (d, w) -> {
                    db.deleteSplitBill(splitBill.getId());
                    loadSplitBills();
                    Toast.makeText(requireContext(), "Split bill dihapus", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSplitBills();
    }
}
