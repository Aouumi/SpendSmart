package com.teammirai.spendsmart;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class SubMenuActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "MyPrefs";
    private static final String IS_LOGGED_IN = "IsLoggedIn";
    private static final String IS_GUEST_MODE = "IsGuestMode";
    private boolean isGuestMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sub_menu);

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Navigation for backButton_1
        ImageButton backButton = findViewById(R.id.backButton_6);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(SubMenuActivity.this, MenuActivity.class);
            startActivity(intent);
        });

        // Navigation for accountManageButton
        Button accountManageButton = findViewById(R.id.accountManageButton);
        accountManageButton.setOnClickListener(v -> {
            Intent intent = new Intent(SubMenuActivity.this, AccountManagerActivity.class);
            startActivity(intent);
        });

        // Navigation for settingsButton
        Button settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(SubMenuActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // Show logout confirmation dialog for LogoutButton
        Button logoutButton = findViewById(R.id.logOutButton);
        logoutButton.setOnClickListener(v -> {
            showLogoutDialog();
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    // Sign out from FirebaseAuth if not in guest mode
                    if (!sharedPreferences.getBoolean(IS_GUEST_MODE, false)) {
                        mAuth.signOut();
                    }

                    // Clear the SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(IS_LOGGED_IN, false);
                    editor.putBoolean(IS_GUEST_MODE, false);
                    editor.apply();

                    // Navigate back to LoginActivity
                    Intent intent = new Intent(SubMenuActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish(); // Optional: Finish the current activity
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
