package com.example.utsa22202303007.ui.Checkout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.utsa22202303007.R;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private Context context;
    private List<AddressItem> addressList;
    private OnAddressSelectedListener listener;
    private int selectedPosition = -1;

    public interface OnAddressSelectedListener {
        void onAddressSelected(AddressItem address);
        void onEditAddress(AddressItem address);
    }

    public AddressAdapter(Context context, List<AddressItem> addressList, OnAddressSelectedListener listener) {
        this.context = context;
        this.addressList = addressList;
        this.listener = listener;

        // Restore selected position temporarily
        SharedPreferences prefs = context.getSharedPreferences("alamat_prefs", Context.MODE_PRIVATE);
        selectedPosition = prefs.getInt("selected_position", -1);
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, @SuppressLint("RecyclerView") int position) {
        AddressItem address = addressList.get(position);

        holder.tvFullName.setText(address.getFullName());
        holder.tvPhoneNumber.setText("| "+address.getPhoneNumber());
        holder.tvAddress.setText(address.getAddress());
        holder.tvCity.setText(address.getCity());
        holder.tvProvince.setText(address.getProvince());
        holder.tvPostalCode.setText(address.getPostalCode());

        // Show/hide label "Default"
        if (address.isDefault()) {
            holder.tvDefaultLabel.setVisibility(View.VISIBLE);
            holder.tvDefaultLabel.setBackground(null);
        } else {
            holder.tvDefaultLabel.setVisibility(View.GONE);
        }

        // Set default if selectedPosition belum diset
        if (selectedPosition == -1 && address.isDefault()) {
            selectedPosition = position;
        }

        holder.radioSelect.setChecked(position == selectedPosition);

        // Klik RadioButton
        holder.radioSelect.setOnClickListener(v -> {
            selectedPosition = holder.getAdapterPosition();

            // Simpan ke SharedPreferences sementara
            SharedPreferences prefs = context.getSharedPreferences("alamat_prefs", Context.MODE_PRIVATE);
            prefs.edit().putInt("selected_position", selectedPosition).apply();

            notifyDataSetChanged();

            if (listener != null) {
                listener.onAddressSelected(address);
            }
        });

        // Klik seluruh item juga bisa pilih
        holder.itemView.setOnClickListener(v -> {
            selectedPosition = holder.getAdapterPosition();

            SharedPreferences prefs = context.getSharedPreferences("alamat_prefs", Context.MODE_PRIVATE);
            prefs.edit().putInt("selected_position", selectedPosition).apply();

            notifyDataSetChanged();

            if (listener != null) {
                listener.onAddressSelected(address);
            }
        });

        // Tombol ubah alamat
        holder.tvUbah.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditAddress(address);
            }
        });
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullName, tvPhoneNumber, tvDefaultLabel, tvAddress, tvCity, tvProvince, tvPostalCode, tvUbah;
        RadioButton radioSelect;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvPhoneNumber = itemView.findViewById(R.id.tvPhoneNumber);
            tvDefaultLabel = itemView.findViewById(R.id.tvDefaultLabel);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvCity = itemView.findViewById(R.id.tvCity);
            tvProvince = itemView.findViewById(R.id.tvProvince);
            tvPostalCode = itemView.findViewById(R.id.tvkodepos);
            tvUbah = itemView.findViewById(R.id.tvUbah);
            radioSelect = itemView.findViewById(R.id.radioSelect);
        }
    }

    public AddressItem getSelectedAddress() {
        if (selectedPosition >= 0 && selectedPosition < addressList.size()) {
            return addressList.get(selectedPosition);
        }
        return null;
    }
}
