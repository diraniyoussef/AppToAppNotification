package com.youssefdirani.citizen;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface AnnouncementDao { //Dao is Data Access Object
    String tableName = "announcements";
    String uid = "uid";
    /*
    String column1 = "a_certain_column_name";
    String column2 = "another_column_name";

    @Query("SELECT * FROM " + tableName)
    List<AnnouncementEntity> getAll();

    @Query("SELECT * FROM " + tableName + " WHERE " + uid + " IN (:userIds)")
    List<AnnouncementEntity> loadAllByIds( int[] userIds );

    @Query("SELECT * FROM " + tableName + " WHERE " + column1 + " LIKE :first AND " +
            column2 + " LIKE :last LIMIT 1")
    AnnouncementEntity findByName(double first, double last);

    @Insert
    void insertAll(AnnouncementEntity... users);

    @Update
     public int updateSongs(List<Song> songs);

     @Delete
    void delete(AnnouncementEntity announcementEntity);
    */
    @Query("SELECT * FROM " + tableName)
    List<AnnouncementEntity> getAll();

    @Query("SELECT * FROM " + tableName + " WHERE " + uid + " LIKE :announcementEntityId LIMIT 1")
    AnnouncementEntity loadById(int announcementEntityId);

    @Update
    public void update(AnnouncementEntity announcementEntity);

    @Insert
    void insert(AnnouncementEntity announcementEntity);
}
