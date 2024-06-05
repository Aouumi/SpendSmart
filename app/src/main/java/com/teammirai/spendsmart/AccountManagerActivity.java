package com.teammirai.spendsmart;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class AccountManagerActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_manager);

        mAuth = FirebaseAuth.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton backButton = findViewById(R.id.backButton_4);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(AccountManagerActivity.this, SubMenuActivity.class);
            startActivity(intent);
            finish();
        });

        Button changeNameButton = findViewById(R.id.changeNameButton);
        changeNameButton.setOnClickListener(v -> showInputDialog("Change Username", "Enter new username:", input -> updateUsername(input)));

        Button changePasswordButton = findViewById(R.id.changePasswordButton);
        changePasswordButton.setOnClickListener(v -> showInputDialog("Change Password", "Enter new password:", input -> updatePassword(input)));

        Button changeEmailButton = findViewById(R.id.changeEmailButton);
        changeEmailButton.setOnClickListener(v -> showInputDialog("Change Email", "Enter new email:", input -> updateEmail(input)));

        Button accountDeletionButton = findViewById(R.id.accountDeletionButton);
        accountDeletionButton.setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void showInputDialog(String title, String message, OnInputCompleteListener onCompleteListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String inputValue = input.getText().toString().trim();
            if (!inputValue.isEmpty()) {
                onCompleteListener.onComplete(inputValue);
            } else {
                Toast.makeText(AccountManagerActivity.this, "Input cannot be empty.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Account Deletion")
                .setMessage("Are you sure you want to delete your account?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> deleteAccount())
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void updateUsername(String newUsername) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newUsername)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(AccountManagerActivity.this, "Username updated successfully.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AccountManagerActivity.this, "Failed to update username.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void updatePassword(String newPassword) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(AccountManagerActivity.this, "Password updated successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AccountManagerActivity.this, "Failed to update password.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void updateEmail(String newEmail) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.updateEmail(newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(AccountManagerActivity.this, "Email updated successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AccountManagerActivity.this, "Failed to update email.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(AccountManagerActivity.this, "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(AccountManagerActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(AccountManagerActivity.this, "Failed to delete account.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    interface OnInputCompleteListener {
        void onComplete(String input);
    }
}
