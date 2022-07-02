package com.jjaworska.p7;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.jjaworska.p7.databinding.ActivityHighestScoresBinding;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import room.ConnectDB;
import room.Result;


public class HighestScoresActivity extends AppCompatActivity {

    private ActivityHighestScoresBinding binding;
    static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

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

        ConnectDB db = ConnectDB.getDbInstance(getApplicationContext());

        /*    Otherwise we would get a NullPointerException    */
        if (db.resultDao().getAnyResult().size() == 0) {
            Toast.makeText(
                    getApplicationContext(),
                    "You don't have any results yet!",
                    Toast.LENGTH_SHORT
            ).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return;
        }

        binding.timeSpentPlaying.setText(Integer.toString(db.resultDao().getTimeSpentPlaying()));
        binding.setsCollected.setText(Integer.toString(db.resultDao().getSetsCollected()));

        binding.headers.dateField.setText("Date");
        binding.headers.dateField.setTextColor(Color.WHITE);
        binding.headers.ownSets.setText("#sets");
        binding.headers.ownSets.setTextColor(Color.WHITE);
        binding.headers.time.setText("Time");
        binding.headers.time.setTextColor(Color.WHITE);

        /*
         * The code below was rewritten in order to avoid code duplication
         * All it does is deciding which list of results to display first (P6 or P7)
         * and programming the transition between these
         */

        /*     RECYCLER VIEWS    */
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        RecyclerView[] recyclerView = {binding.recyclerViewP6, binding.recyclerViewP7};
        ResultsAdapter[] adapter = new ResultsAdapter[2];
        for (int mode = 0; mode < 2; mode++) {
            recyclerView[mode].setLayoutManager(new LinearLayoutManager(this));
            recyclerView[mode].addItemDecoration(dividerItemDecoration);
            adapter[mode] = new ResultsAdapter(getApplicationContext());
            recyclerView[mode].setAdapter(adapter[mode]);
            List<Result> results = db.resultDao().bestGames(mode == 1);
            Log.i("Db", "The results are " + results.toString());
            adapter[mode].setResultList(results);
        }

        /*     FRAME LAYOUT     */
        int[] frameColor = {
                CardView.colors[MainActivity.colorScheme][0],
                CardView.colors[MainActivity.colorScheme][6]
        };
        binding.frame.setMeasureAllChildren(true);
        int gm = MainActivity.gameMode ? 1 : 0;
        recyclerView[1 - gm].setVisibility(View.GONE);
        binding.gameModeSwitch.setChecked(gm == 1);
        binding.gameModeSwitch.setBackgroundColor(frameColor[1 - gm]);
        binding.headers.getRoot().setBackgroundColor(frameColor[1 - gm]);
        binding.gameModeSwitch.setOnClickListener(view -> {
            View toForeground, toBackground;
            int colorFrom, colorTo;
            int state = (binding.gameModeSwitch.isChecked() ? 1 : 0);
            toForeground = recyclerView[state];
            toBackground = recyclerView[1 - state];
            colorFrom = frameColor[state];
            colorTo = frameColor[1 - state];
            /* Animation */
            Utils.fadeAnimation(toBackground, toForeground, Utils.veryShortAnimationDuration);
            Utils.colorAnimation(binding.gameModeSwitch, colorFrom, colorTo, Utils.veryShortAnimationDuration);
            Utils.colorAnimation(binding.headers.getRoot(), colorFrom, colorTo, Utils.veryShortAnimationDuration);

        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent2 = new Intent(this, MainActivity.class);
            startActivity(intent2);
            return true;
        }
        return false;
    }
}
