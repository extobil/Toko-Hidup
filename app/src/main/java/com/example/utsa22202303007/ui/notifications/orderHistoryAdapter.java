package com.example.utsa22202303007.ui.notifications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.utsa22202303007.R;
import com.example.utsa22202303007.ui.notifications.orderHistoryItem;

import java.util.List;

public class orderHistoryAdapter extends RecyclerView.Adapter<orderHistoryAdapter.ViewHolder> {

    private final Context context;
    private final List<orderHistoryItem> list;

    public orderHistoryAdapter(Context context, List<orderHistoryItem> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        orderHistoryItem item = list.get(position);
        holder.tvTransId.setText("Transaksi #" + item.trans_id);
        holder.tvTanggal.setText("Tanggal: " + item.tgl_order);
        holder.tvTotal.setText(String.format("Total: Rp %, .0f", item.total_bayar));
        holder.tvStatus.setText("Status: " + item.getStatusText());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTransId, tvTanggal, tvTotal, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTransId = itemView.findViewById(R.id.tvTransId);
            tvTanggal = itemView.findViewById(R.id.tvTanggal);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
