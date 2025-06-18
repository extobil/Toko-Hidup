package com.example.utsa22202303007;

import com.example.utsa22202303007.ui.Checkout.AddressItem;
import com.example.utsa22202303007.ui.product.Product;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface RegisterAPI {
    @FormUrlEncoded
    @POST("post_register.php")
    Call<ResponseBody> register(@Field("email") String email,
                                @Field("nama") String nama,
                                @Field("password") String password);
    @FormUrlEncoded
    @POST("get_login.php")
    Call<ResponseBody> login(
            @Field("email") String email,
            @Field("password") String password
    );

    @GET("get_profile.php")
    Call<ResponseBody> getProfile(
            @Query("email") String email
    );
    @FormUrlEncoded
    @POST("post_profile.php")
    Call<ResponseBody> updateProfile(
            @Field("nama") String nama,
            @Field("alamat") String alamat,
            @Field("kota") String kota,
            @Field("provinsi") String provinsi,
            @Field("telp") String telp,
            @Field("kodepos") String kodepos,
            @Field("email") String email
    );
    @GET("get_product.php") // Sesuaikan dengan URL API Anda
    Call<List<Product>> getProducts();

    @FormUrlEncoded
    @POST("update_view.php")
    Call<ResponseBody> updateProductView(
            @Field("kode") String kode,
            @Field("view") int viewCount
    );

    @Multipart
    @POST("uploadimages.php")
    Call<ResponseBody> uploadFotoProfil(
            @Part("id") RequestBody id,
            @Part MultipartBody.Part imageupload
    );

    @FormUrlEncoded
    @POST("ganti_password.php") // Pastikan ini sesuai dengan nama file PHP Anda
    Call<ResponseBody> changePassword(
            @Field("email") String email,
            @Field("password_lama") String passwordLama,
            @Field("password_baru") String passwordBaru
    );

    @GET("get_address.php")
    Call<List<AddressItem>> getUserAddresses(
            @Query("user_id") int userId
    );

    @GET("get_address.php") // Sesuaikan dengan nama file PHP Anda
    Call<ResponseBody> getAddress(@Query("user_id") int userId);

    @POST("add_address.php")
    Call<ResponseBody> addAddress(@Body Map<String, Object> addressData);

    // --- Metode BARU untuk mendapatkan user ID berdasarkan email ---
    @GET("get_user_id_by_email.php")
    Call<ResponseBody> getUserIdByEmail(@Query("email") String email);

    @FormUrlEncoded // Gunakan ini jika Anda mengirim data sebagai Form-UrlEncoded
    @POST("update_address.php")
    Call<ResponseBody> updateAddress(
            @Field("alamat_id") int alamatId,
            @Field("pelanggan_id") int pelangganId,
            @Field("nama_penerima") String namaPenerima,
            @Field("telepon") String telepon,
            @Field("alamat_lengkap") String alamatLengkap,
            @Field("kota") String kota,
            @Field("provinsi") String provinsi,
            @Field("kodepos") String kodepos,
            @Field("is_default") int isDefault
    );


}
