package com.paragon.control;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    Switch swLockScreen;
    Button btnGanti, btnSetPin;
    EditText etNewPin;
    OkHttpClient client;
    private static final String API_URL = "https://paragon.pythonanywhere.com/api";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client = new OkHttpClient();

        swLockScreen = findViewById(R.id.swLockScreen);
        btnGanti = findViewById(R.id.btnGanti);
        btnSetPin = findViewById(R.id.btnSetPin);
        etNewPin = findViewById(R.id.etNewPin);

        fetchStatus();

        swLockScreen.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sendLockCommand(isChecked);
        });

        btnSetPin.setOnClickListener(v -> {
            String newPin = etNewPin.getText().toString().trim();
            if (newPin.length() == 4) {
                sendPinCommand(newPin);
            } else {
                Toast.makeText(this, "PIN harus 4 digit!", Toast.LENGTH_SHORT).show();
            }
        });

        btnGanti.setOnClickListener(v -> {
            Toast.makeText(this, "Ganti target", Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchStatus() {
        Request request = new Request.Builder()
                .url(API_URL + "/status")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject obj = new JSONObject(response.body().string());
                        boolean status = obj.getBoolean("lock_status");
                        runOnUiThread(() -> swLockScreen.setChecked(status));
                    } catch (Exception e) {}
                }
            }
        });
    }

    private void sendLockCommand(boolean status) {
        try {
            JSONObject json = new JSONObject();
            json.put("lock_status", status);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(API_URL + "/lock")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Gagal kirim perintah!", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this,
                            status ? "🔴 Lock ON" : "🟢 Lock OFF", Toast.LENGTH_SHORT).show());
                }
            });
        } catch (Exception e) {}
    }

    private void sendPinCommand(String newPin) {
        try {
            JSONObject json = new JSONObject();
            json.put("pin", newPin);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(API_URL + "/pin")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Gagal ganti PIN!", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this,
                            "✅ PIN berhasil diubah!", Toast.LENGTH_SHORT).show());
                }
            });
        } catch (Exception e) {}
    }
                                                       }
