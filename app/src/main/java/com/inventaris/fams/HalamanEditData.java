package com.inventaris.fams;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.frosquivel.magicalcamera.Utilities.ConvertSimpleImage;
import com.inventaris.fams.Model.ScannedCode;
import com.rengwuxian.materialedittext.MaterialEditText;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HalamanEditData extends AppCompatActivity {
    @BindView(R.id.txtCode)
    TextView code;
    @BindView(R.id.txtGedung)
    TextView gedung;
    @BindView(R.id.txtRuang)
    TextView ruang;
    @BindView(R.id.edStatus)
    MaterialEditText status;
    @BindView(R.id.imageView)
    ImageView gambar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_halaman_edit_data);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            ScannedCode data = b.getParcelable("data");
            code.setText("ID : " + data.getCode());
            ruang.setText(data.getLokasi().getName());
            ruang.setVisibility(View.VISIBLE);
            if (!data.getGambar().equals("null")) {
                byte[] anotherArrayBytes = ConvertSimpleImage.stringBase64ToBytes(data.getGambar());
                Bitmap myImageAgain = ConvertSimpleImage.bytesToBitmap(anotherArrayBytes);
                gambar.setImageBitmap(myImageAgain);
                gambar.setVisibility(View.VISIBLE);
            }
        }
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
}
