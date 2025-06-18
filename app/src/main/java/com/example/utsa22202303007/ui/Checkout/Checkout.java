package com.example.utsa22202303007.ui.Checkout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.utsa22202303007.R;
import com.example.utsa22202303007.ServerAPI;
import com.example.utsa22202303007.RegisterAPI; // Pastikan ini diimpor
import com.example.utsa22202303007.ui.dashboard.OrderAdapterCheckout;
import com.example.utsa22202303007.ui.dashboard.OrderHelper;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray; // Pastikan ini diimpor
import org.json.JSONException; // Pastikan ini diimpor
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.example.utsa22202303007.ui.dashboard.OrderItem;

public class Checkout extends AppCompatActivity implements ShippingOptionAdapter.OnOptionSelectedListener {

    private RecyclerView recyclerViewOrders;
    private RecyclerView recyclerViewShippingOptions;
    private TextView tvShippingCost, tvTotalAll, tvSubtotal, tvEstimatedDelivery;
    private Button btnCheckout;
    private RadioGroup rgPaymentMethod;
    private CardView etAlamatCardView;
    private TextView tvFullName, tvAddress, tvPhoneNumber, tvKodepos, tvProvince, tvCity;

    private List<OrderItem> orderItems;

    private List<Province> provinceList = new ArrayList<>();
    private List<City> cityList = new ArrayList<>();
    private List<String> courierOptions = Arrays.asList("jne", "tiki", "pos");

    private List<ShippingOption> allShippingOptions = Collections.synchronizedList(new ArrayList<>());
    private ShippingOption selectedShippingOption = null;

    private ShippingOptionAdapter shippingOptionAdapter;

    private int selectedProvinceId = -1;
    private int selectedCityId = -1;
    private int totalWeight = 40000;
    private double taxAmount = 0;
    private double shippingCost = 0;
    private double subtotal = 0;
    private String estimatedDelivery = "-";
    private String selectedPaymentMethod = "COD";

    private static final int originCityId = 181; // ID Kota asal (contoh: Jakarta)
    private static final String PHP_API_URL = ServerAPI.BASE_URL + "ongkir.php";
    private static final String PROVINCE_API_URL = ServerAPI.BASE_URL + "get_provinsi.php";
    private static final String CITY_API_URL = ServerAPI.BASE_URL + "get_kota.php";
    private static final String GET_ADDRESS_API_URL = ServerAPI.BASE_URL + "get_address.php";

    private OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    private String userEmail; // Variabel ini sebenarnya tidak lagi diperlukan untuk mengambil user_id jika sudah tersimpan
    private int currentUserId = -1;

    private String currentFullName = "";
    private String currentAddress = "";
    private String currentPhoneNumber = "";
    private String currentPostalCode = "";
    private String currentProvinceName = "";
    private String currentCityName = "";
    private boolean isAddressFromProfile = true;

    private static final int SELECT_ADDRESS_REQUEST_CODE = 1;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Inisialisasi View
        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        recyclerViewShippingOptions = findViewById(R.id.recyclerViewShippingOptions);
        tvShippingCost = findViewById(R.id.tvShippingCost);
        tvTotalAll = findViewById(R.id.tvTotalAll);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        btnCheckout = findViewById(R.id.btnCheckout);
        tvEstimatedDelivery = findViewById(R.id.tvEstimatedDelivery);

