package com.example.utsa22202303007.ui.Checkout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.utsa22202303007.R;
import com.example.utsa22202303007.ServerAPI;
import com.example.utsa22202303007.RegisterAPI;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Address extends AppCompatActivity implements AddressAdapter.OnAddressSelectedListener {

    private RecyclerView recyclerViewAddresses;
    private Button btnAddAddress;
    private AddressAdapter addressAdapter;
    private List<AddressItem> addressList;
    private int currentUserId = -1;
    private String userEmail;

    private static final int REQUEST_CODE_EDIT_ADDRESS = 1001;
    private static final int REQUEST_CODE_ADD_NEW_ADDRESS = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_address);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerViewAddresses = findViewById(R.id.recyclerViewAddresses);
        btnAddAddress = findViewById(R.id.btnAddAddress);

        SharedPreferences sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE);
        userEmail = sharedPreferences.getString("email", null);

        addressList = new ArrayList<>();
        addressAdapter = new AddressAdapter(this, addressList, this);
        recyclerViewAddresses.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAddresses.setAdapter(addressAdapter);

        fetchUserIdAndLoadAddresses();

        btnAddAddress.setOnClickListener(v -> {
            Intent intent = new Intent(Address.this, AddNewAddressActivity.class);
            intent.putExtra("current_user_id", currentUserId);
            startActivityForResult(intent, REQUEST_CODE_ADD_NEW_ADDRESS);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUserId != -1) {
            loadAddresses();
        } else {
            fetchUserIdAndLoadAddresses();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_CODE_EDIT_ADDRESS) {
                String updatedAddressJson = data.getStringExtra("updated_address");
                if (updatedAddressJson != null) {
                    AddressItem updatedAddress = new Gson().fromJson(updatedAddressJson, AddressItem.class);
                    if (updatedAddress != null) {
                        Toast.makeText(this, "Alamat berhasil diperbarui.", Toast.LENGTH_SHORT).show();
                        loadAddresses();
                    }
                }
            } else if (requestCode == REQUEST_CODE_ADD_NEW_ADDRESS) {
                Toast.makeText(this, "Alamat baru berhasil ditambahkan.", Toast.LENGTH_SHORT).show();
                loadAddresses();
            }
        }
    }

    private void fetchUserIdAndLoadAddresses() {
        if (userEmail == null) {
            Toast.makeText(this, "Email pengguna tidak ditemukan. Harap login ulang.", Toast.LENGTH_SHORT).show();
            Log.e("AddressActivity", "User email is null, cannot fetch user ID.");
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RegisterAPI apiService = retrofit.create(RegisterAPI.class);
        Call<ResponseBody> call = apiService.getUserIdByEmail(userEmail);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseString = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseString);

                        String result = jsonResponse.getString("result");

                        if (result.equals("1")) {
                            currentUserId = jsonResponse.getInt("user_id");
                            Log.d("AddressActivity", "User ID fetched: " + currentUserId);
                            loadAddresses();
                        } else {
                            String message = jsonResponse.optString("message", "Gagal mendapatkan ID pengguna.");
                            Toast.makeText(Address.this, message, Toast.LENGTH_LONG).show();
                            Log.e("AddressActivity", "Failed to get user ID: " + message);
                            currentUserId = -1;
                        }
                    } catch (Exception e) {
                        Log.e("AddressActivity", "Error parsing getUserIdByEmail response: " + e.getMessage(), e);
                        Toast.makeText(Address.this, "Kesalahan saat memproses respons ID pengguna.", Toast.LENGTH_LONG).show();
                        currentUserId = -1;
                    }
                } else {
                    Log.e("AddressActivity", "getUserIdByEmail not successful: " + response.code() + " " + response.message());
                    Toast.makeText(Address.this, "Gagal mendapatkan ID pengguna. Kode: " + response.code(), Toast.LENGTH_SHORT).show();
                    currentUserId = -1;
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("AddressActivity", "Network error fetching user ID: " + t.getMessage(), t);
                Toast.makeText(Address.this, "Kesalahan koneksi saat mengambil ID pengguna: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                currentUserId = -1;
            }
        });
    }

    private void loadAddresses() {
        if (currentUserId == -1) {
            Toast.makeText(this, "Tidak dapat memuat alamat karena ID pengguna tidak valid.", Toast.LENGTH_SHORT).show();
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RegisterAPI apiService = retrofit.create(RegisterAPI.class);
        Call<ResponseBody> call = apiService.getAddress(currentUserId);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        Log.d("AddressActivity", "Raw Address List JSON: " + responseBody);

                        List<AddressItem> fetchedAddresses = new Gson().fromJson(
                                responseBody,
                                new TypeToken<List<AddressItem>>(){}.getType()
                        );

                        addressList.clear();
                        if (fetchedAddresses != null && !fetchedAddresses.isEmpty()) {
                            addressList.addAll(fetchedAddresses);
                        }
                        addressAdapter.notifyDataSetChanged();
                        Log.d("AddressActivity", "Alamat berhasil dimuat: " + addressList.size() + " alamat.");

                        if (addressList.isEmpty()) {
                            Toast.makeText(Address.this, "Anda belum memiliki alamat tersimpan.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("AddressActivity", "Error parsing address list response: " + e.getMessage(), e);
                        Toast.makeText(Address.this, "Kesalahan saat memproses daftar alamat.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e("AddressActivity", "Gagal memuat alamat: " + response.code() + " " + response.message());
                    Toast.makeText(Address.this, "Gagal memuat alamat. Kode: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("AddressActivity", "Kesalahan jaringan saat memuat alamat: " + t.getMessage(), t);
                Toast.makeText(Address.this, "Kesalahan koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAddressSelected(AddressItem address) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selectedAddress", new Gson().toJson(address));
        setResult(RESULT_OK, resultIntent);
        finish();
        Toast.makeText(this, "Alamat dipilih: " + address.getAddress(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditAddress(AddressItem address) {
        Toast.makeText(this, "Akan mengedit alamat: " + address.getAddress(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Address.this, EditAddressActivity.class);
        intent.putExtra("address_data", new Gson().toJson(address));
        startActivityForResult(intent, REQUEST_CODE_EDIT_ADDRESS);
    }
}