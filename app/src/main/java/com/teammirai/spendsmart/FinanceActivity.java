package com.teammirai.spendsmart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FinanceActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;

    private TextView todaySavedAmountLabel;
    private TextView weekSavedAmountLabel;
    private TextView monthSavedAmountLabel;
    private TextView yearSavedAmountLabel;
    private TextView todaySpentAmountLabel;
    private TextView weekSpentAmountLabel;
    private TextView monthSpentAmountLabel;
    private TextView yearSpentAmountLabel;

    private boolean isGuestMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_finance);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Check if the user is in Guest Mode
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        isGuestMode = sharedPreferences.getBoolean("IsGuestMode", false);

        // Initialize Firebase references
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        if (!isGuestMode && currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Find buttons and labels
        Button addSavedMoneyButton = findViewById(R.id.addSavedMoneyButton);
        Button addSpentMoneyButton = findViewById(R.id.addSpentMoneyButton);
        ImageButton backButton = findViewById(R.id.backButton_8);

        todaySavedAmountLabel = findViewById(R.id.todaySavedAmountLabel);
        weekSavedAmountLabel = findViewById(R.id.weekSavedAmountLabel);
        monthSavedAmountLabel = findViewById(R.id.monthSavedAmountLabel);
        yearSavedAmountLabel = findViewById(R.id.yearSavedAmountLabel);
        todaySpentAmountLabel = findViewById(R.id.todaySpentAmountLabel);
        weekSpentAmountLabel = findViewById(R.id.weekSpentAmountLabel);
        monthSpentAmountLabel = findViewById(R.id.monthSpentAmountLabel);
        yearSpentAmountLabel = findViewById(R.id.yearSpentAmountLabel);

        // Set onClick listeners for navigation
        addSavedMoneyButton.setOnClickListener(v -> {
            Intent intent = new Intent(FinanceActivity.this, SavedActivity.class);
            startActivity(intent);
        });

        addSpentMoneyButton.setOnClickListener(v -> {
            Intent intent = new Intent(FinanceActivity.this, SpentActivity.class);
            startActivity(intent);
        });

        backButton.setOnClickListener(v -> onBackPressed());

        // Load data and update UI
        loadAndDisplaySums();
    }

    private void loadAndDisplaySums() {
        if (isGuestMode) {
            loadAndDisplaySumsFromPreferences();
        } else if (currentUser != null) {
            loadAndDisplaySumsFromFirebase();
        }
    }

    private void loadAndDisplaySumsFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("FinanceData", MODE_PRIVATE);

        double todaySaved = Double.longBitsToDouble(sharedPreferences.getLong("todaySaved", 0));
        double weekSaved = Double.longBitsToDouble(sharedPreferences.getLong("weekSaved", 0));
        double monthSaved = Double.longBitsToDouble(sharedPreferences.getLong("monthSaved", 0));
        double yearSaved = Double.longBitsToDouble(sharedPreferences.getLong("yearSaved", 0));

        double todaySpent = Double.longBitsToDouble(sharedPreferences.getLong("todaySpent", 0));
        double weekSpent = Double.longBitsToDouble(sharedPreferences.getLong("weekSpent", 0));
        double monthSpent = Double.longBitsToDouble(sharedPreferences.getLong("monthSpent", 0));
        double yearSpent = Double.longBitsToDouble(sharedPreferences.getLong("yearSpent", 0));

        todaySavedAmountLabel.setText(String.format(Locale.getDefault(), "%.2f", todaySaved));
        weekSavedAmountLabel.setText(String.format(Locale.getDefault(), "%.2f", weekSaved));
        monthSavedAmountLabel.setText(String.format(Locale.getDefault(), "%.2f", monthSaved));
        yearSavedAmountLabel.setText(String.format(Locale.getDefault(), "%.2f", yearSaved));

        todaySpentAmountLabel.setText(String.format(Locale.getDefault(), "%.2f", todaySpent));
        weekSpentAmountLabel.setText(String.format(Locale.getDefault(), "%.2f", weekSpent));
        monthSpentAmountLabel.setText(String.format(Locale.getDefault(), "%.2f", monthSpent));
        yearSpentAmountLabel.setText(String.format(Locale.getDefault(), "%.2f", yearSpent));
    }

    private void loadAndDisplaySumsFromFirebase() {
        String userId = currentUser.getUid();
        firestore.collection("savedMoney").document(userId).collection("dailySavings")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        double todaySum = 0;
                        double weekSum = 0;
                        double monthSum = 0;
                        double yearSum = 0;

                        Date now = new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        String today = sdf.format(now);

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(now);
                        int currentWeek = calendar.get(Calendar.WEEK_OF_YEAR);
                        int currentMonth = calendar.get(Calendar.MONTH);
                        int currentYear = calendar.get(Calendar.YEAR);

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String date = document.getString("date");
                            double amount = document.getDouble("amount");

                            try {
                                Date entryDate = sdf.parse(date);
                                calendar.setTime(entryDate);

                                if (date.equals(today)) {
                                    todaySum += amount;
                                }
                                if (calendar.get(Calendar.WEEK_OF_YEAR) == currentWeek && calendar.get(Calendar.YEAR) == currentYear) {
                                    weekSum += amount;
                                }
                                if (calendar.get(Calendar.MONTH) == currentMonth && calendar.get(Calendar.YEAR) == currentYear) {
                                    monthSum += amount;
                                }
                                if (calendar.get(Calendar.YEAR) == currentYear) {
                                    yearSum += amount;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        todaySavedAmountLabel.setText(String.format(Locale.getDefault(), "%.2f", todaySum));
                        weekSavedAmountLabel.setText(String.format(Locale.getDefault(), "%.2f", weekSum));
                        monthSavedAmountLabel.setText(String.format(Locale.getDefault(), "%.2f", monthSum));
                        yearSavedAmountLabel.setText(String.format(Locale.getDefault(), "%.2f", yearSum));
                    } else {
                        Toast.makeText(FinanceActivity.this, "Failed to load saved money data: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        firestore.collection("spentMoney").document(userId).collection("dailySpendings")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        double todaySum = 0;
                        double weekSum = 0;
                        double monthSum = 0;
                        double yearSum = 0;

                        Date now = new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        String today = sdf.format(now);

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(now);
                        int currentWeek = calendar.get(Calendar.WEEK_OF_YEAR);
                        int currentMonth = calendar.get(Calendar.MONTH);
                        int currentYear = calendar.get(Calendar.YEAR);

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String date = document.getString("date");
                            double amount = document.getDouble("amount");

                            try {
                                Date entryDate = sdf.parse(date);
                                calendar.setTime(entryDate);

                                if (date.equals(today)) {
                                    todaySum += amount;
                                }
                                if (calendar.get(Calendar.WEEK_OF_YEAR) == currentWeek && calendar.get(Calendar.YEAR) == currentYear) {
                                    weekSum += amount;
                                }
                                if (calendar.get(Calendar.MONTH) == currentMonth && calendar.get(Calendar.YEAR) == currentYear) {
                                    monthSum += amount;
                                }
                                if (calendar.get(Calendar.YEAR) == currentYear) {
                                    yearSum += amount;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        todaySpentAmountLabel.setText(String.format(Locale.getDefault(), "%.2f", todaySum));
                        weekSpentAmountLabel.setText(String.format(Locale.getDefault(), "%.2f", weekSum));
                        monthSpentAmountLabel.setText(String.format(Locale.getDefault(), "%.2f", monthSum));
                        yearSpentAmountLabel.setText(String.format(Locale.getDefault(), "%.2f", yearSum));
                    } else {
                        Toast.makeText(FinanceActivity.this, "Failed to load spent money data: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
