package com.teammirai.spendsmart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teammirai.spendsmart.model.Goal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GoalListActivity extends AppCompatActivity {
    RecyclerView goalListView;
    Adapter goalAdapter;
    ArrayList<Goal> goalList;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private boolean isGuestMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_goal_list);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        goalListView = findViewById(R.id.goalListView);
        goalListView.setLayoutManager(new LinearLayoutManager(this));
        goalList = new ArrayList<>();
        goalAdapter = new Adapter(this, goalList);
        goalListView.setAdapter(goalAdapter);
        ImageButton backButton = findViewById(R.id.backButton_6);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        isGuestMode = sharedPreferences.getBoolean("IsGuestMode", false);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        backButton.setOnClickListener(v -> onBackPressed());

        if (!isGuestMode && currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!isGuestMode) {
            databaseReference = FirebaseDatabase.getInstance().getReference("goals").child(currentUser.getUid());
        }

        fetchGoals();

        Button addGoalButton = findViewById(R.id.addGoalButton);
        addGoalButton.setOnClickListener(v -> {
            Intent intent = new Intent(GoalListActivity.this, AddGoalActivity.class);
            startActivity(intent);
        });
    }

    private void fetchGoals() {
        if (isGuestMode) {
            loadGoalsFromPreferences();
        } else {
            loadGoalsFromFirebase();
        }
    }

    private void loadGoalsFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("GoalsData", MODE_PRIVATE);
        Set<String> goalsSet = sharedPreferences.getStringSet("goals", new HashSet<>());

        goalList.clear();
        for (String goalString : goalsSet) {
            Goal goal = Goal.fromString(goalString);
            if (goal != null) {
                goalList.add(goal);
                Log.d("GoalListActivity", "Loaded goal from preferences: " + goal.toString());
            }
        }
        goalAdapter.notifyDataSetChanged();
    }

    private void loadGoalsFromFirebase() {
        if (databaseReference != null) {
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    goalList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Goal goal = snapshot.getValue(Goal.class);
                        if (goal != null) {
                            goalList.add(goal);
                            Log.d("GoalListActivity", "Loaded goal from Firebase: " + goal.toString());
                        }
                    }
                    goalAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(GoalListActivity.this, "Failed to load goals: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
