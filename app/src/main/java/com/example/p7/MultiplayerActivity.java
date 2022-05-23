package com.example.p7;

import bluetooth.BluetoothChatService;
import bluetooth.Constants;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.p7.databinding.ActivityMultiplayerBinding;

import java.util.Set;


public class MultiplayerActivity extends AppCompatActivity {

    private static final String TAG = "BluetoothChatFragment";

    // Intent request codes
    private static final int REQUEST_ENABLE_BT = 3;

    // Layout Views
    private String mConnectedDeviceName = null;
    private ArrayAdapter<String> mDevicesAdapter;
    private static BluetoothAdapter mBluetoothAdapter = null;
    private static BluetoothChatService mChatService = null;

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
        binding.startGame.setVisibility(View.GONE);
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
        if (mChatService == null) {
            setup();
        }
    }

    public static void finishService() {
        if (mChatService != null) {
            mChatService.stop();
            mChatService = null;
        }
    }

    /* Omitted: fancy onResume */

    private void setup() {
        Log.d(TAG, "setup()");
        mChatService = new BluetoothChatService(getApplicationContext(), mHandler);
        mChatService.start();
        mDevicesAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1);
        binding.pairedDevicesList.setAdapter(mDevicesAdapter);
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices, add each one to the ArrayAdapter
        Log.i("setup: ", "pairedDevices.size() is " + Integer.toString(pairedDevices.size()));
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
                mChatService.nick = nick;
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
            if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
                Toast.makeText(getApplicationContext(),
                        "You are not connected", Toast.LENGTH_SHORT).show();
            } else if (!mChatService.amIServer()) {
                Toast.makeText(getApplicationContext(),
                        "Wait for the host to start the game", Toast.LENGTH_SHORT).show();
            } else {
                startGame();
            }
        });
        binding.nick.setOnEditorActionListener(mWriteListener);
    }

    private TextView.OnEditorActionListener mWriteListener
            = (view, actionId, event) -> {
                // If the action is a key-up event on the return key, send the message
                if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                    String nick = view.getText().toString();
                    mChatService.nick = nick;
                }
                return true;
            };

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

    /* The Handler that gets information back from the BluetoothChatService */
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus("Connected to " + mConnectedDeviceName);
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus("Connecting");
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
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
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getApplicationContext(), "BT not enabled, leaving",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    public static BluetoothChatService getAdapter() {
        return mChatService;
    }

    private void connectDevice(String address, boolean secure) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        mChatService.connect(device, secure);
    }

}
