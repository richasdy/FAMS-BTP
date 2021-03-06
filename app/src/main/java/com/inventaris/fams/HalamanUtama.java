package com.inventaris.fams;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.astuetz.PagerSlidingTabStrip;
import com.inventaris.fams.Fragment.AddData;
import com.inventaris.fams.Fragment.BandingkanData;
import com.inventaris.fams.Fragment.ScanData;
import com.inventaris.fams.Fragment.SearchData;
import com.inventaris.fams.Utils.ModelBase;
import com.inventaris.fams.Utils.TSLBluetoothDeviceActivity;
import com.inventaris.fams.Utils.TSLBluetoothDeviceApplication;
import com.inventaris.fams.Utils.WeakHandler;
import com.pixplicity.easyprefs.library.Prefs;
import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;
import com.uk.tsl.rfid.asciiprotocol.commands.FactoryDefaultsCommand;
import com.uk.tsl.rfid.asciiprotocol.responders.LoggerResponder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HalamanUtama extends TSLBluetoothDeviceActivity {
    private String stat;
    private BluetoothAdapter mBluetoothAdapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FamsModel mModel;
    private boolean isReadderconnected = false;
    private MaterialDialog dialog;
    String connectionMsg;


    public static int int_items = 3;

    private HashMap<Integer, Fragment> refFragmentMap = new HashMap<>();

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;

    private static final int REQUEST_ENABLE_BT = 3;

    // Debug control
    private static final boolean D = BuildConfig.DEBUG;


    public AsciiCommander getCommander() {
        return ((TSLBluetoothDeviceApplication) getApplication()).getCommander();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_halaman_utama);

        ButterKnife.bind(this);


        //initialize shared pref
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        stat = Prefs.getString(Config.TOKEN_SHARED_PREF, "null");

        if (stat.equals("null")) {
            startActivity(new Intent(getApplicationContext(), HalamanLogin.class));
            finish();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarPengaturan);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));

        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(viewPager);


        AsciiCommander commander = getCommander();

        // Add the LoggerResponder - this simply echoes all lines received from the reader to the log
        // and passes the line onto the next responder
        // This is added first so that no other responder can consume received lines before they are logged.
        commander.addResponder(new LoggerResponder());

        // Add a synchronous responder to handle synchronous commands
        commander.addSynchronousResponder();

        mModel = new FamsModel();
        mModel.setCommander(getCommander());
        mModel.setHandler(mGenericModelHandler);
    }

    public void finDevice() {
        selectDevice();
    }

    public void doScan() {
        mModel.scan();
    }

    public FamsModel getmModel() {
        return mModel;
    }

    private MenuItem mReconnectMenuItem;
    private MenuItem mConnectMenuItem;
    private MenuItem mDisconnectMenuItem;
    private MenuItem mResetMenuItem;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_halaman_utama, menu);

        mResetMenuItem = menu.findItem(R.id.reset_reader_menu_item);
        mReconnectMenuItem = menu.findItem(R.id.reconnect_reader_menu_item);
        mConnectMenuItem = menu.findItem(R.id.insecure_connect_reader_menu_item);
        mDisconnectMenuItem = menu.findItem(R.id.disconnect_reader_menu_item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.reconnect_reader_menu_item:
                getSupportActionBar().setTitle("Reconnecting...");
                Toast.makeText(this.getApplicationContext(), "Reconnecting...", Toast.LENGTH_LONG).show();
                reconnectDevice();
                return true;

            case R.id.insecure_connect_reader_menu_item:
                // Choose a device and connect to it
                selectDevice();
                return true;

            case R.id.disconnect_reader_menu_item:
                getSupportActionBar().setTitle("Disconnecting...");
                Toast.makeText(this.getApplicationContext(), "Disconnecting...", Toast.LENGTH_SHORT).show();
                disconnectDevice();
                return true;

            case R.id.reset_reader_menu_item:
                resetReader();
                return true;

            case R.id.logout:
                LogOut();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void resetReader() {
        try {
            // Reset the reader
            FactoryDefaultsCommand fdCommand = FactoryDefaultsCommand.synchronousCommand();
            getCommander().executeCommand(fdCommand);
            String msg = "Reset " + (fdCommand.isSuccessful() ? "succeeded" : "failed");
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);

        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    Fragment inputData = new AddData();
                    refFragmentMap.put(position, inputData);
                    return inputData;
                case 1:
                    Fragment cari = new SearchData();
                    refFragmentMap.put(position, cari);
                    return cari;
                case 2:
                    Fragment scan = new ScanData();
                    refFragmentMap.put(position, scan);
                    return scan;
//                case 3:
//                    Fragment lihat = new BandingkanData();
//                    refFragmentMap.put(position, lihat);
//                    return lihat;
            }
            return null;
        }

        @Override
        public int getCount() {
            return int_items;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Register Asset";
                case 1:
                    return "Search";
                case 2:
                    return "Scan";
//                case 3:
//                    return "Compare";
            }
            return null;
        }

        public void destroyItem(View container, int position, Object object) {
            super.destroyItem(container, position, object);
            refFragmentMap.remove(position);
        }

        public Fragment getFragment(int key) {
            return refFragmentMap.get(key);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode != Activity.RESULT_OK) {
                    // User did not enable Bluetooth or an error occurred
                    bluetoothNotAvailableError("Bluetooth was not enabled\nApplication Quitting...");
                }
                break;
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectToDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectToDevice(data, false);
                }
                break;
        }
    }

    private final WeakHandler<HalamanUtama> mGenericModelHandler = new WeakHandler<HalamanUtama>(this) {

        @Override
        public void handleMessage(Message msg, HalamanUtama halamanUtama) {
            try {
                switch (msg.what) {
                    case ModelBase.BUSY_STATE_CHANGED_NOTIFICATION:
                        //TODO: process change in model busy state
                        break;

                    case ModelBase.MESSAGE_NOTIFICATION:
                        // Examine the message for prefix
                        String message = (String) msg.obj;
                        if (message.startsWith("ER:")) {
                            Toast.makeText(HalamanUtama.this, message.substring(3), Toast.LENGTH_SHORT).show();
                        } else if (message.startsWith("BC:")) {
                            int index = viewPager.getCurrentItem();
                            ViewPagerAdapter adapter = ((ViewPagerAdapter) viewPager.getAdapter());
                            Fragment fragment = adapter.getFragment(index);
                            if (fragment instanceof ScanData) {
                                ((ScanData) fragment).onNewData(message);
                            } else if (fragment instanceof BandingkanData) {
                                ((BandingkanData) fragment).onNewData(message);
                            } else if (fragment instanceof AddData) {
                                AddData a = (AddData) fragment;
                                a.addData(message);
                            }
                        } else if (message.startsWith("EPC")) {
                            int index = viewPager.getCurrentItem();
                            ViewPagerAdapter adapter = ((ViewPagerAdapter) viewPager.getAdapter());
                            Fragment fragment = adapter.getFragment(index);
                            if (fragment instanceof ScanData) {
                                ((ScanData) fragment).onNewData(message);
                            } else if (fragment instanceof BandingkanData) {
                                ((BandingkanData) fragment).onNewData(message);
                            } else if (fragment instanceof AddData) {
                                AddData a = (AddData) fragment;
                                a.addData(message);
                            }
                        }
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
            }
        }
    };

    private BroadcastReceiver mCommanderMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (D) {
                Log.d(getClass().getName(), "AsciiCommander state changed - isConnected: " + getCommander().isConnected());
            }

            String connectionStateMsg = intent.getStringExtra(AsciiCommander.REASON_KEY);
            Toast.makeText(context, connectionStateMsg, Toast.LENGTH_SHORT).show();

            displayReaderState();
            if (getCommander().isConnected()) {
//                // Update for any change in power limits
//                setPowerBarLimits();
//                // This may have changed the current power level setting if the new range is smaller than the old range
//                // so update the model's inventory command for the new power value
//                mModel.getCommand().setOutputPower(mPowerLevel);

                mModel.resetDevice();
                mModel.updateConfiguration();
            }
        }
    };

    private void displayReaderState() {

        connectionMsg = "Reader ";
        switch (getCommander().getConnectionState()) {
            case CONNECTED:
                isReadderconnected = true;
                connectionMsg += getCommander().getConnectedDeviceName();
                getSupportActionBar().setTitle(connectionMsg);
                break;
            case CONNECTING:
                connectionMsg += "Connecting...";
                getSupportActionBar().setTitle(connectionMsg);
                break;
            default:
                isReadderconnected = false;
                connectionMsg += "Disconnected";
                getSupportActionBar().setTitle(connectionMsg);
        }
//        Prefs.putString("status", connectionMsg);
//        Prefs.putBoolean("reader", isReadderconnected);
//        ViewPagerAdapter adapter = (ViewPagerAdapter) viewPager.getAdapter();
//        int index = viewPager.getCurrentItem();
//        Fragment fragment = adapter.getFragment(index);
//        if (fragment instanceof ScanData) {
//            ((ScanData) fragment).setStatus(connectionMsg);
//            if (isReadderconnected) {
//                ((ScanData) fragment).showScanButton();
//            } else {
//                ((ScanData) fragment).hideScanButton();
//            }
//        } else if (fragment instanceof AddData) {
//            ((AddData) fragment).setStatus(connectionMsg);
//            if (isReadderconnected) {
//                ((AddData) fragment).showScanButton();
//            } else {
//                ((AddData) fragment).hideScanButton();
//            }
//        }
    }

    public String getStatus() {
        return connectionMsg;
    }

    @Override
    public void onResume() {
        super.onResume();
        mModel.setEnabled(true);

        // Register to receive notifications from the AsciiCommander
        LocalBroadcastManager.getInstance(HalamanUtama.this).registerReceiver(mCommanderMessageReceiver,
                new IntentFilter(AsciiCommander.STATE_CHANGED_NOTIFICATION));

        displayReaderState();
    }

    @Override
    public void onPause() {
        super.onPause();
        mModel.setEnabled(false);

        // Unregister to receive notifications from the AsciiCommander
        LocalBroadcastManager.getInstance(HalamanUtama.this).unregisterReceiver(mCommanderMessageReceiver);
    }

    @Override
    public void onStop() {
        super.onStop();
        mModel.setEnabled(false);

        // Unregister to receive notifications from the AsciiCommander
        LocalBroadcastManager.getInstance(HalamanUtama.this).unregisterReceiver(mCommanderMessageReceiver);
    }

    private void LogOut() {
        if (isReadderconnected) {
            showDialog();
            disconnectDevice();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Prefs.putString(Config.TOKEN_SHARED_PREF, "null");
                    dismissDialog();
                    startActivity(new Intent(getApplicationContext(), HalamanLogin.class));
                    finish();
                }
            }, 3000);
        } else {
            Prefs.putString(Config.TOKEN_SHARED_PREF, "null");
            startActivity(new Intent(getApplicationContext(), HalamanLogin.class));
            finish();
        }
    }

    private void showDialog() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(HalamanUtama.this)
                .title("Log Out")
                .progress(true, 0)
                .content("Please Wait !");

        dialog = builder.build();
        dialog.show();
    }

    private void dismissDialog() {
        dialog.dismiss();
    }
}
