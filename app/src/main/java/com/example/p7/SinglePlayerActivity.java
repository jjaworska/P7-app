package com.example.p7;

import static android.os.SystemClock.sleep;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.p7.databinding.ActivitySinglePlayerBinding;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SinglePlayerActivity extends AppCompatActivity implements RecyclerAdapter.ItemClickListener {
    final static int NR_OF_CARDS = 12;
    RecyclerAdapter adapter;
    List<CardView> clickedCards = new LinkedList<>();
    Stopwatch stopwatch = new Stopwatch();
    boolean[] wasClicked = new boolean[NR_OF_CARDS];
    boolean paused = false;
    int setsTaken = 0;
    int helpTaken = 0;
    int xorOfCards = 0;
    int deckSize;
    int firstUnvisibleCard = NR_OF_CARDS;
    Integer [] data;

    private ActivitySinglePlayerBinding binding;

    private MediaPlayer mpClick;
    private MediaPlayer mpSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySinglePlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarSingle.getRoot());
        Utils.dealWithToolbar(binding.toolbarSingle.getRoot(), getApplicationContext());
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        binding.frame.setMeasureAllChildren(true);
        binding.pause.setVisibility(View.GONE);

        List<Integer> helper;
        if (MainActivity.gameMode == CardView.P7)
            deckSize = 128;
        else
            deckSize = 64;
        helper = IntStream.range(0, deckSize).boxed().collect(Collectors.toList());

        Collections.shuffle(helper);
        data = helper.toArray(new Integer[0]);

        // TODO: Make sure these are distinct
        for (int i = 0; i < NR_OF_CARDS; i++)
            wasClicked[i] = false;

        binding.cardsLeft.setText(String.format("%d cards left in the deck", deckSize - NR_OF_CARDS));

        RecyclerView recyclerView = binding.rvNumbers;
        int numberOfColumns = 3;
        recyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        adapter = new RecyclerAdapter(this, helper.subList(0, NR_OF_CARDS).toArray(new Integer[0]));
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        mpClick = MediaPlayer.create(this, R.raw.click);
        mpSet = MediaPlayer.create(this, R.raw.set);

        runTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopwatch.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopwatch.resume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_single_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.question:
                for (CardView c : clickedCards)
                    c.click();
                clickedCards.clear();
                xorOfCards = 0;
                List<CardView> visibleCards = adapter.getVisibleCards();
                for (int i = 3; i < 12; i++) for (int j = 2; j < i; j++) for (int k = 1; k < j; k++) for (int l = 0; l < k; l++) {
                    int a = visibleCards.get(i).getValue();
                    int b = visibleCards.get(j).getValue();
                    int c = visibleCards.get(k).getValue();
                    int d = visibleCards.get(l).getValue();
                    if ((a ^ b ^ c ^ d) == 0) {
                        visibleCards.get(i).highlight();
                        visibleCards.get(j).highlight();
                        visibleCards.get(k).highlight();
                        visibleCards.get(l).highlight();
                        helpTaken++;
                        return true;
                    }
                }
                return(true);
            case R.id.finish:
                endGame(false);
                return(true);
            case R.id.pause:
                if (!paused) {
                    Utils.fadeAnimation(binding.rvNumbers, binding.pause, 500);
                    item.setIcon(R.drawable.resume);
                    stopwatch.pause();
                    paused = true;
                    return (true);
                } else {
                    Utils.fadeAnimation(binding.pause, binding.rvNumbers, 500);
                    item.setIcon(R.drawable.pause);
                    stopwatch.resume();
                    paused = false;
                    return (true);
                }
            case android.R.id.home:
                Intent intent2 = new Intent(this, MainActivity.class);
                startActivity(intent2);
                return(true);
        }
        return(super.onOptionsItemSelected(item));
    }

    @Override
    public void onItemClick(View view, int position) {

        CardView cv = view.findViewById(R.id.card_image);
        if (cv.isClicked()) {
            clickedCards.remove(cv);
            xorOfCards ^= cv.getValue();
            wasClicked[position] = false;
        } else {
            clickedCards.add(cv);
            xorOfCards ^= cv.getValue();
            wasClicked[position] = true;
        }
        if (MainActivity.soundEffects)
            mpClick.start();
        cv.click();
        if (clickedCards.size() == 4 && xorOfCards == 0) {
            sleep(150);
            if (MainActivity.soundEffects)
                mpSet.start();
            setsTaken++;
            if (firstUnvisibleCard == deckSize) {
                endGame(true);
                return;
            }
            for (CardView clicked : clickedCards) {
                clicked.click();
                clicked.setValue(data[firstUnvisibleCard++]);
            }
            binding.cardsLeft.setText(String.format("%d cards left in the deck", deckSize - firstUnvisibleCard));
            clickedCards.clear();
        }
    }

    public void endGame(boolean finished) {
        Intent intent = new Intent(this, SinglePlayerFinishActivity.class);
        intent.putExtra("finished", finished);
        intent.putExtra("time_int", stopwatch.getIntTime());
        intent.putExtra("time_string", stopwatch.getTime());
        intent.putExtra("sets", setsTaken);
        intent.putExtra("own_sets", setsTaken - helpTaken);
        startActivity(intent);
    }

    private void runTimer() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                stopwatch.update();
                String text1 = stopwatch.getTime();
                binding.stoper.setText(text1);
                handler.postDelayed(this, 500);
            }
        });
    }

}
