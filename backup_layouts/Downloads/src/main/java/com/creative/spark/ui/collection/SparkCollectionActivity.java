package com.creative.spark.ui.collection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.creative.spark.R;
import com.creative.spark.adapter.SparkListAdapter;
import com.creative.spark.data.database.RetrofitSupabaseManager;
import com.creative.spark.data.model.TravelNote;
import com.creative.spark.ui.detail.SparkDetailActivity;
import java.util.ArrayList;
import java.util.List;

public class SparkCollectionActivity extends AppCompatActivity implements SparkListAdapter.OnNoteClickListener {
    private MaterialToolbar topToolbar;
    private TextInputLayout collectionInputLayout;
    private MaterialAutoCompleteTextView collectionDropdown;
    private RecyclerView sparkRecyclerView;
    private LinearLayout emptyStateContainer;
    private TextView emptyStateMessage;
    private LinearLayout collectionStatsContainer;
    private TextView collectionCountText;
    private TextView collectionDescriptionText;
    private MaterialCardView[] collectionCards;
    private RetrofitSupabaseManager databaseManager;
    private SparkListAdapter sparkAdapter;
    private String activeUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spark_collection);
        
        initializeUserSession();
        initializeViews();
        setupToolbar();
        setupCollectionDropdown();
        setupCollectionCards();
        loadDefaultCollection();
    }

    private void initializeUserSession() {
        SharedPreferences preferences = getSharedPreferences("creative_spark_prefs", MODE_PRIVATE);
        activeUserId = preferences.getString("active_user_id", "");
        databaseManager = new RetrofitSupabaseManager(this);
    }

    private void initializeViews() {
        topToolbar = findViewById(R.id.toolbar);
        collectionInputLayout = findViewById(R.id.til_collection_filter);
        collectionDropdown = findViewById(R.id.dropdown_collection_filter);
        sparkRecyclerView = findViewById(R.id.collection_sparks_recycler);
        emptyStateContainer = findViewById(R.id.empty_state_container);
        emptyStateMessage = findViewById(R.id.tv_empty_message);
        collectionStatsContainer = findViewById(R.id.collection_stats_container);
        collectionCountText = findViewById(R.id.tv_collection_count);
        collectionDescriptionText = findViewById(R.id.tv_collection_description);
        
        collectionCards = new MaterialCardView[4];
        collectionCards[0] = findViewById(R.id.card_inspiration);
        collectionCards[1] = findViewById(R.id.card_goals);
        collectionCards[2] = findViewById(R.id.card_thoughts);
        collectionCards[3] = findViewById(R.id.card_favorites);
    }

    private void setupToolbar() {
        setSupportActionBar(topToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_category_management);
        }
        
        topToolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupCollectionDropdown() {
        String[] collections = getResources().getStringArray(R.array.categories_all);
        ArrayAdapter<String> collectionAdapter = new ArrayAdapter<>(
            this, 
            com.google.android.material.R.layout.support_simple_spinner_dropdown_item, 
            collections
        );
        collectionDropdown.setAdapter(collectionAdapter);
        collectionDropdown.setText(collections[0], false);
        
        collectionDropdown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                loadSparksByCollection(position);
                updateCollectionStats(collections[position]);
            }
        });
        
        sparkRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        sparkRecyclerView.setHasFixedSize(true);
    }

    private void setupCollectionCards() {
        String[] collections = {"灵感闪现", "目标计划", "随心记录", "精选收藏"};
        
        for (int i = 0; i < collectionCards.length; i++) {
            final String collection = collections[i];
            final int index = i + 1; // Skip "全部"
            
            collectionCards[i].setOnClickListener(v -> {
                collectionDropdown.setText(collection, false);
                loadSparksByCollection(index);
                updateCollectionStats(collection);
                highlightSelectedCard(v);
            });
        }
    }

    private void loadDefaultCollection() {
        loadSparksByCollection(0);
        updateCollectionStats("全部");
    }

    private void loadSparksByCollection(int position) {
        if (position == 0) {
            // Load all notes
            databaseManager.fetchUserNotes(activeUserId, new RetrofitSupabaseManager.DatabaseCallback() {
                @Override
                public void onSuccess(Object result) {
                    List<TravelNote> noteList = (List<TravelNote>) result;
                    runOnUiThread(() -> {
                        displaySparks(noteList);
                        updateCollectionCount(noteList.size());
                    });
                }
                
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(SparkCollectionActivity.this, "加载失败: " + error, Toast.LENGTH_SHORT).show();
                        displaySparks(new ArrayList<>());
                    });
                }
            });
        } else {
            // Load notes by category
            String[] collections = getResources().getStringArray(R.array.categories_all);
            String selectedCollection = collections[position];
            databaseManager.fetchNotesByCategory(activeUserId, selectedCollection, new RetrofitSupabaseManager.DatabaseCallback() {
                @Override
                public void onSuccess(Object result) {
                    List<TravelNote> noteList = (List<TravelNote>) result;
                    runOnUiThread(() -> {
                        displaySparks(noteList);
                        updateCollectionCount(noteList.size());
                    });
                }
                
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(SparkCollectionActivity.this, "加载分类失败: " + error, Toast.LENGTH_SHORT).show();
                        displaySparks(new ArrayList<>());
                    });
                }
            });
        }
    }

    private void displaySparks(List<TravelNote> sparkList) {
        if (sparkList.isEmpty()) {
            sparkRecyclerView.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
        } else {
            sparkRecyclerView.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
            
            if (sparkAdapter == null) {
                sparkAdapter = new SparkListAdapter(this, sparkList, this);
                sparkRecyclerView.setAdapter(sparkAdapter);
            } else {
                sparkAdapter.updateNoteList(sparkList);
            }
        }
    }

    private void updateCollectionStats(String collection) {
        collectionStatsContainer.setVisibility(View.VISIBLE);
        
        String description = getCollectionDescription(collection);
        collectionDescriptionText.setText(description);
    }

    private void updateCollectionCount(int count) {
        collectionCountText.setText(String.format("共 %d 条灵感", count));
    }

    private String getCollectionDescription(String collection) {
        switch (collection) {
            case "全部":
                return "查看所有收集的灵感内容";
            case "灵感闪现":
                return "记录瞬间的创意和想法";
            case "目标计划":
                return "制定目标和规划未来";
            case "随心记录":
                return "生活点滴和日常感悟";
            case "精选收藏":
                return "收藏的重要内容和精华";
            default:
                return "";
        }
    }

    private void highlightSelectedCard(View selectedCard) {
        for (MaterialCardView card : collectionCards) {
            card.setStrokeWidth(0);
            card.setCardElevation(2);
        }
        
        if (selectedCard instanceof MaterialCardView) {
            MaterialCardView selected = (MaterialCardView) selectedCard;
            selected.setStrokeWidth(3);
            selected.setStrokeColor(getResources().getColor(R.color.inspiration_primary));
            selected.setCardElevation(8);
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
        String currentCollection = collectionDropdown.getText().toString();
        String[] collections = getResources().getStringArray(R.array.categories_all);
        
        int selectedPosition = 0;
        for (int i = 0; i < collections.length; i++) {
            if (collections[i].equals(currentCollection)) {
                selectedPosition = i;
                break;
            }
        }
        
        loadSparksByCollection(selectedPosition);
        updateCollectionStats(currentCollection);
    }
}