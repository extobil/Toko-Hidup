package com.example.utsa22202303007.ui.notifications;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.utsa22202303007.R;
import com.example.utsa22202303007.RegisterAPI;
import com.example.utsa22202303007.ServerAPI;

import java.io.File;
import java.util.List;

import okhttp3.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryFragment extends Fragment {

    private RecyclerView rvOrder;
    private OrderHistoryAdapter adapter;
    private static final int PICK_IMAGE_REQUEST = 1;

    private Uri imageUri;
    private OrderModel selectedOrder;
    private int userId; // Tidak lagi hardcoded

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        rvOrder = view.findViewById(R.id.rvOrderHistory);
        rvOrder.setLayoutManager(new LinearLayoutManager(getContext()));

        // Ambil id_pelanggan dari SharedPreferences
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_session", getContext().MODE_PRIVATE);
        userId = sharedPreferences.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(getContext(), "User belum login!", Toast.LENGTH_SHORT).show();
            return view;
        }

        loadOrderHistory();

        return view;
    }

    private void loadOrderHistory() {
        RegisterAPI api = ServerAPI.getClient().create(RegisterAPI.class);
        Call<ResponseOrderHistory> call = api.getOrderHistory(userId);

        call.enqueue(new Callback<ResponseOrderHistory>() {
            @Override
            public void onResponse(Call<ResponseOrderHistory> call, Response<ResponseOrderHistory> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<OrderModel> orderList = response.body().getData();

                    adapter = new OrderHistoryAdapter(getContext(), orderList, new OrderHistoryAdapter.OnUploadClickListener() {
                        @Override
                        public void onPilihGambarClicked(OrderModel order) {
                            selectedOrder = order;
                            openGallery();
                        }
                    });

                    rvOrder.setAdapter(adapter);
                } else {
                    Toast.makeText(getContext(), "Gagal: " + (response.body() != null ? response.body().getMessage() : "Null response"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseOrderHistory> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            String fileName = getFileNameFromUri(imageUri);
            if (selectedOrder != null) {
                selectedOrder.setFileName(fileName);
            }

            uploadBuktiBayar(selectedOrder.getTrans_id(), imageUri, fileName);
        }
    }


    private void uploadBuktiBayar(int transId, Uri imageUri, String fileName) {
        String filePath = getRealPathFromURI(imageUri);
        if (filePath == null) {
            Toast.makeText(getContext(), "Gagal membaca file gambar", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = new File(filePath);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("bukti_bayar", file.getName(), requestFile);
        RequestBody id = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(transId));

        RegisterAPI api = ServerAPI.getClient().create(RegisterAPI.class);
        Call<ResponseBody> call = api.uploadBuktiBayar(id, body);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Toast.makeText(getContext(), "Upload berhasil", Toast.LENGTH_SHORT).show();

                // Update nama file dan refresh adapter
                if (selectedOrder != null) {
                    selectedOrder.setFileName(fileName);
                }

                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "Upload gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private String getRealPathFromURI(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContext().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int colIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String result = cursor.getString(colIndex);
            cursor.close();
            return result;
        }
        return null;
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            cursor.moveToFirst();
            result = cursor.getString(nameIndex);
            cursor.close();
        }
        return result;
    }
}
