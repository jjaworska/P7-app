package com.example.p7;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import com.example.p7.databinding.ActivityMultiplayerFinishBinding;


public class MultiplayerFinishActivity extends AppCompatActivity {

    private ActivityMultiplayerFinishBinding binding;
    private MediaPlayer applause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMultiplayerFinishBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Bundle info = getIntent().getExtras();

        binding.congrats.setText("Finished\n");
        binding.congrats.setTextColor(CardView.colors[MainActivity.colorScheme][6]);


        binding.summary.setText(info.getString("summary"));

        binding.homeButton.setBackgroundColor(CardView.colors[MainActivity.colorScheme][6]);
        binding.homeButton.setOnClickListener(view -> {
            MultiplayerActivity.finishService();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        binding.rematch.setBackgroundColor(CardView.colors[MainActivity.colorScheme][6]);
        binding.rematch.setOnClickListener(view -> {
            Intent intent = new Intent(this, MultiplayerGameActivity.class);
            startActivity(intent);
        });

    }
}
