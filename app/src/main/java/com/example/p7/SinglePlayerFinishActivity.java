package com.example.p7;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;

import com.example.p7.databinding.ActivitySinglePlayerFinishBinding;

import java.util.Date;

import room.ConnectDB;
import room.Result;

public class SinglePlayerFinishActivity extends AppCompatActivity {

    private ActivitySinglePlayerFinishBinding binding;
    private MediaPlayer applause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySinglePlayerFinishBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Bundle info = getIntent().getExtras();
        applause = MediaPlayer.create(this, R.raw.applause);

        if (info.getBoolean("finished")) {
            binding.congrats.setText("Congratulations!\n");
            binding.congrats.setTextColor(CardView.colors[MainActivity.colorScheme][0]);
            if (MainActivity.soundEffects)
                applause.start();
        } else {
            binding.congrats.setText("Finished\n");
        }

        binding.stats.setText(String.format(
                " Time: %s \n Sets collected by yourself: %d \n",
                info.getString("time_string"),
                info.getInt("own_sets")
        ));

        binding.homeButton.setBackgroundColor(CardView.colors[MainActivity.colorScheme][0]);
        binding.homeButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        ConnectDB savedResults = ConnectDB.getDbInstance(getApplicationContext());
        Result r = new Result(
            new Date(),
            MainActivity.gameMode,
            info.getInt("sets"),
            info.getInt("own_sets"),
            info.getInt("time_int")
        );
        savedResults.resultDao().insertResult(r);
    }
}