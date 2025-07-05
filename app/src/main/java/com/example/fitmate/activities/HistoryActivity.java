package com.example.fitmate.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitmate.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView tvEmail, historyTextView;
    private Button btnClearHistory;
    private String email = "";

    private static final String TAG = "HistoryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        db = FirebaseFirestore.getInstance();

        tvEmail = findViewById(R.id.tvEmail);
        historyTextView = findViewById(R.id.historyTextView);
        btnClearHistory = findViewById(R.id.btnClearHistory);

        email = getIntent().getStringExtra("USER_EMAIL");

        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "User email not found", Toast.LENGTH_SHORT).show();
            historyTextView.setText("No email found for current user.");
            btnClearHistory.setEnabled(false);
            return;
        }

        fetchUserName(email);
        fetchHistoryData(email);

        btnClearHistory.setOnClickListener(v -> clearAllHistory(email));
    }

    private void fetchUserName(String userEmail) {
        db.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        String name = doc.getString("name");
                        if (name != null && !name.isEmpty()) {
                            tvEmail.setText("History for: " + name);
                        } else {
                            tvEmail.setText("History");
                        }
                    } else {
                        tvEmail.setText("History");
                    }
                })
                .addOnFailureListener(e -> {
                    tvEmail.setText("History");
                    Log.e(TAG, "Failed to fetch user name", e);
                });
    }

    private void fetchHistoryData(String userEmail) {
        db.collection("History")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    if (querySnapshots.isEmpty()) {
                        historyTextView.setText("No history data found.");
                        return;
                    }

                    displayHistory(querySnapshots.getDocuments());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch history", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Firestore error: ", e);
                });
    }

    private void displayHistory(List<DocumentSnapshot> docs) {
        StringBuilder builder = new StringBuilder();
        int index = 1;

        for (DocumentSnapshot doc : docs) {
            builder.append(index).append(".\n")
                    .append("📅 Date: ").append(getSafeString(doc, "date")).append("\n")
                    .append("🎂 Age: ").append(getSafeLong(doc, "age")).append("\n")
                    .append("⚖️ Weight: ").append(getSafeDouble(doc, "weight")).append(" kg\n")
                    .append("📏 Height: ").append(getSafeDouble(doc, "height")).append(" cm\n")
                    .append("🧮 BMI: ").append(getSafeDouble(doc, "bmi")).append("\n")
                    .append("📊 Status: ").append(getSafeString(doc, "bmiStatus")).append("\n")
                    .append("🦠 Health Conditions:\n");

            boolean hasAny = false;

            if (Boolean.TRUE.equals(doc.getBoolean("hasHeartDisease"))) {
                builder.append("   ❤️ Heart Disease\n");
                hasAny = true;
            }
            if (Boolean.TRUE.equals(doc.getBoolean("hasDiabetes"))) {
                builder.append("   🍬 Diabetes\n");
                hasAny = true;
            }
            if (Boolean.TRUE.equals(doc.getBoolean("hasCancer"))) {
                builder.append("   🎗️ Cancer\n");
                hasAny = true;
            }
            if (Boolean.TRUE.equals(doc.getBoolean("hasCholesterol"))) {
                builder.append("   🧈 Cholesterol\n");
                hasAny = true;
            }

            if (!hasAny) {
                builder.append("   ✅ No known conditions\n");
            }

            builder.append("---------------------------------------------\n");
            index++;
        }

        historyTextView.setText(builder.toString());
    }

    private void clearAllHistory(String userEmail) {
        db.collection("History")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    if (querySnapshots.isEmpty()) {
                        Toast.makeText(this, "No history to clear.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (DocumentSnapshot doc : querySnapshots.getDocuments()) {
                        db.collection("History").document(doc.getId()).delete();
                    }
                    Toast.makeText(this, "All history cleared.", Toast.LENGTH_SHORT).show();
                    historyTextView.setText("No history data found.");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to clear history.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Clear history failed", e);
                });
    }

    // Helper functions for null-safe retrieval
    private String getSafeString(DocumentSnapshot doc, String field) {
        String value = doc.getString(field);
        return (value != null && !value.isEmpty()) ? value : "N/A";
    }

    private long getSafeLong(DocumentSnapshot doc, String field) {
        Long value = doc.getLong(field);
        return (value != null) ? value : 0L;
    }

    private double getSafeDouble(DocumentSnapshot doc, String field) {
        Double value = doc.getDouble(field);
        return (value != null) ? value : 0.0;
    }
}
