package com.example.utsa22202303007.ui.Checkout;

public class ShippingOption {
    private String courierCode; // e.g., "jne"
    private String serviceCode; // e.g., "REG"
    private String serviceDescription; // e.g., "Reguler"
    private int costValue; // in IDR
    private String etd; // Estimated Time of Delivery

    public ShippingOption(String courierCode, String serviceCode, String serviceDescription, int costValue, String etd) {
        this.courierCode = courierCode;
        this.serviceCode = serviceCode;
        this.serviceDescription = serviceDescription;
        this.costValue = costValue;
        this.etd = etd;
    }

    public String getCourierCode() {
        return courierCode;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public int getCostValue() {
        return costValue;
    }

    public String getEtd() {
        return etd;
    }

    // Untuk tampilan di UI
    public String getDisplayName() {
        return courierCode.toUpperCase() + " " + serviceCode + (serviceDescription.isEmpty() ? "" : " (" + serviceDescription + ")");
    }
}