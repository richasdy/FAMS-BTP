package com.inventaris.fams.Model;

/**
 * Created by mwildani on 16/09/2017.
 */

public class Gedung {
    private String id, nama;

    public Gedung(String id, String nama) {
        this.id = id;
        this.nama = nama;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }
}
