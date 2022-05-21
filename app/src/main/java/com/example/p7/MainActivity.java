package com.example.p7;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;

import com.example.p7.databinding.ActivityMainBinding;

import android.view.Menu;
import android.widget.Button;

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

        /* to avoid 5-fold repetition */
        Button[] buttons = {binding.gotoSingleplayer, binding.gotoMultiplayer,
                binding.gotoHighscores, binding.gotoSettings, binding.gotoRules};
        Class[] destinations = {SinglePlayerActivity.class, MultiplayerActivity.class,
                HighestScoresActivity.class, SettingsActivity.class, RulesActivity.class};
        Integer[] colorIndices = {0, 1, 3, 5, 6};
        for (int i = 0; i < 5; i++) {
            buttons[i].setBackgroundColor(CardView.colors[colorScheme][colorIndices[i]]);
            int finalI = i;
            buttons[i].setOnClickListener(view -> {
                Intent intent = new Intent(this, destinations[finalI]);
                startActivity(intent);
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

}
