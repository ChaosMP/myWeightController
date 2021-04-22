package com.cmp.myweightcontroller.app;

import android.app.Application;

import androidx.room.Room;

import com.cmp.myweightcontroller.database.WeightDB;

public class WeightControllerApp extends Application {

    public static WeightControllerApp instance;
    private WeightDB database;
    private static final String DB_NAME = "weight.db";

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        database = Room.databaseBuilder(
                this,
                WeightDB.class,
                DB_NAME).build();
    }

    public static WeightControllerApp getInstance() {
        return instance;
    }

    public WeightDB getDatabase() {
        return database;
    }
}
