package com.creative.spark.data.model;

import java.io.Serializable;
import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;

public class TravelNote implements Serializable {
    @SerializedName("id")
    @Expose(serialize = false, deserialize = true)
    private int noteId;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("content")
    @Expose
    private String content;

    @SerializedName("category")
    @Expose
    private String category;

    @SerializedName("user_id")
    @Expose
    private int userId;

    @SerializedName("created_at")
    @Expose
    private String createdTimestamp;

    @SerializedName("updated_at")
    @Expose
    private String updatedAt;

    @SerializedName("is_favorite")
    @Expose
    private boolean isFavorite;

    @SerializedName("image_url")
    @Expose
    private String imageUrl;

    @SerializedName("rating")
    @Expose
    private double rating;

    // 新增协作相关字段 - 这些字段不发送到服务器，仅用于本地显示
    @SerializedName("is_collaborative")
    @Expose(serialize = false)
    private boolean isCollaborative = false;

    @Expose(serialize = false)
    private int collaboratorCount = 1;

    @Expose(serialize = false)
    private int onlineUserCount = 1;

    @Expose(serialize = false)
    private String lastEditBy = "";

    @Expose(serialize = false)
    private long lastEditTime = 0;

    @Expose(serialize = false)
    private int commentCount = 0;

    @Expose(serialize = false)
    private int versionCount = 1;

    @Expose(serialize = false)
    private String syncStatus = "synced"; // "synced", "syncing", "offline"

    public TravelNote() {
        this.isFavorite = false;
        this.rating = 5.0;
        this.isCollaborative = false;
        this.collaboratorCount = 1;
        this.onlineUserCount = 1;
        this.commentCount = 0;
        this.versionCount = 1;
        this.syncStatus = "synced";
    }

    public TravelNote(String title, String content, String category) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.isFavorite = false;
        this.rating = 5.0;
    }

    public int getNoteId() {
        return noteId;
    }

    public void setNoteId(int noteId) {
        this.noteId = noteId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(String createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getFormattedDate() {
        if (createdTimestamp != null && createdTimestamp.length() >= 10) {
            return createdTimestamp.substring(0, 10);
        }
        return createdTimestamp;
    }

    public String getFormattedTime() {
        if (createdTimestamp != null && createdTimestamp.length() >= 16) {
            return createdTimestamp.substring(11, 16);
        }
        return "";
    }

    // 协作相关的getter和setter方法
    public boolean isCollaborative() {
        return isCollaborative;
    }

    public void setCollaborative(boolean collaborative) {
        isCollaborative = collaborative;
    }

    public int getCollaboratorCount() {
        return collaboratorCount;
    }

    public void setCollaboratorCount(int collaboratorCount) {
        this.collaboratorCount = collaboratorCount;
    }

    public int getOnlineUserCount() {
        return onlineUserCount;
    }

    public void setOnlineUserCount(int onlineUserCount) {
        this.onlineUserCount = onlineUserCount;
    }

    public String getLastEditBy() {
        return lastEditBy;
    }

    public void setLastEditBy(String lastEditBy) {
        this.lastEditBy = lastEditBy;
    }

    public long getLastEditTime() {
        return lastEditTime;
    }

    public void setLastEditTime(long lastEditTime) {
        this.lastEditTime = lastEditTime;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getVersionCount() {
        return versionCount;
    }

    public void setVersionCount(int versionCount) {
        this.versionCount = versionCount;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }

    // Collaboration status display methods
    public String getCollaborationStatusText() {
        if (isCollaborative) {
            return onlineUserCount + " users online";
        } else {
            return "Personal note";
        }
    }

    public String getLastActivityText() {
        if (lastEditBy != null && !lastEditBy.isEmpty()) {
            return "Last edited by: " + lastEditBy;
        } else {
            return "No collaboration activity";
        }
    }

    @Override
    public String toString() {
        return "TravelNote{" +
                "noteId=" + noteId +
                ", title='" + title + '\'' +
                ", content='" + (content != null ? content.substring(0, Math.min(content.length(), 50)) + "..." : "null") + '\'' +
                ", category='" + category + '\'' +
                ", userId=" + userId +
                ", createdTimestamp='" + createdTimestamp + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", isFavorite=" + isFavorite +
                ", imageUrl='" + imageUrl + '\'' +
                ", rating=" + rating +
                '}';
    }
}