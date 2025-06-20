package com.example.utsa22202303007.ui.notifications;

public class OrderModel {
    private int trans_id;
    private int id;
    private String nama_kirim;
    private String email_kirim;
    private String telp_kirim;
    private String alamat_kirim;
    private String kota_kirim;
    private String provinsi_kirim;
    private String kodepos_kirim;
    private String lama_kirim;
    private String tgl_order;
    private String status_text;
    private String fileName;
    private double subtotal;
    private double ongkir;
    private double total_bayar;
    private String metode_bayar;
    private String bukti_bayar;
    private String status;

    // dari tbl_order_detail
    private String kode;
    private double harga;
    private int qty;
    private double bayar;

    // dari mobil
    private String merk;
    private String foto;

    public OrderModel() {
    }

    // --- Getter & Setter ---

    public int getTrans_id() {
        return trans_id;
    }

    public void setTrans_id(int trans_id) {
        this.trans_id = trans_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNama_kirim() {
        return nama_kirim;
    }

    public void setNama_kirim(String nama_kirim) {
        this.nama_kirim = nama_kirim;
    }

    public String getEmail_kirim() {
        return email_kirim;
    }

    public void setEmail_kirim(String email_kirim) {
        this.email_kirim = email_kirim;
    }

    public String getTelp_kirim() {
        return telp_kirim;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setTelp_kirim(String telp_kirim) {
        this.telp_kirim = telp_kirim;
    }

    public String getAlamat_kirim() {
        return alamat_kirim;
    }

    public void setAlamat_kirim(String alamat_kirim) {
        this.alamat_kirim = alamat_kirim;
    }

    public String getKota_kirim() {
        return kota_kirim;
    }

    public void setKota_kirim(String kota_kirim) {
        this.kota_kirim = kota_kirim;
    }

    public String getProvinsi_kirim() {
        return provinsi_kirim;
    }

    public void setProvinsi_kirim(String provinsi_kirim) {
        this.provinsi_kirim = provinsi_kirim;
    }

    public String getKodepos_kirim() {
        return kodepos_kirim;
    }

    public void setKodepos_kirim(String kodepos_kirim) {
        this.kodepos_kirim = kodepos_kirim;
    }

    public String getLama_kirim() {
        return lama_kirim;
    }

    public void setLama_kirim(String lama_kirim) {
        this.lama_kirim = lama_kirim;
    }

    public String getTgl_order() {
        return tgl_order;
    }

    public void setTgl_order(String tgl_order) {
        this.tgl_order = tgl_order;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getOngkir() {
        return ongkir;
    }

    public void setOngkir(double ongkir) {
        this.ongkir = ongkir;
    }

    public double getTotal_bayar() {
        return total_bayar;
    }

    public void setTotal_bayar(double total_bayar) {
        this.total_bayar = total_bayar;
    }

    public String getMetode_bayar() {
        return metode_bayar;
    }

    public void setMetode_bayar(String metode_bayar) {
        this.metode_bayar = metode_bayar;
    }

    public String getBukti_bayar() {
        return bukti_bayar;
    }

    public void setBukti_bayar(String bukti_bayar) {
        this.bukti_bayar = bukti_bayar;
    }

    public String getStatus_text() {
        return status_text;
    }

    public void setStatus_text(String status_text) {
        this.status_text = status_text;
    }

    public String getKode() {
        return kode;
    }

    public void setKode(String kode) {
        this.kode = kode;
    }

    public double getHarga() {
        return harga;
    }

    public void setHarga(double harga) {
        this.harga = harga;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public double getBayar() {
        return bayar;
    }

    public void setBayar(double bayar) {
        this.bayar = bayar;
    }

    public String getMerk() {
        return merk;
    }

    public void setMerk(String merk) {
        this.merk = merk;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }
}
