package com.example.grocery;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MyDao {

    @Insert
    public void addItem(Item item);

    @Query("select * from Items")
    public List<Item> getItems();



}
