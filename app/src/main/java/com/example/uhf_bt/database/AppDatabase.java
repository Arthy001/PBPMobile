package com.example.uhf_bt.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.uhf_bt.models.ProductItemEntity;

@Database(entities = {ProductItemEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract ProductItemDao productItemDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "pbp_inventory_db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
