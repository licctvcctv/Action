package com.creative.spark.ui.discover;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.creative.spark.R;
import com.creative.spark.adapter.SparkListAdapter;
import com.creative.spark.data.database.RetrofitSupabaseManager;
import com.creative.spark.data.model.TravelNote;
import com.creative.spark.ui.detail.SparkDetailActivity;
import java.util.List;

public class DiscoverActivity extends AppCompatActivity implements SparkListAdapter.OnNoteClickListener {
    private MaterialToolbar topToolbar;
    private TextInputLayout searchInputLayout;
    private TextInputEditText searchEditText;
    private ChipGroup filterChipGroup;
    private RecyclerView searchResultsRecycler;
    private LinearLayout emptyStateContainer;
    private TextView emptyStateMessage;
    private TextView searchResultCount;
    private RetrofitSupabaseManager databaseManager;
    private SparkListAdapter sparkAdapter;
    private String activeUserId;
    private String currentFilter = "全部";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);
        
        initializeUserSession();
        initializeViews();
        setupToolbar();
        setupSearchListener();
        setupFilterChips();
        configureRecyclerView();
        showInitialState();
    }

    private void initializeUserSession() {
        SharedPreferences preferences = getSharedPreferences("creative_spark_prefs", MODE_PRIVATE);
        activeUserId = preferences.getString("active_user_id", "");
        databaseManager = new RetrofitSupabaseManager(this);
    }

    private void initializeViews() {
        topToolbar = findViewById(R.id.toolbar);
        searchInputLayout = findViewById(R.id.til_search);
        searchEditText = findViewById(R.id.et_search);
        filterChipGroup = findViewById(R.id.filter_chip_group);
        searchResultsRecycler = findViewById(R.id.search_results_recycler);
        emptyStateContainer = findViewById(R.id.empty_state_container);
        emptyStateMessage = findViewById(R.id.tv_empty_message);
        searchResultCount = findViewById(R.id.tv_result_count);
    }

    private void setupToolbar() {
        setSupportActionBar(topToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_search_notes);
        }
        
        topToolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupSearchListener() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        searchEditText.requestFocus();
    }

    private void setupFilterChips() {
        String[] categories = getResources().getStringArray(R.array.categories_all);
        
        for (String category : categories) {
            Chip filterChip = new Chip(this);
            filterChip.setText(category);
            filterChip.setCheckable(true);
            filterChip.setChecked(category.equals("全部"));
            
            filterChip.setOnCheckedChangeListener((chip, isChecked) -> {
                if (isChecked) {
                    currentFilter = category;
                    uncheckOtherChips(filterChip);
                    performSearch(searchEditText.getText().toString().trim());
                }
            });
            
            filterChipGroup.addView(filterChip);
        }
    }

    private void uncheckOtherChips(Chip selectedChip) {
        for (int i = 0; i < filterChipGroup.getChildCount(); i++) {
            View child = filterChipGroup.getChildAt(i);
            if (child instanceof Chip && child != selectedChip) {
                ((Chip) child).setChecked(false);
            }
        }
    }

    private void configureRecyclerView() {
        searchResultsRecycler.setLayoutManager(new LinearLayoutManager(this));
        searchResultsRecycler.setHasFixedSize(true);
    }

    private void performSearch(String searchTerm) {
        if (searchTerm.isEmpty()) {
            showInitialState();
            return;
        }
        
        databaseManager.searchNotes(activeUserId, searchTerm, new RetrofitSupabaseManager.DatabaseCallback() {
            @Override
            public void onSuccess(Object result) {
                List<TravelNote> searchResults = (List<TravelNote>) result;
                
                // Apply filter if not "全部"
                if (!currentFilter.equals("全部")) {
                    searchResults = filterSearchResults(searchResults, currentFilter);
                }
                
                final List<TravelNote> finalResults = searchResults;
                runOnUiThread(() -> {
                    displaySearchResults(finalResults, searchTerm);
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(DiscoverActivity.this, "搜索失败: " + error, Toast.LENGTH_SHORT).show();
                    showInitialState();
                });
            }
        });
    }

    private List<TravelNote> filterSearchResults(List<TravelNote> allResults, String filter) {
        allResults.removeIf(note -> !note.getCategory().equals(filter));
        return allResults;
    }

    private void displaySearchResults(List<TravelNote> results, String searchTerm) {
        if (results.isEmpty()) {
            showEmptyResults(searchTerm);
        } else {
            showResults(results);
        }
    }

    private void showInitialState() {
        searchResultsRecycler.setVisibility(View.GONE);
        emptyStateContainer.setVisibility(View.VISIBLE);
        emptyStateMessage.setText(R.string.hint_search_keyword);
        searchResultCount.setVisibility(View.GONE);
    }

    private void showEmptyResults(String searchTerm) {
        searchResultsRecycler.setVisibility(View.GONE);
        emptyStateContainer.setVisibility(View.VISIBLE);
        emptyStateMessage.setText(getString(R.string.empty_search_results));
        searchResultCount.setVisibility(View.VISIBLE);
        searchResultCount.setText(String.format("未找到与 \"%s\" 相关的灵感", searchTerm));
    }

    private void showResults(List<TravelNote> results) {
        searchResultsRecycler.setVisibility(View.VISIBLE);
        emptyStateContainer.setVisibility(View.GONE);
        searchResultCount.setVisibility(View.VISIBLE);
        searchResultCount.setText(String.format("找到 %d 条相关灵感", results.size()));
        
        if (sparkAdapter == null) {
            sparkAdapter = new SparkListAdapter(this, results, this);
            searchResultsRecycler.setAdapter(sparkAdapter);
        } else {
            sparkAdapter.updateNoteList(results);
        }
    }

    @Override
    public void onNoteClick(TravelNote note) {
        Intent detailIntent = new Intent(this, SparkDetailActivity.class);
        detailIntent.putExtra("note_id", note.getNoteId());
        startActivity(detailIntent);
    }
    
    @Override
    public void onNoteLongClick(TravelNote note) {
        // Handle long click if needed
    }


    @Override
    protected void onResume() {
        super.onResume();
        String currentSearchTerm = searchEditText.getText().toString().trim();
        if (!currentSearchTerm.isEmpty()) {
            performSearch(currentSearchTerm);
        }
    }
}