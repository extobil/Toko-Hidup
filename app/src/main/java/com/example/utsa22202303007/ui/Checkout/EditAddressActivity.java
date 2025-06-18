package com.example.utsa22202303007.ui.Checkout;

import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.utsa22202303007.R;
import com.example.utsa22202303007.RegisterAPI; // Sesuaikan dengan lokasi RegisterAPI Anda
import com.example.utsa22202303007.ServerAPI; // Sesuaikan dengan lokasi ServerAPI Anda
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// Import model kelas yang dibutuhkan dari Checkout.java
import static com.example.utsa22202303007.ui.Checkout.Checkout.City;
import static com.example.utsa22202303007.ui.Checkout.Checkout.Province;
import static com.example.utsa22202303007.ui.Checkout.Checkout.RajaOngkirResponseCity;
import static com.example.utsa22202303007.ui.Checkout.Checkout.RajaOngkirResponseProvince;

public class EditAddressActivity extends AppCompatActivity {

    private TextView tvEmailDisplay;
    private EditText etFullName, etAddress, etPhoneNumber, etPostalCode;
    private Spinner spinnerProvinsi, spinnerKota;
    private CheckBox cbIsDefault;
    private Button btnSaveAddress;

    private int userId;
    private AddressItem addressToEdit; // Alamat yang akan diedit
    private int currentAddressId; // ID dari alamat yang sedang diedit

    private List<Province> provinceList = new ArrayList<>();
    private List<City> cityList = new ArrayList<>();

    private ArrayAdapter<String> provinceAdapter;
    private ArrayAdapter<String> cityAdapter;

    private int selectedProvinceId = -1;
    private int selectedCityId = -1;

    private OkHttpClient httpClient = new OkHttpClient(); // Gunakan OkHttpClient yang sama untuk RajaOngkir

