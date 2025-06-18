package com.example.utsa22202303007;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.utsa22202303007.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SharedViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Atur status bar transparan dengan opacity 20%
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            // Warna hitam transparan alpha 20%
            window.setStatusBarColor(Color.parseColor("#33000000"));
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Tambah padding atas agar konten tidak tertutup status bar
        View rootView = binding.getRoot();
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        rootView.setPadding(0, statusBarHeight, 0, 0);

        // Ambil data dari Intent
        String nama = getIntent().getStringExtra("nama");

        // Inisialisasi ViewModel dan set data
        viewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        if (nama != null) {
            viewModel.setNama(nama);
        }

        // Setup Bottom Navigation
        BottomNavigationView navView = binding.navView;
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_product, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(navView, navController);
    }

    // Method untuk pindah ke tab Home secara programatik
    public void switchToHome() {
        binding.navView.setSelectedItemId(R.id.navigation_home);
    }

    // Kamu bisa buat method lain untuk pindah ke tab lain
    public void switchToProduct() {
        binding.navView.setSelectedItemId(R.id.navigation_product);
    }
}
