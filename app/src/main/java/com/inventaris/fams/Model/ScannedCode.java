package com.inventaris.fams.Model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mwildani on 07/09/2017.
 */

public class ScannedCode implements Parcelable {
    private String tahun, code, gambar;
    private Lokasi lokasi;
    private TipeAset tipeAset;
    private boolean isNewData = false;

    public ScannedCode(String code, String tahun, Lokasi lokasi, TipeAset tipeAset, String gambar) {
        this.code = code;
        this.tahun = tahun;
        this.lokasi = lokasi;
        this.tipeAset = tipeAset;
        this.gambar = gambar;
        isNewData = false;
    }

    public ScannedCode(String code) {
        this.code = code;
        isNewData = true;
    }


    protected ScannedCode(Parcel in) {
        tahun = in.readString();
        code = in.readString();
        gambar = in.readString();
        lokasi = in.readParcelable(Lokasi.class.getClassLoader());
        tipeAset = in.readParcelable(TipeAset.class.getClassLoader());
        isNewData = in.readByte() != 0;
    }

    public static final Creator<ScannedCode> CREATOR = new Creator<ScannedCode>() {
        @Override
        public ScannedCode createFromParcel(Parcel in) {
            return new ScannedCode(in);
        }

        @Override
        public ScannedCode[] newArray(int size) {
            return new ScannedCode[size];
        }
    };

    public String getGambar() {
        return gambar;
    }

    public void setGambar(String gambar) {
        this.gambar = gambar;
    }

    public String getTahun() {
        return tahun;
    }

    public void setTahun(String tahun) {
        this.tahun = tahun;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Lokasi getLokasi() {
        return lokasi;
    }

    public void setLokasi(Lokasi lokasi) {
        this.lokasi = lokasi;
    }

    public TipeAset getTipeAset() {
        return tipeAset;
    }

    public void setTipeAset(TipeAset tipeAset) {
        this.tipeAset = tipeAset;
    }

    public boolean isNewData() {
        return isNewData;
    }

    public void setNewData(boolean newData) {
        isNewData = newData;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(tahun);
        dest.writeString(code);
        dest.writeString(gambar);
        dest.writeParcelable(lokasi, flags);
        dest.writeParcelable(tipeAset, flags);
        dest.writeByte((byte) (isNewData ? 1 : 0));
    }
}
