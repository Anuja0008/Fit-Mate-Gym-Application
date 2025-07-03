package com.example.fitmate.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitmate.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HistoryActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView tvEmail, historyTextView;
    private String email = "";

    private static final String TAG = "HistoryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        db = FirebaseFirestore.getInstance();

        tvEmail = findViewById(R.id.tvEmail);
        historyTextView = findViewById(R.id.historyTextView);

        // Get email from intent
        email = getIntent().getStringExtra("USER_EMAIL");

        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "User email not found", Toast.LENGTH_SHORT).show();
            historyTextView.setText("No email found for current user.");
            return;
        }

        // Fetch user name using the "email" field from users collection
        fetchUserName(email);

        // Fetch history data for this user
        fetchHistoryData(email);
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
                            tvEmail.setText("History for: " + name + "\n(" + userEmail + ")");
                        } else {
                            tvEmail.setText("History for: " + userEmail);
                        }
                    } else {
                        tvEmail.setText("History for: " + userEmail);
                    }
                })
                .addOnFailureListener(e -> {
                    tvEmail.setText("History for: " + userEmail);
                    Log.e(TAG, "Failed to fetch user name", e);
                });
    }

    private void fetchHistoryData(String userEmail) {
        db.collection("History")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    if (querySnapshots.isEmpty()) {
                        historyTextView.setText("No history data found for " + userEmail);
                        return;
                    }

                    StringBuilder builder = new StringBuilder();
                    for (DocumentSnapshot doc : querySnapshots) {
                        builder.append("📅 Date: ").append(doc.getString("date")).append("\n")
                                .append("🎂 Age: ").append(doc.getLong("age")).append("\n")
                                .append("⚖️ Weight: ").append(doc.getDouble("weight")).append(" kg\n")
                                .append("📏 Height: ").append(doc.getDouble("height")).append(" cm\n")
                                .append("🧮 BMI: ").append(doc.getDouble("bmi")).append("\n")
                                .append("📊 Status: ").append(doc.getString("bmiStatus")).append("\n")
                                .append("🦠 Disease 1: ").append(doc.getString("disease1")).append("\n")
                                .append("🦠 Disease 2: ").append(doc.getString("disease2")).append("\n")
                                .append("🦠 Disease 3: ").append(doc.getString("disease3")).append("\n")
                                .append("🦠 Disease 4: ").append(doc.getString("disease4")).append("\n")
                                .append("---------------------------------------------\n");
                    }
                    historyTextView.setText(builder.toString());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch history", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Firestore error: ", e);
                });
    }
}
