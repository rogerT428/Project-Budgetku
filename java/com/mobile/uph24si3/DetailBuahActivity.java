package com.mobile.uph24si3;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import java.util.List;

public class DetailBuahActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_buah);

        // Ambil data dari Intent
        String nama      = getIntent().getStringExtra("nama");
        String emoji     = getIntent().getStringExtra("emoji");
        String asal      = getIntent().getStringExtra("asal");
        String rasa      = getIntent().getStringExtra("rasa");
        String deskripsi = getIntent().getStringExtra("deskripsi");
        List<String> manfaat = getIntent().getStringArrayListExtra("manfaat");

        // Setup Toolbar + tombol back panah ←
        Toolbar toolbar = findViewById(R.id.toolbarDetail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Detail: " + nama);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);   // tampilkan ←
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Handle tombol back (← di toolbar) — cara modern, tidak deprecated
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();   // tutup activity, kembali ke daftar
            }
        });

        // Tampilkan emoji buah besar
        TextView tvEmoji = findViewById(R.id.tvDetailEmoji);
        tvEmoji.setText(emoji);

        // Tampilkan nama buah
        TextView tvNama = findViewById(R.id.tvDetailNama);
        tvNama.setText(nama != null ? nama : "");

        // Tampilkan asal
        TextView tvAsal = findViewById(R.id.tvDetailAsal);
        tvAsal.setText(asal != null ? asal : "");

        // Tampilkan rasa
        TextView tvRasa = findViewById(R.id.tvDetailRasa);
        tvRasa.setText(rasa != null ? rasa : "");

        // Tampilkan deskripsi
        TextView tvDeskripsi = findViewById(R.id.tvDetailDeskripsi);
        tvDeskripsi.setText(deskripsi != null ? deskripsi : "");

        // Tambahkan manfaat kesehatan secara dinamis (bullet points)
        LinearLayout containerManfaat = findViewById(R.id.containerManfaat);
        if (manfaat != null && !manfaat.isEmpty()) {
            for (String item : manfaat) {
                TextView tvItem = new TextView(this);
                tvItem.setText("• " + item);
                tvItem.setTextSize(14f);
                tvItem.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                tvItem.setLineSpacing(0f, 1.4f);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 8, 0, 8);
                tvItem.setLayoutParams(params);
                containerManfaat.addView(tvItem);
            }
        }
    }

    // Handle klik tombol ← di ActionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();   // kembali ke halaman sebelumnya
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
