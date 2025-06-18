package com.example.utsa22202303007.ui.dashboard;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.utsa22202303007.ui.product.Product;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class OrderHelper {
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "order_prefs";
    private static final String ORDER_KEY = "order_items";
    private Gson gson = new Gson();

    public OrderHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void addToOrder(Product product) {
        List<OrderItem> orderItems = getOrderItems();
        boolean exists = false;

        for (OrderItem item : orderItems) {
            if (item.getKode().equals(product.getKode())) {
                item.setQuantity(item.getQuantity() + 1);
                exists = true;
                break;
            }
        }

        if (!exists) {
            orderItems.add(new OrderItem(product));
        }

        saveOrderItems(orderItems);
    }

    public void removeFromOrder(String productCode) {
        List<OrderItem> orderItems = getOrderItems();
        for (int i = 0; i < orderItems.size(); i++) {
            if (orderItems.get(i).getKode().equals(productCode)) {
                orderItems.remove(i);
                break;
            }
        }
        saveOrderItems(orderItems);
    }

    public void updateQuantity(String productCode, int quantity) {
        List<OrderItem> orderItems = getOrderItems();
        for (OrderItem item : orderItems) {
            if (item.getKode().equals(productCode)) {
                item.setQuantity(quantity);
                break;
            }
        }
        saveOrderItems(orderItems);
    }

    public List<OrderItem> getOrderItems() {
        String json = sharedPreferences.getString(ORDER_KEY, null);
        if (json == null) return new ArrayList<>();

        Type type = new TypeToken<List<OrderItem>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public double getTotal() {
        double total = 0;
        for (OrderItem item : getOrderItems()) {
            total += item.getSubtotal();
        }
        return total;
    }

    private void saveOrderItems(List<OrderItem> items) {
        String json = gson.toJson(items);
        sharedPreferences.edit().putString(ORDER_KEY, json).apply();
    }

    public void clearOrders() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("order_items");
        editor.apply();
    }



}