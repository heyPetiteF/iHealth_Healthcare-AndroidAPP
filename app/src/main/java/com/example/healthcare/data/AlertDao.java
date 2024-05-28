package com.example.healthcare.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AlertDao {
    @Insert
    void insert(Alert alert);

    @Query("SELECT * FROM alert WHERE timestamp > :timestamp")
    List<Alert> getAlertsSince(long timestamp);
}
