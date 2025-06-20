package com.creative.spark.mock;

import com.creative.spark.data.model.TravelNote;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 模拟协作数据管理器
 * 用于为界面提供协作功能的模拟数据
 */
public class MockCollaborationManager {
    private static MockCollaborationManager instance;
    private Random random = new Random();

    // 模拟用户名列表
    private String[] mockUserNames = {
        "张文档", "李编辑", "王协作", "刘审核", "陈设计",
        "赵策划", "孙开发", "周产品", "吴运营", "郑测试"
    };

    // 模拟头像URL列表
    private String[] mockAvatarUrls = {
        "https://via.placeholder.com/40x40/FF5722/white?text=文",
        "https://via.placeholder.com/40x40/2196F3/white?text=编",
        "https://via.placeholder.com/40x40/4CAF50/white?text=协",
        "https://via.placeholder.com/40x40/FF9800/white?text=审",
        "https://via.placeholder.com/40x40/9C27B0/white?text=设",
        "https://via.placeholder.com/40x40/F44336/white?text=策",
        "https://via.placeholder.com/40x40/00BCD4/white?text=开",
        "https://via.placeholder.com/40x40/8BC34A/white?text=产",
        "https://via.placeholder.com/40x40/FFC107/white?text=运",
        "https://via.placeholder.com/40x40/E91E63/white?text=测"
    };

    public static MockCollaborationManager getInstance() {
        if (instance == null) {
            instance = new MockCollaborationManager();
        }
        return instance;
    }

    /**
     * 为文档列表添加模拟协作数据
     */
    public void addMockCollaborationData(List<TravelNote> noteList) {
        if (noteList == null || noteList.isEmpty()) {
            return;
        }

        for (TravelNote note : noteList) {
            // 随机决定是否为协作文档
            boolean isCollaborative = random.nextBoolean();
            note.setCollaborative(isCollaborative);

            if (isCollaborative) {
                // 协作文档的模拟数据
                int collaboratorCount = random.nextInt(5) + 2; // 2-6个协作者
                int onlineCount = random.nextInt(collaboratorCount) + 1; // 至少1个在线
                String lastEditor = mockUserNames[random.nextInt(mockUserNames.length)];
                int commentCount = random.nextInt(10); // 0-9个评论
                int versionCount = random.nextInt(8) + 1; // 1-8个版本

                note.setCollaboratorCount(collaboratorCount);
                note.setOnlineUserCount(onlineCount);
                note.setLastEditBy(lastEditor);
                note.setLastEditTime(System.currentTimeMillis() - random.nextInt(3600000)); // 1小时内
                note.setCommentCount(commentCount);
                note.setVersionCount(versionCount);
                note.setSyncStatus("synced");
            } else {
                // 个人文档的数据
                note.setCollaboratorCount(1);
                note.setOnlineUserCount(1);
                note.setLastEditBy("");
                note.setCommentCount(0);
                note.setVersionCount(1);
                note.setSyncStatus("synced");
            }
        }
    }

    /**
     * 获取模拟协作者头像URL列表
     */
    public List<String> getMockCollaboratorAvatars(int count) {
        List<String> avatars = new ArrayList<>();
        for (int i = 0; i < Math.min(count, mockAvatarUrls.length); i++) {
            avatars.add(mockAvatarUrls[i]);
        }
        return avatars;
    }

    /**
     * 获取模拟协作者名称列表
     */
    public List<String> getMockCollaboratorNames(int count) {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < Math.min(count, mockUserNames.length); i++) {
            names.add(mockUserNames[i]);
        }
        return names;
    }

    /**
     * 模拟实时状态更新
     */
    public void simulateRealtimeUpdate(TravelNote note) {
        if (note.isCollaborative()) {
            // 模拟在线用户数量变化
            int maxCollaborators = note.getCollaboratorCount();
            int newOnlineCount = random.nextInt(maxCollaborators) + 1;
            note.setOnlineUserCount(newOnlineCount);

            // 模拟最近编辑者变化
            String newEditor = mockUserNames[random.nextInt(mockUserNames.length)];
            note.setLastEditBy(newEditor);
            note.setLastEditTime(System.currentTimeMillis());

            // 模拟同步状态变化
            String[] statuses = {"synced", "syncing", "synced"};
            note.setSyncStatus(statuses[random.nextInt(statuses.length)]);
        }
    }

    /**
     * 获取协作状态图标资源ID
     */
    public int getCollaborationStatusIcon(TravelNote note) {
        if (note.isCollaborative()) {
            if (note.getOnlineUserCount() > 1) {
                return android.R.drawable.ic_dialog_email; // 多人在线
            } else {
                return android.R.drawable.ic_dialog_info; // 协作文档但无人在线
            }
        } else {
            return android.R.drawable.ic_menu_edit; // 个人文档
        }
    }

    /**
     * 获取同步状态图标资源ID
     */
    public int getSyncStatusIcon(String syncStatus) {
        switch (syncStatus) {
            case "synced":
                return android.R.drawable.ic_menu_upload; // 已同步
            case "syncing":
                return android.R.drawable.ic_popup_sync; // 同步中
            case "offline":
                return android.R.drawable.ic_dialog_alert; // 离线
            default:
                return android.R.drawable.ic_menu_upload;
        }
    }

    /**
     * 获取协作状态颜色
     */
    public int getCollaborationStatusColor(TravelNote note) {
        if (note.isCollaborative()) {
            if (note.getOnlineUserCount() > 1) {
                return 0xFF4CAF50; // 绿色 - 多人在线
            } else {
                return 0xFFFF9800; // 橙色 - 协作文档但无人在线
            }
        } else {
            return 0xFF9E9E9E; // 灰色 - 个人文档
        }
    }
}
