package com.example.p7;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;

import com.example.p7.databinding.ActivityMainBinding;

import android.view.Menu;

import room.ConnectDB;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SharedPreferences settings;
    /* settings will be kept in public variables */
    public static boolean soundEffects;
    public static int colorScheme;
    public static boolean gameMode = CardView.P7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /* check the settings */
        settings = getSharedPreferences("settings", Context.MODE_PRIVATE);
        colorScheme = settings.getInt("colorScheme", CardView.HIGH_CONTRAST);
        gameMode = settings.getBoolean("gameMode", CardView.P7);

        setSupportActionBar(binding.toolbarMain.getRoot());
        Utils.dealWithToolbar(binding.toolbarMain.getRoot(), getApplicationContext());

        binding.gotoSingleplayer.setBackgroundColor(CardView.colors[colorScheme][0]);
        binding.gotoSingleplayer.setOnClickListener(view -> {
            Intent intent = new Intent(this, SinglePlayerActivity.class);
            startActivity(intent);
        });
        binding.gotoMultiplayer.setBackgroundColor(CardView.colors[colorScheme][1]);
        binding.gotoMultiplayer.setOnClickListener(view -> {
            ConnectDB db = ConnectDB.getDbInstance(getApplicationContext());
            db.resultDao().clearResults();
        });
        binding.gotoHighscores.setBackgroundColor(CardView.colors[colorScheme][3]);
        binding.gotoHighscores.setOnClickListener(view -> {
            Intent intent = new Intent(this, HighestScoresActivity.class);
            startActivity(intent);
        });
        binding.gotoSettings.setBackgroundColor(CardView.colors[colorScheme][5]);
        binding.gotoSettings.setOnClickListener(view -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });
        binding.gotoRules.setBackgroundColor(CardView.colors[colorScheme][6]);
        binding.gotoRules.setOnClickListener(view -> {
            Intent intent = new Intent(this, RulesActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

}
