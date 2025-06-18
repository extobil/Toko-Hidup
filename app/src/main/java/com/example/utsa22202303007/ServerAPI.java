package com.example.utsa22202303007;

import com.example.utsa22202303007.ui.dashboard.RajaOngkirRespons;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServerAPI {
    public static String BASE_URL="http://candratri.my.id/androidstudio/";
    public static String IMAGE_BASE_URL= BASE_URL + "/img/";

//    public static String RajaOngkir_Province="https://api.rajaongkir.com/starter/province";
//    public static String RajaOngkir_Kota="https://api.rajaongkir.com/starter/city?province=";

    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            Gson gson = new GsonBuilder()
                    .setLenient() // Tambahkan ini untuk mengatasi JSON malformed
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson)) // Gunakan Gson yang sudah di-set lenient
                    .build();
        }
        return retrofit;
    }


}