    private static final String PROVINCE_API_URL = ServerAPI.BASE_URL + "get_provinsi.php";
    private static final String CITY_API_URL = ServerAPI.BASE_URL + "get_kota.php";
    private static final String UPDATE_ADDRESS_API_URL = ServerAPI.BASE_URL + "update_address.php"; // API untuk update alamat

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_address);

        // Inisialisasi View
        tvEmailDisplay = findViewById(R.id.tvEmailDisplay);
        etFullName = findViewById(R.id.etFullName);
        etAddress = findViewById(R.id.etAddress);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etPostalCode = findViewById(R.id.etPostalCode);
        spinnerProvinsi = findViewById(R.id.spinnerProvinsi);
        spinnerKota = findViewById(R.id.spinnerKota);
        cbIsDefault = findViewById(R.id.cbIsDefault);
        btnSaveAddress = findViewById(R.id.btnSaveAddress);

        // Dapatkan data user_id dari SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE);
        userId = sharedPreferences.getInt("user_id", -1);
        String userEmail = sharedPreferences.getString("email", "N/A"); // Ambil email untuk ditampilkan
        tvEmailDisplay.setText(userEmail);

        if (userId == -1) {
            Toast.makeText(this, "ID Pengguna tidak ditemukan. Silakan login kembali.", Toast.LENGTH_LONG).show();
            finish(); // Tutup Activity jika user ID tidak ada
            return;
        }

        // Dapatkan objek AddressItem dari Intent
        String addressJson = getIntent().getStringExtra("address_data");
        if (addressJson != null) {
            addressToEdit = new Gson().fromJson(addressJson, AddressItem.class);
            if (addressToEdit != null) {
                currentAddressId = addressToEdit.getId(); // Simpan ID alamat yang akan diedit
                fillFormWithAddressData(addressToEdit);
            } else {
                Toast.makeText(this, "Gagal memuat data alamat untuk diedit.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        } else {
            Toast.makeText(this, "Tidak ada data alamat yang diberikan.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Inisialisasi Spinner Adapter
        provinceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvinsi.setAdapter(provinceAdapter);

        cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKota.setAdapter(cityAdapter);

        // Load Provinsi dan Kota
        loadProvinces();

        // Listener untuk Spinner Provinsi
        spinnerProvinsi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Lewati "Pilih Provinsi"
                    Province selectedProv = provinceList.get(position - 1); // -1 karena ada "Pilih Provinsi"
                    try {
                        selectedProvinceId = Integer.parseInt(selectedProv.getProvince_id());
                        loadCities(selectedProvinceId);
                    } catch (NumberFormatException e) {
                        Log.e("EditAddress", "Invalid province ID format: " + selectedProv.getProvince_id(), e);
                        Toast.makeText(EditAddressActivity.this, "ID Provinsi tidak valid.", Toast.LENGTH_SHORT).show();
                        clearCitySpinner();
                    }
                } else {
                    selectedProvinceId = -1;
                    clearCitySpinner();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedProvinceId = -1;
                clearCitySpinner();
            }
        });

        // Listener untuk Spinner Kota
        spinnerKota.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Lewati "Pilih Kota"
                    City selectedCity = cityList.get(position - 1); // -1 karena ada "Pilih Kota"
                    try {
                        selectedCityId = Integer.parseInt(selectedCity.getCity_id());
                    } catch (NumberFormatException e) {
                        Log.e("EditAddress", "Invalid city ID format: " + selectedCity.getCity_id(), e);
                        Toast.makeText(EditAddressActivity.this, "ID Kota tidak valid.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    selectedCityId = -1;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCityId = -1;
            }
        });

        // Listener untuk tombol Simpan
        btnSaveAddress.setOnClickListener(v -> saveAddress());
    }

    private void fillFormWithAddressData(AddressItem address) {
        etFullName.setText(address.getFullName());
        etAddress.setText(address.getAddress());
        etPhoneNumber.setText(address.getPhoneNumber());
        etPostalCode.setText(address.getPostalCode());
        cbIsDefault.setChecked(address.isDefault());
        // Provinsi dan Kota akan diset setelah data RajaOngkir dimuat
    }

    private void loadProvinces() {
        Request request = new Request.Builder()
                .url(PROVINCE_API_URL)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(EditAddressActivity.this, "Gagal memuat provinsi: " + e.getMessage(), Toast.LENGTH_LONG).show());
                Log.e("EditAddress", "Failed to load provinces", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String resStr = response.body().string();
                        RajaOngkirResponseProvince provResponse = new Gson().fromJson(resStr, RajaOngkirResponseProvince.class);

                        if (provResponse != null && provResponse.getRajaongkir() != null && provResponse.getRajaongkir().getResults() != null) {
                            provinceList = provResponse.getRajaongkir().getResults();
                            List<String> provinceNames = new ArrayList<>();
                            provinceNames.add("Pilih Provinsi"); // Placeholder
                            for (Province p : provinceList) {
                                provinceNames.add(p.getProvince());
                            }
                            runOnUiThread(() -> {
                                provinceAdapter.clear();
                                provinceAdapter.addAll(provinceNames);
                                provinceAdapter.notifyDataSetChanged();
                                // Setelah provinsi dimuat, coba pilih provinsi yang sesuai dari alamat yang diedit
                                setSelectedProvinceInSpinner();
                            });
                        } else {
                            runOnUiThread(() -> Toast.makeText(EditAddressActivity.this, "Data provinsi kosong atau tidak valid.", Toast.LENGTH_SHORT).show());
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(EditAddressActivity.this, "Error parsing provinsi: " + e.getMessage(), Toast.LENGTH_LONG).show());
                        Log.e("EditAddress", "Error parsing province JSON", e);
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(EditAddressActivity.this, "Gagal memuat provinsi. Kode: " + response.code(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void setSelectedProvinceInSpinner() {
        if (addressToEdit != null && !provinceList.isEmpty()) {
            String currentProvinceName = addressToEdit.getProvince();
            int position = 0;
            for (int i = 0; i < provinceList.size(); i++) {
                if (provinceList.get(i).getProvince().equalsIgnoreCase(currentProvinceName)) {
                    position = i + 1; // +1 karena ada placeholder "Pilih Provinsi"
                    break;
                }
            }
            spinnerProvinsi.setSelection(position);
            // Ini akan memicu onItemSelectedListener untuk memuat kota secara otomatis
        }
    }

    private void loadCities(int provinceId) {
        Request request = new Request.Builder()
                .url(CITY_API_URL + "?province_id=" + provinceId)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(EditAddressActivity.this, "Gagal memuat kota: " + e.getMessage(), Toast.LENGTH_LONG).show());
                Log.e("EditAddress", "Failed to load cities", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String resStr = response.body().string();
                        RajaOngkirResponseCity cityRes = new Gson().fromJson(resStr, RajaOngkirResponseCity.class);

                        if (cityRes != null && cityRes.getRajaongkir() != null && cityRes.getRajaongkir().getResults() != null) {
                            cityList = cityRes.getRajaongkir().getResults();
                            List<String> cityNames = new ArrayList<>();
                            cityNames.add("Pilih Kota"); // Placeholder
                            for (City c : cityList) {
                                cityNames.add(c.getType() + " " + c.getCity_name());
                            }
                            runOnUiThread(() -> {
                                cityAdapter.clear();
                                cityAdapter.addAll(cityNames);
                                cityAdapter.notifyDataSetChanged();
                                // Setelah kota dimuat, coba pilih kota yang sesuai dari alamat yang diedit
                                setSelectedCityInSpinner();
                            });
                        } else {
                            runOnUiThread(() -> Toast.makeText(EditAddressActivity.this, "Data kota kosong atau tidak valid.", Toast.LENGTH_SHORT).show());
                            clearCitySpinner();
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(EditAddressActivity.this, "Error parsing kota: " + e.getMessage(), Toast.LENGTH_LONG).show());
                        Log.e("EditAddress", "Error parsing city JSON", e);
                        clearCitySpinner();
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(EditAddressActivity.this, "Gagal memuat kota. Kode: " + response.code(), Toast.LENGTH_SHORT).show());
                    clearCitySpinner();
                }

            }
        });
    }

    private void setSelectedCityInSpinner() {
        if (addressToEdit != null && !cityList.isEmpty()) {
            String currentCityName = addressToEdit.getCity();
            int position = 0;
            for (int i = 0; i < cityList.size(); i++) {
                // Periksa kedua format: "Tipe Kota Nama Kota" dan "Nama Kota"
                if ((cityList.get(i).getType() + " " + cityList.get(i).getCity_name()).equalsIgnoreCase(currentCityName) ||
                        cityList.get(i).getCity_name().equalsIgnoreCase(currentCityName)) {
                    position = i + 1; // +1 karena ada placeholder "Pilih Kota"
                    break;
                }
            }
            spinnerKota.setSelection(position);
        }
    }

    private void clearCitySpinner() {
        cityAdapter.clear();
        cityAdapter.add("Pilih Kota");
        cityAdapter.notifyDataSetChanged();
        spinnerKota.setSelection(0);
        selectedCityId = -1;
    }

    private void saveAddress() {
        final String fullName = etFullName.getText().toString().trim();
        final String addressLine = etAddress.getText().toString().trim();
        final String phoneNumber = etPhoneNumber.getText().toString().trim();
        final String postalCode = etPostalCode.getText().toString().trim();
        final boolean isDefault = cbIsDefault.isChecked();

        if (fullName.isEmpty() || addressLine.isEmpty() || phoneNumber.isEmpty() || postalCode.isEmpty() || selectedProvinceId == -1 || selectedCityId == -1) {
            Toast.makeText(this, "Mohon lengkapi semua data alamat dan pilih provinsi/kota.", Toast.LENGTH_LONG).show();
            return;
        }

        final String selectedProvinceName;
        if (spinnerProvinsi.getSelectedItemPosition() > 0) {
            selectedProvinceName = provinceList.get(spinnerProvinsi.getSelectedItemPosition() - 1).getProvince();
        } else {
            selectedProvinceName = "";
        }

        final String selectedCityName;
        if (spinnerKota.getSelectedItemPosition() > 0) {
            City chosenCity = cityList.get(spinnerKota.getSelectedItemPosition() - 1);
            selectedCityName = (chosenCity.getType() + " " + chosenCity.getCity_name()).trim();
        } else {
            selectedCityName = "";
        }


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RegisterAPI api = retrofit.create(RegisterAPI.class);

        api.updateAddress(
                currentAddressId, // Ini adalah ID alamat (kolom 'id' di DB)
                userId,           // Ini adalah pelanggan_id
                fullName,
                phoneNumber,
                addressLine,
                selectedCityName,
                selectedProvinceName,
                postalCode,
                isDefault ? 1 : 0
        ).enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        Log.d("UpdateAddress", "Raw Response: " + responseBody); // Log respons mentah

                        JSONObject json = new JSONObject(responseBody);
                        String result = json.optString("result");
                        String message = json.optString("message");

                        if ("1".equals(result)) {
                            Toast.makeText(EditAddressActivity.this, "Alamat berhasil diperbarui!", Toast.LENGTH_SHORT).show();
                            Intent resultIntent = new Intent();
                            AddressItem updatedAddress = new AddressItem(
                                    currentAddressId,
                                    userId,
                                    fullName,
                                    phoneNumber,
                                    addressLine,
                                    selectedCityName,
                                    selectedProvinceName,
                                    postalCode,
                                    isDefault
                            );
                            resultIntent.putExtra("updated_address", new Gson().toJson(updatedAddress));
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } else {
                            showMessage("Gagal memperbarui alamat: " + message);
                        }
                    } catch (JSONException | IOException e) {
                        Log.e("UpdateAddress", "Error parsing response: " + e.getMessage(), e);
                        showMessage("Terjadi kesalahan saat memproses respons server.");
                    }
                } else {
                    String errorBody = "N/A";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e("UpdateAddress", "Error reading errorBody: " + e.getMessage());
                    }
                    Log.e("UpdateAddress", "Gagal memperbarui alamat. Kode: " + response.code() + ", Error Body: " + errorBody);
                    showMessage("Gagal memperbarui alamat. Respon server tidak valid. Kode: " + response.code() + ". Detail: " + errorBody);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                Log.e("UpdateAddress", "Koneksi gagal: " + t.getMessage(), t);
                showMessage("Koneksi gagal saat memperbarui alamat: " + t.getMessage());
            }
        });
    }

    private void showMessage(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .create()
                .show();
    }
}