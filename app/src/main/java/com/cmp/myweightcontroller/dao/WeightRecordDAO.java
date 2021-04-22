package com.cmp.myweightcontroller.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.cmp.myweightcontroller.model.WeightRecord;

import java.util.List;

@Dao
public interface WeightRecordDAO {

    @Insert
    long addWeightRecord(WeightRecord weightRecord);

    @Query("DELETE FROM weight WHERE id = :weightRecordId")
    void deleteWeightRecord(long weightRecordId);

    @Query("SELECT * FROM weight ORDER BY year DESC, month DESC, day DESC, id DESC")
    List<WeightRecord> getAllWeightRecords();

}
