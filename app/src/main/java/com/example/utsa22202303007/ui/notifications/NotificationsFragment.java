package com.example.utsa22202303007.ui.notifications;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.utsa22202303007.MainActivity;
import com.example.utsa22202303007.R;
import com.example.utsa22202303007.RegisterAPI;
import com.example.utsa22202303007.ServerAPI;
import com.example.utsa22202303007.databinding.FragmentNotificationsBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.example.utsa22202303007.login;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private String email; // Variabel email ini sudah benar untuk menyimpan email pengguna

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user_session", getContext().MODE_PRIVATE);
        email = sharedPreferences.getString("email", "");

        // Mendapatkan NavController
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

        // Jika tidak login, arahkan ke GuestProfile Fragment
        if (email.isEmpty()) {
            navController.navigate(R.id.GuestProfile);
            return root;
        }

        // Jika login, ambil data profil
        getProfile(email);

        // Navigasi ke Edit Profile
        binding.menuEditProfile.setOnClickListener(v -> {
            navController.navigate(R.id.fragmentProfile);
        });

        // Navigasi ke Informasi Perusahaan
        binding.menuInfoPerusahaan.setOnClickListener(v -> {
            navController.navigate(R.id.fragmentInformasi);
        });

        // Panggil dialog ganti password
        binding.menuGantiPassword.setOnClickListener(v -> {
            showChangePasswordDialog();
        });

        // Klik menu Logout
        binding.menuLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        return root;
    }

    private void getProfile(String vemail) {
        // ... (kode ini tidak berubah) ...
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(new ServerAPI().BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RegisterAPI api = retrofit.create(RegisterAPI.class);
        api.getProfile(vemail).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        if (json.getString("result").equals("1")) {
                            JSONObject data = json.getJSONObject("data");

                            String nama = data.getString("nama");
                            binding.textNamaUser.setText(nama);

                            String fotoFilename = data.optString("filename", "");
                            if (!fotoFilename.isEmpty()) {
                                String imageUrl = new ServerAPI().BASE_URL + "img/" + fotoFilename;

                                Glide.with(requireContext())
                                        .load(imageUrl)
                                        .circleCrop()
                                        .placeholder(R.drawable.logo)
                                        .error(R.drawable.logo)
                                        .into(binding.imgProfileNotif);
                            }
                        } else {
                            Toast.makeText(getContext(), "Data tidak ditemukan", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("GetProfileNotif", "Error parsing", e);
                        Toast.makeText(getContext(), "Terjadi kesalahan saat membaca data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Gagal mengambil data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("GetProfileNotif", "onFailure", t);
                Toast.makeText(getContext(), "Terjadi kesalahan koneksi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_ganti_password, null);
        dialogBuilder.setView(dialogView);

        TextInputEditText etPasswordLama = dialogView.findViewById(R.id.etPasswordLama);
        TextInputEditText etPasswordBaru = dialogView.findViewById(R.id.etPasswordBaru);
        TextInputEditText etKonfirmasiPassword = dialogView.findViewById(R.id.etKonfirmasiPassword);
        Button btnGantiPassword = dialogView.findViewById(R.id.btnGantiPassword);

        AlertDialog alertDialog = dialogBuilder.create();

        btnGantiPassword.setOnClickListener(v -> {
            String oldPassword = etPasswordLama.getText().toString().trim();
            String newPassword = etPasswordBaru.getText().toString().trim();
            String confirmNewPassword = etKonfirmasiPassword.getText().toString().trim();

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
                Toast.makeText(getContext(), "Harap isi semua kolom password", Toast.LENGTH_SHORT).show(); // Pesan lebih spesifik
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(getContext(), "Password baru minimal 6 karakter", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmNewPassword)) {
                Toast.makeText(getContext(), "Password baru dan konfirmasi tidak cocok", Toast.LENGTH_SHORT).show();
                return;
            }

            // --- PERUBAHAN DI SINI: Kirimkan 'email' yang sudah disimpan ---
            callChangePasswordApi(email, oldPassword, newPassword, alertDialog);
            // -----------------------------------------------------------
        });

        alertDialog.show();
    }

    // --- PERUBAHAN DI SINI: Tambahkan parameter 'userEmail' ---
    private void callChangePasswordApi(String userEmail, String oldPassword, String newPassword, AlertDialog dialog) {
        // -----------------------------------------------------------
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(new ServerAPI().BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RegisterAPI api = retrofit.create(RegisterAPI.class);

        // --- PERUBAHAN DI SINI: Gunakan 'userEmail' sebagai parameter pertama ---
        api.changePassword(userEmail, oldPassword, newPassword).enqueue(new Callback<ResponseBody>() {
            // ----------------------------------------------------------------------
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        Log.d("ChangePassword", "API Response: " + responseBody);

                        JSONObject jsonResponse = new JSONObject(responseBody);
                        boolean status = jsonResponse.getBoolean("status");
                        String message = jsonResponse.getString("message");

                        if (status) {
                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                            dialog.dismiss(); // Tutup dialog jika berhasil
                        } else {
                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("ChangePassword", "Error parsing JSON: " + e.getMessage());
                        Toast.makeText(getContext(), "Terjadi kesalahan saat memproses respons", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e("ChangePassword", "Error membaca error body", e);
                    }
                    Log.e("ChangePassword", "Panggilan API tidak berhasil: " + response.code() + " " + errorBody);
                    Toast.makeText(getContext(), "Gagal mengubah password. Kode kesalahan: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("ChangePassword", "Panggilan API gagal: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Kesalahan koneksi jaringan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}