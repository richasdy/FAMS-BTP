package com.inventaris.fams;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.blackcat.currencyedittext.CurrencyEditText;
import com.farbod.labelledspinner.LabelledSpinner;
import com.frosquivel.magicalcamera.MagicalCamera;
import com.frosquivel.magicalcamera.MagicalPermissions;
import com.frosquivel.magicalcamera.Utilities.ConvertSimpleImage;
import com.inventaris.fams.Model.Gedung;
import com.inventaris.fams.Model.Lokasi;
import com.inventaris.fams.Model.TipeAset;
import com.inventaris.fams.Utils.APIService;
import com.inventaris.fams.Utils.APIUtils;
import com.inventaris.fams.Utils.ResponsePost;
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
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;

public class HalamanInputData extends AppCompatActivity implements LabelledSpinner.OnItemChosenListener {
    @BindView(R.id.spinnerSource)
    LabelledSpinner spinnerSource;
    @BindView(R.id.edTahun)
    MaterialEditText edTahun;
    @BindView(R.id.edMerk)
    MaterialEditText edMerk;
    @BindView(R.id.imageView)
    ImageView gambar;
    @BindView(R.id.NilaiAset)
    CurrencyEditText harga;

    @BindView(R.id.txtGedung)
    TextView pilihanGedung;
    //    @BindView(R.id.txtLantai)
//    TextView pilihanLantai;
    @BindView(R.id.txtRuang)
    TextView pilihanRuang;
    @BindView(R.id.txtTipe)
    TextView pilihanTipe;

    @BindView(R.id.txtTotalDataRegister)
    TextView txtDataRegister;

    ListView listDialog, listDataRegister;
    private AlertDialog dialog;
    private MaterialDialog dialog1;

    private String sumber[] = {"Hibah", "Logistik"};
    private String asal = "";
    private String konteks = "";
    private String idLokasi, idGedung, idLantai, idTipeDetailAset;

    private ArrayList<Gedung> dataGedung = new ArrayList<>();
    private ArrayAdapter<String> gedungArrayAdapter;

    private ArrayList<TipeAset> dataTipeaset = new ArrayList<>();
    private ArrayAdapter<String> tipeAsetArrayAdapter;

    private ArrayList<Lokasi> dataRuangan = new ArrayList<>();
    private ArrayAdapter<String> ruanganArrayAdapter;

    private int last_page, currentVisibleItemCount, currentScrollState, currentFirstVisibleItem;
    private int currentPage = 1;

    private MagicalCamera magicalCamera;
    private MagicalPermissions magicalPermissions;

    private int RESIZE_PHOTO_PIXELS_PERCENTAGE = 90;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;

    private ArrayList<String> data = new ArrayList<>();
    private ArrayAdapter<String> dataRegister;

    private Config config;

    private int stat = 0;
    private int maks = 0;

    private APIService mAPIService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_halaman_input_data);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        ButterKnife.bind(this);

        mAPIService = APIUtils.getAPIService();

        config = new Config();

        harga.setLocale(Config.locale);
        harga.setDecimalDigits(0);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        data = getIntent().getStringArrayListExtra("data");
        txtDataRegister.setText("Total data yang akan di register sebanyak : " + Integer.toString(data.size()));

        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        listDialog = new ListView(this);
        listDataRegister = new ListView(this);

        spinnerSource.setOnItemChosenListener(this);
        spinnerSource.setItemsArray(sumber);

        gedungArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        tipeAsetArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        ruanganArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        dataRegister = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

