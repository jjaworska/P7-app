package com.example.p7;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;

import com.example.p7.databinding.ActivitySettingsBinding;


public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private int myRed;
    private int myPurple;
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        settings = getSharedPreferences("settings", Context.MODE_PRIVATE);
        setTitle("Settings");
        setSupportActionBar(binding.toolbarSettings.getRoot());
        Utils.dealWithToolbar(binding.toolbarSettings.getRoot(), getApplicationContext());
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        /*     gameModeSwitch    */
        myRed = CardView.colors[MainActivity.colorScheme][0];
        myPurple = CardView.colors[MainActivity.colorScheme][6];
        binding.gameModeSwitch.setChecked(MainActivity.gameMode);
        binding.gameModeSwitch.setBackgroundColor(MainActivity.gameMode ? myRed : myPurple);
        binding.gameModeSwitch.setOnClickListener(view -> {
            MainActivity.gameMode = binding.gameModeSwitch.isChecked();
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("gameMode", MainActivity.gameMode);
            /* animation */
            int colorFrom = (binding.gameModeSwitch.isChecked() ? myPurple : myRed);
            int colorTo = (colorFrom ^ myPurple ^ myRed);
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(250); // milliseconds
            colorAnimation.addUpdateListener(animator -> {
                binding.gameModeSwitch.setBackgroundColor((int) animator.getAnimatedValue());
            });
            colorAnimation.start();
            editor.apply();
        });

        /*     SoundEffects     */
        binding.soundEffects.setChecked(MainActivity.soundEffects);
        binding.soundEffects.setOnClickListener(view -> {
            MainActivity.soundEffects = binding.soundEffects.isChecked();
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("soundEffects", MainActivity.soundEffects);
            editor.apply();
        });

        binding.highContrast.demoColorScheme(0);
        binding.pastel.demoColorScheme(1);
        binding.autumn.demoColorScheme(2);
    }

    /*    ColorScheme    */
    public void onRadioButtonClicked(View view) {
        int newScheme;
        if (((RadioButton) view).isChecked()) {
            if (binding.radioHighContrast.equals(view))
                newScheme = CardView.HIGH_CONTRAST;
            else if (binding.radioPastel.equals(view))
                newScheme = CardView.PASTEL;
            else
                newScheme = CardView.AUTUMN;
            MainActivity.colorScheme = newScheme;
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("colorScheme", newScheme);
            editor.apply();
            Utils.dealWithToolbar(binding.toolbarSettings.getRoot(), getApplicationContext());
            myRed = CardView.colors[MainActivity.colorScheme][0];
            myPurple = CardView.colors[MainActivity.colorScheme][6];
            binding.gameModeSwitch.setBackgroundColor(MainActivity.gameMode ? myRed : myPurple);
        }
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

}
