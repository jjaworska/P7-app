package com.example.p7;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example.p7.databinding.ActivityHighestScoresBinding;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import room.ConnectDB;
import room.Result;

public class HighestScoresActivity extends AppCompatActivity {

    private ActivityHighestScoresBinding binding;
    static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHighestScoresBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Highest Scores");
        setSupportActionBar(binding.toolbarHighestScores.getRoot());
        Utils.dealWithToolbar(binding.toolbarHighestScores.getRoot(), getApplicationContext());
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.headers.dateField.setText("Date");
        binding.headers.dateField.setTextColor(Color.WHITE);
        binding.headers.ownSets.setText("#sets");
        binding.headers.ownSets.setTextColor(Color.WHITE);
        binding.headers.time.setText("Time");
        binding.headers.time.setTextColor(Color.WHITE);
        ConnectDB db = ConnectDB.getDbInstance(getApplicationContext());
        binding.timeSpentPlaying.setText(Integer.toString(db.resultDao().getTimeSpentPlaying()));
        binding.setsCollected.setText(Integer.toString(db.resultDao().getSetsCollected()));
        /*     RECYCLER VIEWS    */
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        /*     BEST P7 GAMES     */
        RecyclerView recyclerViewP7 = binding.recyclerViewP7;
        recyclerViewP7.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewP7.addItemDecoration(dividerItemDecoration);
        ResultsAdapter adapterP7 = new ResultsAdapter(getApplicationContext());
        recyclerViewP7.setAdapter(adapterP7);
        List<Result> resultListP7 = db.resultDao().bestP7Games();
        adapterP7.setResultList(resultListP7);
        /*     BEST P6 GAMES     */
        RecyclerView recyclerViewP6 = binding.recyclerViewP6;
        recyclerViewP6.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewP6.addItemDecoration(dividerItemDecoration);
        ResultsAdapter adapterP6 = new ResultsAdapter(getApplicationContext());
        recyclerViewP6.setAdapter(adapterP6);
        List<Result> resultListP6 = db.resultDao().bestP6Games();
        adapterP6.setResultList(resultListP6);
        /*     FRAME LAYOUT     */
        int myRed = CardView.colors[MainActivity.colorScheme][0];
        int myPurple = CardView.colors[MainActivity.colorScheme][6];
        binding.frame.setMeasureAllChildren(true);
        if (MainActivity.gameMode == CardView.P7) {
            binding.recyclerViewP6.setVisibility(View.GONE);
            binding.gameModeSwitch.setChecked(true);
            binding.gameModeSwitch.setBackgroundColor(myRed);
            binding.headers.getRoot().setBackgroundColor(myRed);
        } else {
            binding.recyclerViewP7.setVisibility(View.GONE);
            binding.gameModeSwitch.setChecked(false);
            binding.gameModeSwitch.setBackgroundColor(myPurple);
            binding.headers.getRoot().setBackgroundColor(myPurple);
        }
        binding.gameModeSwitch.setOnClickListener(view -> {
            View toForeground, toBackground;
            int colorFrom, colorTo;
            if (binding.gameModeSwitch.isChecked()) {
                toForeground = binding.recyclerViewP7;
                toBackground = binding.recyclerViewP6;
                colorFrom = myPurple;
                colorTo = myRed;
            }
            else {
                toForeground = binding.recyclerViewP6;
                toBackground = binding.recyclerViewP7;
                colorFrom = myRed;
                colorTo = myPurple;
            }
            Utils.fadeAnimation(toBackground, toForeground, 250);
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(250); // milliseconds
            colorAnimation.addUpdateListener(animator -> {
                binding.gameModeSwitch.setBackgroundColor((int) animator.getAnimatedValue());
                binding.headers.getRoot().setBackgroundColor((int) animator.getAnimatedValue());
            });
            colorAnimation.start();

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
}