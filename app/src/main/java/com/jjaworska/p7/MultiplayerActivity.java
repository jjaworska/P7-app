package com.jjaworska.p7;

import bluetooth.BluetoothService;
import bluetooth.Constants;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.jjaworska.p7.databinding.ActivityMultiplayerBinding;

import java.util.Set;


public class MultiplayerActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 3;

    // Layout Views
    private String mConnectedDeviceName = null;
    private ArrayAdapter<String> mDevicesAdapter;
    private static BluetoothAdapter mBluetoothAdapter = null;
    private static BluetoothService mService = null;

    public ActivityMultiplayerBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMultiplayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarMultiplayer.getRoot());
        Utils.dealWithToolbar(binding.toolbarMultiplayer.getRoot(), getApplicationContext());
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.startGame.setVisibility(View.INVISIBLE);
        binding.startGame.setEnabled(false);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        if (mService == null) {
            setup();
        }
    }

    public static void finishService() {
        if (mService != null) {
            mService.stop();
            mService = null;
            mBluetoothAdapter = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mService != null && mService.getState() == BluetoothService.STATE_NONE)
                mService.start();
    }

    private void setup() {
        mService = new BluetoothService(getApplicationContext(), mHandler);
        mService.start();
        mDevicesAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1);
        binding.pairedDevicesList.setAdapter(mDevicesAdapter);
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            binding.pairedDevicesList.setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                mDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
        binding.pairedDevicesList.setVisibility(View.INVISIBLE);
        binding.pairedDevicesList.setEnabled(false);

        binding.confirm.setOnClickListener(v -> {
            String nick = binding.nick.getText().toString();
            if (nick.equals("")) {
                Toast.makeText(
                        getApplicationContext(),
                        "Your nick cannot be empty",
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                mService.nick = nick;
                binding.nick.setEnabled(false);
                binding.confirm.setEnabled(false);
                binding.startGame.setEnabled(true);
                binding.pairedDevicesList.setEnabled(true);
                Utils.fadeAnimation(binding.confirm, binding.pairedDevicesList, Utils.shortAnimationDuration);
                Utils.fadeAnimation(binding.nick, binding.startGame, Utils.shortAnimationDuration);
                binding.startGame.setVisibility(View.VISIBLE);
            }
        });

        binding.pairedDevicesList.setOnItemClickListener((av, view, arg2, arg3) -> {
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);
            Log.i("Multiplayer: ", "Connecting to " + address);
            connectDevice(address, true);
        });
        binding.startGame.setOnClickListener(view -> {
            if (mService.getState() != BluetoothService.STATE_CONNECTED) {
                Toast.makeText(getApplicationContext(),
                        "You are not connected", Toast.LENGTH_SHORT).show();
            } else if (!mService.amIServer()) {
                Toast.makeText(getApplicationContext(),
                        "Wait for the host to start the game", Toast.LENGTH_SHORT).show();
            } else {
                startGame();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                Intent intent2 = new Intent(this, MainActivity.class);
                startActivity(intent2);
                return true;
        }
        return false;
    }

    private void startGame() {
        Intent intent = new Intent(this, MultiplayerGameActivity.class);
        startActivity(intent);
    }

    private void setStatus(CharSequence subTitle) {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(subTitle);
    }

    /* The Handler that gets information back from the BluetoothService */
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            setStatus("Connected to " + mConnectedDeviceName);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            setStatus("Connecting");
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            setStatus("Not connected");
                            break;
                    }
                    break;
                case Constants.MESSAGE_READ:
                    /* The only possible message would be "start the game! */
                    startGame();
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    setup();
                } else {
                    Toast.makeText(
                            getApplicationContext(), "BT not enabled, leaving", Toast.LENGTH_SHORT
                    ).show();
                    finish();
                }
        }
    }

    public static BluetoothService getAdapter() {
        return mService;
    }

    private void connectDevice(String address, boolean secure) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        mService.connect(device, secure);
    }

}
