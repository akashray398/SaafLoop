package com.example.saafloop.core.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.saafloop.core.database.dao.ReportDraftDao;
import com.example.saafloop.core.database.entity.ReportDraftEntity;

@Database(entities = {ReportDraftEntity.class}, version = 1, exportSchema = false)
public abstract class SaafLoopDatabase extends RoomDatabase {

    public abstract ReportDraftDao reportDraftDao();

    private static volatile SaafLoopDatabase INSTANCE;

    public static SaafLoopDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SaafLoopDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            SaafLoopDatabase.class,
                            "saafloop_database"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
