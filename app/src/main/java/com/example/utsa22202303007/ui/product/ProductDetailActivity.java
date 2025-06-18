package com.example.utsa22202303007.ui.product;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.utsa22202303007.R;
import com.example.utsa22202303007.ServerAPI;
import com.example.utsa22202303007.ui.dashboard.OrderHelper;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView imgProductDetail;
    private TextView tvProductName, tvProductPrice, tvProductStock, tvProductDescription, tvProductViews, tvProductCategory;
    private TextView tvQuantity;
    private MaterialButton btnAddToCart, btnIncrease, btnDecrease;
    private OrderHelper orderHelper;
    private Product product;

    private int quantity = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_product);

        // Inisialisasi view
        imgProductDetail = findViewById(R.id.imgProductDetail);
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvProductStock = findViewById(R.id.tvProductStatus);
        tvProductCategory = findViewById(R.id.tvProductCategory);
        tvProductDescription = findViewById(R.id.tvProductDescription);
        tvProductViews = findViewById(R.id.tvProductViews);
        btnAddToCart = findViewById(R.id.btnAddToCart);

        tvQuantity = findViewById(R.id.tvQuantity);
        btnIncrease = findViewById(R.id.btnIncrease);
        btnDecrease = findViewById(R.id.btnDecrease);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        orderHelper = new OrderHelper(this);

        // Ambil data produk dari intent
        product = getIntent().getParcelableExtra("product");

        if (product != null) {
            tampilkanDetailProduk();
        }

        // Set listener tombol +
        btnIncrease.setOnClickListener(v -> {
            if (quantity < product.getStok()) {
                quantity++;
                updateQuantity();
            } else {
                Toast.makeText(this, "Maksimum stok tercapai", Toast.LENGTH_SHORT).show();
            }
        });

        // Set listener tombol -
        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateQuantity();
            }
        });

        btnAddToCart.setOnClickListener(v -> {
            if (product.getStok() > 0) {
                product.setOrderQuantity(quantity);  // set jumlah order ke product (asumsi ada setter)
                orderHelper.addToOrder(product);
                Toast.makeText(this, "Produk ditambahkan ke keranjang (" + quantity + " item)", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Produk tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateQuantity() {
        tvQuantity.setText(String.valueOf(quantity));
    }

    private void tampilkanDetailProduk() {
        tvProductName.setText(product.getMerk());

        // Format harga
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat formatter = new DecimalFormat("#,###", symbols);
        String hargaFormatted = "Rp " + formatter.format(product.getHargajual());
        tvProductPrice.setText(hargaFormatted);


        //Tampilan kategori
        tvProductCategory.setText(product.getKategori());
        // Tampilkan stok dan deskripsi
        tvProductStock.setText("Stok: " + product.getStok());
        tvProductDescription.setText(product.getDeskripsi());

        tvProductViews.setText("Dilihat " + product.getViewCount() + "x");

        // Atur warna dan status tombol berdasarkan stok
        if (product.getStok() == 0) {
            tvProductStock.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            btnAddToCart.setEnabled(false);
            btnAddToCart.setText("Stok Habis");
            btnIncrease.setEnabled(false);
            btnDecrease.setEnabled(false);
        } else {
            tvProductStock.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            btnAddToCart.setEnabled(true);
            btnAddToCart.setText("Keranjang");
            btnIncrease.setEnabled(true);
            btnDecrease.setEnabled(true);
        }

        updateQuantity();

        // Tampilkan gambar produk
        Glide.with(this)
                .load(new ServerAPI().IMAGE_BASE_URL + product.getFoto())
                .placeholder(android.R.drawable.ic_menu_report_image)
                .error(android.R.drawable.stat_notify_error)
                .into(imgProductDetail);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
