package com.example.utsa22202303007.ui.Checkout;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
// Ganti import CardView menjadi MaterialCardView jika Anda menggunakan MaterialCardView di XML
// import androidx.cardview.widget.CardView; // Ini yang sebelumnya menyebabkan masalah
import com.google.android.material.card.MaterialCardView; // Import ini yang benar untuk MaterialCardView
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.utsa22202303007.R;

import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;

public class ShippingOptionAdapter extends RecyclerView.Adapter<ShippingOptionAdapter.ShippingOptionViewHolder> {

    private List<ShippingOption> optionsList;
    private OnOptionSelectedListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private int selectedColor; // Warna untuk border terpilih
    // private int defaultColor; // Warna default border - tidak lagi terlalu penting karena diset di XML

    public ShippingOptionAdapter(List<ShippingOption> optionsList, OnOptionSelectedListener listener, android.content.Context context) {
        this.optionsList = optionsList;
        this.listener = listener;
        this.selectedColor = ContextCompat.getColor(context, R.color.colorPrimary); // Misalnya, warna hijau
        // this.defaultColor = ContextCompat.getColor(context, android.R.color.transparent); // Tidak lagi digunakan secara langsung
    }

    public interface OnOptionSelectedListener {
        void onOptionSelected(ShippingOption option);
    }

    public void updateOptions(List<ShippingOption> newOptions) {
        this.optionsList = newOptions;
        selectedPosition = RecyclerView.NO_POSITION; // Reset selection
        notifyDataSetChanged();
    }

    public void clearSelection() {
        int oldSelectedPosition = selectedPosition;
        selectedPosition = RecyclerView.NO_POSITION;
        if (oldSelectedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(oldSelectedPosition);
        }
    }

    @NonNull
    @Override
    public ShippingOptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shipping_option, parent, false);
        return new ShippingOptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShippingOptionViewHolder holder, int position) {
        ShippingOption option = optionsList.get(position);

        holder.tvShippingName.setText(option.getDisplayName());

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        currencyFormat.setMaximumFractionDigits(0);
        String formattedCost = currencyFormat.format(option.getCostValue());

        holder.tvShippingCostAndETD.setText(String.format("%s | Estimasi: %s hari", formattedCost, option.getEtd()));

        // Logika pemilihan CardView
        if (position == selectedPosition) {
            holder.cardShippingOption.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorLightGreen)); // Warna background saat terpilih
            holder.cardShippingOption.setStrokeWidth(4);
            holder.cardShippingOption.setStrokeColor(selectedColor);
        } else {
            holder.cardShippingOption.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white)); // Warna background default
            holder.cardShippingOption.setStrokeWidth(1); // Set ke 0 atau 1 untuk border default
            holder.cardShippingOption.setStrokeColor(Color.parseColor("#E0E0E0")); // Warna border default
        }

        holder.itemView.setOnClickListener(v -> {
            int previousSelectedPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousSelectedPosition);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onOptionSelected(optionsList.get(selectedPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return optionsList.size();
    }

    static class ShippingOptionViewHolder extends RecyclerView.ViewHolder {
        // Ganti CardView menjadi MaterialCardView
        MaterialCardView cardShippingOption;
        TextView tvShippingName;
        TextView tvShippingCostAndETD;

        ShippingOptionViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ganti findViewById dengan MaterialCardView
            cardShippingOption = itemView.findViewById(R.id.cardShippingOption);
            tvShippingName = itemView.findViewById(R.id.tvShippingName);
            tvShippingCostAndETD = itemView.findViewById(R.id.tvShippingCostAndETD);
        }
    }
}