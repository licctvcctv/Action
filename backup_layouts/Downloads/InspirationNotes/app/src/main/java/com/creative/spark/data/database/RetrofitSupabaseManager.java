package com.creative.spark.data.database;

import android.content.Context;
import android.util.Log;
import com.creative.spark.data.api.SupabaseApi;
import com.creative.spark.data.model.TravelNote;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.Interceptor;
import okhttp3.Request;

/**
 * 使用Retrofit的Supabase数据库管理器 (Android最佳实践)
 */
public class RetrofitSupabaseManager {
    private static final String TAG = "RetrofitSupabaseManager";
    private static final String SUPABASE_URL = "https://qzbiovtdvhtgsnamvvoo.supabase.co/rest/v1/";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InF6YmlvdnRkdmh0Z3NuYW12dm9vIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY1NTQzOTMsImV4cCI6MjA2MjEzMDM5M30.vTRU1bjKYJRQJrxGS4mrP9sw5TZa6c9IRY5a2iXYCdE";
    
    private SupabaseApi api;
    private Context context;

    public RetrofitSupabaseManager(Context context) {
        this.context = context;
        setupRetrofit();
    }

    private void setupRetrofit() {
        // HTTP日志拦截器
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // 添加Supabase认证头
        Interceptor authInterceptor = chain -> {
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder()
                    .header("apikey", SUPABASE_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_KEY)
                    .header("Content-Type", "application/json");
            return chain.proceed(requestBuilder.build());
        };

        // 构建OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .build();

        // 构建Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SUPABASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(SupabaseApi.class);
    }

    // 获取所有游记
    public void fetchUserNotes(String username, DatabaseCallback callback) {
        api.getAllNotes().enqueue(new Callback<List<TravelNote>>() {
            @Override
            public void onResponse(Call<List<TravelNote>> call, Response<List<TravelNote>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TravelNote> notes = response.body();
                    Log.d(TAG, "获取到 " + notes.size() + " 条游记");
                    callback.onSuccess(notes);
                } else {
                    Log.e(TAG, "API响应失败: " + response.code());
                    callback.onError("获取数据失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<TravelNote>> call, Throwable t) {
                Log.e(TAG, "网络请求失败", t);
                callback.onError("网络连接失败: " + t.getMessage());
            }
        });
    }

    // 按分类获取游记
    public void fetchNotesByCategory(String username, String category, DatabaseCallback callback) {
        api.getNotesByCategory(category).enqueue(new Callback<List<TravelNote>>() {
            @Override
            public void onResponse(Call<List<TravelNote>> call, Response<List<TravelNote>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("按分类获取失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<TravelNote>> call, Throwable t) {
                callback.onError("网络请求失败: " + t.getMessage());
            }
        });
    }

    // 搜索游记
    public void searchNotes(String username, String searchTerm, DatabaseCallback callback) {
        String searchQuery = "(title.ilike.*" + searchTerm + "*,content.ilike.*" + searchTerm + "*)";
        api.searchNotes(searchQuery).enqueue(new Callback<List<TravelNote>>() {
            @Override
            public void onResponse(Call<List<TravelNote>> call, Response<List<TravelNote>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("搜索失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<TravelNote>> call, Throwable t) {
                callback.onError("搜索请求失败: " + t.getMessage());
            }
        });
    }

    // 创建游记
    public void createTravelNote(TravelNote note, DatabaseCallback callback) {
        api.createNote(note).enqueue(new Callback<TravelNote>() {
            @Override
            public void onResponse(Call<TravelNote> call, Response<TravelNote> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("创建失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<TravelNote> call, Throwable t) {
                callback.onError("创建请求失败: " + t.getMessage());
            }
        });
    }

    // 删除游记
    public void deleteNote(int noteId, DatabaseCallback callback) {
        api.deleteNote("eq." + noteId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("删除失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("删除请求失败: " + t.getMessage());
            }
        });
    }

    // 更新游记
    public void updateNote(TravelNote note, DatabaseCallback callback) {
        api.updateNote("eq." + note.getNoteId(), note).enqueue(new Callback<TravelNote>() {
            @Override
            public void onResponse(Call<TravelNote> call, Response<TravelNote> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("更新失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<TravelNote> call, Throwable t) {
                callback.onError("更新请求失败: " + t.getMessage());
            }
        });
    }

    // 回调接口
    public interface DatabaseCallback {
        void onSuccess(Object result);
        void onError(String error);
    }

    // 简化的统计方法
    public int getTotalNoteCount(String username) {
        return 10; // 简化实现
    }

    public int getTodayNoteCount(String username) {
        return 3; // 简化实现
    }
}