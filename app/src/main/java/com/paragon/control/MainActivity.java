package com.paragon.control;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
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

        // NAVIGASI
        navBeranda.setOnClickListener(v -> setNavActive(navBeranda));

        navMenu.setOnClickListener(v -> {
            setNavActive(navMenu);
            startActivity(new Intent(this, MenuDetailActivity.class));
        });

        navTools.setOnClickListener(v -> {
            setNavActive(navTools);
            startActivity(new Intent(this, TargetListActivity.class));
        });

        navSetting.setOnClickListener(v -> {
            setNavActive(navSetting);
            startActivity(new Intent(this, SettingActivity.class));
        });

        // GUIDE
        btnGuideTermux.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/syam_guide")));
        });

        btnGuideVps.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/syam_vps")));
        });

        setupMenu();
    }

    private void setNavActive(TextView active) {
        TextView[] navs = {navBeranda, navMenu, navTools, navSetting};
        for (TextView nav : navs) {
            if (nav == active) {
                nav.setTextColor(getColor(R.color.cyan));
                nav.setBackgroundResource(R.drawable.nav_active);
            } else {
                nav.setTextColor(getColor(R.color.gray));
                nav.setBackgroundResource(0);
            }
        }
    }

    private void setupMenu() {
        String[][] menus = {
            {"Bug & Pairing", "Server di HP · Server Lokal", "BUG"}
        };

        for (String[] menu : menus) {
            View item = getLayoutInflater().inflate(R.layout.item_menu, null);
            ((TextView) item.findViewById(R.id.tvMenuTitle)).setText(menu[0]);
            ((TextView) item.findViewById(R.id.tvMenuSub)).setText(menu[1]);
            ((TextView) item.findViewById(R.id.tvMenuBadge)).setText(menu[2]);

            item.setOnClickListener(v -> {
                Intent intent = new Intent(this, MenuDetailActivity.class);
                intent.putExtra("menuTitle", menu[0]);
                intent.putExtra("menuSub", menu[1]);
                intent.putExtra("menuBadge", menu[2]);
                startActivity(intent);
            });

            menuContainer.addView(item);
        }
    }
}
