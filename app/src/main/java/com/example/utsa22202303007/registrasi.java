package com.example.utsa22202303007;

import android.content.Context; // Import ini
import android.content.Intent;
import android.content.SharedPreferences; // Import ini
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class registrasi extends AppCompatActivity {
    TextView back;
    EditText etnama, etemail, etpassword;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registrasi);

        etnama = findViewById(R.id.yourname);
        etemail = findViewById(R.id.email);
        etpassword = findViewById(R.id.katasandi);
        back = findViewById(R.id.back);
        submit = findViewById(R.id.submit);

        submit.setOnClickListener(view -> prosesSubmit(etemail.getText().toString().trim(),
                etnama.getText().toString().trim(),
                etpassword.getText().toString().trim()));

        back.setOnClickListener(view -> {
            Intent intent = new Intent(registrasi.this, login.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void prosesSubmit(String vemail, String vnama, String vpassword) {
        if (vemail.isEmpty() || vnama.isEmpty() || vpassword.isEmpty()) {
            showMessage("Semua kolom harus diisi!");
            return;
        }

        if (!isEmailValid(vemail)) {
            showMessage("Email Tidak Valid!");
            return;
        }

        ServerAPI urlapi = new ServerAPI();
        String URL = urlapi.BASE_URL;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RegisterAPI api = retrofit.create(RegisterAPI.class);

        api.register(vemail, vnama, vpassword).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        Log.d("API Response", responseBody);

                        JSONObject json = new JSONObject(responseBody);
                        String status = json.optString("status");
                        String result = json.optString("result");
                        String message = json.optString("message");

                        if ("1".equals(status)) { // Berhasil mendaftar (email tidak duplikat)
                            if ("1".equals(result)) { // Data berhasil disimpan ke tbl_pelanggan DAN alamat_pengguna
                                showMessage("Register Berhasil");

                                // --- Penting: Ambil user_id dan simpan ke SharedPreferences ---
                                int userId = json.optInt("user_id", -1); // Ambil user_id dari respons
                                if (userId != -1) {
                                    SharedPreferences sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putInt("user_id", userId); // Simpan ID pengguna (int)
                                    editor.putString("user_email", vemail); // Juga simpan email
                                    editor.apply(); // Terapkan perubahan
                                    Log.d("Registrasi", "User ID " + userId + " saved to SharedPreferences.");
                                } else {
                                    Log.e("Registrasi", "User ID not returned from API or is invalid.");
                                }
                                // --- Akhir penyimpanan user_id ---

                                etemail.setText("");
                                etnama.setText("");
                                etpassword.setText("");

                                // Setelah registrasi berhasil dan ID tersimpan, arahkan ke login
                                Intent intent = new Intent(registrasi.this, login.class);
                                startActivity(intent);
                                finish(); // Tutup aktivitas registrasi
                            } else {
                                showMessage("Simpan Gagal: " + message); // Pesan gagal dari insert
                            }
                        } else {
                            showMessage("User Sudah Ada"); // Pesan jika email duplikat
                        }
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                        showMessage("Error Parsing Response: " + e.getMessage());
                    }
                } else {
                    showMessage("Gagal Register! Response tidak valid.");
                    Log.e("Register Error", "Response Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                showMessage("Koneksi gagal: " + t.getMessage());
                Log.e("Info Register", "Register Gagal: " + t.getMessage(), t);
            }
        });
    }

    private void showMessage(String message) {
        new AlertDialog.Builder(registrasi.this)
                .setMessage(message)
                .setNegativeButton("OK", null)
                .create()
                .show();
    }
}