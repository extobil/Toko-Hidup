package com.example.utsa22202303007.ui.Checkout;

import com.google.gson.annotations.SerializedName;

public class AddressItem {
    @SerializedName("id")
    private int id;
    @SerializedName("pelanggan_id") // This will be the user's ID from tbl_pelanggan
    private int userId;
    @SerializedName("nama")
    private String fullName;
    @SerializedName("telepon")
    private String phoneNumber;
    @SerializedName("alamat")
    private String address;
    @SerializedName("kota")
    private String city;
    @SerializedName("provinsi")
    private String province;
    @SerializedName("kodepos")
    private String postalCode;
    @SerializedName("is_default")
    private boolean isDefault;

    // Constructor (useful for creating AddressItem objects, e.g., when adding new static addresses)
    public AddressItem(int id, int userId, String fullName, String phoneNumber, String address, String city, String province, String postalCode, boolean isDefault) {
        this.id = id;
        this.userId = userId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.city = city;
        this.province = province;
        this.postalCode = postalCode;
        this.isDefault = isDefault;
    }

    // Getters for all properties (Retrofit/Gson will use these implicitly,
    // but you need them to access data in your adapter and other parts of the app)
    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getProvince() {
        return province;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public boolean isDefault() {
        return isDefault;
    }
}