package com.example.utsa22202303007.ui.notifications;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.content.CursorLoader;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.utsa22202303007.R;
import com.example.utsa22202303007.RegisterAPI;
import com.example.utsa22202303007.ServerAPI;
import com.example.utsa22202303007.databinding.FragmentProfileBinding;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import id.zelory.compressor.Compressor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.app.Activity.RESULT_OK;

public class Profile extends Fragment {

    private FragmentProfileBinding binding;
    private String email;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;

    // For RajaOngkir API
    private List<Province> provinceList = new ArrayList<>();
    private List<City> cityList = new ArrayList<>();
    private int selectedProvinceId = -1;
    private int selectedCityId = -1;
    private String selectedProvinceName = "";
    private String selectedCityName = "";

    private OkHttpClient httpClient;

    private static final String PROVINCE_API_URL = ServerAPI.BASE_URL + "get_provinsi.php";
    private static final String CITY_API_URL = ServerAPI.BASE_URL + "get_kota.php";

    // Variables to hold saved province and city names from database
    private String savedProvinceNameFromDb = "";
    private String savedCityNameFromDb = "";

    // Flag to prevent onItemSelected from re-triggering during programmatic selection
    private boolean isProgrammaticSelection = false;

    public Profile() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        httpClient = new OkHttpClient();

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        email = sharedPreferences.getString("email", "");

        // Set email field non-editable
        binding.etEmail.setText(email);
        binding.etEmail.setEnabled(false);

        // Load profile first. This will populate savedProvinceNameFromDb and savedCityNameFromDb.
        // Once profile data is retrieved, loadProvinces() will be called, which will then trigger city loading.
        if (!email.isEmpty()) {
            getProfile(email);
        } else {
            // If email is empty, just load provinces to allow manual selection.
            loadProvinces();
        }

