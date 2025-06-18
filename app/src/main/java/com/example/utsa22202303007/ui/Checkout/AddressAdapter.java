package com.example.utsa22202303007.ui.Checkout;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.utsa22202303007.R;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private Context context;
    private List<AddressItem> addressList;
    private OnAddressSelectedListener listener;

    public interface OnAddressSelectedListener {
        void onAddressSelected(AddressItem address);
        void onEditAddress(AddressItem address);
    }

    public AddressAdapter(Context context, List<AddressItem> addressList, OnAddressSelectedListener listener) {
        this.context = context;
        this.addressList = addressList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        AddressItem address = addressList.get(position);

        holder.tvFullName.setText(address.getFullName());
        holder.tvPhoneNumber.setText(address.getPhoneNumber());
        holder.tvAddress.setText(address.getAddress());
        holder.tvCity.setText(address.getCity());
        holder.tvProvince.setText(address.getProvince());
        holder.tvPostalCode.setText(address.getPostalCode());

        if (address.isDefault()) {
            holder.tvDefaultLabel.setVisibility(View.VISIBLE);
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.RECTANGLE);
            shape.setCornerRadius(8);
            shape.setColor(Color.parseColor("#388E3C"));
            holder.tvDefaultLabel.setBackground(shape);
        } else {
            holder.tvDefaultLabel.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddressSelected(address);
            }
        });

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
        }
    }
}