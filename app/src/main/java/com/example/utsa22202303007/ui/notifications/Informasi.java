package com.example.utsa22202303007.ui.notifications;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.utsa22202303007.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class Informasi extends Fragment implements OnMapReadyCallback {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private GoogleMap mMap;

    public Informasi() {
        // Required empty public constructor
    }

    public static Informasi newInstance(String param1, String param2) {
        Informasi fragment = new Informasi();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_informasi, container, false);

        // Inisialisasi komponen UI
        TextView textWebsite = view.findViewById(R.id.textWebsite);
        TextView textPhone = view.findViewById(R.id.textPhone);
        Button btnOpenMaps = view.findViewById(R.id.btnOpenMaps);
        ImageView btnBack = view.findViewById(R.id.back);

        // Listener tombol website
        textWebsite.setOnClickListener(v -> openWebsite());

        // Listener tombol telepon
        textPhone.setOnClickListener(v -> callPhone());

        // Listener tombol buka Google Maps
        btnOpenMaps.setOnClickListener(v -> openGoogleMaps());

        // Listener tombol kembali
        btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.navigation_notifications);
        });

        // Inisialisasi Map Fragment
        setupMapFragment();

        return view;
    }

    private void setupMapFragment() {
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.mapContainer, mapFragment)
                .commit();
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Lokasi Toko Hidup
        LatLng tokoHidup = new LatLng(-6.9202449, 110.1998279);

        // Tambahkan marker dan fokus kamera
        mMap.addMarker(new MarkerOptions()
                .position(tokoHidup)
                .title("Toko Hidup"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tokoHidup, 15f));

        // Tampilkan kontrol zoom
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    private void openWebsite() {
        String url = "https://www.instagram.com/tokohidup/";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void callPhone() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:+62123456789"));
        startActivity(intent);
    }

    private void openGoogleMaps() {
        Uri gmmIntentUri = Uri.parse("https://maps.app.goo.gl/XfTDN8zdGwQF8Z7g8");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.app.goo.gl/XfTDN8zdGwQF8Z7g8"));
            startActivity(browserIntent);
        }
    }
}