        // Listener for Province Spinner
        binding.etProvinsi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isProgrammaticSelection) {
                    // This selection was made programmatically, so don't re-trigger city load
                    // The flag will be reset after this onItemSelected or a subsequent post() call.
                    return;
                }

                if (position > 0) { // Index 0 is "Pilih Provinsi"
                    Province p = provinceList.get(position - 1);
                    selectedProvinceId = Integer.parseInt(p.getProvince_id());
                    selectedProvinceName = p.getProvince();
                    Log.d("ProfileSpinner", "Province selected by user/program: " + selectedProvinceName + " (ID: " + selectedProvinceId + ")");
                    // When province is selected, load cities.
                    // If it's the initial load from DB, savedCityNameFromDb will be used to pre-select.
                    loadCities(selectedProvinceId, savedCityNameFromDb);
                } else {
                    selectedProvinceId = -1;
                    selectedProvinceName = "";
                    cityList.clear();
                    updateCitySpinner(); // Clear city spinner if "Pilih Provinsi" is selected
                    Log.d("ProfileSpinner", "Province unselected. City spinner cleared.");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Listener for City Spinner
        binding.etKota.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isProgrammaticSelection) {
                    // This selection was made programmatically, do nothing
                    return;
                }

                if (position > 0) { // Index 0 is "Pilih Kota"
                    City c = cityList.get(position - 1);
                    selectedCityId = Integer.parseInt(c.getCity_id());
                    selectedCityName = c.getType() + " " + c.getCity_name();
                    Log.d("ProfileSpinner", "City selected by user: " + selectedCityName + " (ID: " + selectedCityId + ")");
                } else {
                    selectedCityId = -1;
                    selectedCityName = "";
                    Log.d("ProfileSpinner", "City unselected.");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        binding.btnSimpan.setOnClickListener(view -> {
            String nama = binding.etNama.getText().toString().trim();
            String alamat = binding.etAlamat.getText().toString().trim();
            String telp = binding.etTelepon.getText().toString().trim();
            String kodepos = binding.etKodePos.getText().toString().trim();

            String kota = selectedCityName; // This is already the "Type City_Name" format (e.g. "Kabupaten Kendal")
            String provinsi = selectedProvinceName; // This is the "Province Name" format

            if (nama.isEmpty() || alamat.isEmpty() || kota.isEmpty() || provinsi.isEmpty() || telp.isEmpty() || kodepos.isEmpty() || selectedProvinceId == -1 || selectedCityId == -1) {
                Toast.makeText(getContext(), "Semua field harus diisi dan pilih Provinsi/Kota", Toast.LENGTH_SHORT).show();
                return;
            }

            DataPelanggan data = new DataPelanggan();
            data.setNama(nama);
            data.setAlamat(alamat);
            data.setKota(kota); // Save the formatted city name (e.g., "Kabupaten Kendal")
            data.setProvinsi(provinsi); // Save the province name
            data.setTelp(telp);
            data.setKodepos(kodepos);
            data.setEmail(email);

            updateProfile(data);
        });

        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

        binding.btnKembali.setOnClickListener(view -> {
            navController.navigate(R.id.navigation_notifications);
        });

        binding.btnEditPhoto.setOnClickListener(view -> {
            openImageChooser();
        });

        return root;
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            Glide.with(requireContext())
                    .load(selectedImageUri)
                    .circleCrop()
                    .into(binding.imgProfile);

            uploadImageToServer();
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(getContext(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        if (cursor == null) {
            return null;
        }
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    private void uploadImageToServer() {
        if (selectedImageUri == null) return;

        String filePath = getRealPathFromURI(selectedImageUri);
        if (filePath == null) {
            Toast.makeText(getContext(), "Gagal mendapatkan path gambar", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File originalFile = new File(filePath);

            File compressedFile = new Compressor(requireContext())
                    .setMaxWidth(640)
                    .setMaxHeight(480)
                    .setQuality(75)
                    .compressToFile(originalFile);

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), compressedFile);
            okhttp3.MultipartBody.Part body = okhttp3.MultipartBody.Part.createFormData("imageupload", compressedFile.getName(), requestFile);

            RequestBody emailBody = RequestBody.create(MediaType.parse("text/plain"), email);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(new ServerAPI().BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            RegisterAPI api = retrofit.create(RegisterAPI.class);
            Call<ResponseBody> call = api.uploadFotoProfil(emailBody, body);

            call.enqueue(new retrofit2.Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            JSONObject json = new JSONObject(response.body().string());
                            if (json.getInt("kode") == 1) {
                                Toast.makeText(getContext(), "Foto profil berhasil diubah", Toast.LENGTH_SHORT).show();
                                getProfile(email); // Refresh profile to show new image
                            } else {
                                Toast.makeText(getContext(), "Gagal: " + json.getString("pesan"), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "Gagal upload foto", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("UploadPhoto", "Parsing error", e);
                        Toast.makeText(getContext(), "Terjadi kesalahan", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("UploadPhoto", "Failed", t);
                    Toast.makeText(getContext(), "Gagal terhubung ke server", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Log.e("UploadImage", "Compress error", e);
            Toast.makeText(getContext(), "Gagal mengompres gambar", Toast.LENGTH_SHORT).show();
        }
    }

    private void getProfile(String vemail) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(new ServerAPI().BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RegisterAPI api = retrofit.create(RegisterAPI.class);
        api.getProfile(vemail).enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        if (json.getString("result").equals("1")) {
                            JSONObject data = json.getJSONObject("data");
                            binding.etNama.setText(data.getString("nama"));
                            binding.etAlamat.setText(data.getString("alamat"));
                            binding.etTelepon.setText(data.getString("telp"));
                            binding.etKodePos.setText(data.getString("kodepos"));
                            binding.etEmail.setText(email);

                            // Store saved province and city names from database
                            savedProvinceNameFromDb = data.getString("provinsi");
                            savedCityNameFromDb = data.getString("kota");
                            Log.d("ProfileData", "Loaded Profile - Province: '" + savedProvinceNameFromDb + "', City: '" + savedCityNameFromDb + "'");

                            // Now load provinces. This will eventually lead to cities being loaded and selected.
                            loadProvinces();

                            String fotoFilename = data.optString("filename", "");
                            if (!fotoFilename.isEmpty()) {
                                String imageUrl = new ServerAPI().BASE_URL + "img/" + fotoFilename;

                                Glide.with(requireContext())
                                        .load(imageUrl)
                                        .circleCrop()
                                        .placeholder(R.drawable.logo)
                                        .error(R.drawable.logo)
                                        .into(binding.imgProfile);
                            }

                        } else {
                            Toast.makeText(getContext(), "Data tidak ditemukan", Toast.LENGTH_SHORT).show();
                            Log.w("GetProfile", "Profile data not found for email: " + email);
                            loadProvinces(); // Still load provinces to allow manual selection
                        }
                    } catch (Exception e) {
                        Log.e("GetProfile", "Error parsing profile JSON: " + e.getMessage(), e);
                        Toast.makeText(getContext(), "Terjadi kesalahan saat parsing data profil", Toast.LENGTH_SHORT).show();
                        loadProvinces(); // Still load provinces
                    }
                } else {
                    Log.e("GetProfile", "Failed to get profile data. Response code: " + response.code());
                    Toast.makeText(getContext(), "Gagal mengambil data profil", Toast.LENGTH_SHORT).show();
                    loadProvinces(); // Still load provinces
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("GetProfile", "Profile API call failed: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Terjadi kesalahan koneksi saat mengambil profil", Toast.LENGTH_SHORT).show();
                loadProvinces(); // Still load provinces
            }
        });
    }

    private void updateProfile(DataPelanggan data) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(new ServerAPI().BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RegisterAPI api = retrofit.create(RegisterAPI.class);
        api.updateProfile(
                data.getNama(),
                data.getAlamat(),
                data.getKota(),
                data.getProvinsi(),
                data.getTelp(),
                data.getKodepos(),
                data.getEmail()
        ).enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseString = response.body().string();
                        Log.d("UpdateProfile", "Raw response: " + responseString);
                        try {
                            JSONObject json = new JSONObject(responseString);
                            if (json.has("message")) {
                                Toast.makeText(getContext(), json.getString("message"), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Berhasil, tapi tidak ada pesan", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e("UpdateProfile", "JSON parse error from update response: " + e.getMessage(), e);
                            Toast.makeText(getContext(), "Respon update bukan JSON: " + responseString, Toast.LENGTH_LONG).show();
                        }

                    } else {
                        Log.e("UpdateProfile", "Failed to save changes. Response code: " + response.code());
                        Toast.makeText(getContext(), "Gagal menyimpan perubahan", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("UpdateProfile", "Exception reading update response: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Terjadi kesalahan saat membaca respon update", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("UpdateProfile", "Update API call failed: " + t.getMessage(), t);
                new AlertDialog.Builder(getContext())
                        .setMessage("Simpan Gagal: " + t.getMessage())
                        .setNegativeButton("Retry", null)
                        .create().show();
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
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Gagal terhubung ke server provinsi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("ProfileProvinces", "Connection error loading provinces", e);
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response.code() + " from provinces API: " + response.body().string());
                    }

                    String resStr = response.body().string();
                    Log.d("ProfileProvinceResponse", "Raw JSON: " + resStr);

                    RajaOngkirResponseProvince provResponse = new Gson().fromJson(resStr, RajaOngkirResponseProvince.class);

                    if (provResponse != null && provResponse.getRajaongkir() != null && provResponse.getRajaongkir().getResults() != null) {
                        provinceList = provResponse.getRajaongkir().getResults();
                        requireActivity().runOnUiThread(() -> {
                            List<String> names = new ArrayList<>();
                            names.add("Pilih Provinsi");
                            for (Province p : provinceList) {
                                names.add(p.getProvince());
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                                    android.R.layout.simple_spinner_item, names);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            binding.etProvinsi.setAdapter(adapter);
                            Log.d("ProfileSpinner", "Province spinner updated with " + names.size() + " items.");

                            // After provinces are loaded, try to set the selection
                            setProvinceSpinnerSelection(savedProvinceNameFromDb);
                        });
                    } else {
                        requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Data provinsi kosong atau tidak valid.", Toast.LENGTH_SHORT).show());
                        Log.w("ProfileProvinces", "Empty or invalid province data received.");
                    }

                } catch (Exception e) {
                    requireActivity().runOnUiThread(() -> {
                        String errorMsg = "Error parsing provinsi JSON: " + e.getMessage();
                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                        Log.e("ProfileParseProvinces", errorMsg, e);
                    });
                } finally {
                    if (response.body() != null) {
                        response.body().close();
                    }
                }
            }
        });
    }

    private void loadCities(int provinceId, String savedCityNameToSelect) {
        Request request = new Request.Builder()
                .url(CITY_API_URL + "?province_id=" + provinceId)
                .build();

        httpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Gagal load kota: " + e.getMessage(), Toast.LENGTH_LONG).show());
                Log.e("ProfileCities", "Connection error loading cities for province " + provinceId + ": " + e.getMessage(), e);
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String resStr = response.body().string();
                    Log.d("ProfileCityResponse", "Raw JSON for cities (Province ID " + provinceId + "): " + resStr);
                    try {
                        RajaOngkirResponseCity cityRes = new Gson().fromJson(resStr, RajaOngkirResponseCity.class);

                        if (cityRes != null && cityRes.getRajaongkir() != null && cityRes.getRajaongkir().getResults() != null) {
                            cityList = cityRes.getRajaongkir().getResults();
                            requireActivity().runOnUiThread(() -> {
                                updateCitySpinner();
                                setCitySpinnerSelection(savedCityNameToSelect);
                            });
                        } else {
                            requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Data kota kosong atau tidak valid.", Toast.LENGTH_SHORT).show());
                            Log.w("ProfileCities", "Empty or invalid city data received for province " + provinceId);
                            cityList.clear();
                            updateCitySpinner();
                        }
                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error parsing kota JSON: " + e.getMessage(), Toast.LENGTH_LONG).show());
                        Log.e("ProfileParseCities", "Error parsing city JSON: " + e.getMessage(), e);
                        cityList.clear();
                        updateCitySpinner();
                    }
                } else {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Gagal mendapatkan data kota dari server. Kode: " + response.code(), Toast.LENGTH_SHORT).show();
                        Log.e("ProfileCities", "Failed to get city data. Code: " + response.code() + ", Message: " + response.message());
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, cityNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.etKota.setAdapter(adapter);
        Log.d("ProfileSpinner", "City spinner updated with " + cityNames.size() + " items. First item: " + (cityNames.isEmpty() ? "N/A" : cityNames.get(0)));
    }

    private void setProvinceSpinnerSelection(String savedProvinceName) {
        if (savedProvinceName == null || savedProvinceName.isEmpty()) {
            Log.d("ProfileSpinner", "No saved province name to select or it's empty.");
            return;
        }

        ArrayAdapter<String> provinceAdapter = (ArrayAdapter<String>) binding.etProvinsi.getAdapter();
        if (provinceAdapter != null) {
            int provincePosition = -1;
            for (int i = 0; i < provinceAdapter.getCount(); i++) {
                if (provinceAdapter.getItem(i).equalsIgnoreCase(savedProvinceName)) {
                    provincePosition = i;
                    break;
                }
            }

            if (provincePosition != -1) {
                isProgrammaticSelection = true;
                binding.etProvinsi.setSelection(provincePosition);

                // --- BAGIAN INI TELAH DITAMBAHKAN/DIUBAH ---
                // Post a runnable to reset the flag AFTER onItemSelected has been processed
                binding.etProvinsi.post(() -> {
                    isProgrammaticSelection = false;
                    Log.d("ProfileSpinner", "isProgrammaticSelection for province reset to false.");
                });
                // --- AKHIR PERUBAHAN ---

                Log.d("ProfileSpinner", "Province '" + savedProvinceName + "' selected at position " + provincePosition);
            } else {
                Log.w("ProfileSpinner", "Provinsi '" + savedProvinceName + "' tidak ditemukan di spinner. Available items: " + getSpinnerItems(provinceAdapter));
            }
        } else {
            Log.w("ProfileSpinner", "Province adapter is null when trying to set selection.");
        }
    }

    private void setCitySpinnerSelection(String savedCityName) {
        if (savedCityName == null || savedCityName.isEmpty()) {
            Log.d("ProfileSpinner", "No saved city name to select or it's empty.");
            return;
        }

        ArrayAdapter<String> cityAdapter = (ArrayAdapter<String>) binding.etKota.getAdapter();
        if (cityAdapter != null) {
            int cityPosition = -1;
            String targetCityName = savedCityName.trim();

            for (int i = 0; i < cityAdapter.getCount(); i++) {
                String adapterItem = cityAdapter.getItem(i);
                if (adapterItem != null && adapterItem.equalsIgnoreCase(targetCityName)) {
                    cityPosition = i;
                    break;
                }
            }

            if (cityPosition != -1) {
                isProgrammaticSelection = true;
                binding.etKota.setSelection(cityPosition);
                isProgrammaticSelection = false; // Reset flag immediately after selection
                Log.d("ProfileSpinner", "City '" + savedCityName + "' selected at position " + cityPosition);
            } else {
                Log.w("ProfileSpinner", "City '" + savedCityName + "' NOT found in city spinner items. Available items: " + getSpinnerItems(cityAdapter));
            }
        } else {
            Log.w("ProfileSpinner", "City adapter is null when trying to set city selection.");
        }
    }

    private String getSpinnerItems(ArrayAdapter<String> adapter) {
        if (adapter == null) return "Adapter is null";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < adapter.getCount(); i++) {
            sb.append("'").append(adapter.getItem(i)).append("'");
            if (i < adapter.getCount() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // --- Data Model Classes from Checkout.java ---
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