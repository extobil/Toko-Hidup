package com.example.utsa22202303007.ui.notifications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Keep this import if you might use Glide elsewhere
import com.example.utsa22202303007.R;
import com.example.utsa22202303007.ServerAPI; // Keep this import if you might use ServerAPI elsewhere

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder> {

    private final Context context;
    private final List<OrderModel> orderList;
    private final OnUploadClickListener uploadClickListener;

    public interface OnUploadClickListener {
        void onPilihGambarClicked(OrderModel order);
    }

    public OrderHistoryAdapter(Context context, List<OrderModel> orderList, OnUploadClickListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.uploadClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderModel order = orderList.get(position);

        holder.tvNoorder.setText("No order #" + order.getTrans_id());
        holder.tvNamaMobil.setText(order.getMerk());
        holder.tvAlamat.setText(order.getAlamat_kirim() + ", " + order.getKota_kirim() + ", " + order.getProvinsi_kirim());
        holder.tvEstimasi.setText("Estimasi: " + order.getLama_kirim() + " hari");
        holder.tvTanggalOrder.setText("Tanggal Order: " + order.getTgl_order());
        holder.tvSubtotal.setText(formatRupiah(order.getSubtotal()));
        holder.tvOngkir.setText(formatRupiah(order.getOngkir()));
        holder.tvPyment.setText(order.getMetode_bayar());
        holder.tvTotalBayar.setText("Total: " + formatRupiah(order.getTotal_bayar()));
        holder.tvStatus.setText("Status: " + order.getStatus_text());

        if (order.getMetode_bayar().equalsIgnoreCase("cod")) {
            holder.layoutUpload.setVisibility(View.GONE);
            // holder.imgBuktiBayar.setVisibility(View.GONE); // Already hidden by default if no image to show
        } else {
            holder.layoutUpload.setVisibility(View.VISIBLE);

            if (order.getBukti_bayar() != null && !order.getBukti_bayar().isEmpty()) {
                holder.tvNamaFile.setVisibility(View.VISIBLE);
                holder.tvNamaFile.setText("Nama file: " + order.getBukti_bayar());
            } else {
                holder.tvNamaFile.setVisibility(View.GONE);
            }

            // Comment out or remove this entire block to ensure imgBuktiBayar is never visible
            // if (order.getBukti_bayar() == null || order.getBukti_bayar().isEmpty()) {
            //     holder.imgBuktiBayar.setVisibility(View.GONE);
            //     holder.btnPilihGambar.setOnClickListener(v -> uploadClickListener.onPilihGambarClicked(order));
            // } else {
            //     holder.imgBuktiBayar.setVisibility(View.VISIBLE);
            //     Glide.with(context)
            //             .load(ServerAPI.BASE_URL + "images/" + order.getBukti_bayar())
            //             .into(holder.imgBuktiBayar);
            // }

            // If you always want the "Pilih Gambar" button to be available for non-COD,
            // regardless of whether a file exists or not, you can simplify like this:
            holder.btnPilihGambar.setOnClickListener(v -> uploadClickListener.onPilihGambarClicked(order));
            holder.imgBuktiBayar.setVisibility(View.GONE); // Explicitly hide it here
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNamaMobil, tvAlamat, tvEstimasi, tvTanggalOrder, tvSubtotal, tvOngkir, tvTotalBayar, tvStatus, tvPyment, tvNoorder, tvNamaFile;
        LinearLayout layoutUpload;
        Button btnPilihGambar; // Removed btnUpload as it's not in the XML
        ImageView imgBuktiBayar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNamaMobil = itemView.findViewById(R.id.tvNamaMobil);
            tvAlamat = itemView.findViewById(R.id.tvAlamat);
            tvEstimasi = itemView.findViewById(R.id.tvEstimasi);
            tvTanggalOrder = itemView.findViewById(R.id.tvTanggalOrder);
            tvSubtotal = itemView.findViewById(R.id.tvSubtotal);
            tvOngkir = itemView.findViewById(R.id.tvOngkir);
            tvTotalBayar = itemView.findViewById(R.id.tvTotalBayar);
            tvNamaFile = itemView.findViewById(R.id.tvNamaFile);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            layoutUpload = itemView.findViewById(R.id.layoutUpload);
            btnPilihGambar = itemView.findViewById(R.id.btnPilihGambar);
            imgBuktiBayar = itemView.findViewById(R.id.imgBuktiBayar);
            tvNoorder = itemView.findViewById(R.id.tvNoorder);
            tvPyment = itemView.findViewById(R.id.tvPyment);
        }
    }

    private String formatRupiah(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return formatter.format(amount);
    }
}