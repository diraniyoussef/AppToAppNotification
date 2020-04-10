package com.youssefdirani.citizen;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {AnnouncementEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AnnouncementDao announcementDao();
}