        tvFullName = findViewById(R.id.tvFullName);
        tvAddress = findViewById(R.id.tvAddress);
        tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
        tvKodepos = findViewById(R.id.tvkodepos);
        tvProvince = findViewById(R.id.tvProvince);
        tvCity = findViewById(R.id.tvCity);
        etAlamatCardView = findViewById(R.id.etalamat);

        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);

        // Listener untuk tombol/cardview pilih alamat
        etAlamatCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Checkout.this, Address.class);
                intent.putExtra("current_user_id", currentUserId);
                startActivityForResult(intent, SELECT_ADDRESS_REQUEST_CODE);
            }
        });

        // Ambil User ID dari SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE);
        // Pastikan kunci "email" konsisten dengan apa yang disimpan di registrasi/login
        userEmail = sharedPreferences.getString("email", ""); // Mengambil email (opsional, untuk debug atau keperluan lain)
        currentUserId = sharedPreferences.getInt("user_id", -1); // Ambil user_id yang sudah disimpan

        Log.d("Checkout", "Retrieved currentUserId: " + currentUserId + ", Email: " + userEmail);


        rgPaymentMethod.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRadioButton = findViewById(checkedId);
            if (selectedRadioButton != null) {
                selectedPaymentMethod = selectedRadioButton.getText().toString().equals("Cash On Delivery (COD)") ? "cod" : "transfer";
            }
        });

        // Memuat daftar pesanan
        String json = getIntent().getStringExtra("orderList");
        if (json != null && !json.isEmpty()) {
            try {
                orderItems = new Gson().fromJson(json, new TypeToken<List<OrderItem>>(){}.getType());
            } catch (Exception e) {
                Log.e("Checkout", "Error parsing order list JSON: " + e.getMessage());
                orderItems = new ArrayList<>();
                Toast.makeText(this, "Gagal memuat daftar pesanan.", Toast.LENGTH_LONG).show();
            }
        } else {
            orderItems = new ArrayList<>();
            Toast.makeText(this, "Daftar pesanan kosong.", Toast.LENGTH_SHORT).show();
        }

        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOrders.setAdapter(new OrderAdapterCheckout(this, orderItems));

        calculateTotals();

        recyclerViewShippingOptions.setLayoutManager(new LinearLayoutManager(this));
        shippingOptionAdapter = new ShippingOptionAdapter(new ArrayList<>(), this, this);
        recyclerViewShippingOptions.setAdapter(shippingOptionAdapter);

        // --- Ambil Alamat Default DULU, Lalu Load Provinsi & Kota ---
        if (currentUserId != -1) {
            getAddressForCheckout(currentUserId); // Ambil alamat default berdasarkan user ID
        } else {
            Toast.makeText(this, "ID Pengguna tidak ditemukan. Harap login kembali.", Toast.LENGTH_LONG).show();
            clearShippingOptions();
        }

        // Listener untuk tombol Checkout
        btnCheckout.setOnClickListener(v -> {
            if (selectedShippingOption == null) {
                Toast.makeText(Checkout.this, "Silakan pilih metode pengiriman terlebih dahulu.", Toast.LENGTH_SHORT).show();
                return;
            }

            sendOrderToServer();
        });

    }

    private void sendOrderToServer() {
            SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        int pelangganId = prefs.getInt("user_id", -1);
            String sessionEmail = prefs.getString("email", "");

            JSONObject orderObj = new JSONObject();
            JSONArray orderDetailArray = new JSONArray();

            try {
                // ===== Bagian order (header) =====
                orderObj.put("id", pelangganId); // <-- pastikan kamu punya ID pelanggan dari session atau response login
                orderObj.put("nama_kirim", tvFullName.getText().toString());
                orderObj.put("email_kirim", sessionEmail);
                orderObj.put("telp_kirim", tvPhoneNumber.getText().toString());
                orderObj.put("alamat_kirim", tvAddress.getText().toString());
                orderObj.put("kota_kirim", tvCity.getText().toString());
                orderObj.put("provinsi_kirim", tvProvince.getText().toString());
                orderObj.put("kodepos_kirim", tvKodepos.getText().toString().replaceAll("[^0-9]", ""));
                orderObj.put("lama_kirim", estimatedDelivery); // dari ongkir
                orderObj.put("subtotal", subtotal);
                orderObj.put("ongkir", shippingCost);
                orderObj.put("total_bayar", subtotal + shippingCost);
                orderObj.put("metode_bayar", rgPaymentMethod); // atau "cod" sesuai pilihan user
                orderObj.put("bukti_bayar", ""); // bisa dikosongkan saat awal
                orderObj.put("status", "menunggu"); // default status awal

                // ===== Bagian detail order =====
                for (OrderItem item : orderItems) {
                    JSONObject itemObj = new JSONObject();
                    itemObj.put("kode", item.getKode());
                    itemObj.put("harga", item.getHargajual());
                    itemObj.put("qty", item.getQuantity());
                    orderDetailArray.put(itemObj);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Gagal membangun data pesanan.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kirim ke server sebagai application/x-www-form-urlencoded
            RequestBody requestBody = new FormBody.Builder()
                    .add("order", orderObj.toString())
                    .add("order_detail", orderDetailArray.toString())
                    .build();

            Request request = new Request.Builder()
                    .url(ServerAPI.BASE_URL + "post_pesan.php")
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(Checkout.this, "Gagal terhubung ke server: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String resStr = response.body().string();
                    Log.d("OrderResponse", resStr);

                    runOnUiThread(() -> {
                        try {
                            JSONObject res = new JSONObject(resStr);
                            int kode = res.getInt("kode");
                            if (kode == 1) {
                                OrderHelper orderHelper = new OrderHelper(Checkout.this);
                                orderHelper.clearOrders();
                                Toast.makeText(Checkout.this, "Pesanan berhasil dibuat!", Toast.LENGTH_LONG).show();
                                // Arahkan ke halaman sukses atau kosongkan keranjang
                                finish();
                            } else {
                                Toast.makeText(Checkout.this, "Gagal membuat pesanan: " + res.getString("pesan"), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(Checkout.this, "Format respon tidak valid dari server.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_ADDRESS_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String selectedAddressJson = data.getStringExtra("selectedAddress");
            if (selectedAddressJson != null) {
                AddressItem selectedAddress = new Gson().fromJson(selectedAddressJson, AddressItem.class);
                if (selectedAddress != null) {
                    updateAddressUI(selectedAddress);
                    isAddressFromProfile = false; // Menandakan alamat dipilih manual, bukan default

                    // Update variabel alamat yang digunakan untuk perhitungan ongkir
                    currentFullName = selectedAddress.getFullName();
                    currentAddress = selectedAddress.getAddress();
                    currentPhoneNumber = selectedAddress.getPhoneNumber();
                    currentPostalCode = selectedAddress.getPostalCode();
                    currentProvinceName = selectedAddress.getProvince();
                    currentCityName = selectedAddress.getCity();

                    // Setelah memilih alamat baru, muat ulang daftar provinsi/kota RajaOngkir
                    // untuk memastikan ID sesuai dengan alamat yang baru dipilih.
                    loadProvincesAndMapAddress();
                }
            }
        }
    }

    /**
     * Mengambil alamat default dari API berdasarkan userId.
     * Setelah berhasil, akan memuat data provinsi RajaOngkir.
     */
    private void getAddressForCheckout(int userId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RegisterAPI api = retrofit.create(RegisterAPI.class);
        api.getAddress(userId).enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        Log.d("CheckoutAddress", "Raw Address JSON: " + responseBody);

                        // --- PERBAIKAN UTAMA DI SINI ---
                        // Parse responseBody langsung sebagai JSONArray
                        JSONArray dataArray = new JSONArray(responseBody);

                        // Konversi JSONArray ke List<AddressItem> menggunakan Gson
                        List<AddressItem> addressList = new Gson().fromJson(
                                dataArray.toString(), // Konversi JSONArray kembali ke String untuk Gson
                                new TypeToken<List<AddressItem>>(){}.getType()
                        );

                        if (!addressList.isEmpty()) {
                            // Cari alamat yang is_default = true, jika tidak ada, ambil yang pertama
                            AddressItem defaultAddress = null;
                            for (AddressItem address : addressList) {
                                if (address.isDefault()) { // Memanggil method isDefault() dari AddressItem
                                    defaultAddress = address;
                                    break;
                                }
                            }
                            if (defaultAddress == null) {
                                defaultAddress = addressList.get(0); // Ambil yang pertama jika tidak ada default
                            }

                            // Update variabel alamat dengan alamat default/pertama
                            currentFullName = defaultAddress.getFullName();
                            currentProvinceName = defaultAddress.getProvince();
                            currentCityName = defaultAddress.getCity();
                            currentAddress = defaultAddress.getAddress();
                            currentPhoneNumber = defaultAddress.getPhoneNumber();
                            currentPostalCode = defaultAddress.getPostalCode();

                            // Update UI dengan alamat yang ditemukan
                            updateAddressUI(defaultAddress);

                            Log.d("CheckoutAddress", "Data alamat default berhasil dimuat dari server.");
                            // Lanjutkan dengan memuat provinsi RajaOngkir setelah alamat berhasil diambil
                            loadProvincesAndMapAddress();
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(Checkout.this, "Tidak ada alamat ditemukan untuk pengguna ini. Mohon tambahkan alamat.", Toast.LENGTH_LONG).show();
                                clearShippingOptions(); // Kosongkan opsi pengiriman
                            });
                        }
                    } catch (JSONException e) { // Tangkap JSONException secara spesifik
                        Log.e("CheckoutAddress", "Error parsing address data: " + e.getMessage(), e);
                        runOnUiThread(() -> {
                            Toast.makeText(Checkout.this, "Terjadi kesalahan format data alamat dari server.", Toast.LENGTH_LONG).show();
                            clearShippingOptions();
                        });
                    } catch (IOException e) {
                        Log.e("CheckoutAddress", "IOException reading response body: " + e.getMessage(), e);
                        runOnUiThread(() -> {
                            Toast.makeText(Checkout.this, "Terjadi kesalahan saat membaca respons alamat.", Toast.LENGTH_LONG).show();
                            clearShippingOptions();
                        });
                    } catch (Exception e) { // Tangkap error umum lainnya
                        Log.e("CheckoutAddress", "Unexpected error in getAddressForCheckout: " + e.getMessage(), e);
                        runOnUiThread(() -> {
                            Toast.makeText(Checkout.this, "Terjadi kesalahan tidak terduga saat memuat alamat.", Toast.LENGTH_LONG).show();
                            clearShippingOptions();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        String errorMessage = "Gagal mengambil data alamat dari server. Kode: " + response.code();
                        if (response.message() != null && !response.message().isEmpty()) {
                            errorMessage += " - " + response.message();
                        }
                        Toast.makeText(Checkout.this, errorMessage, Toast.LENGTH_SHORT).show();
                        Log.e("CheckoutAddress", errorMessage);
                        clearShippingOptions();
                    });
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                Log.e("CheckoutAddress", "Gagal terhubung ke server untuk alamat: " + t.getMessage(), t);
                runOnUiThread(() -> {
                    Toast.makeText(Checkout.this, "Terjadi kesalahan koneksi saat memuat alamat.", Toast.LENGTH_SHORT).show();
                    clearShippingOptions();
                });
            }
        });
    }

    /**
     * Memuat daftar provinsi dari RajaOngkir, kemudian memetakan alamat yang ada ke ID RajaOngkir.
     * Dipanggil setelah alamat default berhasil diambil atau setelah pengguna memilih alamat baru.
     */
    private void loadProvincesAndMapAddress() {
        Request request = new Request.Builder()
                .url(PROVINCE_API_URL)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(Checkout.this, "Gagal terhubung ke server provinsi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("LoadProvinces", "Connection error", e);
                });
                clearShippingOptions();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }

                    String resStr = response.body().string();
                    Log.d("ProvinceResponse", "Raw JSON: " + resStr);

                    RajaOngkirResponseProvince provResponse = new Gson().fromJson(resStr, RajaOngkirResponseProvince.class);

                    if (provResponse != null && provResponse.getRajaongkir() != null && provResponse.getRajaongkir().getResults() != null) {
                        provinceList = provResponse.getRajaongkir().getResults();
                        runOnUiThread(() -> {
                            // Setelah provinsi dimuat, petakan alamat yang sudah di-set ke ID RajaOngkir
                            mapAddressToRajaOngkirIds(currentProvinceName, currentCityName);
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(Checkout.this, "Data provinsi kosong atau tidak valid.", Toast.LENGTH_SHORT).show());
                        clearShippingOptions();
                    }

                } catch (Exception e) {
                    runOnUiThread(() -> {
                        String errorMsg = "Error parsing provinsi JSON: " + e.getMessage();
                        Toast.makeText(Checkout.this, errorMsg, Toast.LENGTH_LONG).show();
                        Log.e("ParseProvinces", errorMsg, e);
                    });
                    clearShippingOptions();
                } finally {
                    if (response.body() != null) {
                        response.body().close();
                    }
                }
            }
        });
    }

    /**
     * Memperbarui elemen UI alamat dengan data dari objek AddressItem.
     * @param address Objek AddressItem yang berisi data alamat.
     */
    private void updateAddressUI(AddressItem address) {
        tvFullName.setText(address.getFullName());
        tvAddress.setText(address.getAddress());
        tvPhoneNumber.setText(address.getPhoneNumber());
        tvKodepos.setText("(" + address.getPostalCode() + ")");
        tvProvince.setText(address.getProvince());
        tvCity.setText(address.getCity());
    }

    /**
     * Memetakan nama provinsi dan kota dari alamat ke ID RajaOngkir yang sesuai.
     * Setelah mendapatkan ID provinsi, akan memanggil loadCitiesForMapping.
     * @param provinceName Nama provinsi dari alamat.
     * @param cityName Nama kota dari alamat.
     */
    private void mapAddressToRajaOngkirIds(String provinceName, String cityName) {
        if (provinceName.isEmpty() || cityName.isEmpty()) {
            Toast.makeText(this, "Nama provinsi atau kota kosong. Tidak dapat menghitung ongkir.", Toast.LENGTH_LONG).show();
            clearShippingOptions();
            return;
        }

        selectedProvinceId = -1;
        for (Province p : provinceList) {
            if (p.getProvince().equalsIgnoreCase(provinceName)) {
                try {
                    selectedProvinceId = Integer.parseInt(p.getProvince_id());
                    break;
                } catch (NumberFormatException e) {
                    Log.e("MapAddress", "Invalid province ID format: " + p.getProvince_id());
                }
            }
        }

        if (selectedProvinceId != -1) {
            loadCitiesForMapping(selectedProvinceId, cityName);
        } else {
            runOnUiThread(() -> Toast.makeText(this, "Provinsi '" + provinceName + "' tidak ditemukan di daftar RajaOngkir. Mohon perbarui alamat Anda.", Toast.LENGTH_LONG).show());
            clearShippingOptions();
        }
    }

    /**
     * Memuat daftar kota dari RajaOngkir berdasarkan provinceId, lalu memetakan nama kota ke ID kota.
     * Setelah mendapatkan ID kota, akan memanggil getCostsForAllCouriers.
     * @param provinceId ID provinsi dari RajaOngkir.
     * @param cityName Nama kota dari alamat yang akan dipetakan.
     */
    private void loadCitiesForMapping(int provinceId, String cityName) {
        Request request = new Request.Builder()
                .url(CITY_API_URL + "?province_id=" + provinceId)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(Checkout.this, "Gagal load kota untuk mapping: " + e.getMessage(), Toast.LENGTH_LONG).show());
                clearShippingOptions();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String resStr = response.body().string();
                    Log.d("CityResponseMapping", "Raw JSON: " + resStr);
                    try {
                        RajaOngkirResponseCity cityRes = new Gson().fromJson(resStr, RajaOngkirResponseCity.class);

                        if (cityRes != null && cityRes.getRajaongkir() != null && cityRes.getRajaongkir().getResults() != null) {
                            cityList = cityRes.getRajaongkir().getResults(); // Perbarui cityList global
                            runOnUiThread(() -> {
                                selectedCityId = -1;
                                for (City c : cityList) {
                                    // Periksa kedua format: "Tipe Kota Nama Kota" dan "Nama Kota"
                                    if ((c.getType() + " " + c.getCity_name()).equalsIgnoreCase(cityName) ||
                                            c.getCity_name().equalsIgnoreCase(cityName)) {
                                        try {
                                            selectedCityId = Integer.parseInt(c.getCity_id());
                                            break;
                                        } catch (NumberFormatException e) {
                                            Log.e("MapAddress", "Invalid city ID format: " + c.getCity_id());
                                        }
                                    }
                                }

                                if (selectedCityId != -1) {
                                    Log.d("Checkout", "Successfully mapped city to ID: " + selectedCityId);
                                    getCostsForAllCouriers(); // Lanjutkan untuk mendapatkan biaya pengiriman
                                } else {
                                    runOnUiThread(() -> Toast.makeText(Checkout.this, "Kota '" + cityName + "' tidak ditemukan di daftar RajaOngkir untuk provinsi ini. Mohon perbarui alamat Anda.", Toast.LENGTH_LONG).show());
                                    clearShippingOptions();
                                }
                            });
                        } else {
                            runOnUiThread(() -> Toast.makeText(Checkout.this, "Data kota kosong atau tidak valid.", Toast.LENGTH_SHORT).show());
                            clearShippingOptions();
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(Checkout.this, "Error parsing kota JSON saat mapping: " + e.getMessage(), Toast.LENGTH_LONG).show());
                        Log.e("ParseCitiesMapping", "Error parsing JSON: " + e.getMessage(), e);
                        clearShippingOptions();
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(Checkout.this, "Gagal mendapatkan data kota dari server saat mapping. Kode: " + response.code(), Toast.LENGTH_SHORT).show();
                        clearShippingOptions();
                    });
                }
            }
        });
    }

    @Override
    public void onOptionSelected(ShippingOption option) {
        selectedShippingOption = option;
        shippingCost = option.getCostValue();
        estimatedDelivery = option.getEtd();
        Log.d("ShippingOption", "Opsi pengiriman dipilih: " + option.getDisplayName() + ", Biaya: " + shippingCost);
        updatePaymentDetails();
    }

    private void clearShippingOptions() {
        selectedShippingOption = null;
        allShippingOptions.clear();
        shippingCost = 0;
        estimatedDelivery = "-";
        runOnUiThread(() -> {
            shippingOptionAdapter.updateOptions(new ArrayList<>());
            updatePaymentDetails();
        });
    }

    private void calculateTotals() {
        subtotal = 0;
        totalWeight = 0;
        for (OrderItem order : orderItems) {
            subtotal += order.getHargajual() * order.getQuantity();
            totalWeight += 100 * order.getQuantity(); // Asumsi 100 gram per item
        }
        if (totalWeight == 0) {
            totalWeight = 1000; // Default 1kg jika tidak ada item
        }
        tvSubtotal.setText(String.format("Subtotal: Rp %,.0f", subtotal));
        updatePaymentDetails();
    }

    /**
     * Mengambil biaya pengiriman untuk semua kurir yang ditentukan.
     * Membutuhkan originCityId, selectedCityId, dan totalWeight.
     */
    private void getCostsForAllCouriers() {
        if (selectedCityId == -1) {
            runOnUiThread(() -> Toast.makeText(Checkout.this, "ID kota tujuan tidak ditemukan. Tidak dapat menghitung ongkir.", Toast.LENGTH_LONG).show());
            clearShippingOptions();
            return;
        }

        runOnUiThread(() -> {
            Toast.makeText(Checkout.this, "Mencari opsi pengiriman...", Toast.LENGTH_SHORT).show();
            clearShippingOptions(); // Hapus opsi sebelumnya saat mencari yang baru
        });

        CountDownLatch latch = new CountDownLatch(courierOptions.size());
        final List<ShippingOption> tempShippingOptions = Collections.synchronizedList(new ArrayList<>());

        for (String courier : courierOptions) {
            String formBody = "origin=" + originCityId +
                    "&destination=" + selectedCityId +
                    "&weight=" + totalWeight +
                    "&courier=" + courier;

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/x-www-form-urlencoded"),
                    formBody
            );

            Request request = new Request.Builder()
                    .url(PHP_API_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("GetCostAll", "Gagal terhubung ke server untuk kurir " + courier + ": " + e.getMessage());
                    latch.countDown();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            String resStr = response.body().string();
                            Log.d("CostResponse_" + courier, "Raw JSON: " + resStr);
                            CostResponse costResponse = new Gson().fromJson(resStr, CostResponse.class);

                            if (costResponse != null && costResponse.rajaongkir != null &&
                                    costResponse.rajaongkir.results != null && !costResponse.rajaongkir.results.isEmpty()) {

                                CostResponse.Result result = costResponse.rajaongkir.results.get(0);
                                if (result.costs != null && !result.costs.isEmpty()) {
                                    for (CostResponse.Service service : result.costs) {
                                        if (!service.cost.isEmpty()) {
                                            tempShippingOptions.add(new ShippingOption(
                                                    courier,
                                                    service.service,
                                                    service.description,
                                                    service.cost.get(0).value,
                                                    service.cost.get(0).etd
                                            ));
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.e("GetCostAll", "Gagal mendapatkan ongkir untuk kurir " + courier + ". Kode: " + response.code());
                        }
                    } catch (Exception e) {
                        Log.e("GetCostAll", "Error parsing response untuk kurir " + courier + ": " + e.getMessage(), e);
                    } finally {
                        if (response.body() != null) {
                            response.body().close();
                        }
                        latch.countDown();
                    }
                }
            });
        }

        new Thread(() -> {
            try {
                latch.await(); // Tunggu semua panggilan kurir selesai
                runOnUiThread(() -> {
                    allShippingOptions.clear();
                    allShippingOptions.addAll(tempShippingOptions);
                    if (allShippingOptions.isEmpty()) {
                        Toast.makeText(Checkout.this, "Tidak ada opsi pengiriman tersedia untuk tujuan ini.", Toast.LENGTH_LONG).show();
                    }
                    // Urutkan opsi pengiriman berdasarkan biaya termurah
                    Collections.sort(allShippingOptions, (o1, o2) -> Double.compare(o1.getCostValue(), o2.getCostValue()));

                    // Perbarui adapter dan reset pilihan
                    shippingOptionAdapter.updateOptions(new ArrayList<>(allShippingOptions));
                    selectedShippingOption = null;
                    shippingCost = 0;
                    estimatedDelivery = "-";
                    updatePaymentDetails();
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                runOnUiThread(() -> {
                    Toast.makeText(Checkout.this, "Proses pengecekan ongkir dibatalkan.", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void updatePaymentDetails() {
        tvShippingCost.setText(String.format("Ongkir: Rp %,.0f", shippingCost));
        tvEstimatedDelivery.setText(String.format("Estimasi Pengiriman: %s hari", estimatedDelivery));

        double totalPayment = subtotal + shippingCost + taxAmount;
        tvTotalAll.setText(String.format("Total: Rp %,.0f", totalPayment));
    }


    // --- Data Model Classes (Pastikan ini sudah sesuai dengan API RajaOngkir dan AddressItem) ---

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

    public static class CostResponse {
        public Rajaongkir rajaongkir;
        public static class Rajaongkir {
            public Query query;
            public Status status;
            public List<Result> results;
        }
        public static class Query {
            public int origin;
            public int destination;
            public int weight;
            public String courier;
        }

        public static class Status {
            public int code;
            public String description;
        }

        public static class Result {
            public String code;
            public String name;
            public List<Service> costs;
        }
        public static class Service {
            public String service;
            public String description;
            public List<CostDetail> cost;
        }
        public static class CostDetail {
            public int value;
            public String etd;
            public String note;
        }
    }
}