//        listDialog.setOnScrollListener(new AbsListView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//                currentScrollState = scrollState;
//                isScrollCompleted();
//            }
//
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                currentFirstVisibleItem = firstVisibleItem;
//                currentVisibleItemCount = visibleItemCount;
//            }
//
//            private void isScrollCompleted() {
//                if (currentVisibleItemCount > 0 && currentScrollState == SCROLL_STATE_IDLE) {
//                    /*** In this way I detect if there's been a scroll which has completed ***/
//                    /*** do the work for load more date! ***/
//                    if (currentPage != last_page) {
//                        currentPage++;
//                        if (konteks.equals("tipeaset")) {
//                            getDataTipeAset(currentPage);
//                        } else if (konteks.equals("gedung")) {
//                            ambilDataGedung(currentPage);
//                        } else if (konteks.equals("ruangan")) {
//                            ambilDataRuang(currentPage);
//                        }
//                    }
//                }
//            }
//        });

        listDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (konteks.equals("gedung")) {
                    pilihanGedung.setText(dataGedung.get(position).getNama());
                    idGedung = dataGedung.get(position).getId();
                    pilihanGedung.setVisibility(View.VISIBLE);
                } else if (konteks.equals("tipeaset")) {
                    pilihanTipe.setText(dataTipeaset.get(position).getName());
                    idTipeDetailAset = dataTipeaset.get(position).getId();
                    pilihanTipe.setVisibility(View.VISIBLE);
                } else if (konteks.equals("ruangan")) {
                    idLokasi = dataRuangan.get(position).getId();
                    pilihanRuang.setText(dataRuangan.get(position).getName());
                    pilihanRuang.setVisibility(View.VISIBLE);
                }
                dialog.dismiss();

            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                this.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
                break;
        }
        return true;
    }

    @OnClick(R.id.btnLihatDataRegister)
    void cekDataRegister() {
        dataRegister.clear();
        for (String nama : data) {
            dataRegister.add(nama);
        }
        listDataRegister.setAdapter(dataRegister);
        AlertDialog.Builder builder = new AlertDialog.Builder(HalamanInputData.this);
        builder.setCancelable(true);
        builder.setPositiveButton("OK", null);
        if (listDataRegister.getParent() != null) {
            ((ViewGroup) listDataRegister.getParent()).removeView(listDataRegister);
        }
        builder.setView(listDataRegister);
        dialog = builder.create();
        dialog.show();
    }

    @OnClick(R.id.btnSubmit)
    void createAsset() {
//        Toast.makeText(this, Long.toString(harga.getRawValue()), Toast.LENGTH_SHORT).show();
        if (pilihanGedung.getVisibility() == View.VISIBLE && pilihanRuang.getVisibility() == View.VISIBLE
                && pilihanTipe.getVisibility() == View.VISIBLE && !edMerk.getText().toString().equals("")
                && !edTahun.getText().toString().equals("")) {
            showLoading("Sedang Menyimpan Data Aset ke Database !");
            String imageBase64 = "";
            if (gambar.getVisibility() == View.VISIBLE) {
                Bitmap bitmap = ((BitmapDrawable) gambar.getDrawable()).getBitmap();
                byte[] bytesArray = ConvertSimpleImage.bitmapToBytes(bitmap, MagicalCamera.PNG);
                imageBase64 = ConvertSimpleImage.bytesToStringBase64(bytesArray);
                Log.i("imgBase64", imageBase64);
            }
            showLoading("Menyimpan Data !");
            maks = data.size();
            String rfid = data.get(stat).substring(5);
            String bc = "";
            createAsetwithRetro(rfid, bc, asal.substring(0, 1), edTahun.getText().toString(), idLokasi, idTipeDetailAset
                    , edMerk.getText().toString(), imageBase64);
//                    inputAsetToDB(rfid, bc, asal.substring(0, 1), edTahun.getText().toString(), idLokasi, idTipeDetailAset
//                            , edMerk.getText().toString(), imageBase64);
//                } else if (kode.startsWith("BC")) {
//                    String rfid = "";
//                    String bc = kode.substring(4);
//                    createAsetwithRetro(rfid, bc, asal.substring(0, 1), edTahun.getText().toString(), idLokasi, idTipeDetailAset
//                            , edMerk.getText().toString(), imageBase64);
////                    inputAsetToDB(rfid, bc, asal.substring(0, 1), edTahun.getText().toString(), idLokasi, idTipeDetailAset
////                            , edMerk.getText().toString(), imageBase64);
//                }

            dismissLoading();

        } else {
            Toast.makeText(this, "Data anda belum lengkap !", Toast.LENGTH_SHORT).show();
        }

    }

    @OnClick(R.id.spinnerGedung)
    void pilihGedung() {
        currentPage = 1;
        konteks = "gedung";
        showLoading("Mengambil Data dari server");
        listDialog.setAdapter(gedungArrayAdapter);
        ambilDataGedung(currentPage);
    }

