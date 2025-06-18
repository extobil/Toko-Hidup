package com.example.utsa22202303007.ui.product;

import com.example.utsa22202303007.ServerAPI;
import com.example.utsa22202303007.ui.dashboard.RajaOngkirRespons;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;

public class RetrofitClient {
    private static Retrofit retrofit;
    private static final String BASE_URL = ServerAPI.BASE_URL; // Sesuaikan dengan server Anda

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.rajaongkir.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
