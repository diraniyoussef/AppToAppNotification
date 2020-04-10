package com.youssefdirani.citizen;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "announcements")
public class AnnouncementEntity {
    @PrimaryKey  //(autoGenerate = true) is same as autoincrement.
    public int uid = 0; //may be needed for it to be auto-incremented

    @ColumnInfo(name = "announcer")
    public String announcer;

    @ColumnInfo(name = "announcement")
    public String announcement;

    @ColumnInfo(name = "date_of_announcement")
    public long receiptDateOfAnnouncement;

    @ColumnInfo(name = "center_lat")
    public double centerLat;

    @ColumnInfo(name = "center_lng")
    public double centerLng;

    @ColumnInfo(name = "radius")
    public double radius;
}
