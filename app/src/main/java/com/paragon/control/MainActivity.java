package com.paragon.control;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    Switch swLockScreen;
    Button btnGanti;
    DatabaseReference mDatabase;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference("target");
        
        swLockScreen = findViewById(R.id.swLockScreen);
        btnGanti = findViewById(R.id.btnGanti);
        
        swLockScreen.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mDatabase.child("lockScreen").setValue(isChecked);
            Toast.makeText(this, "Lock Screen: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });
        
        btnGanti.setOnClickListener(v -> {
            Toast.makeText(this, "Ganti target", Toast.LENGTH_SHORT).show();
        });
    }
}
