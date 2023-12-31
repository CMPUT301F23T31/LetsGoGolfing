package com.example.letsgogolfing.utils;

import android.util.Log;

import com.example.letsgogolfing.models.Item;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Fetches product details from the UPCItemDB API
 */
public class BarcodeFetchInfo {


    /**
     * Fetches product details from the UPCItemDB API
     * @param upc
     * @param listener
     * @throws IOException
     * @throws JSONException
     */
    public void fetchProductDetails(String upc, OnProductFetchedListener listener) throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS) // connect timeout
        .writeTimeout(20, TimeUnit.SECONDS) // write timeout
        .readTimeout(30, TimeUnit.SECONDS) // read timeout
        .build();

        Request request = new Request.Builder()
                .url("https://api.upcitemdb.com/prod/trial/lookup?upc=" + upc)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonData = response.body().string();
                    Log.d("JSON Data", jsonData);
                    try {
                        JSONObject Jobject = new JSONObject(jsonData);
                        JSONArray Jarray = Jobject.getJSONArray("items");
                        Item item = new Item();
                        for (int i = 0; i < Jarray.length(); i++) {



                            JSONObject object = Jarray.getJSONObject(i);
                            String brand = object.getString("brand");
                            String model = object.getString("model");
                            String category = object.getString("category");
                            List<String> tags = new ArrayList<>();
                            String[] categoryParts = category.split(" > ");
                            if (categoryParts.length == 1) {
                                tags.add(categoryParts[0]);
                            } else if (categoryParts.length > 1) {
                                tags.add(categoryParts[0]);
                                tags.add(categoryParts[categoryParts.length - 1]);
                            }
                            String title = object.getString("title");

                            String upc = object.getString("upc");
                            String description = object.getString("description");
                            double lowestPrice = object.getDouble("lowest_recorded_price");
                            double highestPrice = object.getDouble("highest_recorded_price");
                            double averagePrice = (lowestPrice + highestPrice) / 2;

                            Log.d("Product Details", "Title: " + title + "\nBrand: " + brand + "\nModel: " + model + "\nAverage Price: " + averagePrice + "\nTags: " + tags + "\nUPC: " + upc + "\nDescription: " + description);
                            JSONArray offers = object.getJSONArray("offers");
                            for (int j = 0; j < offers.length(); j++) {
                                JSONObject offer = offers.getJSONObject(j);
                                Log.d("Offer Details", offer.getString("domain") + "\t" + offer.getString("title") + "\t" + offer.getString("price"));
                            }

                            item.setName(title);
                            item.setMake(brand);
                            item.setModel(model);
                            item.setEstimatedValue(averagePrice);
                            item.setTags(tags);
                            item.setDescription(description);
                            item.setSerialNumber(upc);
                            Date date = new Date();
                            item.setDateOfPurchase(date);
                        }

                        listener.onProductFetched(item);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Listener for when the product details are fetched
     */
    public interface OnProductFetchedListener {
        void onProductFetched(Item item);
    }





}

