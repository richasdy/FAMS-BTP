package com.inventaris.fams.Fragment;


import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.farbod.labelledspinner.LabelledSpinner;
import com.frosquivel.magicalcamera.MagicalCamera;
import com.frosquivel.magicalcamera.MagicalPermissions;
import com.inventaris.fams.Config;
import com.inventaris.fams.HalamanInputData;
import com.inventaris.fams.HalamanUtama;
import com.inventaris.fams.Model.Gedung;
import com.inventaris.fams.Model.Lokasi;
import com.inventaris.fams.Model.PairedDevice;
import com.inventaris.fams.Model.TipeAset;
import com.inventaris.fams.R;
import com.inventaris.fams.Utils.BluetoothConnector;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.pixplicity.easyprefs.library.Prefs;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddData extends Fragment {
    private BluetoothDevice device = null;
    final int handlerState = 0;
    private BluetoothConnector.BluetoothSocketWrapper mSocket = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    BluetoothConnector bluetoothConnector;
    private Handler bluetoothIn;
    private PairedDevice dev;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;

    @BindView(R.id.txtTotalMasterData)
    TextView total;


    // The list of results from actions
    private ArrayAdapter<String> mResultsArrayAdapter;
    private ListView mResultsListView;

    private ArrayList<String> data = new ArrayList<>();

    public AddData() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_data, container, false);
        ButterKnife.bind(this, view);

        new Prefs.Builder()
                .setContext(AddData.this.getContext())
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getActivity().getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        mResultsArrayAdapter = new ArrayAdapter<>(AddData.this.getContext(), android.R.layout.simple_list_item_1);


        mResultsListView = (ListView) view.findViewById(R.id.ListMasterData);
        mResultsListView.setAdapter(mResultsArrayAdapter);
        mResultsListView.setFastScrollEnabled(true);

        mResultsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mResultsArrayAdapter.remove(mResultsArrayAdapter.getItem(position));
                mResultsArrayAdapter.notifyDataSetChanged();
                total.setText("Total Item : " + Integer.toString(mResultsArrayAdapter.getCount()));
                return true;
            }
        });


        return view;
    }

    public void addData(String pesan) {
        if (mResultsArrayAdapter.getCount() == 0) {
            mResultsArrayAdapter.add(pesan);
        } else {
            boolean ada = false;
            for (int i = 0; i < mResultsArrayAdapter.getCount(); i++) {
                if (pesan.equals(mResultsArrayAdapter.getItem(i))) {
                    ada = true;
                    break;
                }
            }
            if (!ada) {
                mResultsArrayAdapter.add(pesan);
            }
        }
        total.setText("Total Item : " + Integer.toString(mResultsArrayAdapter.getCount()));
        scrollResultsListViewToBottom();
    }




    private void scrollResultsListViewToBottom() {
        mResultsListView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                mResultsListView.setSelection(mResultsArrayAdapter.getCount() - 1);
            }
        });
    }



    @OnClick(R.id.btnRegister)
    void doRegister() {
        if (mResultsArrayAdapter.getCount() > 0) {
            data = new ArrayList<>();
            for (int i = 0; i < mResultsArrayAdapter.getCount(); i++) {
                data.add(mResultsArrayAdapter.getItem(i));
            }
            Intent intent = new Intent(getContext(), HalamanInputData.class);
            intent.putStringArrayListExtra("data", data);
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "Tidak ada data yang bisa di register !", Toast.LENGTH_SHORT).show();
        }

    }

}
