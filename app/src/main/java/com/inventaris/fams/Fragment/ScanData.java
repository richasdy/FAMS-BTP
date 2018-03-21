package com.inventaris.fams.Fragment;


import android.app.Dialog;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
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
import com.frosquivel.magicalcamera.Utilities.ConvertSimpleImage;
import com.inventaris.fams.Adapter.AdapterScannedCode;
import com.inventaris.fams.Config;
import com.inventaris.fams.FamsModel;
import com.inventaris.fams.HalamanEditData;
import com.inventaris.fams.HalamanInputData;
import com.inventaris.fams.HalamanUtama;
import com.inventaris.fams.Model.Lokasi;
import com.inventaris.fams.Model.ScannedCode;
import com.inventaris.fams.Model.TipeAset;
import com.inventaris.fams.R;
import com.inventaris.fams.Utils.ModelBase;
import com.inventaris.fams.Utils.WeakHandler;
import com.pixplicity.easyprefs.library.Prefs;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScanData extends Fragment implements LabelledSpinner.OnItemChosenListener {
    @BindView(R.id.listScanner)
    ListView list;
    @BindView(R.id.txtJumlahItem)
    TextView jumItem;
    @BindView(R.id.edCari)
    MaterialEditText edCari;
    @BindView(R.id.bt_toggle_filter)
    ImageButton toggleFilter;
    @BindView(R.id.layoutIcon)
    View layoutIcon;
    @BindView(R.id.layoututama)
    View layoututama;
    @BindView(R.id.layoutIsiFilter)
    View layoutFilter;
    @BindView(R.id.txtLokasi)
    TextView lokasi;

    @BindView(R.id.spinnerKlasifikasi)
    View spinnerKlasifikasi;

    AdapterScannedCode adapter;

    AlertDialog dialog1;

    MaterialDialog dialog;

    boolean show = false;

    ArrayList<String> dataKlasifikasi = new ArrayList<>();
    private ArrayList<String> newdata = new ArrayList<>();
    private ArrayAdapter<String> klasifikasiArrayAdapter;

    private ListView listKlasifikasi;
    private int currentPage, last_page;

    public ScanData() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_scan_data, container, false);
        ButterKnife.bind(this, v);

        listKlasifikasi = new ListView(ScanData.this.getContext());

        //PAKAI DUMMY DATA
        dummyData();

        new Prefs.Builder()
                .setContext(ScanData.this.getContext())
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getActivity().getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        adapter = new AdapterScannedCode(ScanData.this.getContext());
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (adapter.getItem(position).isNewData()) {
                    newdata.clear();
                    newdata.add(adapter.getItem(position).getCode());
                    Intent intent = new Intent(ScanData.this.getContext(), HalamanInputData.class)
                            .putStringArrayListExtra("data", newdata);
                    startActivity(intent);
                } else {
//                    Toast.makeText(getContext(), "Halaman Edit", Toast.LENGTH_SHORT).show();
                    detailOnDialog(position);
                }
            }
        });
        listKlasifikasi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.filterbylokasi(dataKlasifikasi.get(position));
                lokasi.setText(dataKlasifikasi.get(position));
                dialog1.dismiss();
            }
        });
        edCari.addTextChangedListener(cariListener);
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                jumItem.setText("Total Item : " + Integer.toString(adapter.getCount()));
                jumItem.setVisibility(View.VISIBLE);
                super.onChanged();
            }
        });
        return v;
    }

    TextWatcher cariListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            adapter.filterbyCode(s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @OnClick(R.id.txtRefresh)
    void muatUlang() {
        lokasi.setText("All");
        adapter.refreshList();
    }

    @OnClick({R.id.layoututama, R.id.bt_toggle_filter})
    void clickarrow() {
        show = toggleArrow(toggleFilter);
        if (show) {
            layoutFilter.setVisibility(View.VISIBLE);
        } else {
            layoutFilter.setVisibility(View.GONE);
        }
    }

    public boolean toggleArrow(View view) {
        if (view.getRotation() == 0) {
            view.animate().setDuration(200).rotation(180);
            return true;
        } else {
            view.animate().setDuration(200).rotation(0);
            return false;
        }
    }


    @OnClick(R.id.btnCari)
    void cariClick() {
        adapter.refreshList();
        spinnerKlasifikasi.setVisibility(View.GONE);
        edCari.setVisibility(View.VISIBLE);
    }


    @OnClick(R.id.btnKategori)
    void kategoriClick() {
        edCari.setVisibility(View.GONE);
        spinnerKlasifikasi.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.spinnerKlasifikasi)
    void bukaSpinnerKlasifikasi() {
        currentPage = 1;
        klasifikasiArrayAdapter = new ArrayAdapter<String>(ScanData.this.getContext(), android.R.layout.simple_list_item_1, dataKlasifikasi);
        listKlasifikasi.setAdapter(klasifikasiArrayAdapter);
        AlertDialog.Builder builder = new AlertDialog.Builder(ScanData.this.getContext());
        builder.setCancelable(true);
        builder.setPositiveButton("OK", null);
        if (listKlasifikasi.getParent() != null) {
            ((ViewGroup) listKlasifikasi.getParent()).removeView(listKlasifikasi);
        }
        builder.setView(listKlasifikasi);
        dialog1 = builder.create();
        dialog1.show();
//        getDataKlasifikasi(currentPage);
    }

    private void detailOnDialog(final int position) {
        final Dialog dialog = new Dialog(ScanData.this.getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_detail_asset);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        ((TextView) dialog.findViewById(R.id.txtYear)).setText("Tahun : " + adapter.getItem(position).getTahun());
        ((TextView) dialog.findViewById(R.id.txtLocation)).setText("Lokasi : " + adapter.getItem(position).getLokasi().getName());
        ((TextView) dialog.findViewById(R.id.txtTipeAset)).setText("Klasifikasi : " + adapter.getItem(position).getTipeAset().getName());
        ((TextView) dialog.findViewById(R.id.txtMerkAset)).setText("Merk");
        ((TextView) dialog.findViewById(R.id.txtNilaiAset)).setText("Nilai Aset");

        if (!adapter.getItem(position).getGambar().equals("null")) {
            byte[] anotherArrayBytes = ConvertSimpleImage.stringBase64ToBytes(adapter.getItem(position).getGambar());
            Bitmap myImageAgain = ConvertSimpleImage.bytesToBitmap(anotherArrayBytes);
            ((ImageView) dialog.findViewById(R.id.gambarAset)).setImageBitmap(myImageAgain);
            ((ImageView) dialog.findViewById(R.id.gambarAset)).setVisibility(View.VISIBLE);
        }

        ((ImageButton) dialog.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        ((Button) dialog.findViewById(R.id.btnDelete)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                deleteData(position, adapter.getItem(position).getCode());
            }
        });
        ((Button) dialog.findViewById(R.id.btnEdit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScanData.this.getContext(), HalamanEditData.class);
                intent.putExtra("data", adapter.getItem(position));
                dialog.dismiss();
                startActivity(intent);
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    private void deleteData(final int position, String id) {
        new MaterialDialog.Builder(ScanData.this.getContext())
                .content("Apakah Anda Yakin Ingin Menghapus Data dengan ID : " + id + "?")
                .positiveText("YA")
                .negativeText("TIDAK")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        hapusData((adapter.getItem(position).getCode()), position);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }


    public void onNewData(String kode) {
        cariData(kode);
    }

    private void cariData(final String epcbc) {
        String kode = "";
        if (epcbc.startsWith("EPC")) {
            kode = epcbc.substring(5);
        } else if (epcbc.startsWith("BC")) {
            kode = epcbc.substring(4);
        } else {
            kode = epcbc;
        }
//        showDialog();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Config.URL_SEARCH_ASSET + kode,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject dataJson = new JSONObject(response);
                            if (dataJson.getString("status").equals("success")) {
                                JSONObject data = dataJson.getJSONObject("data");
                                String dataTahun = data.getString("year");
                                JSONObject lokasi = data.getJSONObject("location");
                                Lokasi datalokasi = new Lokasi(lokasi.getString("id"), lokasi.getString("name")
                                        , lokasi.getString("id_gedung"));
                                JSONObject tipeaset = data.getJSONObject("type_detail");
                                JSONObject tipeGeneral = tipeaset.getJSONObject("type_parent");
                                TipeAset dataTipeAset = new TipeAset(tipeaset.getString("id"), tipeaset.getString("name"),
                                        tipeGeneral.getString("name"));
                                ScannedCode scannedCode = new ScannedCode(data.getString("id"), dataTahun, datalokasi, dataTipeAset, data.getString("image"));
                                addData(data.getString("id"), scannedCode);
                                addDataKlasifikasi(datalokasi.getName());
                                jumItem.setText("Total Item : " + Integer.toString(adapter.getCount()));
                                jumItem.setVisibility(View.VISIBLE);
                                scrollListViewToBottom();
//                                dismissDialog();
                            } else {
                                if (dataJson.getString("message").equals("Asset Not Found")) {
                                    ScannedCode scannedCode = new ScannedCode(epcbc);
                                    addData(epcbc, scannedCode);
                                    jumItem.setText("Total Item : " + Integer.toString(adapter.getCount()));
                                    jumItem.setVisibility(View.VISIBLE);
                                    scrollListViewToBottom();
                                }
//                                dismissDialog();
                            }

                        } catch (Exception e) {
//                            dismissDialog();
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if (error instanceof NetworkError || error instanceof NoConnectionError || error instanceof TimeoutError) {
//                    dismissDialog();
                    Toast.makeText(getContext(), "Terjadi kesalah pada koneksi internet anda ! tolong ulangi lagi", Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap();
                try {
                    headers.put("Authorization", Prefs.getString(Config.TOKEN_SHARED_PREF, ""));
                    return headers;
                } catch (Exception e) {
//                    dismissDialog();
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    return headers;
                }
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(stringRequest);
    }

    private void showDialog(String title) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(ScanData.this.getContext())
                .title(title)
                .progress(true, 0)
                .content("Mohon tunggu !");

        dialog = builder.build();
        dialog.show();
    }

    private void dismissDialog() {
        dialog.dismiss();
    }

    private void scrollListViewToBottom() {
        list.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                list.setSelection(adapter.getCount() - 1);
            }
        });
    }

    private void addData(String kode, ScannedCode data) {
        if (adapter.getDataCodeOrigin().size() == 0) {
            adapter.addData(data);
        } else {
            boolean ada = false;
            for (ScannedCode data1 : adapter.getDataCodeOrigin()) {
                if (data1.getCode().equals(kode)) {
                    ada = true;
                    break;
                }
            }
            if (!ada) {
                adapter.addData(data);
            }
        }
    }

    @Override
    public void onItemChosen(View labelledSpinner, AdapterView<?> adapterView, View itemView, int position, long id) {
        switch (labelledSpinner.getId()) {

            // If you have multiple LabelledSpinners, you can add more cases here
        }
    }

    @Override
    public void onNothingChosen(View labelledSpinner, AdapterView<?> adapterView) {

    }

    private void hapusData(String idAsset, final int posisi) {
        showDialog("Menghapus Data");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Config.URL_DELETE_ASSET + idAsset,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject data = new JSONObject(response);
                            if (data.getString("status").equals("success")) {
                                Toast.makeText(ScanData.this.getContext(), data.getString("message"), Toast.LENGTH_SHORT).show();
                                adapter.hapusData(posisi);
                            } else {
                                Toast.makeText(ScanData.this.getContext(), "gagal menghapus data", Toast.LENGTH_SHORT).show();
                            }
                            dismissDialog();
                        } catch (Exception e) {
                            dismissDialog();
                            Toast.makeText(ScanData.this.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if (error instanceof NetworkError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                    dismissDialog();
                    Toast.makeText(ScanData.this.getContext(), "Terjadi kesalah pada koneksi internet anda ! tolong ulangi lagi", Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap();
                try {
                    headers.put("Authorization", Prefs.getString(Config.TOKEN_SHARED_PREF, ""));
                    return headers;
                } catch (Exception e) {
                    dismissDialog();
                    Toast.makeText(ScanData.this.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    return headers;
                }
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(ScanData.this.getContext());
        requestQueue.add(stringRequest);
    }

    private void addDataKlasifikasi(String nama) {
        if (dataKlasifikasi.size() == 0) {
            dataKlasifikasi.add(nama);
        } else {
            boolean ada = false;
            for (int i = 0; i < dataKlasifikasi.size(); i++) {
                if (dataKlasifikasi.get(i).equals(nama)) {
                    ada = true;
                    break;
                }
            }
            if (!ada) {
                dataKlasifikasi.add(nama);
            }
        }
    }

    private void dummyData() {
        ArrayList<String> dummy = new ArrayList<>();
        dummy.add("H1702501014");
        dummy.add("H1704501018");
        dummy.add("L0002501010");
        dummy.add("H9803201012");
        dummy.add("L1203201011");
        dummy.add("L1503201009");
        dummy.add("L1703301002");
        dummy.add("L1704202003");
        dummy.add("L9002203008");

        for (String data : dummy) {
            cariData(data);
        }
    }
}

