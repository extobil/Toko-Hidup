package com.example.utsa22202303007.ui.notifications;

public class DataPelanggan {
    private String email, nama, alamat, kota, provinsi, kodepos, telp, filename;

    public void setEmail(String email) { this.email = email; }
    public void setNama(String nama) { this.nama = nama; }
    public void setAlamat(String alamat) { this.alamat = alamat; }
    public void setKota(String kota) { this.kota = kota; }
    public void setProvinsi(String provinsi) { this.provinsi = provinsi; }
    public void setKodepos(String kodepos) { this.kodepos = kodepos; }
    public void setTelp(String telp) { this.telp = telp; }
    public void setFilename(String filename) { this.filename = filename; } // Fix di sini

    public String getEmail() { return email; }
    public String getNama() { return nama; }
    public String getAlamat() { return alamat; }
    public String getKota() { return kota; }
    public String getProvinsi() { return provinsi; }
    public String getKodepos() { return kodepos; }
    public String getTelp() { return telp; }
    public String getFilename() { return filename; }
}
