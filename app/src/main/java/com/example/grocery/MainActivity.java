package com.example.grocery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static  MyAppDatabase myAppDatabase;
    RequestQueue queue;

    String url = "https://api.data.gov.in/resource/9ef84268-d588-465a-a308-a864a43d0070?api-key=579b464db66ec23bdd000001cdd3946e44ce4aad7209ff7b23ac571b&format=csv&offset=0&limit=10";


    SwipeRefreshLayout pullToRefresh;
    ListView itemLV;
    EditText searchTextView;
    Grocery_item_List_Adapter adapter;
    SearchView searchView;
    boolean searching = false;

    ArrayList<Item_Info> item_infos = new ArrayList<>();
    ArrayList<Integer> prices = new ArrayList<>();
    ArrayList<String> item_names = new ArrayList<>();
    ArrayList<String> districts = new ArrayList<>();
    ArrayList<String> villages = new ArrayList<>();
    ArrayList<String> dates_times = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);

        // Initializing my room database to run queries on my db on the main thread of the app.
        myAppDatabase = Room.databaseBuilder(getApplicationContext(), MyAppDatabase.class,"ItemDB").allowMainThreadQueries().build();

        // Intitializing Volley variable to make requests to api.
        queue = Volley.newRequestQueue(this);


        pullToRefresh = (SwipeRefreshLayout) findViewById(R.id.RefreshLayout); // Variable that reloads listview
        itemLV = findViewById(R.id.GroceryListView); // Variable for listview of grocery items
        adapter = new Grocery_item_List_Adapter(); // Listview adapter that manages all data for listview.

        searchTextView = findViewById(R.id.SearchEditText); // Editable textview for user to search for a item by districts.
        searchTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().length() == 0){
                    getDataFromDB();
                    searching = false;
                }else{
                    searching = true;
                    adapter.getFilter().filter(charSequence);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                
            }
        });

        // Reloads Listview
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullToRefresh.setRefreshing(false);
                setItemListView();

            }
        });

        // This if condition checks if theres data already present on the users room database and if there is data, then a request to api is NOT made.
        // If the user wishes to "Get more data", there is an option available on drop down menu.
        if (!getDataFromDB()){
            makeRequest();
        }

    }

    public void setItemListView(){
        adapter = new Grocery_item_List_Adapter();
        itemLV.setAdapter(adapter);
    }
    int comma_cnt = 0, uid = 0;
    boolean added = true;
    String itemName = null, price = null, district = null, date_time = null, village = null;

    // This funtion makes a request to the api to read data.
    public void makeRequest(){
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d("RESPONSE ",response.substring(94, response.length() - 1));
                        for(int i = 94;i<response.length();i++){

                        // Checking for count Comma in the response and extracting the substring based on the indices of comma.
                            if(response.charAt(i) == ','){
                                comma_cnt += 1;
                                if (comma_cnt == 9) {
                                    comma_cnt = 0;
                                }
                                added = false;
                            }

                            if(comma_cnt == 2){

                                if (!added) {
                                    int j = i + 1;
                                    while (j < response.length() && response.charAt(j) != ',') {
                                        j += 1;

                                    }

                                    district = response.substring(i + 1, j);
                                    added = true;
                                }
                            }else
                            if(comma_cnt == 3){

                                if (!added) {
                                    int j = i + 1;
                                    while (j < response.length() && response.charAt(j) != ',') {
                                        j += 1;

                                    }

                                    village = response.substring(i + 1, j);
                                    added = true;
                                }
                            }else
                            if(comma_cnt == 4){

                                if (!added) {

                                    int j = i + 1;
                                    while (j < response.length() && response.charAt(j) != ',') {
                                        j += 1;
                                    }

                                    itemName = response.substring(i + 1, j);
                                    added = true;
                                }
                            }else
                            if(comma_cnt == 6){

                                if(!added){
                                    int j = i + 1;
                                    while (j < response.length() && response.charAt(j) != ',') {
                                        j += 1;
                                    }

                                    date_time = response.substring(i + 1, j);
                                    added = true;
                                }
                            }else
                            if(comma_cnt == 7){

                                if(!added) {
                                    int j = i + 1;
                                    while (j < response.length() && response.charAt(j) != ',') {
                                        j += 1;
                                    }
                                    price = response.substring(i + 1,j);
                                    added = true;
                                    saveData(uid, itemName, district, Integer.parseInt(price), date_time, village);
                                    uid += 1;

                                }
                            }

                            if(i == response.length() - 1){
                                getDataFromDB();
                            }

                        }



                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("RESPONSE_IS","NOT_WORKING");

            }
        });

        queue.add(stringRequest);

    }

    public void saveData(int uid, String itemName, String district, int price, String date_time, String village){
        Log.d("SAVING","DATA");

        Item item = new Item();

        item.uid = uid;
        item.name = itemName;
        item.price = price;
        item.district = district;
        item.date_time = date_time;
        item.village = village;

        myAppDatabase.myDao().addItem(item);
        item_infos.add(new Item_Info(itemName, district, price, item.getDate_time(), village));

    }

    public boolean getDataFromDB(){
        boolean isData = false;
        Log.d("GETTING","DATA");
        List<Item> items = myAppDatabase.myDao().getItems();

        int cnt = 0;

        item_infos.clear();
        item_names.clear();
        districts.clear();
        prices.clear();
        dates_times.clear();
        villages.clear();
        for(Item item: items){

            isData = true;

            String itemName = item.getName();
            String district = item.getDistrict();
            int price = item.getPrice();

            item_names.add(itemName);
            districts.add(district);
            prices.add(price);
            dates_times.add(item.getDate_time());
            villages.add(item.getVillage());
            item_infos.add(new Item_Info(itemName, district, price, item.getDate_time(), item.getVillage()));

            cnt += 1;

            if(cnt == items.size() - 1){
                setItemListView();
            }
        }

        return isData;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem sort_price = menu.findItem(R.id.Sort_Price);
        MenuItem sort_date = menu.findItem(R.id.Sort_Date);
        MenuItem getMoreDate = menu.findItem(R.id.Get_More_Date);

        sort_price.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {


                Collections.sort(item_infos, new Comparator<Item_Info>() {
                    @Override
                    public int compare(Item_Info t1, Item_Info t2) {
                        return t1.getPrice()-t2.getPrice();
                    }
                });

                adapter.notifyDataSetChanged();
                return false;
            }
        });

        sort_date.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                Collections.sort(item_infos, new Comparator<Item_Info>() {
                    @Override
                    public int compare(Item_Info t1, Item_Info t2) {

                        return t1.getDate_time().compareTo(t2.getDate_time());
                    }
                });

                adapter.notifyDataSetChanged();
                return false;
            }
        });

        getMoreDate.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                if(getDataFromDB()){
                    List<Item> items = myAppDatabase.myDao().getItems();
                    uid = items.get(items.size() - 1).uid + 1;

                    makeRequest();
                }

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    public class Grocery_item_List_Adapter extends BaseAdapter implements Filterable {

        @Override
        public int getCount() {
            return item_infos.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {

            convertView = getLayoutInflater().inflate(R.layout.list_row,null);



            TextView itemName, itemPrice, district, date_time, village;
            if(convertView != null){

                itemName = convertView.findViewById(R.id.Item_Name);
                itemPrice = convertView.findViewById(R.id.Item_Price);
                district = convertView.findViewById(R.id.District);
                date_time = convertView.findViewById(R.id.Date_Time);
                village = convertView.findViewById(R.id.Village);

//                Log.d("SIZES ",String.valueOf(dates_times.size()));
                if(i < item_infos.size()){

                    if(!searching) {

                        itemName.setText("Name "+item_infos.get(i).getItemName());
                        itemPrice.setText("Rs. " + item_infos.get(i).getPrice());
                        district.setText("District: "+ item_infos.get(i).getDistrict());
                        date_time.setText(item_infos.get(i).getDate_time());
                        village.setText(item_infos.get(i).getVillage());

                    }else{
                        if(i < item_names.size() && i < prices.size() && i < districts.size() && i < dates_times.size() && i < villages.size()) {

                            itemName.setText("Name "+item_names.get(i));
                            itemPrice.setText("Rs. " + prices.get(i));
                            district.setText("District: "+ districts.get(i));
                            date_time.setText(dates_times.get(i));
                            village.setText(villages.get(i));


                        }
                    }
                }
            }
            return convertView;
        }


        @Override
        public Filter getFilter() {
            return filter;
        }

        Filter filter = new Filter() {

            // Running on background thread
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                ArrayList<String> filteredList = new ArrayList<>();
                List<Item> items = myAppDatabase.myDao().getItems();

                if(!charSequence.toString().isEmpty()){

                        for (Item item : items) {
                            if (item.getDistrict().toLowerCase().contains(charSequence.toString().toLowerCase())) {
                                filteredList.add(item.getDistrict());
                            }
                        }
                }else{
                        filteredList.addAll(districts);
                }

                    FilterResults filterResults = new FilterResults();
                    filterResults.values = filteredList;

                return filterResults;
            }


            // Running on UI thread
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {


                    districts.clear();
                    districts.addAll((Collection<? extends String>) filterResults.values);
                    notifyDataSetChanged();

            }
        };
    }

}
