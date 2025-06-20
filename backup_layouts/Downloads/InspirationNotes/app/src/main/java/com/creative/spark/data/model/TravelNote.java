package com.creative.spark.data.model;

import java.io.Serializable;
import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;

public class TravelNote implements Serializable {
    @SerializedName("id")
    @Expose(serialize = false)
    private int noteId;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("content")
    private String content;
    
    @SerializedName("category")
    private String category;
    
    @SerializedName("user_id")
    private int userId;
    
    @SerializedName("created_at")
    private String createdTimestamp;
    
    @SerializedName("updated_at")
    private String updatedAt;
    
    @SerializedName("is_favorite")
    private boolean isFavorite;
    
    @SerializedName("image_url")
    private String imageUrl;
    
    @SerializedName("rating")
    private double rating;

    public TravelNote() {
        this.isFavorite = false;
        this.rating = 5.0;
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
}