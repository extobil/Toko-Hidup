package com.example.utsa22202303007.ui.dashboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.utsa22202303007.R;
import com.example.utsa22202303007.ServerAPI;
import com.example.utsa22202303007.ui.Checkout.Checkout;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
    private Context context;
    private List<OrderItem> orderItems;
    private OrderHelper orderHelper;
    private OnOrderChangeListener orderChangeListener;

    public OrderAdapter(Context context, List<OrderItem> orderItems, OrderHelper orderHelper) {
        this.context = context;
        this.orderItems = orderItems;
        this.orderHelper = orderHelper;
    }

    public OrderAdapter(Checkout context, List<OrderItem> orderItems) {
        this.context = context;
        this.orderItems = orderItems;
        this.orderHelper = null; // atau tidak digunakan
    }

    public void setOnOrderChangeListener(OnOrderChangeListener listener) {
        this.orderChangeListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem item = orderItems.get(position);

        holder.tvProductName.setText(item.getMerk());
        holder.tvPrice.setText(String.format("Rp %,.0f", item.getHargajual()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        holder.tvSubtotal.setText(String.format("Subtotal: Rp %,.0f", item.getSubtotal()));

        // Gunakan IMAGE_BASE_URL dari ServerAPI
        Glide.with(context)
                .load(new ServerAPI().IMAGE_BASE_URL + item.getFoto())
                .placeholder(android.R.drawable.ic_menu_report_image)
                .error(android.R.drawable.stat_notify_error)
                .into(holder.imageProduct);

        // Tombol Hapus
        holder.btnRemove.setOnClickListener(v -> {
            orderHelper.removeFromOrder(item.getKode());
            orderItems.remove(position);
            notifyDataSetChanged();
            if (orderChangeListener != null) {
                orderChangeListener.onOrderChanged();
            }
        });

        // Tombol Tambah (+)
        holder.btnIncrease.setOnClickListener(v -> {
            int currentQuantity = item.getQuantity();
            int stock = item.getStok();

            if (currentQuantity < stock) {
                item.setQuantity(currentQuantity + 1);
                orderHelper.updateQuantity(item.getKode(), item.getQuantity());
                notifyItemChanged(position);
                if (orderChangeListener != null) {
                    orderChangeListener.onOrderChanged();
                }
            } else {
                Toast.makeText(context, "Stok tidak mencukupi", Toast.LENGTH_SHORT).show();
            }
        });

        // Tombol Kurang (-)
        holder.btnDecrease.setOnClickListener(v -> {
            int currentQuantity = item.getQuantity();
            if (currentQuantity > 1) {
                item.setQuantity(currentQuantity - 1);
                orderHelper.updateQuantity(item.getKode(), item.getQuantity());
                notifyItemChanged(position);
            } else {
                orderHelper.removeFromOrder(item.getKode());
                orderItems.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, orderItems.size());
            }
            if (orderChangeListener != null) {
                orderChangeListener.onOrderChanged();
            }
        });
    }

    public interface OnOrderChangeListener {
        void onOrderChanged();
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageProduct;
        TextView tvProductName, tvPrice, tvQuantity, tvSubtotal;
        ImageView btnRemove, btnIncrease, btnDecrease;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvSubtotal = itemView.findViewById(R.id.tvSubtotal);
            imageProduct = itemView.findViewById(R.id.imageProduct);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
        }
    }
}
