package com.teammirai.spendsmart;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final int DELAY_MILLIS = 3000;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private ConnectivityManager connectivityManager;

    private static final String PREFS_NAME = "MyPrefs";
    private static final String IS_LOGGED_IN = "IsLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize ConnectivityManager
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            boolean isLoggedIn = sharedPreferences.getBoolean(IS_LOGGED_IN, false);

            // Check if there's internet connectivity
            if (!isNetworkAvailable()) {
                Toast.makeText(MainActivity.this, "Offline Mode enabled", Toast.LENGTH_SHORT).show();
            }

            Intent intent;
            if (currentUser != null || isLoggedIn) {
                // User is logged in, navigate to MenuActivity
                intent = new Intent(MainActivity.this, MenuActivity.class);
            } else {
                // User is not logged in, navigate to LoginActivity
                intent = new Intent(MainActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish(); // Close MainActivity
        }, DELAY_MILLIS);
    }

    // Method to check if there's internet connectivity
    private boolean isNetworkAvailable() {
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }
}
