package com.teammirai.spendsmart;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SavedActivity extends AppCompatActivity {

    private EditText edSavedMoney;
    private TextView currentAmountSavedTitle;
    private DatabaseReference databaseReference;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private String currentDate;
    private boolean isGuestMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_saved);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edSavedMoney = findViewById(R.id.edSavedMoney);
        currentAmountSavedTitle = findViewById(R.id.currentAmountSavedTitle);
        Button addSavedButton = findViewById(R.id.addSavedButton);
        Button subtractSavedButton = findViewById(R.id.subtractSavedButton);
        ImageButton backButton = findViewById(R.id.backbutton_10);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        // Check if the user is in Guest Mode
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        isGuestMode = sharedPreferences.getBoolean("IsGuestMode", false);

        if (!isGuestMode && currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!isGuestMode) {
            databaseReference = FirebaseDatabase.getInstance().getReference("savedMoney").child(currentUser.getUid());
            firestore = FirebaseFirestore.getInstance();
        }

        currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        backButton.setOnClickListener(v -> onBackPressed());

        addSavedButton.setOnClickListener(v -> updateSavedMoney(true));
        subtractSavedButton.setOnClickListener(v -> updateSavedMoney(false));

        loadCurrentSavedMoney();
    }

    private void loadCurrentSavedMoney() {
        SharedPreferences sharedPreferences = getSharedPreferences("SavedMoney", MODE_PRIVATE);
        String savedAmount = sharedPreferences.getString(currentDate, "0.00");
        currentAmountSavedTitle.setText(savedAmount);
    }

    private void updateSavedMoney(boolean isAddition) {
        String inputAmountStr = edSavedMoney.getText().toString();
        if (inputAmountStr.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double inputAmount = Double.parseDouble(inputAmountStr);
        double currentAmount = Double.parseDouble(currentAmountSavedTitle.getText().toString());

        if (!isAddition && inputAmount > currentAmount) {
            Toast.makeText(this, "Cannot subtract more than the current saved amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double newAmount = isAddition ? currentAmount + inputAmount : currentAmount - inputAmount;

        currentAmountSavedTitle.setText(String.format(Locale.getDefault(), "%.2f", newAmount));
        saveAmountToLocalAndFirebase(newAmount);

        edSavedMoney.setText("");
    }

    private void saveAmountToLocalAndFirebase(double amount) {
        SharedPreferences sharedPreferences = getSharedPreferences("SavedMoney", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(currentDate, String.format(Locale.getDefault(), "%.2f", amount));
        editor.apply();

        if (!isGuestMode) {
            String userId = currentUser.getUid();

            Map<String, Object> data = new HashMap<>();
            data.put("date", currentDate);
            data.put("amount", amount);

            databaseReference.child(currentDate).setValue(data).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(SavedActivity.this, "Saved money updated in Realtime Database", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SavedActivity.this, "Failed to update saved money in Realtime Database: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            firestore.collection("savedMoney").document(userId).collection("dailySavings").document(currentDate).set(data).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(SavedActivity.this, "Saved money updated in Firestore", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SavedActivity.this, "Failed to update saved money in Firestore: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