//    @OnClick(R.id.spinnerLantai)
//    void pilihLantai() {
//
//    }

    @OnClick(R.id.spinnerRuang)
    void pilihRuang() {
        currentPage = 1;
        konteks = "ruangan";
        showLoading("Mengambil Data dari server");
        listDialog.setAdapter(ruanganArrayAdapter);
        ambilDataRuang(currentPage);
    }

    @OnClick(R.id.spinnerTipe)
    void pilihTipe() {
        currentPage = 1;
        konteks = "tipeaset";
        showLoading("Mengambil Data dari server");
        listDialog.setAdapter(tipeAsetArrayAdapter);
        getDataTipeAset(currentPage);
    }

    @OnClick(R.id.btnImage)
    void pilihGambar() {
        final CharSequence[] options = {"Take Photo", "Choose From Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(HalamanInputData.this);
        builder.setTitle("Choose Image");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (options[which].equals("Cancel")) {
                    dialog.dismiss();
                } else if (options[which].equals("Take Photo")) {
                    takePhoto();
                } else if (options[which].equals("Choose From Gallery")) {
                    chooseFromGallery();
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void createAsetwithRetro(final String tagRFID, final String barcode, final String sumber, final String tahun, final String idRuang, final String idTipeDetailAset, final String merk, final String gambar) {
        mAPIService.createAset(Prefs.getString(Config.TOKEN_SHARED_PREF, ""), tagRFID, barcode, sumber,
                idRuang, tahun, idTipeDetailAset, merk, gambar).enqueue(new Callback<ResponsePost>() {
            @Override
            public void onResponse(Call<ResponsePost> call, retrofit2.Response<ResponsePost> response) {
                if (response.isSuccessful()) {
                    stat++;
                    if (stat < maks) {
                        String rfid = data.get(stat).substring(5);
                        String bc = "";
                        createAsetwithRetro(rfid, bc, asal.substring(0, 1), edTahun.getText().toString(), idLokasi, idTipeDetailAset
                                , edMerk.getText().toString(), gambar);
                    } else {
                        dismissLoading();
                        Toast.makeText(HalamanInputData.this, "Asset Successfully Created !", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponsePost> call, Throwable t) {
                Toast.makeText(HalamanInputData.this, "Unable to submit post to API. Reason : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void takePhoto() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Dexter.withActivity(this)
                    .withPermission(android.Manifest.permission.CAMERA)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, REQUEST_CAMERA);
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                            Toast.makeText(HalamanInputData.this, "Permission not Granted !", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                    }).check();
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_CAMERA);
        }
    }

    private void chooseFromGallery() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Dexter.withActivity(this)
                    .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);//
                            startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                            Toast.makeText(HalamanInputData.this, "Permission not Granted !", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                    }).check();
        } else {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                onSelectFromGalleryResult(data);
            } else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Picasso.with(getApplicationContext()).load(data.getData()).fit().into(gambar);
        gambar.setVisibility(View.VISIBLE);
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, RESIZE_PHOTO_PIXELS_PERCENTAGE, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Picasso.with(getApplicationContext()).load(Uri.fromFile(destination)).fit().into(gambar);
        gambar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemChosen(View labelledSpinner, AdapterView<?> adapterView, View itemView, int position, long id) {
        switch (labelledSpinner.getId()) {
            case R.id.spinnerSource:
                asal = adapterView.getItemAtPosition(position).toString();
                break;
        }
    }

    @Override
    public void onNothingChosen(View labelledSpinner, AdapterView<?> adapterView) {

    }

    private void inputAsetToDB(final String tagRFID, final String barcode, final String sumber, final String tahun, final String idRuang, final String idTipeDetailAset, final String merk, final String gambar) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.URL_CREATE_ASSET,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(HalamanInputData.this, "Asset successfully created !", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if (error instanceof NetworkError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                    dismissLoading();
                    Toast.makeText(HalamanInputData.this, "Terjadi kesalah pada koneksi internet anda ! tolong ulangi lagi", Toast.LENGTH_SHORT).show();
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
                    dismissLoading();
                    Toast.makeText(HalamanInputData.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    return headers;
                }
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> body = new HashMap();
                try {
                    body.put("tag_rfid", tagRFID);
                    body.put("barcode", barcode);
                    body.put("asset_origin", sumber);
                    body.put("id_location", idRuang);
                    body.put("year", tahun);
                    body.put("id_asset_type_detail", idTipeDetailAset);
                    body.put("merk", merk);
                    body.put("image", gambar);
                    return body;
                } catch (Exception e) {
                    dismissLoading();
                    Toast.makeText(HalamanInputData.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    return body;
                }
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void ambilDataGedung(int page) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Config.URL_GET_GEDUNG +
                "?page=" + Integer.toString(page),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject callback = new JSONObject(response);
                            currentPage = callback.getInt("current_page");
                            last_page = callback.getInt("last_page");
                            dataGedung.clear();
                            JSONArray data = callback.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject gedung = data.getJSONObject(i);
                                Gedung datagedung = new Gedung(gedung.getString("id"), gedung.getString("name"));
                                addDataGedung(gedung.getString("name"), datagedung);
                            }
                            gedungArrayAdapter.clear();
                            for (Gedung gedung : dataGedung) {
                                gedungArrayAdapter.add(gedung.getNama());
                            }
                            dismissLoading();
                            showDetailDialog(konteks);
                        } catch (Exception e) {
                            dismissLoading();
                            Toast.makeText(HalamanInputData.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if (error instanceof NetworkError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                    dismissLoading();
                    Toast.makeText(HalamanInputData.this, "Terjadi kesalah pada koneksi internet anda ! tolong ulangi lagi", Toast.LENGTH_SHORT).show();
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
                    dismissLoading();
                    Toast.makeText(HalamanInputData.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    return headers;
                }
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(HalamanInputData.this);
        requestQueue.add(stringRequest);
    }

    private void getDataTipeAset(int page) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Config.URL_GET_TIPEASSET +
                "?page=" + Integer.toString(page),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject callback = new JSONObject(response);
                            currentPage = callback.getInt("current_page");
                            last_page = callback.getInt("last_page");
                            JSONArray data = callback.getJSONArray("data");
                            dataTipeaset.clear();
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject isi = data.getJSONObject(i);
                                TipeAset tipeAset = new TipeAset(isi.getString("id"), isi.getString("name"),
                                        isi.getString("type_general"));
//                                Toast.makeText(HalamanInputData.this, isi.getString("type_general"), Toast.LENGTH_SHORT).show();
                                addDataTipeAset(isi.getString("name"), tipeAset);
                            }
                            tipeAsetArrayAdapter.clear();
                            for (TipeAset aset : dataTipeaset) {
                                tipeAsetArrayAdapter.add(aset.getName());
                            }
                            dismissLoading();
                            showDetailDialog(konteks);
                        } catch (Exception e) {
                            dismissLoading();
                            Toast.makeText(HalamanInputData.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if (error instanceof NetworkError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                    dismissLoading();
                    Toast.makeText(HalamanInputData.this, "Terjadi kesalah pada koneksi internet anda ! tolong ulangi lagi", Toast.LENGTH_SHORT).show();
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
                    dismissLoading();
                    Toast.makeText(HalamanInputData.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    return headers;
                }
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(HalamanInputData.this);
        requestQueue.add(stringRequest);
    }

    private void ambilDataRuang(int page) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Config.URL_GET_LOCATION +
                "?page=" + Integer.toString(page),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject callback = new JSONObject(response);
                            currentPage = callback.getInt("current_page");
                            last_page = callback.getInt("last_page");
                            JSONArray data = callback.getJSONArray("data");
                            dataRuangan.clear();
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject isi = data.getJSONObject(i);
                                Lokasi lokasi = new Lokasi(isi.getString("id"),
                                        isi.getString("name"), isi.getString("id_gedung"));
                                addDataRuang(isi.getString("name"), lokasi);
                            }
                            ruanganArrayAdapter.clear();
                            for (Lokasi lokasi : dataRuangan) {
                                ruanganArrayAdapter.add(lokasi.getName());
                            }
                            dismissLoading();
                            showDetailDialog(konteks);
                        } catch (Exception e) {
                            dismissLoading();
                            Toast.makeText(HalamanInputData.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if (error instanceof NetworkError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                    dismissLoading();
                    Toast.makeText(HalamanInputData.this, "Terjadi kesalah pada koneksi internet anda ! tolong ulangi lagi", Toast.LENGTH_SHORT).show();
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
                    dismissLoading();
                    Toast.makeText(HalamanInputData.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    return headers;
                }
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(HalamanInputData.this);
        requestQueue.add(stringRequest);
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HalamanInputData.this);
        builder.setCancelable(true);
        builder.setPositiveButton("OK", null);
        if (listDialog.getParent() != null) {
            ((ViewGroup) listDialog.getParent()).removeView(listDialog);
        }
        builder.setView(listDialog);
        dialog = builder.create();
        dismissLoading();
        dialog.show();
    }

    private void showDetailDialog(String konteks) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_spinner);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;


        ImageView kanan = (ImageView) dialog.findViewById(R.id.btnKanan);
        ImageView kiri = (ImageView) dialog.findViewById(R.id.btnKiri);
        ListView listView = (ListView) dialog.findViewById(R.id.listData);
        if (konteks.equals("tipeaset")) {
            EditText halaman = (EditText) dialog.findViewById(R.id.edPage);
            halaman.setText(Integer.toString(currentPage));
            ((TextView) dialog.findViewById(R.id.lastPage)).setText("/ " + Integer.toString(last_page));
            listView.setAdapter(tipeAsetArrayAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    dialog.dismiss();
                    pilihanTipe.setText(dataTipeaset.get(position).getName());
                    idTipeDetailAset = dataTipeaset.get(position).getId();
                    pilihanTipe.setVisibility(View.VISIBLE);
                }
            });
            kiri.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentPage != 1) {
                        tipeAsetArrayAdapter.clear();
                        dialog.dismiss();
                        getDataTipeAset(currentPage - 1);
                    }
                }
            });

            kanan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentPage != last_page) {
                        tipeAsetArrayAdapter.clear();
                        dialog.dismiss();
                        getDataTipeAset(currentPage + 1);
                    }
                }
            });

            halaman.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() > 0) {
                        String hal = s.toString();
                        Integer page = Integer.parseInt(hal);
                        if (!(page < 1 || page > last_page)) {
                            tipeAsetArrayAdapter.clear();
                            dialog.dismiss();
                            currentPage = page;
                            getDataTipeAset(currentPage);
                        }
                    }
                }
            });

        } else if (konteks.equals("gedung")) {
            EditText halaman = (EditText) dialog.findViewById(R.id.edPage);
            halaman.setText(Integer.toString(currentPage));
            ((TextView) dialog.findViewById(R.id.lastPage)).setText("/ " + Integer.toString(last_page));
            listView.setAdapter(gedungArrayAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    dialog.dismiss();
                    pilihanGedung.setText(dataGedung.get(position).getNama());
                    idGedung = dataGedung.get(position).getId();
                    pilihanGedung.setVisibility(View.VISIBLE);
                }
            });
            kiri.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentPage != 1) {
                        dialog.dismiss();
                        ambilDataGedung(currentPage - 1);
                    }
                }
            });

            kanan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentPage != last_page) {
                        dialog.dismiss();
                        ambilDataGedung(currentPage + 1);
                    }
                }
            });
            halaman.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() > 0) {
                        String hal = s.toString();
                        Integer page = Integer.parseInt(hal);
                        if (!(page < 1 || page > last_page)) {
                            tipeAsetArrayAdapter.clear();
                            dialog.dismiss();
                            currentPage = page;
                            ambilDataGedung(currentPage);
                        }
                    }
                }
            });
        } else if (konteks.equals("ruangan")) {
            EditText halaman = (EditText) dialog.findViewById(R.id.edPage);
            halaman.setText(Integer.toString(currentPage));
            ((TextView) dialog.findViewById(R.id.lastPage)).setText("/ " + Integer.toString(last_page));
            listView.setAdapter(ruanganArrayAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    dialog.dismiss();
                    idLokasi = dataRuangan.get(position).getId();
                    pilihanRuang.setText(dataRuangan.get(position).getName());
                    pilihanRuang.setVisibility(View.VISIBLE);
                }
            });
            kiri.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentPage != 1) {
                        dialog.dismiss();
                        ambilDataRuang(currentPage - 1);
                    }
                }
            });

            kanan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentPage != last_page) {
                        dialog.dismiss();
                        ambilDataRuang(currentPage + 1);
                    }
                }
            });
            halaman.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() > 0) {
                        String hal = s.toString();
                        Integer page = Integer.parseInt(hal);
                        if (!(page < 1 || page > last_page)) {
                            tipeAsetArrayAdapter.clear();
                            dialog.dismiss();
                            currentPage = page;
                            ambilDataRuang(currentPage);
                        }
                    }
                }
            });
        }


        ((ImageButton) dialog.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);

    }

    private void showLoading(String title) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(HalamanInputData.this)
                .title(title)
                .progress(true, 0)
                .content("Mohon tunggu !");

        dialog1 = builder.build();
        dialog1.show();
    }

    private void dismissLoading() {
        dialog1.dismiss();
    }

    private void addDataGedung(String nama, Gedung data) {
        if (dataGedung.size() == 0) {
            dataGedung.add(data);
        } else {
            boolean ada = false;
            for (Gedung data1 : dataGedung) {
                if (data1.getNama().equals(nama)) {
                    ada = true;
                    break;
                }
            }
            if (!ada) {
                dataGedung.add(data);
            }
        }
    }

    private void addDataTipeAset(String nama, TipeAset data) {
        if (dataTipeaset.size() == 0) {
            dataTipeaset.add(data);
        } else {
            boolean ada = false;
            for (TipeAset data1 : dataTipeaset) {
                if (data1.getName().equals(nama)) {
                    ada = true;
                    break;
                }
            }
            if (!ada) {
                dataTipeaset.add(data);
            }
        }
    }

    private void addDataRuang(String nama, Lokasi data) {
        if (dataRuangan.size() == 0) {
            dataRuangan.add(data);
        } else {
            boolean ada = false;
            for (Lokasi data1 : dataRuangan) {
                if (data1.getName().equals(nama)) {
                    ada = true;
                    break;
                }
            }
            if (!ada) {
                dataRuangan.add(data);
            }
        }
    }


}