package com.paragon.control;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private LinearLayout menuContainer;
    private Button btnGuideTermux, btnGuideVps;
    private TextView navBeranda, navMenu, navTools, navSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        menuContainer = findViewById(R.id.menuContainer);
        btnGuideTermux = findViewById(R.id.btnGuideTermux);
        btnGuideVps = findViewById(R.id.btnGuideVps);
        navBeranda = findViewById(R.id.navBeranda);
        navMenu = findViewById(R.id.navMenu);
        navTools = findViewById(R.id.navTools);
        navSetting = findViewById(R.id.navSetting);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference("target");

        // ===== NAVIGASI =====
        navBeranda.setOnClickListener(v -> {
            Toast.makeText(this, "🏠 Beranda", Toast.LENGTH_SHORT).show();
        });

        navMenu.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MenuDetailActivity.class);
            startActivity(intent);
        });

        navTools.setOnClickListener(v -> {
            Toast.makeText(this, "🔧 Tools - RAT", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, ControlActivity.class);
            startActivity(intent);
        });

        navSetting.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent);
        });

        // ===== GUIDE =====
        btnGuideTermux.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://t.me/syam_guide"));
            startActivity(intent);
        });

        btnGuideVps.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://t.me/syam_vps"));
            startActivity(intent);
        });

        // ===== SETUP MENU =====
        setupMenu();
    }

    private void setupMenu() {
        String[][] menus = {
            {"install Ubot", "Termux/VPS - form rebrand", "UBOT"},
            {"Preview Ui", "Termux/VPS - Flutter", "DEV"},
            {"9router + Hermes", "Termux/VPS · Ai stack", "AI"},
            {"install Panel", "Termux/VPS · Panel + Wings", "PANEL"},
            {"Bug & Pairing", "Server di HP · Server Lokal", "BUG"}
        };

        for (String[] menu : menus) {
            View item = getLayoutInflater().inflate(R.layout.item_menu, null);

            TextView tvTitle = item.findViewById(R.id.tvMenuTitle);
            TextView tvSub = item.findViewById(R.id.tvMenuSub);
            TextView tvBadge = item.findViewById(R.id.tvMenuBadge);

            tvTitle.setText(menu[0]);
            tvSub.setText(menu[1]);
            tvBadge.setText(menu[2]);

            item.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, MenuDetailActivity.class);
                intent.putExtra("menuTitle", menu[0]);
                intent.putExtra("menuSub", menu[1]);
                intent.putExtra("menuBadge", menu[2]);
                startActivity(intent);
            });

            menuContainer.addView(item);
        }
    }
}
