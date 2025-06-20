package com.creative.spark.ui.home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

import android.util.Log;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.appbar.MaterialToolbar;
import android.view.MenuItem;
import com.creative.spark.R;
import com.creative.spark.adapter.SparkListAdapter;
import com.creative.spark.data.model.TravelNote;
import com.creative.spark.data.database.RetrofitSupabaseManager;
import com.creative.spark.mock.MockCollaborationManager;
import com.creative.spark.ui.auth.AuthenticationActivity;
import com.creative.spark.ui.collection.SparkCollectionActivity;
import com.creative.spark.ui.create.CreateSparkActivity;
import com.creative.spark.ui.detail.SparkDetailActivity;
import com.creative.spark.ui.discover.DiscoverActivity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SparkHomeActivity extends AppCompatActivity implements SparkListAdapter.OnNoteClickListener {
    private RecyclerView sparkRecyclerView;
    private LinearLayout emptyStateContainer;
    private FloatingActionButton createSparkButton;
    private ImageButton searchButton;
    private ImageButton collectionButton;
    private TextView totalSparksTextView;
    private TextView todaySparksTextView;
    private MaterialToolbar toolbar;
    private RetrofitSupabaseManager databaseManager;
    private SparkListAdapter noteAdapter;
    private String activeUserId;
    private SharedPreferences userPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spark_home);

        initializePreferences();
        initializeViews();
        setupDatabase();
        loadNotesFromSupabase();
        configureRecyclerView();
        attachClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSparkList();
        updateStatistics();
    }

    private void initializePreferences() {
        userPreferences = getSharedPreferences("creative_spark_prefs", MODE_PRIVATE);
        activeUserId = userPreferences.getString("active_user_id", "");
    }

    private void initializeViews() {
        sparkRecyclerView = findViewById(R.id.recyclerView);
        emptyStateContainer = findViewById(R.id.empty_view);
        createSparkButton = findViewById(R.id.fab_add);
        searchButton = findViewById(R.id.btn_search);
        collectionButton = findViewById(R.id.btn_category);
        totalSparksTextView = findViewById(R.id.tv_total_notes);
        todaySparksTextView = findViewById(R.id.tv_today_notes);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupDatabase() {
        databaseManager = new RetrofitSupabaseManager(this);
    }

    private void configureRecyclerView() {
        sparkRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        sparkRecyclerView.setHasFixedSize(true);
    }

    private void attachClickListeners() {
        createSparkButton.setOnClickListener(v -> navigateToCreateSpark());

        searchButton.setOnClickListener(v -> navigateToDiscover());

        collectionButton.setOnClickListener(v -> navigateToCollection());

        toolbar.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.action_logout) {
                performLogout(null);
                return true;
            }
            return false;
        });
    }

    private void refreshSparkList() {
        // 使用Supabase异步加载
        loadNotesFromSupabase();
    }

    private void loadNotesFromSupabase() {
        databaseManager.fetchUserNotes(activeUserId, new RetrofitSupabaseManager.DatabaseCallback() {
            @Override
            public void onSuccess(Object result) {
                List<TravelNote> noteList = (List<TravelNote>) result;
                runOnUiThread(() -> {
                    if (noteList.isEmpty()) {
                        showEmptyState();
                    } else {
                        showNoteList(noteList);
                    }
                    updateStatistics();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(SparkHomeActivity.this, "加载游记失败: " + error, Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
            }
        });
    }

    private void showEmptyState() {
        sparkRecyclerView.setVisibility(View.GONE);
        emptyStateContainer.setVisibility(View.VISIBLE);
    }

    private void showNoteList(List<TravelNote> noteList) {
        sparkRecyclerView.setVisibility(View.VISIBLE);
        emptyStateContainer.setVisibility(View.GONE);

        // 添加模拟协作数据
        MockCollaborationManager.getInstance().addMockCollaborationData(noteList);

        if (noteAdapter == null) {
            noteAdapter = new SparkListAdapter(this, noteList, this);
            sparkRecyclerView.setAdapter(noteAdapter);
        } else {
            noteAdapter.updateNoteList(noteList);
        }
    }

    private void updateStatistics() {
        int totalNotes = databaseManager.getTotalNoteCount(activeUserId);
        int todayNotes = databaseManager.getTodayNoteCount(activeUserId);

        totalSparksTextView.setText(String.valueOf(totalNotes));
        todaySparksTextView.setText(String.valueOf(todayNotes));
    }

    @Override
    public void onNoteClick(TravelNote note) {
        Intent detailIntent = new Intent(this, SparkDetailActivity.class);
        detailIntent.putExtra("note_id", note.getNoteId());
        startActivity(detailIntent);
    }

    @Override
    public void onNoteLongClick(TravelNote note) {
        displayDeleteConfirmation(note);
    }

    private void displayDeleteConfirmation(final TravelNote note) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performNoteDeletion(note);
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void performNoteDeletion(TravelNote note) {
        databaseManager.deleteNote(note.getNoteId(), new RetrofitSupabaseManager.DatabaseCallback() {
            @Override
            public void onSuccess(Object result) {
                runOnUiThread(() -> {
                    Toast.makeText(SparkHomeActivity.this, R.string.success_delete, Toast.LENGTH_SHORT).show();
                    refreshSparkList();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(SparkHomeActivity.this, R.string.error_delete_failed, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void navigateToCreateSpark() {
        Intent createIntent = new Intent(this, CreateSparkActivity.class);
        startActivity(createIntent);
    }

    private void navigateToDiscover() {
        Intent discoverIntent = new Intent(this, DiscoverActivity.class);
        startActivity(discoverIntent);
    }

    private void navigateToCollection() {
        Intent collectionIntent = new Intent(this, SparkCollectionActivity.class);
        startActivity(collectionIntent);
    }

    public void performLogout(View view) {
        userPreferences.edit().remove("active_user_id").apply();
        Intent authIntent = new Intent(this, AuthenticationActivity.class);
        authIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(authIntent);
        finish();
    }
}