package com.example.utsa22202303007.ui.product;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.utsa22202303007.R;
import com.example.utsa22202303007.ServerAPI;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private Context context;
    private List<Product> productList;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onProductViewClick(Product product, ViewHolder holder);
    }

    public ProductAdapter(Context context, List<Product> productList, OnProductClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (productList == null || productList.isEmpty()) {
            return;
        }

        Product product = productList.get(position);
        if (product == null) return;

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat formatter = new DecimalFormat("#,###", symbols);

        String hargaFormatted = formatter.format(product.getHargajual());

        holder.tvMerk.setText(product.getMerk());
        holder.tvHarga.setText("Rp " + hargaFormatted);

        holder.tvView.setText("Dilihat " + product.getViewCount() + "x");

        int stok = product.getStok();
        if (stok > 0) {
            holder.tvStok.setText("Tersedia (" + stok + ")");
            holder.tvStok.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.tvStok.setText("Tidak Tersedia");
            holder.tvStok.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        }

        Glide.with(context)
                .load(new ServerAPI().IMAGE_BASE_URL + product.getFoto())
                .placeholder(android.R.drawable.ic_menu_report_image)
                .error(android.R.drawable.stat_notify_error)
                .into(holder.imageProduct);

        holder.btnKeranjang.setOnClickListener(v -> {
            if (stok > 0) {
                if (listener != null) listener.onProductClick(product);
            } else {
                Toast.makeText(context, "Produk tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductViewClick(product, holder);
            }

            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("product", product);
            context.startActivity(intent);
        });
    }

    public void filterList(List<Product> filteredList) {
        productList = filteredList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMerk;
        TextView tvHarga;
        TextView tvStok;
        public TextView tvView;
        ImageView imageProduct;
        ImageButton btnKeranjang;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMerk = itemView.findViewById(R.id.tvMerk);
            tvHarga = itemView.findViewById(R.id.tvHarga);
            tvStok = itemView.findViewById(R.id.tvStok);
            tvView = itemView.findViewById(R.id.tvView);
            imageProduct = itemView.findViewById(R.id.imageProduct);
            btnKeranjang = itemView.findViewById(R.id.btnKeranjang);
        }
    }
}
