package com.example.grocery;

public class Item_Info implements Comparable<Item_Info>{

    String itemName, district, date_time, village;
    int price;

    public Item_Info(String itemName, String district, int price, String date_time, String village) {
        this.itemName = itemName;
        this.district = district;
        this.price = price;
        this.date_time = date_time;
        this.village = village;
    }

    public String getItemName() {
        return itemName;
    }

    public String getDistrict() {
        return district;
    }

    public int getPrice() {
        return price;
    }

    public String getDate_time() {
        return date_time;
    }

    public String getVillage() {
        return village;
    }

    @Override
    public int compareTo(Item_Info item_info) {
        return this.itemName.compareTo(item_info.itemName);
    }
}
