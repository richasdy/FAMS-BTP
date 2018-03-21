package com.inventaris.fams.Model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mwildani on 29/08/2017.
 */

public class Lokasi implements Parcelable {
    private String id, name,idGedung ;

    public Lokasi(String id, String name, String idGedung) {
        this.id = id;
        this.name = name;
        this.idGedung = idGedung;
    }

    protected Lokasi(Parcel in) {
        id = in.readString();
        name = in.readString();
        idGedung = in.readString();
    }

    public static final Creator<Lokasi> CREATOR = new Creator<Lokasi>() {
        @Override
        public Lokasi createFromParcel(Parcel in) {
            return new Lokasi(in);
        }

        @Override
        public Lokasi[] newArray(int size) {
            return new Lokasi[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdGedung() {
        return idGedung;
    }

    public void setIdGedung(String idGedung) {
        this.idGedung = idGedung;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(idGedung);
    }
}
