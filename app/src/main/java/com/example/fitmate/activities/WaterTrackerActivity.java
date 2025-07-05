package com.example.fitmate.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitmate.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class WaterTrackerActivity extends AppCompatActivity {

    private TextView tvWaterAmount;
    private Button btnAdd100, btnAdd250, btnAdd500, btnReset;

    private int currentWaterAmount = 0;
    private FirebaseFirestore firestore;
    private String userEmail = "user@example.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_tracker);


        tvWaterAmount = findViewById(R.id.tvWaterAmount);
        btnAdd100 = findViewById(R.id.btnAdd100);
        btnAdd250 = findViewById(R.id.btnAdd250);
        btnAdd500 = findViewById(R.id.btnAdd500);
        btnReset = findViewById(R.id.btnReset);

        // Get email from intent or fallback
        String emailFromIntent = getIntent().getStringExtra("USER_EMAIL");
        if (emailFromIntent != null && !emailFromIntent.isEmpty()) {
            userEmail = emailFromIntent;
        }

        firestore = FirebaseFirestore.getInstance();


        loadWaterAmount();


        btnAdd100.setOnClickListener(v -> {
            currentWaterAmount += 100;
            updateUI();
            saveWaterAmount();
        });

        btnAdd250.setOnClickListener(v -> {
            currentWaterAmount += 250;
            updateUI();
            saveWaterAmount();
        });

        btnAdd500.setOnClickListener(v -> {
            currentWaterAmount += 500;
            updateUI();
            saveWaterAmount();
        });

        btnReset.setOnClickListener(v -> {
            currentWaterAmount = 0;
            updateUI();
            saveWaterAmount();
        });
    }

    private void updateUI() {
        tvWaterAmount.setText("Cosumed : " + currentWaterAmount + " ml");
    }

    private void loadWaterAmount() {
        DocumentReference docRef = firestore.collection("value").document(userEmail);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long savedAmount = documentSnapshot.getLong("water count");
                        if (savedAmount != null) {
                            currentWaterAmount = savedAmount.intValue();
                        } else {
                            currentWaterAmount = 0;
                        }
                    } else {
                        currentWaterAmount = 0;
                    }
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(WaterTrackerActivity.this, "Failed to load water data", Toast.LENGTH_SHORT).show();
                    currentWaterAmount = 0;
                    updateUI();
                });
    }

    private void saveWaterAmount() {
        Map<String, Object> data = new HashMap<>();
        data.put("water count", currentWaterAmount);

        DocumentReference docRef = firestore.collection("value").document(userEmail);
        docRef.set(data)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Water intake saved", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save water intake", Toast.LENGTH_SHORT).show());
    }
}
