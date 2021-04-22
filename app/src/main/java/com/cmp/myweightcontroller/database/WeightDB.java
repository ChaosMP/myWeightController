package com.cmp.myweightcontroller.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.cmp.myweightcontroller.dao.WeightRecordDAO;
import com.cmp.myweightcontroller.model.WeightRecord;

@Database(entities = {WeightRecord.class}, version = 1)
public abstract class WeightDB  extends RoomDatabase {

    public abstract WeightRecordDAO weightRecordDAO();

}
