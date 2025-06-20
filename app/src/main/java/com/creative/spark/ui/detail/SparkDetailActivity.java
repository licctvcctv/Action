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
import com.creative.spark.mock.MockCollaborationManager;
import android.widget.LinearLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.card.MaterialCardView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import android.os.Handler;
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

    // 协作功能相关UI元素
    private LinearLayout collaborationStatusBar;
    private LinearLayout collaboratorsAvatars;
    private MaterialCardView collaborationFeaturesCard;
    private ImageButton btnComments;
    private ImageButton btnVersionHistory;
    private ImageButton btnCollaborationSettings;
    private MaterialButton btnAddComment;
    private MaterialButton btnViewVersions;
    private TextView realtimeStatus;
    private TextView lastEditInfo;

    // 协作功能相关
    private Handler collaborationHandler = new Handler();
    private MockCollaborationManager mockManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spark_detail);

        sparkId = getIntent().getIntExtra("note_id", -1);
        android.util.Log.d("SparkDetail", "接收到的笔记ID: " + sparkId);

        if (sparkId == -1) {
            Toast.makeText(this, "Invalid note ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupDatabase();
        setupCollaboration();
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

        // 初始化协作功能UI元素
        collaborationStatusBar = findViewById(R.id.collaboration_status_bar);
        collaboratorsAvatars = findViewById(R.id.collaborators_avatars);
        collaborationFeaturesCard = findViewById(R.id.collaboration_features_card);
        btnComments = findViewById(R.id.btn_comments);
        btnVersionHistory = findViewById(R.id.btn_version_history);
        btnCollaborationSettings = findViewById(R.id.btn_collaboration_settings);
        btnAddComment = findViewById(R.id.btn_add_comment);
        btnViewVersions = findViewById(R.id.btn_view_versions);
        realtimeStatus = findViewById(R.id.realtime_status);
        lastEditInfo = findViewById(R.id.last_edit_info);
    }

    private void setupDatabase() {
        databaseManager = new RetrofitSupabaseManager(this);
    }

    private void setupCollaboration() {
        mockManager = MockCollaborationManager.getInstance();

        // 设置协作功能按钮点击事件
        if (btnComments != null) {
            btnComments.setOnClickListener(v -> showCommentsDialog());
        }

        if (btnVersionHistory != null) {
            btnVersionHistory.setOnClickListener(v -> showVersionHistoryDialog());
        }

        if (btnCollaborationSettings != null) {
            btnCollaborationSettings.setOnClickListener(v -> showCollaborationSettingsDialog());
        }

        if (btnAddComment != null) {
            btnAddComment.setOnClickListener(v -> showAddCommentDialog());
        }

        if (btnViewVersions != null) {
            btnViewVersions.setOnClickListener(v -> showVersionHistoryDialog());
        }

        // 开始模拟实时更新
        startRealtimeUpdates();
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
        // 获取当前用户ID
        android.content.SharedPreferences prefs = getSharedPreferences("creative_spark_prefs", MODE_PRIVATE);
        String userId = prefs.getString("active_user_id", "");

        databaseManager.fetchUserNotes(userId, new RetrofitSupabaseManager.DatabaseCallback() {
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
                        android.util.Log.d("SparkDetail", "找到笔记: " + finalNote.getTitle() + ", ID: " + finalNote.getNoteId());
                        displaySparkData();
                    } else {
                        android.util.Log.e("SparkDetail", "未找到ID为 " + sparkId + " 的笔记");
                        Toast.makeText(SparkDetailActivity.this, "Unable to load note content (ID: " + sparkId + ")", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(SparkDetailActivity.this, "Load failed: " + error, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void displaySparkData() {
        if (currentNote == null) {
            Toast.makeText(this, "Unable to load note data", Toast.LENGTH_SHORT).show();
            return;
        }

        // 显示标题
        if (sparkTitleInput != null && sparkTitleInput.getEditText() != null) {
            sparkTitleInput.getEditText().setText(currentNote.getTitle());
            android.util.Log.d("SparkDetail", "设置标题: " + currentNote.getTitle());
        }

        // 显示内容
        if (sparkContentInput != null && sparkContentInput.getEditText() != null) {
            sparkContentInput.getEditText().setText(currentNote.getContent());
            android.util.Log.d("SparkDetail", "设置内容: " + currentNote.getContent());
        }

        // 显示分类
        if (collectionDropdown != null) {
            collectionDropdown.setText(currentNote.getCategory(), false);
            android.util.Log.d("SparkDetail", "设置分类: " + currentNote.getCategory());
        }

        // 设置收藏状态
        if (favoriteChip != null) {
            favoriteChip.setChecked(currentNote.isFavorite());
        }

        // 添加模拟协作数据并显示协作功能
        if (mockManager != null) {
            mockManager.addMockCollaborationData(java.util.Arrays.asList(currentNote));
            setupCollaborationUI();
        }

        android.util.Log.d("SparkDetail", "数据显示完成");
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
        updateSparkButton.setText("Save");
        updateSparkButton.setIcon(getDrawable(R.drawable.ic_save));
        enableInputs(true);
        topToolbar.setTitle("Edit Note");
    }

    private void setViewMode() {
        updateSparkButton.setText("Edit");
        updateSparkButton.setIcon(getDrawable(R.drawable.ic_edit));
        enableInputs(false);
        topToolbar.setTitle("Note Details");
    }

    private void enableInputs(boolean enable) {
        sparkTitleInput.setEnabled(enable);
        sparkContentInput.setEnabled(enable);
        sparkCollectionInput.setEnabled(enable);
        deleteButton.setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    private void performUpdate() {
        android.util.Log.d("SparkDetail", "开始执行更新操作");

        if (!validateInputs()) {
            android.util.Log.w("SparkDetail", "输入验证失败");
            return;
        }

        updateSparkData();
        android.util.Log.d("SparkDetail", "准备更新笔记: " + currentNote.toString());

        databaseManager.updateNote(currentNote, new RetrofitSupabaseManager.DatabaseCallback() {
            @Override
            public void onSuccess(Object result) {
                android.util.Log.d("SparkDetail", "笔记更新成功: " + result);
                runOnUiThread(() -> {
                    Toast.makeText(SparkDetailActivity.this, R.string.success_update, Toast.LENGTH_SHORT).show();
                    toggleEditMode();
                });
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("SparkDetail", "笔记更新失败: " + error);
                runOnUiThread(() -> {
                    Toast.makeText(SparkDetailActivity.this, "Update failed: " + error, Toast.LENGTH_LONG).show();
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
        // 修复：更新时应该设置updatedAt而不是createdTimestamp
        currentNote.setUpdatedAt(
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
                          "—— From Inspiration Notes";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share Note"));
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
                .setTitle("Discard Changes")
                .setMessage("You have unsaved changes. Are you sure you want to discard them?")
                .setPositiveButton("Discard", (dialog, which) -> finish())
                .setNegativeButton("Continue Editing", null)
                .show();
    }

    // ==================== 协作功能实现 ====================

    private void setupCollaborationUI() {
        if (currentNote == null) return;

        // 总是显示协作功能界面（无论是否为协作文档）
        collaborationStatusBar.setVisibility(View.VISIBLE);
        collaborationFeaturesCard.setVisibility(View.VISIBLE);

        // 设置协作者头像
        setupCollaboratorAvatars();

        // 更新协作状态信息
        updateCollaborationStatus();

        // 如果不是协作文档，显示"开启协作"选项
        if (!currentNote.isCollaborative()) {
            // 将当前文档设置为协作文档以便演示
            currentNote.setCollaborative(true);
            currentNote.setCollaboratorCount(1); // 当前用户
            currentNote.setOnlineUserCount(1);
            currentNote.setLastEditBy("Me");
            currentNote.setCommentCount(0);
            currentNote.setVersionCount(1);
        }
    }

    private void setupCollaboratorAvatars() {
        if (collaboratorsAvatars == null || currentNote == null) return;

        // 清除现有头像
        collaboratorsAvatars.removeAllViews();

        if (currentNote.isCollaborative()) {
            java.util.List<String> avatarUrls = mockManager.getMockCollaboratorAvatars(currentNote.getCollaboratorCount());
            java.util.List<String> names = mockManager.getMockCollaboratorNames(currentNote.getCollaboratorCount());

            for (int i = 0; i < Math.min(avatarUrls.size(), 5); i++) { // 最多显示5个头像
                ImageView avatarView = new ImageView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int) (32 * getResources().getDisplayMetrics().density),
                    (int) (32 * getResources().getDisplayMetrics().density)
                );
                params.setMargins(0, 0, (int) (4 * getResources().getDisplayMetrics().density), 0);
                avatarView.setLayoutParams(params);

                // 使用Glide加载头像
                Glide.with(this)
                        .load(avatarUrls.get(i))
                        .apply(new RequestOptions().circleCrop())
                        .into(avatarView);

                // 设置点击事件显示用户信息
                final String userName = names.get(i);
                avatarView.setOnClickListener(v ->
                    Toast.makeText(this, "Collaborator: " + userName, Toast.LENGTH_SHORT).show()
                );

                collaboratorsAvatars.addView(avatarView);
            }

            // 如果协作者超过5个，显示"+N"
            if (currentNote.getCollaboratorCount() > 5) {
                TextView moreView = new TextView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int) (32 * getResources().getDisplayMetrics().density),
                    (int) (32 * getResources().getDisplayMetrics().density)
                );
                moreView.setLayoutParams(params);
                moreView.setText("+" + (currentNote.getCollaboratorCount() - 5));
                moreView.setTextSize(10);
                moreView.setGravity(android.view.Gravity.CENTER);
                moreView.setBackgroundResource(R.drawable.circle_background);
                moreView.setTextColor(getResources().getColor(R.color.text_primary));

                collaboratorsAvatars.addView(moreView);
            }
        }
    }

    private void updateCollaborationStatus() {
        if (currentNote == null || realtimeStatus == null || lastEditInfo == null) return;

        // 更新实时状态
        String statusText = "Real-time sync - " + currentNote.getOnlineUserCount() + " users online";
        realtimeStatus.setText(statusText);

        // 更新最近编辑信息
        String lastEditText = currentNote.getLastActivityText();
        lastEditInfo.setText(lastEditText);
    }

    private void startRealtimeUpdates() {
        collaborationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentNote != null && currentNote.isCollaborative()) {
                    // 模拟实时状态更新
                    mockManager.simulateRealtimeUpdate(currentNote);
                    updateCollaborationStatus();
                }
                // 每5秒更新一次
                collaborationHandler.postDelayed(this, 5000);
            }
        }, 5000);
    }

    private void showCommentsDialog() {
        String[] comments = {
            "💡 张文档 (2小时前)：这个文档结构很清晰！建议再增加一些实例说明。",
            "📝 李编辑 (1小时前)：建议优化第二段的表述，让逻辑更加清晰。",
            "🔧 王协作 (30分钟前)：整体内容很好，格式可以再统一一下。",
            "👍 刘审核 (10分钟前)：内容质量很高！可以作为标准模板。",
            "❓ 陈设计 (5分钟前)：是否需要添加一些图表来辅助说明？"
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("💬 评论区 (" + Math.max(currentNote.getCommentCount(), 5) + "条)")
                .setItems(comments, (dialog, which) -> {
                    // 显示评论详情和回复选项
                    showCommentDetailDialog(comments[which]);
                })
                .setPositiveButton("添加评论", (dialog, which) -> showAddCommentDialog())
                .setNegativeButton("关闭", null)
                .show();
    }

    private void showCommentDetailDialog(String comment) {
        String[] actions = {"👍 点赞", "💬 回复", "📋 复制", "🚫 举报"};

        new MaterialAlertDialogBuilder(this)
                .setTitle("评论详情")
                .setMessage(comment)
                .setItems(actions, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            Toast.makeText(this, "👍 已点赞", Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            showReplyCommentDialog();
                            break;
                        case 2:
                            Toast.makeText(this, "📋 评论已复制", Toast.LENGTH_SHORT).show();
                            break;
                        case 3:
                            Toast.makeText(this, "🚫 举报已提交", Toast.LENGTH_SHORT).show();
                            break;
                    }
                })
                .setNegativeButton("关闭", null)
                .show();
    }

    private void showReplyCommentDialog() {
        android.widget.EditText replyInput = new android.widget.EditText(this);
        replyInput.setHint("输入回复内容...");
        replyInput.setMinLines(2);

        new MaterialAlertDialogBuilder(this)
                .setTitle("💬 回复评论")
                .setView(replyInput)
                .setPositiveButton("发布回复", (dialog, which) -> {
                    String reply = replyInput.getText().toString().trim();
                    if (!reply.isEmpty()) {
                        currentNote.setCommentCount(currentNote.getCommentCount() + 1);
                        updateCollaborationStatus();
                        Toast.makeText(this, "✅ 回复已发布：" + reply, Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showAddCommentDialog() {
        android.widget.EditText editText = new android.widget.EditText(this);
        editText.setHint("输入您的评论...");

        new MaterialAlertDialogBuilder(this)
                .setTitle("添加评论")
                .setView(editText)
                .setPositiveButton("发布", (dialog, which) -> {
                    String comment = editText.getText().toString().trim();
                    if (!comment.isEmpty()) {
                        currentNote.setCommentCount(currentNote.getCommentCount() + 1);
                        Toast.makeText(this, "评论已发布：" + comment, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showVersionHistoryDialog() {
        String[] versions = {
            "📝 v" + currentNote.getVersionCount() + " - 当前版本 (刚刚) - 我",
            "📝 v" + (currentNote.getVersionCount() - 1) + " - 2小时前 - 张文档 [完善了文档结构]",
            "📝 v" + (currentNote.getVersionCount() - 2) + " - 昨天 - 李编辑 [优化了内容表述]",
            "📝 v" + (currentNote.getVersionCount() - 3) + " - 3天前 - 王协作 [创建初始版本]",
            "📝 v" + (currentNote.getVersionCount() - 4) + " - 1周前 - 刘审核 [添加了格式规范]"
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("📚 版本历史 (" + (currentNote.getVersionCount()) + "个版本)")
                .setItems(versions, (dialog, which) -> {
                    showVersionDetailDialog(versions[which], which);
                })
                .setPositiveButton("关闭", null)
                .show();
    }

    private void showVersionDetailDialog(String version, int versionIndex) {
        if (versionIndex == 0) {
            Toast.makeText(this, "📝 这是当前版本", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] actions = {"👁️ 预览版本", "🔄 恢复到此版本", "📋 复制版本", "📊 版本对比"};

        new MaterialAlertDialogBuilder(this)
                .setTitle("版本详情")
                .setMessage(version)
                .setItems(actions, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            Toast.makeText(this, "👁️ 正在加载版本预览...", Toast.LENGTH_SHORT).show();
                            new android.os.Handler().postDelayed(() -> {
                                Toast.makeText(this, "✅ 版本预览已打开", Toast.LENGTH_SHORT).show();
                            }, 1500);
                            break;
                        case 1:
                            showRestoreVersionDialog(versionIndex);
                            break;
                        case 2:
                            Toast.makeText(this, "📋 版本内容已复制", Toast.LENGTH_SHORT).show();
                            break;
                        case 3:
                            Toast.makeText(this, "📊 正在生成版本对比...", Toast.LENGTH_SHORT).show();
                            new android.os.Handler().postDelayed(() -> {
                                Toast.makeText(this, "✅ 版本对比已生成", Toast.LENGTH_SHORT).show();
                            }, 2000);
                            break;
                    }
                })
                .setNegativeButton("关闭", null)
                .show();
    }

    private void showRestoreVersionDialog(int versionIndex) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("⚠️ 恢复版本")
                .setMessage("确定要恢复到此版本吗？\n\n当前的更改将会丢失，建议先备份当前版本。")
                .setPositiveButton("确定恢复", (dialog, which) -> {
                    // 模拟恢复版本
                    currentNote.setVersionCount(currentNote.getVersionCount() + 1);
                    updateCollaborationStatus();
                    Toast.makeText(this, "✅ 版本已恢复，并创建了新版本 v" + currentNote.getVersionCount(), Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showCollaborationSettingsDialog() {
        String[] options = {
            "📧 邀请协作者",
            "👥 管理协作者权限",
            "🔗 生成分享链接",
            "📤 导出文档",
            "⚙️ 协作设置",
            "🔒 文档权限设置",
            "📊 协作统计",
            "🔄 同步设置"
        };

        String[] descriptions = {
            "邀请其他用户协作编辑",
            "管理协作者的访问权限",
            "创建分享链接邀请协作",
            "导出文档为多种格式",
            "设置协作偏好和通知",
            "控制文档的访问权限",
            "查看详细的协作数据",
            "配置文档同步方式"
        };

        // 创建自定义适配器来显示图标和描述
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<String>(this,
            android.R.layout.simple_list_item_2, android.R.id.text1, options) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);

                text1.setText(options[position]);
                text1.setTextSize(16);
                text1.setTextColor(getResources().getColor(R.color.text_primary));

                text2.setText(descriptions[position]);
                text2.setTextSize(12);
                text2.setTextColor(getResources().getColor(R.color.text_secondary));
                text2.setVisibility(View.VISIBLE);

                view.setPadding(24, 16, 24, 16);
                return view;
            }
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("🛠️ 协作功能管理")
                .setAdapter(adapter, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showInviteCollaboratorDialog();
                            break;
                        case 1:
                            showManagePermissionsDialog();
                            break;
                        case 2:
                            showGenerateShareLinkDialog();
                            break;
                        case 3:
                            showExportDocumentDialog();
                            break;
                        case 4:
                            showCollaborationPreferencesDialog();
                            break;
                        case 5:
                            showDocumentPermissionsDialog();
                            break;
                        case 6:
                            showCollaborationStatsDialog();
                            break;
                        case 7:
                            showSyncSettingsDialog();
                            break;
                    }
                })
                .setNegativeButton("关闭", null)
                .show();
    }

    // ==================== 扩展协作功能实现 ====================

    private void showInviteCollaboratorDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_invite_collaborator, null);

        com.google.android.material.textfield.TextInputEditText emailInput =
            dialogView.findViewById(R.id.et_collaborator_email);
        android.widget.RadioGroup permissionGroup = dialogView.findViewById(R.id.rg_permission_level);
        com.google.android.material.button.MaterialButton btnCancel =
            dialogView.findViewById(R.id.btn_cancel);
        com.google.android.material.button.MaterialButton btnSendInvite =
            dialogView.findViewById(R.id.btn_send_invite);

        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(dialogView);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSendInvite.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (!email.isEmpty()) {
                // 获取选中的权限级别
                int selectedId = permissionGroup.getCheckedRadioButtonId();
                String permission = "编辑权限";
                if (selectedId == R.id.rb_view_permission) permission = "查看权限";
                else if (selectedId == R.id.rb_comment_permission) permission = "评论权限";

                // 模拟邀请成功
                currentNote.setCollaboratorCount(currentNote.getCollaboratorCount() + 1);
                currentNote.setOnlineUserCount(currentNote.getOnlineUserCount() + 1);
                setupCollaboratorAvatars();
                updateCollaborationStatus();

                dialog.dismiss();
                showSuccessSnackbar("✅ 邀请已发送至 " + email + "\n权限级别：" + permission);
            } else {
                emailInput.setError("请输入邮箱地址");
            }
        });

        dialog.show();
    }

    private void showManagePermissionsDialog() {
        String[] permissions = {
            "👤 张文档 - 编辑权限",
            "👤 李编辑 - 查看权限",
            "👤 王协作 - 评论权限",
            "👤 刘审核 - 编辑权限"
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("管理协作者权限")
                .setItems(permissions, (dialog, which) -> {
                    String[] permissionLevels = {"查看", "评论", "编辑", "管理员"};
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("设置权限级别")
                            .setItems(permissionLevels, (d, level) -> {
                                Toast.makeText(this, "✅ 已将权限设置为：" + permissionLevels[level], Toast.LENGTH_SHORT).show();
                            })
                            .show();
                })
                .setPositiveButton("关闭", null)
                .show();
    }

    private void showGenerateShareLinkDialog() {
        String shareLink = "https://spark.app/doc/" + currentNote.getNoteId() + "?token=abc123def456";
        showSimpleShareLinkDialog(shareLink);
    }

    private void showSimpleShareLinkDialog(String shareLink) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("🔗 分享链接")
                .setMessage("通过链接邀请协作者：\n\n" + shareLink + "\n\n📅 链接有效期：7天\n🔒 权限：查看和评论")
                .setPositiveButton("📋 复制链接", (dialog, which) -> {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("分享链接", shareLink);
                    clipboard.setPrimaryClip(clip);
                    showSuccessSnackbar("🔗 链接已复制到剪贴板");
                })
                .setNeutralButton("📤 分享", (dialog, which) -> {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "邀请您协作编辑文档：" + shareLink);
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "协作邀请 - " + currentNote.getTitle());
                    startActivity(Intent.createChooser(shareIntent, "分享协作链接"));
                })
                .setNegativeButton("关闭", null)
                .show();
    }

    private void showExportDocumentDialog() {
        String[] formats = {"📄 PDF格式", "📝 Word文档", "📋 纯文本", "🌐 HTML网页", "📊 Markdown", "📑 EPUB电子书"};
        String[] descriptions = {
            "适合打印和正式分享",
            "可编辑的Office文档",
            "纯文本格式，兼容性最好",
            "网页格式，可在浏览器查看",
            "程序员友好的标记语言",
            "电子书格式，适合阅读"
        };

        // 创建自定义适配器
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<String>(this,
            android.R.layout.simple_list_item_2, android.R.id.text1, formats) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);

                text1.setText(formats[position]);
                text1.setTextSize(16);
                text1.setTextColor(getResources().getColor(R.color.text_primary));

                text2.setText(descriptions[position]);
                text2.setTextSize(12);
                text2.setTextColor(getResources().getColor(R.color.text_secondary));
                text2.setVisibility(View.VISIBLE);

                view.setPadding(24, 16, 24, 16);
                return view;
            }
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("📤 导出文档")
                .setAdapter(adapter, (dialog, which) -> {
                    String format = formats[which];
                    showExportProgressDialog(format);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showExportProgressDialog(String format) {
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setTitle("导出中...");
        progressDialog.setMessage("正在导出为 " + format);
        progressDialog.setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        progressDialog.show();

        // 模拟导出进度
        new Thread(() -> {
            for (int i = 0; i <= 100; i += 10) {
                final int progress = i;
                runOnUiThread(() -> progressDialog.setProgress(progress));
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            runOnUiThread(() -> {
                progressDialog.dismiss();
                showSuccessSnackbar("✅ 导出完成！文件已保存到下载文件夹\n格式：" + format);
            });
        }).start();
    }

    private void showCollaborationPreferencesDialog() {
        boolean[] checkedItems = {true, false, true, false, true};
        String[] preferences = {
            "实时同步编辑",
            "显示协作者光标",
            "评论通知",
            "版本变更通知",
            "自动保存"
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("协作偏好设置")
                .setMultiChoiceItems(preferences, checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                })
                .setPositiveButton("保存设置", (dialog, which) -> {
                    Toast.makeText(this, "✅ 协作偏好已保存", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showDocumentPermissionsDialog() {
        String[] permissionOptions = {
            "🔓 公开 - 任何人都可查看",
            "🔗 链接分享 - 有链接的人可查看",
            "👥 仅协作者 - 仅邀请的人可访问",
            "🔒 私有 - 仅自己可访问"
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("文档权限设置")
                .setSingleChoiceItems(permissionOptions, 2, (dialog, which) -> {
                    dialog.dismiss();
                    String permission = permissionOptions[which].substring(2);
                    Toast.makeText(this, "✅ 文档权限已设置为：" + permission, Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showCollaborationStatsDialog() {
        String stats = "📊 协作统计\n\n" +
                "👥 协作者数量：" + currentNote.getCollaboratorCount() + " 人\n" +
                "🟢 当前在线：" + currentNote.getOnlineUserCount() + " 人\n" +
                "💬 评论总数：" + currentNote.getCommentCount() + " 条\n" +
                "📝 版本数量：" + currentNote.getVersionCount() + " 个\n" +
                "⏰ 最近活动：" + currentNote.getLastActivityText() + "\n" +
                "📅 创建时间：" + currentNote.getFormattedDate() + "\n" +
                "🔄 同步状态：" + currentNote.getSyncStatus();

        new MaterialAlertDialogBuilder(this)
                .setTitle("协作统计")
                .setMessage(stats)
                .setPositiveButton("导出报告", (dialog, which) -> {
                    Toast.makeText(this, "✅ 协作报告已导出", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("关闭", null)
                .show();
    }

    private void showSyncSettingsDialog() {
        String[] syncOptions = {
            "🔄 实时同步（推荐）",
            "⏰ 每5分钟同步",
            "📱 仅WiFi时同步",
            "🔧 手动同步"
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("同步设置")
                .setSingleChoiceItems(syncOptions, 0, (dialog, which) -> {
                    dialog.dismiss();
                    String option = syncOptions[which].substring(2);
                    currentNote.setSyncStatus("synced");
                    updateCollaborationStatus();
                    Toast.makeText(this, "✅ 同步设置已更改为：" + option, Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showSuccessSnackbar(String message) {
        com.google.android.material.snackbar.Snackbar snackbar =
            com.google.android.material.snackbar.Snackbar.make(
                findViewById(android.R.id.content),
                message,
                com.google.android.material.snackbar.Snackbar.LENGTH_LONG
            );

        snackbar.setBackgroundTint(getResources().getColor(R.color.inspiration_primary));
        snackbar.setTextColor(getResources().getColor(R.color.inspiration_on_primary));
        snackbar.setActionTextColor(getResources().getColor(R.color.inspiration_on_primary));
        snackbar.show();
    }

    private void showErrorSnackbar(String message) {
        com.google.android.material.snackbar.Snackbar snackbar =
            com.google.android.material.snackbar.Snackbar.make(
                findViewById(android.R.id.content),
                message,
                com.google.android.material.snackbar.Snackbar.LENGTH_LONG
            );

        snackbar.setBackgroundTint(getResources().getColor(R.color.inspiration_error));
        snackbar.setTextColor(getResources().getColor(R.color.inspiration_on_error));
        snackbar.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (collaborationHandler != null) {
            collaborationHandler.removeCallbacksAndMessages(null);
        }
    }
}