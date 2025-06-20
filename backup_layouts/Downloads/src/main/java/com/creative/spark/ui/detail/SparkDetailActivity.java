package com.creative.spark.ui.detail;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.creative.spark.R;
import com.creative.spark.data.database.RetrofitSupabaseManager;
import com.creative.spark.data.model.TravelNote;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SparkDetailActivity extends AppCompatActivity {
    private MaterialToolbar topToolbar;
    private TextInputLayout sparkTitleInput;
    private TextInputLayout sparkContentInput;
    private TextInputLayout sparkCollectionInput;
    private MaterialAutoCompleteTextView collectionDropdown;
    private Chip favoriteChip;
    private Chip shareChip;
    private ExtendedFloatingActionButton updateSparkButton;
    private MaterialButton deleteButton;
    private RetrofitSupabaseManager databaseManager;
    private TravelNote currentNote;
    private int sparkId;
    private boolean isEditMode = false;
    private boolean hasModifications = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spark_detail);

        sparkId = getIntent().getIntExtra("note_id", -1);
        
        initializeViews();
        setupDatabase();
        loadSparkDetails();
        setupToolbar();
        setupCollectionDropdown();
        setupActionButtons();
        setupEditToggle();
        setViewMode();
    }

    private void initializeViews() {
        topToolbar = findViewById(R.id.toolbar);
        sparkTitleInput = findViewById(R.id.til_spark_title);
        sparkContentInput = findViewById(R.id.til_spark_content);
        sparkCollectionInput = findViewById(R.id.til_collection);
        collectionDropdown = (MaterialAutoCompleteTextView) sparkCollectionInput.getEditText();
        favoriteChip = findViewById(R.id.chip_favorite);
        shareChip = findViewById(R.id.chip_share);
        updateSparkButton = findViewById(R.id.fab_update_spark);
        deleteButton = findViewById(R.id.btn_delete_spark);
    }

    private void setupDatabase() {
        databaseManager = new RetrofitSupabaseManager(this);
    }

    private void setupToolbar() {
        setSupportActionBar(topToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_edit_note);
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
    }

    private void loadSparkDetails() {
        databaseManager.fetchUserNotes("", new RetrofitSupabaseManager.DatabaseCallback() {
            @Override
            public void onSuccess(Object result) {
                List<TravelNote> notes = (List<TravelNote>) result;
                TravelNote foundNote = null;
                for (TravelNote note : notes) {
                    if (note.getNoteId() == sparkId) {
                        foundNote = note;
                        break;
                    }
                }
                
                final TravelNote finalNote = foundNote;
                runOnUiThread(() -> {
                    if (finalNote != null) {
                        currentNote = finalNote;
                        displaySparkData();
                    } else {
                        Toast.makeText(SparkDetailActivity.this, "无法加载游记内容", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(SparkDetailActivity.this, "加载失败: " + error, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void displaySparkData() {
        if (sparkTitleInput.getEditText() != null) {
            sparkTitleInput.getEditText().setText(currentNote.getTitle());
        }
        if (sparkContentInput.getEditText() != null) {
            sparkContentInput.getEditText().setText(currentNote.getContent());
        }
        collectionDropdown.setText(currentNote.getCategory(), false);
        favoriteChip.setChecked(currentNote.isFavorite());
    }

    private void setupActionButtons() {
        updateSparkButton.setOnClickListener(v -> {
            if (isEditMode) {
                performUpdate();
            } else {
                toggleEditMode();
            }
        });

        deleteButton.setOnClickListener(v -> showDeleteConfirmation());

        favoriteChip.setOnCheckedChangeListener((chip, isChecked) -> {
            currentNote.setFavorite(isChecked);
            hasModifications = true;
        });

        shareChip.setOnClickListener(v -> shareSparkContent());
    }

    private void setupEditToggle() {
        sparkTitleInput.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !isEditMode) {
                toggleEditMode();
            }
        });

        sparkContentInput.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !isEditMode) {
                toggleEditMode();
            }
        });
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;
        if (isEditMode) {
            setEditMode();
        } else {
            setViewMode();
        }
    }

    private void setEditMode() {
        updateSparkButton.setText("保存");
        updateSparkButton.setIcon(getDrawable(R.drawable.ic_save));
        enableInputs(true);
        topToolbar.setTitle("编辑灵感");
    }

    private void setViewMode() {
        updateSparkButton.setText("编辑");
        updateSparkButton.setIcon(getDrawable(R.drawable.ic_edit));
        enableInputs(false);
        topToolbar.setTitle("灵感详情");
    }

    private void enableInputs(boolean enable) {
        sparkTitleInput.setEnabled(enable);
        sparkContentInput.setEnabled(enable);
        sparkCollectionInput.setEnabled(enable);
        deleteButton.setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    private void performUpdate() {
        if (!validateInputs()) {
            return;
        }

        updateSparkData();
        
        databaseManager.updateNote(currentNote, new RetrofitSupabaseManager.DatabaseCallback() {
            @Override
            public void onSuccess(Object result) {
                runOnUiThread(() -> {
                    Toast.makeText(SparkDetailActivity.this, R.string.success_update, Toast.LENGTH_SHORT).show();
                    toggleEditMode();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(SparkDetailActivity.this, R.string.error_update_failed, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateSparkData() {
        String title = sparkTitleInput.getEditText().getText().toString().trim();
        String content = sparkContentInput.getEditText().getText().toString().trim();
        String collection = collectionDropdown.getText().toString();

        currentNote.setTitle(title);
        currentNote.setContent(content);
        currentNote.setCategory(collection);
        currentNote.setCreatedTimestamp(
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date())
        );
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (sparkTitleInput.getEditText().getText().toString().trim().isEmpty()) {
            sparkTitleInput.setError(getString(R.string.error_empty_title));
            isValid = false;
        } else {
            sparkTitleInput.setError(null);
        }

        if (sparkContentInput.getEditText().getText().toString().trim().isEmpty()) {
            sparkContentInput.setError(getString(R.string.error_empty_content));
            isValid = false;
        } else {
            sparkContentInput.setError(null);
        }

        return isValid;
    }

    private void showDeleteConfirmation() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setPositiveButton(R.string.dialog_confirm, (dialog, which) -> performDelete())
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void performDelete() {
        databaseManager.deleteNote(sparkId, new RetrofitSupabaseManager.DatabaseCallback() {
            @Override
            public void onSuccess(Object result) {
                runOnUiThread(() -> {
                    Toast.makeText(SparkDetailActivity.this, R.string.success_delete, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(SparkDetailActivity.this, R.string.error_delete_failed, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void shareSparkContent() {
        String shareText = currentNote.getTitle() + "\n\n" + 
                          currentNote.getContent() + "\n\n" +
                          "—— 来自灵感笔记";
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "分享灵感"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_favorite) {
            favoriteChip.setChecked(!favoriteChip.isChecked());
            return true;
        } else if (item.getItemId() == R.id.action_share) {
            shareSparkContent();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (hasModifications || isEditMode) {
            showDiscardChangesDialog();
        } else {
            super.onBackPressed();
        }
    }

    private void showDiscardChangesDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("放弃更改")
                .setMessage("您有未保存的更改，确定要放弃吗？")
                .setPositiveButton("放弃", (dialog, which) -> finish())
                .setNegativeButton("继续编辑", null)
                .show();
    }
}