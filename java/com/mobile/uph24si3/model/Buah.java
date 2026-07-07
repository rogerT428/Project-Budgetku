package com.mobile.uph24si3.model;

import java.util.List;

public class Buah {
    private String nama;
    private String emoji;
    private String asal;
    private String rasa;
    private String deskripsi;
    private List<String> manfaat;

    public Buah(String nama, String emoji, String asal, String rasa, String deskripsi, List<String> manfaat) {
        this.nama = nama;
        this.emoji = emoji;
        this.asal = asal;
        this.rasa = rasa;
        this.deskripsi = deskripsi;
        this.manfaat = manfaat;
    }

    public String getNama() { return nama; }
    public String getEmoji() { return emoji; }
    public String getAsal() { return asal; }
    public String getRasa() { return rasa; }
    public String getDeskripsi() { return deskripsi; }
    public List<String> getManfaat() { return manfaat; }
}
