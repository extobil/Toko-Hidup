package com.example.utsa22202303007.ui.product;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Product implements Parcelable {
    private String kode;
    private String merk;
    private double hargajual;
    private int stok;
    private String status;
    private String foto;
    private String kategori;
    private String deskripsi;

    @SerializedName("view")
    private int viewCount;

    // ✅ Tambahan field orderQuantity
    private int orderQuantity = 1;

    public Product(String kode, String merk, double hargajual, int stok,
                   String status, String foto, String kategori, String deskripsi, int viewCount) {
        this.kode = kode;
        this.merk = merk;
        this.hargajual = hargajual;
        this.stok = stok;
        this.status = status;
        this.foto = foto;
        this.kategori = kategori;
        this.deskripsi = deskripsi;
        this.viewCount = viewCount;
    }

    protected Product(Parcel in) {
        kode = in.readString();
        merk = in.readString();
        hargajual = in.readDouble();
        stok = in.readInt();
        status = in.readString();
        foto = in.readString();
        kategori = in.readString();
        deskripsi = in.readString();
        viewCount = in.readInt();
        orderQuantity = in.readInt(); // ✅ Baca orderQuantity dari Parcel
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    // ✅ Getter & Setter untuk orderQuantity
    public int getOrderQuantity() {
        return orderQuantity;
    }

    public void setOrderQuantity(int orderQuantity) {
        this.orderQuantity = orderQuantity;
    }

    // Getter & Setter lainnya
    public String getKode() { return kode; }
    public String getMerk() { return merk; }
    public double getHargajual() { return hargajual; }
    public int getStok() { return stok; }
    public String getStatus() { return status; }
    public String getFoto() { return foto; }
    public String getKategori() { return kategori; }
    public String getDeskripsi() { return deskripsi; }
    public int getViewCount() { return viewCount; }

    public void setViewCount(int viewCount) { this.viewCount = viewCount; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(kode);
        dest.writeString(merk);
        dest.writeDouble(hargajual);
        dest.writeInt(stok);
        dest.writeString(status);
        dest.writeString(foto);
        dest.writeString(kategori);
        dest.writeString(deskripsi);
        dest.writeInt(viewCount);
        dest.writeInt(orderQuantity); // ✅ Tulis orderQuantity ke Parcel
    }
}
