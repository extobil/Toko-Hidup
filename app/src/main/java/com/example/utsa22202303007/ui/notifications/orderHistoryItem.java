package com.example.utsa22202303007.ui.notifications;

public class orderHistoryItem {
    public int trans_id;
    public String tgl_order;
    public double total_bayar;
    public int status;

    public String getStatusText() {
        switch (status) {
            case 0: return "Belum Dibayar";
            case 1: return "Diproses";
            case 2: return "Dikirim";
            case 3: return "Selesai";
            default: return "Tidak Diketahui";
        }
    }
}
