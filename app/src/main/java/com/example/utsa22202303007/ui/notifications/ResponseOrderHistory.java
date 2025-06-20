package com.example.utsa22202303007.ui.notifications;

import java.util.List;

public class ResponseOrderHistory {
    private boolean status;
    private String message;
    private List<OrderModel> data;

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<OrderModel> getData() {
        return data;
    }
}
