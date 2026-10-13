package com.example.uhf_bt.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String TAG = "RetrofitClient";
    //private static final String BASE_URL = "http://192.168.0.12:9000/"; // Production
    private static final String BASE_URL = "http://192.168.1.39:6000/"; // localhost
    public static String ipAddress = ""; // You might want to get this dynamicallycom
    public static String token = ""; // This should be securely stored
    private static Retrofit retrofit = null;
    
    // Set to true only when debugging and dealing with small responses
    // WARNING: Enabling this with large API responses will cause OutOfMemoryError
    private static final boolean ENABLE_LOGGING = false;

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .connectTimeout(120, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .writeTimeout(120, TimeUnit.SECONDS);
            
            // Only add logging interceptor if explicitly enabled
            // Disabled by default to prevent OutOfMemoryError with large responses
            if (ENABLE_LOGGING) {
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                    @Override
                    public void log(String message) {
                        Log.d(TAG, message);
                    }
                });
                logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);
                clientBuilder.addInterceptor(logging);
            }
            
            OkHttpClient client = clientBuilder.build();

            // สร้าง Gson ที่มีการกำหนดรูปแบบสำหรับการแปลงวันที่
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                        @Override
                        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                            try {
                                String dateStr = json.getAsString();
                                // รองรับรูปแบบวันที่ "2025-05-03T00:00:00"
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                                return format.parse(dateStr);
                            } catch (ParseException e) {
                                Log.e(TAG, "Date parse error: " + e.getMessage());
                                return null;
                            }
                        }
                    })
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson)) // ใช้ gson ที่กำหนดเอง
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}
