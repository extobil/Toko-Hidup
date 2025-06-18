package com.example.utsa22202303007.ui.notifications;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.utsa22202303007.R;
import com.example.utsa22202303007.login;

public class GuestProfile extends Fragment {

    public GuestProfile() {
        // Required empty public constructor
    }

    public static GuestProfile newInstance(String param1, String param2) {
        GuestProfile fragment = new GuestProfile();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout
        View view = inflater.inflate(R.layout.fragment_guest_profile, container, false);

        // Tombol Login
        Button btnLogin = view.findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> {
            // Hapus SharedPreferences user_session
            if (getActivity() != null) {
                getActivity().getSharedPreferences("user_session", getActivity().MODE_PRIVATE)
                        .edit()
                        .clear()
                        .apply();
            }

            // Intent ke login.java
            Intent intent = new Intent(getActivity(), login.class);
            startActivity(intent);
            requireActivity().finish(); // Tutup activity agar tidak bisa kembali ke guest
        });

        return view;
    }
}
