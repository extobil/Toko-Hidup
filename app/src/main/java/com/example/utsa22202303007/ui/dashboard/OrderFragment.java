package com.example.utsa22202303007.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.utsa22202303007.databinding.FragmentOrderBinding;
import com.example.utsa22202303007.ui.Checkout.Checkout;
import com.google.gson.Gson;

import java.util.List;

public class OrderFragment extends Fragment {
    private FragmentOrderBinding binding;
    private OrderHelper orderHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOrderBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.recyclerViewOrders.setNestedScrollingEnabled(false);
        orderHelper = new OrderHelper(requireContext());
        setupRecyclerView();

        binding.btnCheckout.setOnClickListener(v -> {
            List<OrderItem> orderItems = orderHelper.getOrderItems();

            if (orderItems.isEmpty()) {
                Toast.makeText(getContext(), "Keranjang kosong!", Toast.LENGTH_SHORT).show();
                return;
            }

            Gson gson = new Gson();
            String json = gson.toJson(orderItems);

            Log.d("OrderFragment", "Checkout orderItems JSON: " + json);

            Intent intent = new Intent(getContext(), Checkout.class);
            intent.putExtra("orderList", json);
            startActivity(intent);
        });

        return root;
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = binding.recyclerViewOrders;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<OrderItem> orderItems = orderHelper.getOrderItems();
        OrderAdapter adapter = new OrderAdapter(requireContext(), orderItems, orderHelper);
        recyclerView.setAdapter(adapter);

        // Set total awal
        updateTotal();

        // Pasang listener agar total bayar update otomatis, jika adapter mendukung
        // Misal adapter punya method setOnOrderChangeListener
        if (adapter instanceof OrderAdapterWithListener) {
            ((OrderAdapterWithListener) adapter).setOnOrderChangeListener(this::updateTotal);
        }
    }

    private void updateTotal() {
        double total = orderHelper.getTotal();
        binding.tvTotal.setText(String.format("Total Bayar: Rp %,.0f", total));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        setupRecyclerView(); // ini akan reload keranjang setelah kembali dari Checkout
    }

    // Contoh interface jika adapter punya listener untuk update total
    public interface OrderAdapterWithListener {
        void setOnOrderChangeListener(Runnable listener);
    }
}
