package com.teammirai.spendsmart;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AddGoalActivity extends AppCompatActivity {

    private EditText edGoalName, edDate, edTime, edAmount;
    private DatabaseReference databaseReference;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private boolean isGuestMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_goal);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        isGuestMode = sharedPreferences.getBoolean("IsGuestMode", false);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        if (!isGuestMode && currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!isGuestMode) {
            databaseReference = FirebaseDatabase.getInstance().getReference("goals").child(currentUser.getUid());
            firestore = FirebaseFirestore.getInstance();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edGoalName = findViewById(R.id.edgoalName);
        edDate = findViewById(R.id.edDate);
        edTime = findViewById(R.id.edTime);
        edAmount = findViewById(R.id.editTextNumberDecimal);
        Button saveGoalButton = findViewById(R.id.saveGoalButton);

        edDate.setOnClickListener(v -> showDatePickerDialog());
        edTime.setOnClickListener(v -> showTimePickerDialog());

        saveGoalButton.setOnClickListener(v -> saveGoal());
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> {
            String selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year1);
            edDate.setText(selectedDate);
        }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            int hour12 = hourOfDay % 12 == 0 ? 12 : hourOfDay % 12;
            String amPm = hourOfDay < 12 ? "AM" : "PM";
            String selectedTime = String.format("%02d:%02d %s", hour12, minute1, amPm);
            edTime.setText(selectedTime);
        }, hour, minute, false);
        timePickerDialog.show();
    }

    private void saveGoal() {
        String goalName = edGoalName.getText().toString();
        String date = edDate.getText().toString();
        String time = edTime.getText().toString();
        String amount = edAmount.getText().toString();

        if (goalName.isEmpty() || date.isEmpty() || time.isEmpty() || amount.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isGuestMode) {
            saveGoalLocally(goalName, date, time, amount);
        } else {
            saveGoalToFirebase(goalName, date, time, amount);
        }

        clearFields();
    }

    private void saveGoalLocally(String goalName, String date, String time, String amount) {
        SharedPreferences sharedPreferences = getSharedPreferences("GoalsData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Set<String> goalsSet = sharedPreferences.getStringSet("goals", new HashSet<>());
        goalsSet.add(goalName + ";" + date + ";" + time + ";" + amount);
        editor.putStringSet("goals", goalsSet);
        editor.apply();

        Toast.makeText(this, "Goal saved locally", Toast.LENGTH_SHORT).show();
    }

    private void saveGoalToFirebase(String goalName, String date, String time, String amount) {
        String goalId = databaseReference.push().getKey();
        Goal goal = new Goal(goalName, date, time, amount);
        if (goalId != null) {
            databaseReference.child(goalId).setValue(goal).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(AddGoalActivity.this, "Goal saved to Realtime Database", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddGoalActivity.this, "Failed to save goal to Realtime Database: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }

        Map<String, String> goalData = new HashMap<>();
        goalData.put("goalName", goalName);
        goalData.put("date", date);
        goalData.put("time", time);
        goalData.put("amount", amount);
        firestore.collection("goals").document(currentUser.getUid()).collection("userGoals").add(goalData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(AddGoalActivity.this, "Goal saved to Firestore", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(AddGoalActivity.this, "Failed to save goal to Firestore: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void clearFields() {
        edGoalName.setText("");
        edDate.setText("");
        edTime.setText("");
        edAmount.setText("");
    }

    public static class Goal {
        public String goalName;
        public String date;
        public String time;
        public String amount;

        public Goal() {
            // Default constructor required for calls to DataSnapshot.getValue(Goal.class)
        }

        public Goal(String goalName, String date, String time, String amount) {
            this.goalName = goalName;
            this.date = date;
            this.time = time;
            this.amount = amount;
        }

        public static Goal fromString(String goalString) {
            String[] parts = goalString.split(";");
            if (parts.length == 4) {
                return new Goal(parts[0], parts[1], parts[2], parts[3]);
            } else {
                return null;
            }
        }

        @Override
        public String toString() {
            return goalName + ";" + date + ";" + time + ";" + amount;
        }
    }
}
