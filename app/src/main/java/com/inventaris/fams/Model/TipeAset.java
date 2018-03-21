package com.inventaris.fams.Model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mwildani on 29/08/2017.
 */

public class TipeAset implements Parcelable {
    private String id, name, tipegeneral;

    public TipeAset(String id, String name, String tipegeneral) {
        this.id = id;
        this.name = name;
        this.tipegeneral = tipegeneral;
    }

    protected TipeAset(Parcel in) {
        id = in.readString();
        name = in.readString();
        tipegeneral = in.readString();
    }

    public static final Creator<TipeAset> CREATOR = new Creator<TipeAset>() {
        @Override
        public TipeAset createFromParcel(Parcel in) {
            return new TipeAset(in);
        }

        @Override
        public TipeAset[] newArray(int size) {
            return new TipeAset[size];
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

    public String getTipegeneral() {
        return tipegeneral;
    }

    public void setTipegeneral(String tipegeneral) {
        this.tipegeneral = tipegeneral;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(tipegeneral);
    }
}
