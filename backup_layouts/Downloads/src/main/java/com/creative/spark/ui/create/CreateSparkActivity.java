package com.creative.spark.ui.create;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.creative.spark.R;
import com.creative.spark.data.database.RetrofitSupabaseManager;
import com.creative.spark.data.model.TravelNote;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateSparkActivity extends AppCompatActivity {
    private MaterialToolbar topToolbar;
    private TextInputLayout sparkTitleInput;
    private TextInputLayout sparkContentInput;
    private TextInputLayout sparkCollectionInput;
    private MaterialAutoCompleteTextView collectionDropdown;
    private ChipGroup quickTagsGroup;
    private FloatingActionButton saveSparkButton;
    private RetrofitSupabaseManager databaseManager;
    private String activeUserId;
    private SimpleDateFormat dateTimeFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_spark);

        initializeUserSession();
        initializeViews();
        setupToolbar();
        setupCollectionDropdown();
        setupQuickTags();
        setupSaveAction();
    }

    private void initializeUserSession() {
        SharedPreferences preferences = getSharedPreferences("creative_spark_prefs", MODE_PRIVATE);
        activeUserId = preferences.getString("active_user_id", "");
        dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        databaseManager = new RetrofitSupabaseManager(this);
    }

    private void initializeViews() {
        topToolbar = findViewById(R.id.toolbar);
        sparkTitleInput = findViewById(R.id.til_spark_title);
        sparkContentInput = findViewById(R.id.til_spark_content);
        sparkCollectionInput = findViewById(R.id.til_collection);
        collectionDropdown = (MaterialAutoCompleteTextView) sparkCollectionInput.getEditText();
        quickTagsGroup = findViewById(R.id.quick_tags_group);
        saveSparkButton = findViewById(R.id.fab_save_spark);
    }

    private void setupToolbar() {
        setSupportActionBar(topToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_add_note);
        }
    }

    private void setupCollectionDropdown() {
        String[] collections = getResources().getStringArray(R.array.categories);
        ArrayAdapter<String> collectionAdapter = new ArrayAdapter<>(
            this, 
            android.R.layout.simple_dropdown_item_1line, 
            collections
        );
        collectionDropdown.setAdapter(collectionAdapter);
        collectionDropdown.setText(collections[0], false);
    }

    private void setupQuickTags() {
        String[] quickTags = {"灵感", "创意", "想法", "计划", "笔记", "重要"};
        
        for (String tag : quickTags) {
            Chip tagChip = new Chip(this);
            tagChip.setText(tag);
            tagChip.setCheckable(true);
            tagChip.setOnCheckedChangeListener((chip, isChecked) -> {
                if (isChecked) {
                    appendTagToContent(tag);
                }
            });
            quickTagsGroup.addView(tagChip);
        }
    }

    private void appendTagToContent(String tag) {
        String currentContent = sparkContentInput.getEditText().getText().toString();
        String newContent = currentContent + " #" + tag;
        sparkContentInput.getEditText().setText(newContent);
        sparkContentInput.getEditText().setSelection(newContent.length());
    }

    private void setupSaveAction() {
        saveSparkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSaveSpark();
            }
        });
    }

    private void attemptSaveSpark() {
        if (!validateInputs()) {
            return;
        }

        String title = sparkTitleInput.getEditText().getText().toString().trim();
        String content = sparkContentInput.getEditText().getText().toString().trim();
        String collection = collectionDropdown.getText().toString();

        TravelNote newNote = new TravelNote(title, content, collection);
        newNote.setUserId(1); // 默认用户ID
        
        databaseManager.createTravelNote(newNote, new RetrofitSupabaseManager.DatabaseCallback() {
            @Override
            public void onSuccess(Object result) {
                runOnUiThread(() -> {
                    showSuccessAnimation();
                    Toast.makeText(CreateSparkActivity.this, R.string.success_save, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(CreateSparkActivity.this, R.string.error_save_failed, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (sparkTitleInput.getEditText() == null || 
            sparkTitleInput.getEditText().getText().toString().trim().isEmpty()) {
            sparkTitleInput.setError(getString(R.string.error_empty_title));
            isValid = false;
        } else {
            sparkTitleInput.setError(null);
        }

        if (sparkContentInput.getEditText() == null || 
            sparkContentInput.getEditText().getText().toString().trim().isEmpty()) {
            sparkContentInput.setError(getString(R.string.error_empty_content));
            isValid = false;
        } else {
            sparkContentInput.setError(null);
        }

        return isValid;
    }

    private void showSuccessAnimation() {
        saveSparkButton.hide();
        saveSparkButton.postDelayed(() -> saveSparkButton.show(), 100);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (hasUnsavedChanges()) {
            showDiscardDialog();
        } else {
            super.onBackPressed();
        }
    }

    private boolean hasUnsavedChanges() {
        return (sparkTitleInput.getEditText() != null && 
                !sparkTitleInput.getEditText().getText().toString().trim().isEmpty()) ||
               (sparkContentInput.getEditText() != null && 
                !sparkContentInput.getEditText().getText().toString().trim().isEmpty());
    }

    private void showDiscardDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("放弃编辑")
            .setMessage("您有未保存的内容，确定要放弃吗？")
            .setPositiveButton("放弃", (dialog, which) -> finish())
            .setNegativeButton("继续编辑", null)
            .show();
    }
}