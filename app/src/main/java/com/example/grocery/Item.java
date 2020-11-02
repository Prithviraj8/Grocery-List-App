package com.example.grocery;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Items")
public class Item {

    @PrimaryKey
    public int uid;

    @ColumnInfo(name = "Name")
    public String name;

    @ColumnInfo(name = "Price")
    public int price;

    @ColumnInfo(name = "District")
    public String district;

    @ColumnInfo(name = "Date & Time")
    public String date_time;

    @ColumnInfo(name = "Village")
    public String village;


    public int getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public String getDistrict() {
        return district;
    }

    public String getDate_time() {
        return date_time;
    }

    public String getVillage() {
        return village;
    }

}