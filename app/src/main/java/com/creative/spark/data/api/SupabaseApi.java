package com.creative.spark.data.api;

import com.creative.spark.data.model.TravelNote;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * Supabase REST API接口 - 使用Retrofit (Android最佳实践)
 */
public interface SupabaseApi {

    @GET("travel_notes?select=*&order=created_at.desc")
    Call<List<TravelNote>> getAllNotes();

    @GET("travel_notes?category=eq.{category}")
    Call<List<TravelNote>> getNotesByCategory(@Path("category") String category);

    @GET("travel_notes")
    Call<List<TravelNote>> searchNotes(@Query("or") String searchQuery);

    @POST("travel_notes")
    Call<java.util.List<TravelNote>> createNote(@Body java.util.Map<String, Object> createData);

    @PATCH("travel_notes")
    Call<java.util.List<TravelNote>> updateNote(@Query("id") String filter, @Body java.util.Map<String, Object> updateData);

    @DELETE("travel_notes")
    Call<Void> deleteNote(@Query("id") String filter);
}