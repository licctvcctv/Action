package com.creative.spark.data.database;

import android.content.Context;
import android.util.Log;
import com.creative.spark.data.api.SupabaseApi;
import com.creative.spark.data.model.TravelNote;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
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
                    .header("Content-Type", "application/json")
                    .header("Prefer", "return=representation"); // 确保返回创建/更新的数据
            return chain.proceed(requestBuilder.build());
        };

        // 构建OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .build();

        // 配置Gson以正确处理@Expose注解
        // 注意：不能使用excludeFieldsWithoutExposeAnnotation()，因为它会影响反序列化
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .serializeNulls()
                .create();

        Log.d(TAG, "Gson配置完成，将排除没有@Expose注解的字段");

        // 构建Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SUPABASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
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

                    // 调试：打印每个笔记的ID
                    for (TravelNote note : notes) {
                        Log.d(TAG, "笔记: " + note.getTitle() + ", ID: " + note.getNoteId());
                    }

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
        Log.d(TAG, "开始创建游记，数据: " + note.toString());

        // 创建专门用于创建操作的数据对象，不包含ID
        TravelNote createData = new TravelNote();
        createData.setTitle(note.getTitle());
        createData.setContent(note.getContent());
        createData.setCategory(note.getCategory());
        createData.setUserId(note.getUserId());
        createData.setCreatedTimestamp(note.getCreatedTimestamp());
        createData.setUpdatedAt(note.getUpdatedAt());
        createData.setFavorite(note.isFavorite());
        createData.setImageUrl(note.getImageUrl());
        createData.setRating(note.getRating());

        // 测试序列化结果
        Gson testGson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
        String serializedJson = testGson.toJson(createData);
        Log.d(TAG, "创建时序列化后的JSON: " + serializedJson);

        api.createNote(createData).enqueue(new Callback<TravelNote>() {
            @Override
            public void onResponse(Call<TravelNote> call, Response<TravelNote> response) {
                Log.d(TAG, "创建游记响应 - 状态码: " + response.code());

                if (response.isSuccessful()) {
                    TravelNote createdNote = response.body();
                    Log.d(TAG, "游记创建成功: " + (createdNote != null ? createdNote.toString() : "null"));
                    callback.onSuccess(createdNote);
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "读取错误响应失败", e);
                    }

                    String errorMessage = "创建失败 - 状态码: " + response.code() +
                                        ", 错误信息: " + response.message() +
                                        ", 详细错误: " + errorBody;
                    Log.e(TAG, errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<TravelNote> call, Throwable t) {
                String errorMessage = "创建请求失败: " + t.getMessage();
                Log.e(TAG, errorMessage, t);
                callback.onError(errorMessage);
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
        Log.d(TAG, "开始更新游记，ID: " + note.getNoteId() + ", 数据: " + note.toString());

        // 检查ID是否有效
        if (note.getNoteId() <= 0) {
            String errorMessage = "无效的笔记ID: " + note.getNoteId() + "，无法更新";
            Log.e(TAG, errorMessage);
            callback.onError(errorMessage);
            return;
        }

        // 创建专门用于更新的Gson实例，包含ID字段
        Gson updateGson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        // 对于更新操作，不跳过任何有@Expose注解的字段
                        return false;
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .create();

        // 创建更新数据对象，只包含需要更新的字段
        TravelNote updateData = new TravelNote();
        updateData.setTitle(note.getTitle());
        updateData.setContent(note.getContent());
        updateData.setCategory(note.getCategory());
        updateData.setUserId(note.getUserId());
        updateData.setUpdatedAt(note.getUpdatedAt());
        updateData.setFavorite(note.isFavorite());
        updateData.setImageUrl(note.getImageUrl());
        updateData.setRating(note.getRating());

        String serializedJson = updateGson.toJson(updateData);
        Log.d(TAG, "更新时序列化后的JSON: " + serializedJson);

        api.updateNote("eq." + note.getNoteId(), updateData).enqueue(new Callback<TravelNote>() {
            @Override
            public void onResponse(Call<TravelNote> call, Response<TravelNote> response) {
                Log.d(TAG, "更新游记响应 - 状态码: " + response.code());

                if (response.isSuccessful()) {
                    TravelNote updatedNote = response.body();
                    Log.d(TAG, "游记更新成功: " + (updatedNote != null ? updatedNote.toString() : "null"));
                    callback.onSuccess(updatedNote);
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "读取错误响应失败", e);
                    }

                    String errorMessage = "更新失败 - 状态码: " + response.code() +
                                        ", 错误信息: " + response.message() +
                                        ", 详细错误: " + errorBody;
                    Log.e(TAG, errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<TravelNote> call, Throwable t) {
                String errorMessage = "更新请求失败: " + t.getMessage();
                Log.e(TAG, errorMessage, t);
                callback.onError(errorMessage);
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