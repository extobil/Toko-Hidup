package com.example.utsa22202303007;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class login extends AppCompatActivity {
    public static final String URL = new ServerAPI().BASE_URL;

    ProgressDialog pd;
    Button login;
    TextView email, password, guest;
    TextView registerd;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);

        if (isLoggedIn) {
            Intent intent = new Intent(login.this, MainActivity.class);
            intent.putExtra("nama", sharedPreferences.getString("nama", ""));
            intent.putExtra("email", sharedPreferences.getString("email", ""));
            startActivity(intent);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        registerd = findViewById(R.id.registerd);

        registerd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(login.this, registrasi.class);
                startActivity(intent);
            }
        });

        guest = findViewById(R.id.guest);
        guest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("is_logged_in", true);
                editor.putString("nama", "");
                editor.putString("email", "");
                editor.apply();

                Intent intent = new Intent(login.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        login = findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pd = new ProgressDialog(view.getContext());
                pd.setTitle("Proses Login...");
                pd.setMessage("Tunggu Sebentar..");
                pd.setCancelable(true);
                pd.setIndeterminate(true);
                prosesLogin(email.getText().toString(), password.getText().toString());
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    void prosesLogin(String vemail, String vpassword) {
        if (!isFinishing() && !isDestroyed()) {
            pd.show();
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RegisterAPI api = retrofit.create(RegisterAPI.class);

        api.login(vemail, vpassword).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        JSONObject json = new JSONObject(response.body().string());

                        if (json.getString("result").equals("1")) {
                            JSONObject data = json.getJSONObject("data");

                            if (data.getString("status").equals("1")) {
                                Toast.makeText(login.this,
                                        "Login Berhasil",
                                        Toast.LENGTH_SHORT).show();

                                SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("is_logged_in", true);
                                editor.putString("nama", data.getString("nama"));
                                editor.putString("email", data.getString("email"));

                                // Ambil dan simpan user_id
                                int userId = data.optInt("user_id", -1);
                                if (userId != -1) {
                                    editor.putInt("user_id", userId);
                                    Log.d("Login", "User ID " + userId + " saved to SharedPreferences.");
                                } else {
                                    Log.e("Login", "user_id tidak ditemukan dalam response");
                                }

                                editor.apply();

                                Intent intent = new Intent(login.this, MainActivity.class);
                                intent.putExtra("nama", data.getString("nama"));
                                intent.putExtra("email", data.getString("email"));
                                startActivity(intent);
                                finish();
                            } else {
                                pd.dismiss();
                                new AlertDialog.Builder(login.this)
                                        .setMessage("Status User Ini Tidak Aktif")
                                        .setNegativeButton("Retry", null).create().show();
                            }
                        } else {
                            pd.dismiss();
                            new AlertDialog.Builder(login.this)
                                    .setMessage("Email atau Password Salah")
                                    .setNegativeButton("Retry", null).create().show();
                        }
                    } else {
                        pd.dismiss();
                        new AlertDialog.Builder(login.this)
                                .setMessage("Terjadi Kesalahan pada Server")
                                .setNegativeButton("Retry", null).create().show();
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                    pd.dismiss();
                    new AlertDialog.Builder(login.this)
                            .setMessage("Terjadi Kesalahan: " + e.getMessage())
                            .setNegativeButton("Retry", null).create().show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                pd.dismiss();
                Log.e("Info Load", "Load Gagal: " + t.toString());
                new AlertDialog.Builder(login.this)
                        .setMessage("Gagal Terhubung ke Server: " + t.getMessage())
                        .setNegativeButton("Retry", null).create().show();
            }
        });
    }
}
