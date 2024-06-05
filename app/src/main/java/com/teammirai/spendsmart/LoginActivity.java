package com.teammirai.spendsmart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private static final int BACK_PRESS_INTERVAL = 2000; // 2 seconds
    private long lastBackPressTime = 0;
    private Toast backPressToast;
    TextInputEditText edUserName, edPassword;
    FirebaseAuth mAuth;
    SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "MyPrefs";
    private static final String IS_LOGGED_IN = "IsLoggedIn";
    private static final String IS_GUEST_MODE = "IsGuestMode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Check if user is already logged in or in guest mode
        if (mAuth.getCurrentUser() != null || sharedPreferences.getBoolean(IS_LOGGED_IN, false) || sharedPreferences.getBoolean(IS_GUEST_MODE, false)) {
            // Navigate to MenuActivity
            Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
            startActivity(intent);
            finish(); // Close LoginActivity
            return;
        }

        edUserName = findViewById(R.id.edUserName);
        edPassword = findViewById(R.id.edPassword);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup click listener for Sign Up text
        TextView signUpText = findViewById(R.id.textSignUp);
        signUpText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        // Setup click listener for Login button
        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> {
            String email, password;
            email = String.valueOf(edUserName.getText());
            password = String.valueOf(edPassword.getText());

            if (TextUtils.isEmpty(email)){
                Toast.makeText(LoginActivity.this, "Enter email.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)){
                Toast.makeText(LoginActivity.this, "Enter password.", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Save login state
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean(IS_LOGGED_IN, true);
                                editor.apply();

                                Toast.makeText(LoginActivity.this, "Login Successful.", Toast.LENGTH_SHORT).show();

                                // Navigate to MenuActivity
                                Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                                startActivity(intent);
                                finish(); // Close LoginActivity
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

        // Setup click listener for Guest Mode button
        Button guestModeButton = findViewById(R.id.guestModeButton);
        guestModeButton.setOnClickListener(v -> {
            // Save guest mode state
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(IS_GUEST_MODE, true);
            editor.apply();

            // Navigate to MenuActivity
            Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
            startActivity(intent);
            finish(); // Close LoginActivity
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastBackPressTime < BACK_PRESS_INTERVAL) {
                    if (backPressToast != null) {
                        backPressToast.cancel();
                    }
                    finishAffinity();
                } else {
                    backPressToast = Toast.makeText(LoginActivity.this, "Tap the back button again to exit", Toast.LENGTH_SHORT);
                    backPressToast.show();
                    lastBackPressTime = currentTime;
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
}
