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

    @Query("SELECT * FROM weight t WHERE (t.year > :year OR t.year = :year AND (t.month > :month OR t.month = :month AND t.day >= :day))" +
            " AND t.id = (SELECT MAX(tt.id) FROM weight tt WHERE tt.year = t.year AND tt.month = t.month AND tt.day = t.day)" +
            " ORDER BY t.year ASC, t.month ASC, t.day ASC, t.id ASC")
    List<WeightRecord> getWeightRecordsAfter(int year, int month, int day);

}
