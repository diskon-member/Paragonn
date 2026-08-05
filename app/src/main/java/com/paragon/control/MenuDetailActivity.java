package com.paragon.control;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MenuDetailActivity extends AppCompatActivity {

    private LinearLayout menuDetailContainer;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_detail);

        menuDetailContainer = findViewById(R.id.menuDetailContainer);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            finish();
        });

        setupMenuDetail();
    }

    private void setupMenuDetail() {
        String[][] menus = {
            {"install Ubot", "Termux/VPS - form rebrand", "UBOT", "Buka install Ubot"},
            {"Preview Ui", "Termux/VPS - Flutter", "DEV", "Buka Preview Ui"},
            {"9router + Hermes", "Termux/VPS · Ai stack", "AI", "Buka 9router + Hermes"},
            {"install Panel", "Termux/VPS · Panel + Wings", "PANEL", "Buka install Panel"},
            {"Bug & Pairing", "Server di HP · Server Lokal", "BUG", "Buka Bug & Pairing"}
        };

        for (String[] menu : menus) {
            View item = getLayoutInflater().inflate(R.layout.item_menu_detail, null);

            TextView tvTitle = item.findViewById(R.id.tvMenuDetailTitle);
            TextView tvSub = item.findViewById(R.id.tvMenuDetailSub);
            TextView tvBadge = item.findViewById(R.id.tvMenuDetailBadge);
            Button btnAction = item.findViewById(R.id.btnMenuAction);

            tvTitle.setText(menu[0]);
            tvSub.setText(menu[1]);
            tvBadge.setText(menu[2]);
            btnAction.setText(menu[3]);

            btnAction.setOnClickListener(v -> {
                Toast.makeText(this, "🔓 Membuka " + menu[0] + "...", Toast.LENGTH_SHORT).show();
            });

            menuDetailContainer.addView(item);
        }
    }
}
