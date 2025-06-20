package com.creative.spark.ui.auth;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.button.MaterialButton;
import com.creative.spark.R;
import com.creative.spark.ui.home.SparkHomeActivity;
import com.creative.spark.util.SecurityManager;

public class AuthenticationActivity extends AppCompatActivity {
    private TextInputLayout usernameInputLayout;
    private TextInputLayout passwordInputLayout;
    private MaterialButton signInButton;
    private MaterialButton createAccountButton;
    private ImageView logoImageView;
    private SharedPreferences userPreferences;
    private Animation fadeInAnimation;
    private Animation slideUpAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        
        initializePreferences();
        initializeViews();
        loadAnimations();
        setupClickListeners();
        playEntryAnimations();
    }

    private void initializePreferences() {
        userPreferences = getSharedPreferences("creative_spark_prefs", MODE_PRIVATE);
        
        String activeUser = userPreferences.getString("active_user_id", "");
        if (!activeUser.isEmpty()) {
            navigateToHome();
        }
    }

    private void initializeViews() {
        usernameInputLayout = findViewById(R.id.til_username);
        passwordInputLayout = findViewById(R.id.til_password);
        signInButton = findViewById(R.id.btn_login);
        createAccountButton = findViewById(R.id.btn_register);
        logoImageView = findViewById(R.id.logo_image);
    }

    private void loadAnimations() {
        fadeInAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        slideUpAnimation = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
    }

    private void setupClickListeners() {
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performAuthentication();
            }
        });
        
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToSignUp();
            }
        });
    }

    private void playEntryAnimations() {
        logoImageView.startAnimation(fadeInAnimation);
        usernameInputLayout.startAnimation(slideUpAnimation);
        passwordInputLayout.startAnimation(slideUpAnimation);
        signInButton.startAnimation(slideUpAnimation);
        createAccountButton.startAnimation(slideUpAnimation);
    }

    private void performAuthentication() {
        if (!validateInputs()) {
            return;
        }
        
        String username = usernameInputLayout.getEditText().getText().toString().trim();
        String password = passwordInputLayout.getEditText().getText().toString().trim();
        
        String storedHashedPassword = userPreferences.getString("user_" + username, null);
        if (storedHashedPassword != null) {
            boolean isAuthenticated = SecurityManager.validatePassword(password, storedHashedPassword);
            
            if (isAuthenticated) {
                saveActiveUser(username);
                navigateToHome();
            } else {
                showAuthenticationError();
            }
        } else {
            showAuthenticationError();
        }
    }

    private boolean validateInputs() {
        if (usernameInputLayout.getEditText() == null || passwordInputLayout.getEditText() == null) {
            return false;
        }
        
        String username = usernameInputLayout.getEditText().getText().toString().trim();
        String password = passwordInputLayout.getEditText().getText().toString().trim();
        
        boolean isValid = true;
        
        if (username.isEmpty()) {
            usernameInputLayout.setError(getString(R.string.error_empty_username_password));
            isValid = false;
        } else {
            usernameInputLayout.setError(null);
        }
        
        if (password.isEmpty()) {
            passwordInputLayout.setError(getString(R.string.error_empty_username_password));
            isValid = false;
        } else {
            passwordInputLayout.setError(null);
        }
        
        return isValid;
    }

    private void saveActiveUser(String username) {
        SharedPreferences.Editor editor = userPreferences.edit();
        editor.putString("active_user_id", username);
        editor.apply();
    }

    private void showAuthenticationError() {
        Toast.makeText(this, R.string.error_login_failed, Toast.LENGTH_SHORT).show();
        
        Animation shakeAnimation = AnimationUtils.loadAnimation(this, android.R.anim.cycle_interpolator);
        passwordInputLayout.startAnimation(shakeAnimation);
    }

    private void navigateToHome() {
        Intent homeIntent = new Intent(this, SparkHomeActivity.class);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void navigateToSignUp() {
        Intent signUpIntent = new Intent(this, SignUpActivity.class);
        startActivity(signUpIntent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}