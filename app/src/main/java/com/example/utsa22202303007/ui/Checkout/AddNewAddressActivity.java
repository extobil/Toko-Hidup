package com.example.utsa22202303007.ui.Checkout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent; // Pastikan ini diimpor
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.utsa22202303007.R;
import com.example.utsa22202303007.RegisterAPI;
import com.example.utsa22202303007.ServerAPI;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddNewAddressActivity extends AppCompatActivity {

    private EditText etEmail, etNama, etAlamat, etTelepon, etKodePos;
    private Spinner spProvinsi, spKota;
    private Button btnSaveAddress;
    private CheckBox cbIsDefault; // Tambahan untuk fungsionalitas "jadikan default"

    private int currentUserId = -1; // Default to -1
    private String currentUserEmail;

    private OkHttpClient httpClient;
    private List<Province> provinceList = new ArrayList<>();
    private List<City> cityList = new ArrayList<>();
    private int selectedProvinceId = -1;
    private int selectedCityId = -1;
    private String selectedProvinceName = "";
    private String selectedCityName = "";

    private static final String PROVINCE_API_URL = ServerAPI.BASE_URL + "get_provinsi.php";
    private static final String CITY_API_URL = ServerAPI.BASE_URL + "get_kota.php";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_address);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inisialisasi Views sesuai ID dari XML Anda
        etEmail = findViewById(R.id.etEmail);
        etNama = findViewById(R.id.etNama);
        spProvinsi = findViewById(R.id.etProvinsi);
        spKota = findViewById(R.id.etKota);
        etAlamat = findViewById(R.id.etAlamat);
        etTelepon = findViewById(R.id.etTelepon);
        etKodePos = findViewById(R.id.etKodePos);
        btnSaveAddress = findViewById(R.id.btnSaveProfile);
        cbIsDefault = findViewById(R.id.cbIsDefault); // Pastikan Anda memiliki CheckBox dengan ID ini di layout Anda

        httpClient = new OkHttpClient();

        // **PERBAIKAN DI SINI:** Ambil ID pengguna dari Intent
        currentUserId = getIntent().getIntExtra("current_user_id", -1);

        SharedPreferences sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE);
        currentUserEmail = sharedPreferences.getString("email", null);

        if (currentUserId == -1) {
            Toast.makeText(this, "ID pengguna tidak ditemukan dari aktivitas sebelumnya. Harap login kembali.", Toast.LENGTH_LONG).show();
            Log.e("AddNewAddressActivity", "currentUserId not passed via Intent or is -1.");
            finish(); // Tutup activity jika ID pengguna tidak valid
            return;
        }

        // Set email dari SharedPreferences dan buat tidak bisa diedit
        if (currentUserEmail != null) {
            etEmail.setText(currentUserEmail);
            etEmail.setEnabled(false);
        } else {
            // Jika email juga null, ada masalah dengan sesi pengguna
            Toast.makeText(this, "Email pengguna tidak ditemukan. Harap login ulang.", Toast.LENGTH_SHORT).show();
            Log.e("AddNewAddressActivity", "User email is null from SharedPreferences.");
            finish();
            return;
        }


        // --- Logika Spinner Provinsi dan Kota ---
        spProvinsi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Index 0 adalah "Pilih Provinsi"
                    Province p = provinceList.get(position - 1);
                    selectedProvinceId = Integer.parseInt(p.getProvince_id());
                    selectedProvinceName = p.getProvince();
                    Log.d("AddAddressSpinner", "Province selected: " + selectedProvinceName + " (ID: " + selectedProvinceId + ")");
                    loadCities(selectedProvinceId);
                } else {
                    selectedProvinceId = -1;
                    selectedProvinceName = "";
                    cityList.clear();
                    updateCitySpinner();
                    Log.d("AddAddressSpinner", "Province unselected. City spinner cleared.");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        spKota.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Index 0 adalah "Pilih Kota"
                    City c = cityList.get(position - 1);
                    selectedCityId = Integer.parseInt(c.getCity_id());
                    selectedCityName = c.getType() + " " + c.getCity_name();
                    Log.d("AddAddressSpinner", "City selected: " + selectedCityName + " (ID: " + selectedCityId + ")");
                } else {
                    selectedCityId = -1;
                    selectedCityName = "";
                    Log.d("AddAddressSpinner", "City unselected.");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        loadProvinces(); // Muat daftar provinsi saat aktivitas dimulai

        btnSaveAddress.setOnClickListener(v -> saveNewAddress());
    }

    private void saveNewAddress() {
        if (currentUserId == -1) {
            Toast.makeText(this, "ID pengguna tidak valid. Silakan coba lagi.", Toast.LENGTH_LONG).show();
            return;
        }

        String fullName = etNama.getText().toString().trim();
        String phoneNumber = etTelepon.getText().toString().trim();
        String address = etAlamat.getText().toString().trim();
        String postalCode = etKodePos.getText().toString().trim();
        boolean isDefault = cbIsDefault.isChecked(); // Ambil status CheckBox

        if (selectedProvinceId == -1 || selectedCityId == -1) {
            Toast.makeText(this, "Harap pilih Provinsi dan Kota.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fullName.isEmpty() || phoneNumber.isEmpty() || address.isEmpty() || postalCode.isEmpty()) {
            Toast.makeText(this, "Harap lengkapi semua kolom yang wajib diisi.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> addressData = new HashMap<>();
        addressData.put("pelanggan_id", currentUserId);
        addressData.put("nama", fullName);
        addressData.put("telepon", phoneNumber);
        addressData.put("alamat", address);
        addressData.put("kota", selectedCityName);
        addressData.put("provinsi", selectedProvinceName);
        addressData.put("kodepos", postalCode);
        addressData.put("is_default", isDefault ? "1" : "0");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RegisterAPI apiService = retrofit.create(RegisterAPI.class);
        retrofit2.Call<ResponseBody> call = apiService.addAddress(addressData);

        call.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseString = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseString);
                        String result = jsonResponse.getString("result");
                        String message = jsonResponse.getString("message");

                        Toast.makeText(AddNewAddressActivity.this, message, Toast.LENGTH_SHORT).show();

                        if (result.equals("1")) {
                            setResult(RESULT_OK);
                            finish();
                        }
                    } catch (Exception e) {
                        Log.e("AddNewAddressActivity", "Error parsing response: " + e.getMessage());
                        Toast.makeText(AddNewAddressActivity.this, "Kesalahan saat memproses respons.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e("AddNewAddressActivity", "Failed to add address: " + response.code() + " " + errorBody);
                        Toast.makeText(AddNewAddressActivity.this, "Gagal menambahkan alamat. Kode: " + response.code() + ". Error: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Log.e("AddNewAddressActivity", "Error reading error body: " + e.getMessage());
                        Toast.makeText(AddNewAddressActivity.this, "Gagal menambahkan alamat.", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                Log.e("AddNewAddressActivity", "Network error: " + t.getMessage());
                Toast.makeText(AddNewAddressActivity.this, "Kesalahan koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProvinces() {
        Request request = new Request.Builder()
                .url(PROVINCE_API_URL)
                .build();

        httpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(AddNewAddressActivity.this, "Gagal terhubung ke server provinsi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("AddAddressProvinces", "Connection error loading provinces", e);
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response.code() + " from provinces API: " + response.body().string());
                    }

                    String resStr = response.body().string();
                    Log.d("AddAddressProvinceResponse", "Raw JSON: " + resStr);

                    RajaOngkirResponseProvince provResponse = new Gson().fromJson(resStr, RajaOngkirResponseProvince.class);

                    if (provResponse != null && provResponse.getRajaongkir() != null && provResponse.getRajaongkir().getResults() != null) {
                        provinceList = provResponse.getRajaongkir().getResults();
                        runOnUiThread(() -> {
                            List<String> names = new ArrayList<>();
                            names.add("Pilih Provinsi");
                            for (Province p : provinceList) {
                                names.add(p.getProvince());
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(AddNewAddressActivity.this,
                                    android.R.layout.simple_spinner_item, names);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spProvinsi.setAdapter(adapter);
                            Log.d("AddAddressSpinner", "Province spinner updated with " + names.size() + " items.");
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(AddNewAddressActivity.this, "Data provinsi kosong atau tidak valid.", Toast.LENGTH_SHORT).show());
                        Log.w("AddAddressProvinces", "Empty or invalid province data received.");
                    }

                } catch (Exception e) {
                    runOnUiThread(() -> {
                        String errorMsg = "Error parsing provinsi JSON: " + e.getMessage();
                        Toast.makeText(AddNewAddressActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        Log.e("AddAddressParseProvinces", errorMsg, e);
                    });
                } finally {
                    if (response.body() != null) {
                        response.body().close();
                    }
                }
            }
        });
    }

    private void loadCities(int provinceId) {
        Request request = new Request.Builder()
                .url(CITY_API_URL + "?province_id=" + provinceId)
                .build();

        httpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(AddNewAddressActivity.this, "Gagal load kota: " + e.getMessage(), Toast.LENGTH_LONG).show());
                Log.e("AddAddressCities", "Connection error loading cities for province " + provinceId + ": " + e.getMessage(), e);
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String resStr = response.body().string();
                    Log.d("AddAddressCityResponse", "Raw JSON for cities (Province ID " + provinceId + "): " + resStr);
                    try {
                        RajaOngkirResponseCity cityRes = new Gson().fromJson(resStr, RajaOngkirResponseCity.class);

                        if (cityRes != null && cityRes.getRajaongkir() != null && cityRes.getRajaongkir().getResults() != null) {
                            cityList = cityRes.getRajaongkir().getResults();
                            runOnUiThread(() -> {
                                updateCitySpinner();
                            });
                        } else {
                            runOnUiThread(() -> Toast.makeText(AddNewAddressActivity.this, "Data kota kosong atau tidak valid.", Toast.LENGTH_SHORT).show());
                            Log.w("AddAddressCities", "Empty or invalid city data received for province " + provinceId);
                            cityList.clear();
                            updateCitySpinner();
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(AddNewAddressActivity.this, "Error parsing kota JSON: " + e.getMessage(), Toast.LENGTH_LONG).show());
                        Log.e("AddAddressParseCities", "Error parsing city JSON: " + e.getMessage(), e);
                        cityList.clear();
                        updateCitySpinner();
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(AddNewAddressActivity.this, "Gagal mendapatkan data kota dari server. Kode: " + response.code(), Toast.LENGTH_SHORT).show();
                        Log.e("AddAddressCities", "Failed to get city data. Code: " + response.code() + ", Message: " + response.message());
                        cityList.clear();
                        updateCitySpinner();
                    });
                }
            }
        });
    }

    private void updateCitySpinner() {
        List<String> cityNames = new ArrayList<>();
        cityNames.add("Pilih Kota");
        for (City c : cityList) {
            cityNames.add(c.getType() + " " + c.getCity_name());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(AddNewAddressActivity.this,
                android.R.layout.simple_spinner_item, cityNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spKota.setAdapter(adapter);
        Log.d("AddAddressSpinner", "City spinner updated with " + cityNames.size() + " items. First item: " + (cityNames.isEmpty() ? "N/A" : cityNames.get(0)));
    }

    // --- Data Model Classes (Disarankan untuk memindahkan ini ke file terpisah agar dapat digunakan kembali) ---
    // Pastikan kelas-kelas ini (RajaOngkirResponseProvince, Province, RajaOngkirResponseCity, City)
    // sudah didefinisikan dengan benar dan dapat diakses (misalnya, sebagai kelas public static nested
    // atau di file terpisah).

    public static class RajaOngkirResponseProvince {
        private Rajaongkir rajaongkir;
        public Rajaongkir getRajaongkir() { return rajaongkir; }

        public static class Rajaongkir {
            private List<Province> results;
            public List<Province> getResults() { return results; }
        }
    }

    public static class Province {
        @SerializedName("province_id")
        private String province_id;
        @SerializedName("province")
        private String province;

        public String getProvince_id() { return province_id; }
        public String getProvince() { return province; }
    }

    public static class RajaOngkirResponseCity {
        private Rajaongkir rajaongkir;
        public Rajaongkir getRajaongkir() { return rajaongkir; }

        public static class Rajaongkir {
            private List<City> results;
            public List<City> getResults() { return results; }
        }
    }

    public static class City {
        @SerializedName("city_id")
        private String city_id;
        @SerializedName("province_id")
        private String province_id;
        @SerializedName("province")
        private String province;
        @SerializedName("type")
        private String type;
        @SerializedName("city_name")
        private String city_name;
        @SerializedName("postal_code")
        private String postal_code;

        public String getCity_id() { return city_id; }
        public String getProvince_id() { return province_id; }
        public String getProvince() { return province; }
        public String getType() { return type; }
        public String getCity_name() { return city_name; }
        public String getPostal_code() { return postal_code; }
    }
}