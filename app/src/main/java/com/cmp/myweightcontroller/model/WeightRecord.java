package com.cmp.myweightcontroller.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "weight")
public class WeightRecord {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private int year;
    private int month;
    private int day;
    private double weight;

    public WeightRecord(long id, int year, int month, int day, double weight) {
        this.id = id;
        this.year = year;
        this.month = month;
        this.day = day;
        this.weight = weight;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

}
