package com.teammirai.spendsmart;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MenuActivity extends AppCompatActivity {

    private static final int BACK_PRESS_INTERVAL = 2000; // 2 seconds
    private long lastBackPressTime = 0;
    private Toast backPressToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize buttons
        ImageButton menuButton = findViewById(R.id.menuButton);
        Button goalsButton = findViewById(R.id.goalsButton);
        Button financeButton = findViewById(R.id.financeButton);
        Button historyButton = findViewById(R.id.historyButton);

        // Set click listeners to navigate to respective activities
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, SubMenuActivity.class);
                startActivity(intent);
            }
        });

        goalsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, GoalListActivity.class);
                startActivity(intent);
            }
        });

        financeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, FinanceActivity.class);
                startActivity(intent);
            }
        });

        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        // Handle back press using OnBackPressedDispatcher
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
                    backPressToast = Toast.makeText(MenuActivity.this, "Tap the back button again to exit", Toast.LENGTH_SHORT);
                    backPressToast.show();
                    lastBackPressTime = currentTime;
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
}
