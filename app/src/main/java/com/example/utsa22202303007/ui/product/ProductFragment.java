package com.example.utsa22202303007.ui.product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.utsa22202303007.R;
import com.example.utsa22202303007.RegisterAPI;
import com.example.utsa22202303007.databinding.FragmentProductBinding;
import com.example.utsa22202303007.ui.dashboard.OrderHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductFragment extends Fragment {
    private FragmentProductBinding binding;
    private RecyclerView recyclerView, categoryRecyclerView;
    private ProductAdapter adapter;
    private CategoryAdapter categoryAdapter;
    private OrderHelper orderHelper;
    private List<Product> fullProductList;
    private String selectedCategory = "Semua";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProductBinding.inflate(inflater, container, false);
        recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        categoryRecyclerView = binding.categoryRecyclerView;
        categoryRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        orderHelper = new OrderHelper(requireContext());

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterProduct(newText);
                return true;
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupCategory();
        fetchProduct();

        // Handle category selection from HomeFragment
        getParentFragmentManager().setFragmentResultListener("categoryRequest", this, (requestKey, bundle) -> {
            String categoryFromHome = bundle.getString("selectedCategory", "Semua");
            if (categoryFromHome != null && !categoryFromHome.isEmpty()) {
                selectedCategory = categoryFromHome;

                if (categoryAdapter != null) {
                    categoryAdapter.setSelectedCategory(selectedCategory);
                }

                filterProduct(binding.searchView.getQuery().toString());
            }
        });

        // Handle search query from HomeFragment
        getParentFragmentManager().setFragmentResultListener("searchRequest", this, (requestKey, bundle) -> {
            String searchQuery = bundle.getString("searchQuery", "");
            if (searchQuery != null && !searchQuery.isEmpty()) {
                binding.searchView.setQuery(searchQuery, false);
                filterProduct(searchQuery);

                selectedCategory = "Semua";
                if (categoryAdapter != null) {
                    categoryAdapter.setSelectedCategory(selectedCategory);
                }

                binding.searchView.setFocusable(true);
                binding.searchView.setIconified(false);
                binding.searchView.requestFocusFromTouch();
            }
        });

        // Ensure bottom nav reflects current fragment
        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.nav_view);
        if (bottomNav != null) {
            bottomNav.post(() -> bottomNav.setSelectedItemId(R.id.navigation_product));
        }
    }

    private void setupCategory() {
        List<Category> categoryList = new ArrayList<>();
        categoryList.add(new Category("Semua"));
        categoryList.add(new Category("Televisi Tabung"));
        categoryList.add(new Category("Televisi LED"));
        categoryList.add(new Category("Televisi OLED"));

        if (categoryAdapter == null) {
            categoryAdapter = new CategoryAdapter(categoryList, selected -> {
                selectedCategory = selected;
                filterProduct(binding.searchView.getQuery().toString());
            });
            categoryRecyclerView.setAdapter(categoryAdapter);
        }

        categoryAdapter.setSelectedCategory(selectedCategory);
    }

    private void fetchProduct() {
        RegisterAPI apiService = RetrofitClient.getRetrofitInstance().create(RegisterAPI.class);
        Call<List<Product>> call = apiService.getProducts();

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    fullProductList = response.body();

                    adapter = new ProductAdapter(getContext(), fullProductList, new ProductAdapter.OnProductClickListener() {
                        @Override
                        public void onProductClick(Product product) {
                            orderHelper.addToOrder(product);
                            Toast.makeText(getContext(), product.getMerk() + " berhasil ditambahkan ke keranjang", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onProductViewClick(Product product, ProductAdapter.ViewHolder holder) {
                            int newViewCount = product.getViewCount() + 1;
                            product.setViewCount(newViewCount);
                            holder.tvView.setText("Dilihat " + newViewCount + "x");

                            RegisterAPI api = RetrofitClient.getRetrofitInstance().create(RegisterAPI.class);
                            Call<ResponseBody> updateCall = api.updateProductView(product.getKode(), newViewCount);
                            updateCall.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) { }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    Toast.makeText(getContext(), "Gagal update view count", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                    recyclerView.setAdapter(adapter);

                    filterProduct(binding.searchView.getQuery().toString());
                } else {
                    Toast.makeText(getContext(), "Gagal mengambil data produk", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterProduct(String query) {
        if (fullProductList == null) return;
        List<Product> filteredList = new ArrayList<>();

        for (Product product : fullProductList) {
            boolean matchesCategory = selectedCategory.equals("Semua") || product.getKategori().equalsIgnoreCase(selectedCategory);
            boolean matchesQuery = product.getMerk().toLowerCase().contains(query.toLowerCase());
            if (matchesCategory && matchesQuery) {
                filteredList.add(product);
            }
        }

        if (adapter != null) {
            adapter.filterList(filteredList);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}