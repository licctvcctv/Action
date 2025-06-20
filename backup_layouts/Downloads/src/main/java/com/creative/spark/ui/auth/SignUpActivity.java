package com.creative.spark.ui.auth;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.creative.spark.R;
import com.creative.spark.ui.home.SparkHomeActivity;
import com.creative.spark.util.SecurityManager;

public class SignUpActivity extends AppCompatActivity {
    private TextInputLayout usernameInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputLayout confirmPasswordInputLayout;
    private MaterialButton createAccountButton;
    private ImageButton backButton;
    private SharedPreferences userPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        
        initializePreferences();
        initializeViews();
        setupClickListeners();
        setupValidation();
    }

    private void initializePreferences() {
        userPreferences = getSharedPreferences("creative_spark_prefs", MODE_PRIVATE);
    }

    private void initializeViews() {
        usernameInputLayout = findViewById(R.id.til_reg_username);
        passwordInputLayout = findViewById(R.id.til_reg_password);
        confirmPasswordInputLayout = findViewById(R.id.til_reg_confirm_password);
        createAccountButton = findViewById(R.id.btn_register);
        backButton = findViewById(R.id.btn_back);
    }

    private void setupClickListeners() {
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegistration();
            }
        });
        
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateBack();
            }
        });
    }

    private void setupValidation() {
        usernameInputLayout.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    validateUsername();
                }
            }
        });
        
        passwordInputLayout.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    validatePassword();
                }
            }
        });
    }

    private void attemptRegistration() {
        if (!validateAllFields()) {
            return;
        }
        
        String username = usernameInputLayout.getEditText().getText().toString().trim();
        String password = passwordInputLayout.getEditText().getText().toString().trim();
        
        if (userPreferences.contains("user_" + username)) {
            usernameInputLayout.setError(getString(R.string.error_username_exists));
            return;
        }
        
        try {
            String hashedPassword = SecurityManager.generateSecureHash(password);
            
            SharedPreferences.Editor editor = userPreferences.edit();
            editor.putString("user_" + username, hashedPassword);
            editor.putString("active_user_id", username);
            editor.apply();
            
            showSuccessMessage();
            navigateToHome();
            
        } catch (Exception e) {
            showErrorMessage("账号创建失败，请重试");
        }
    }

    private boolean validateAllFields() {
        boolean isValid = true;
        
        if (!validateUsername()) {
            isValid = false;
        }
        
        if (!validatePassword()) {
            isValid = false;
        }
        
        if (!validatePasswordMatch()) {
            isValid = false;
        }
        
        return isValid;
    }

    private boolean validateUsername() {
        String username = usernameInputLayout.getEditText().getText().toString().trim();
        
        if (username.isEmpty()) {
            usernameInputLayout.setError(getString(R.string.error_fill_all_fields));
            return false;
        }
        
        if (!SecurityManager.isValidUsername(username)) {
            usernameInputLayout.setError("用户名必须为3-20个字符，只能包含字母、数字和下划线");
            return false;
        }
        
        usernameInputLayout.setError(null);
        return true;
    }

    private boolean validatePassword() {
        String password = passwordInputLayout.getEditText().getText().toString();
        
        if (password.isEmpty()) {
            passwordInputLayout.setError(getString(R.string.error_fill_all_fields));
            return false;
        }
        
        if (!SecurityManager.isValidPassword(password)) {
            passwordInputLayout.setError("密码至少需要6个字符");
            return false;
        }
        
        passwordInputLayout.setError(null);
        passwordInputLayout.setHelperText("密码强度：" + getPasswordStrength(password));
        return true;
    }

    private boolean validatePasswordMatch() {
        String password = passwordInputLayout.getEditText().getText().toString();
        String confirmPassword = confirmPasswordInputLayout.getEditText().getText().toString();
        
        if (confirmPassword.isEmpty()) {
            confirmPasswordInputLayout.setError(getString(R.string.error_fill_all_fields));
            return false;
        }
        
        if (!password.equals(confirmPassword)) {
            confirmPasswordInputLayout.setError(getString(R.string.error_password_mismatch));
            return false;
        }
        
        confirmPasswordInputLayout.setError(null);
        return true;
    }

    private String getPasswordStrength(String password) {
        int strength = 0;
        
        if (password.length() >= 8) strength++;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*[a-z].*")) strength++;
        if (password.matches(".*[0-9].*")) strength++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) strength++;
        
        switch (strength) {
            case 0:
            case 1:
                return "弱";
            case 2:
            case 3:
                return "中等";
            case 4:
            case 5:
                return "强";
            default:
                return "中等";
        }
    }

    private void showSuccessMessage() {
        Toast.makeText(this, R.string.success_register, Toast.LENGTH_SHORT).show();
    }

    private void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void navigateToHome() {
        Intent homeIntent = new Intent(this, SparkHomeActivity.class);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void navigateBack() {
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}