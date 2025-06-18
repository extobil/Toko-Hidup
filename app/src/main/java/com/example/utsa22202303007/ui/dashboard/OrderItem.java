package com.example.utsa22202303007.ui.dashboard;

import com.example.utsa22202303007.ui.product.Product; // Pastikan import ini benar

public class OrderItem {
    private String kode;
    private String merk;
    private int stok;
    private double hargajual;
    private String foto;
    private int quantity;
    // Field 'weight' tidak ada di sini, karena kita akan hardcode berat totalnya

    // Constructor dari Product
    public OrderItem(Product product) {
        this.kode = product.getKode();
        this.merk = product.getMerk();
        this.stok = product.getStok();
        this.hargajual = product.getHargajual();
        this.foto = product.getFoto();
        this.quantity = 1; // Kuantitas default 1 saat dibuat dari Product
    }

    // Default constructor (penting untuk deserialisasi Gson jika diperlukan tanpa Product)
    public OrderItem() {}

    // Getters dan setters asli
    public String getKode() { return kode; }
    public String getMerk() { return merk; }
    public int getStok() { return stok; }
    public String getFoto() { return foto; }
    public double getHargajual() { return hargajual; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getSubtotal() {
        return hargajual * quantity;
    }

    // Getter tambahan supaya kompatibel dengan Checkout class dan Adapter
    public String getProductName() {
        return merk;
    }

    public int getQty() {
        return quantity;
    }

    public double getPrice() {
        return hargajual;
    }
}