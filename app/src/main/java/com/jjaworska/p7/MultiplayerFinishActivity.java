package com.jjaworska.p7;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.jjaworska.p7.databinding.ActivityMultiplayerFinishBinding;

import bluetooth.Constants;


public class MultiplayerFinishActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.jjaworska.p7.databinding.ActivityMultiplayerFinishBinding binding = ActivityMultiplayerFinishBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MultiplayerActivity.getAdapter().updateHandler(mHandler);

        binding.congrats.setText("Finished\n");
        binding.congrats.setTextColor(CardView.colors[MainActivity.colorScheme][6]);

        Bundle info = getIntent().getExtras();

        binding.summary.setText(info.getString("summary"));

        binding.homeButton.setBackgroundColor(CardView.colorPurple());
        binding.homeButton.setOnClickListener(view -> {
            MultiplayerActivity.finishService();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        binding.rematch.setBackgroundColor(CardView.colorPurple());
        binding.rematch.setOnClickListener(view -> {
            byte[] message = {Constants.REMATCH};
            MultiplayerActivity.getAdapter().write(message);
            Intent intent = new Intent(this, MultiplayerGameActivity.class);
            startActivity(intent);
        });

    }

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.MESSAGE_READ) {
                byte[] buf = (byte[]) msg.obj;
                if (buf[0] == Constants.REMATCH) {
                    Intent intent = new Intent(getApplicationContext(), MultiplayerGameActivity.class);
                    startActivity(intent);
                }
            }
        }
    };
}
