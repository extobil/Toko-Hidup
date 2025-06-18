package com.example.utsa22202303007.ui.home;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.utsa22202303007.R;
import com.example.utsa22202303007.RegisterAPI;
import com.example.utsa22202303007.ServerAPI;
import com.example.utsa22202303007.databinding.FragmentHomeBinding;
import com.example.utsa22202303007.ui.dashboard.OrderHelper;
import com.example.utsa22202303007.ui.product.Product;
import com.example.utsa22202303007.ui.product.ProductAdapter;
import com.example.utsa22202303007.ui.product.RetrofitClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private String email;
    private OrderHelper orderHelper;
    private ProductAdapter adapter;
    private List<Product> fullProductList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize OrderHelper
        orderHelper = new OrderHelper(requireContext());

        // Setup RecyclerView
        binding.recyclerTrending.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.recyclerTrending.setNestedScrollingEnabled(false);

        // Get email from SharedPreferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user_session", getContext().MODE_PRIVATE);
        email = sharedPreferences.getString("email", "");

        if (!email.isEmpty()) {
            getProfile(email);
        } else {
            binding.textUserName.setText("Hi Guest");
            binding.imageProfile.setImageResource(R.drawable.img);
        }

        // Set up image slider
        setupImageSlider();

        setupCategoryClickListeners();
        setupSearchView();
        fetchTrendingProducts();

        return root;
    }

    private void setupImageSlider() {
        ImageSlider imageSlider = binding.imageSlider;
        ArrayList<SlideModel> slideModels = new ArrayList<>();
        slideModels.add(new SlideModel(R.drawable.promo1, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.promo2, ScaleTypes.FIT));
        imageSlider.setImageList(slideModels, ScaleTypes.FIT);
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                navigateToProductWithSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void setupCategoryClickListeners() {
        binding.tabung.setOnClickListener(v -> navigateToProduct("Televisi Tabung"));
        binding.led.setOnClickListener(v -> navigateToProduct("Televisi LED"));
        binding.oled.setOnClickListener(v -> navigateToProduct("Televisi OLED"));
    }

    private void navigateToProduct(String category) {
        try {
            Bundle result = new Bundle();
            result.putString("selectedCategory", category);
            getParentFragmentManager().setFragmentResult("categoryRequest", result);

            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.navigation_product);

            // Update bottom nav selection
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.nav_view);
            if (bottomNav != null) {
                bottomNav.post(() -> bottomNav.setSelectedItemId(R.id.navigation_product));
            }
        } catch (Exception e) {
            Log.e(TAG, "Navigation error", e);
            Toast.makeText(getContext(), "Navigation error", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToProductWithSearch(String searchQuery) {
        try {
            Bundle result = new Bundle();
            result.putString("searchQuery", searchQuery);
            getParentFragmentManager().setFragmentResult("searchRequest", result);

            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.navigation_product);

            // Update bottom nav selection
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.nav_view);
            if (bottomNav != null) {
                bottomNav.post(() -> bottomNav.setSelectedItemId(R.id.navigation_product));
            }
        } catch (Exception e) {
            Log.e(TAG, "Navigation error", e);
            Toast.makeText(getContext(), "Navigation error", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchTrendingProducts() {
        RegisterAPI apiService = RetrofitClient.getRetrofitInstance().create(RegisterAPI.class);
        Call<List<Product>> call = apiService.getProducts();

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> allProducts = response.body();
                    allProducts.sort((p1, p2) -> Integer.compare(p2.getViewCount(), p1.getViewCount()));
                    fullProductList = allProducts.subList(0, Math.min(6, allProducts.size()));

                    setupProductAdapter();
                } else {
                    Toast.makeText(getContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupProductAdapter() {
        adapter = new ProductAdapter(getContext(), fullProductList, new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                orderHelper.addToOrder(product);
                Toast.makeText(getContext(), product.getMerk() + " added to cart", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProductViewClick(Product product, ProductAdapter.ViewHolder holder) {
                int newViewCount = product.getViewCount() + 1;
                product.setViewCount(newViewCount);
                holder.tvView.setText("Viewed " + newViewCount + "x");

                updateProductViewCount(product.getKode(), newViewCount);
            }
        });

        binding.recyclerTrending.setAdapter(adapter);
    }

    private void updateProductViewCount(String productCode, int viewCount) {
        RegisterAPI api = RetrofitClient.getRetrofitInstance().create(RegisterAPI.class);
        Call<ResponseBody> updateCall = api.updateProductView(productCode, viewCount);
        updateCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // Optional success handling
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to update view count", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getProfile(String vemail) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(new ServerAPI().BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RegisterAPI api = retrofit.create(RegisterAPI.class);
        api.getProfile(vemail).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        processProfileResponse(response.body().string());
                    }
                } catch (Exception e) {
                    handleProfileError(e);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleProfileError(t);
            }
        });
    }

    private void processProfileResponse(String responseString) throws Exception {
        JSONObject json = new JSONObject(responseString);

        if (json.getString("result").equals("1")) {
            JSONObject user = json.getJSONObject("data");
            String nama = user.getString("nama");
            String fotoFilename = user.optString("filename", "");

            binding.textUserName.setText("Hi " + nama);
            loadProfileImage(fotoFilename);
        }
    }

    private void loadProfileImage(String fotoFilename) {
        if (!fotoFilename.isEmpty()) {
            String imageUrl = ServerAPI.BASE_URL + "img/" + fotoFilename;
            Glide.with(requireContext())
                    .load(imageUrl)
                    .circleCrop()
                    .placeholder(R.drawable.img)
                    .error(R.drawable.img)
                    .into(binding.imageProfile);
        } else {
            binding.imageProfile.setImageResource(R.drawable.img);
        }
    }

    private void handleProfileError(Throwable t) {
        Log.e(TAG, "Profile error", t);
        binding.textUserName.setText("Hi Guest");
        binding.imageProfile.setImageResource(R.drawable.logo);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}