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

    //private static final String BASE_URL = "http://192.168.0.12:9000/"; // Developer
    private static final String BASE_URL = "http://192.168.1.43:5240/"; // Production
    public static String ipAddress = ""; // You might want to get this dynamically
    public static String token = ""; // This should be securely stored
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    Log.d(TAG, message);
                }
            });
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(120, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .writeTimeout(120, TimeUnit.SECONDS)
                    .build();

            // สร้าง Gson ที่มีการกำหนดรูปแบบสำหรับการแปลงวันที่
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                        @Override
                        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                            try {
                                String dateStr = json.getAsString();
                                // รองรับรูปแบบวันที่ "2025-05-03T00:00:00"
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
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
//package com.example.uhf_bt.services;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.util.Log;
//import okhttp3.OkHttpClient;
//import okhttp3.logging.HttpLoggingInterceptor;
//import retrofit2.Retrofit;
//import retrofit2.converter.gson.GsonConverterFactory;
//import java.util.concurrent.TimeUnit;
//
//public class RetrofitClient {
//    private static final String TAG = "RetrofitClient";
//
//    //private static final String BASE_URL = "http://192.168.0.12:9000/"; // Developer
//    private static final String BASE_URL = "http://192.168.1.43:5240/"; // Production
//    public static String ipAddress = ""; // You might want to get this dynamically
//    public static String token = ""; // This should be securely stored
//    private static Retrofit retrofit = null;
//
//
//    public static Retrofit getClient() {
//        if (retrofit == null) {
//            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
//                @Override
//                public void log(String message) {
//                    Log.d(TAG, message);
//                }
//            });
//            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
//
//            OkHttpClient client = new OkHttpClient.Builder()
//                    .addInterceptor(logging)
//                    .connectTimeout(120, TimeUnit.SECONDS)
//                    .readTimeout(120, TimeUnit.SECONDS)
//                    .writeTimeout(120, TimeUnit.SECONDS)
//                    .build();
//
//            retrofit = new Retrofit.Builder()
//                    .baseUrl(BASE_URL) // Replace with your actual base URL
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .client(client)
//                    .build();
//        }
//        return retrofit;
//    }
//
//
//}