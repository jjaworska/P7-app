package com.jjaworska.p7;

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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjaworska.p7.databinding.ActivitySinglePlayerBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class SinglePlayerActivity extends AppCompatActivity implements RecyclerAdapter.ItemClickListener {

    final static int NR_OF_CARDS = 12;

    List<CardView> clickedCards = new LinkedList<>();
    Stopwatch stopwatch = new Stopwatch();
    Integer [] data;

    boolean paused = false;
    int setsTaken = 0;
    int mySetsTaken = 0;
    int helpTaken = 0;
    int xorOfCards = 0;
    int deckSize;
    int firstUnvisibleCard = NR_OF_CARDS;
    int myId = 0;

    RecyclerAdapter adapter;
    FrameLayout frame;
    LinearLayout pause;
    TextView cardsLeft;
    RecyclerView rvNumbers;
    TextView stoper;


    protected MediaPlayer mpClick;
    protected MediaPlayer mpSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dealWithViews();

        if (MainActivity.gameMode == CardView.P7)
            deckSize = 128;
        else
            deckSize = 64;
        data = generateSequence();

        updateCardsLeft();
        setRecyclerView();

        mpClick = MediaPlayer.create(this, R.raw.click);
        mpSet = MediaPlayer.create(this, R.raw.set);

        runTimer();
    }

    protected void setRecyclerView() {
        RecyclerView recyclerView = rvNumbers;
        int numberOfColumns = 3;
        recyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        adapter = new RecyclerAdapter(this, Arrays.copyOfRange(data, 0, NR_OF_CARDS));
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    public void dealWithViews() {
        ActivitySinglePlayerBinding binding = ActivitySinglePlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarSingle.getRoot());
        Utils.dealWithToolbar(binding.toolbarSingle.getRoot(), getApplicationContext());
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.frame.setMeasureAllChildren(true);
        binding.pause.setVisibility(View.GONE);
        frame = binding.frame;
        pause = binding.pause;
        cardsLeft = binding.cardsLeft;
        rvNumbers = binding.rvNumbers;
        stoper = binding.stoper;
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopwatch.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!paused)
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
                helpTaken++;
                for (CardView c : clickedCards)
                    c.click();
                clickedCards.clear();
                xorOfCards = 0;
                Integer[] xorFour = getXorFour(adapter.getVisibleValues());
                List<CardView> visibleCards = adapter.getVisibleCards();
                for (int i : xorFour)
                    visibleCards.get(i).highlight();
                return(true);
            case R.id.finish:
                endGame(false);
                return(true);
            case R.id.pause:
                if (!paused) {
                    Utils.fadeAnimation(rvNumbers, pause, 500);
                    item.setIcon(R.drawable.resume);
                    stopwatch.pause();
                    paused = true;
                    return (true);
                } else {
                    Utils.fadeAnimation(pause, rvNumbers, 500);
                    item.setIcon(R.drawable.pause);
                    stopwatch.resume();
                    paused = false;
                    return (true);
                }
            case android.R.id.home:
                saveProgress();
                Intent intent2 = new Intent(this, MainActivity.class);
                startActivity(intent2);
                return(true);
        }
        return(super.onOptionsItemSelected(item));
    }

    public void saveProgress() {
        SinglePlayerFinishActivity.updateCounters(
                mySetsTaken,
                mySetsTaken - helpTaken,
                stopwatch.getIntTime(),
                this.getApplicationContext()
        );
    }

    @Override
    public void onItemClick(View view, int position) {
        CardView cv = view.findViewById(R.id.card_image);
        if (cv.isClicked()) {
            clickedCards.remove(cv);
            xorOfCards ^= cv.getValue();
        } else {
            clickedCards.add(cv);
            xorOfCards ^= cv.getValue();
        }
        if (MainActivity.soundEffects)
            mpClick.start();
        cv.click();
        if (clickedCards.size() == 4 && xorOfCards == 0) {
            takeSet(myId);
        }
    }

    /* takeSet assumes that cards that form the set are clicked */
    protected void takeSet(int whose) {
        if (MainActivity.soundEffects)
            mpSet.start();
        setsTaken++;
        if (whose == myId) mySetsTaken++;
        if (firstUnvisibleCard == deckSize) {
            endGame(true);
            return;
        }
        for (CardView clicked : clickedCards) {
            clicked.click();
            clicked.setValue(data[firstUnvisibleCard++]);
        }
        while (!checkForXor()) {
            if (firstUnvisibleCard == deckSize)
                endGame(true);
            firstUnvisibleCard -= 4;
            List<Integer> remaining = Arrays.asList(
                    Arrays.copyOfRange(data, firstUnvisibleCard, deckSize)
            );
            Collections.shuffle(remaining);
            for (int i = firstUnvisibleCard; i < deckSize; i++)
                data[i] = remaining.get(i - firstUnvisibleCard);
            for (CardView clicked : clickedCards)
                clicked.setValue(data[firstUnvisibleCard++]);
        }
        updateCardsLeft();
        clickedCards.clear();
    }

    public void endGame(boolean finished) {
        Intent intent = new Intent(this, SinglePlayerFinishActivity.class);
        intent.putExtra("finished", finished);
        intent.putExtra("time_int", stopwatch.getIntTime());
        intent.putExtra("time_string", stopwatch.getTime());
        intent.putExtra("sets", mySetsTaken);
        intent.putExtra("own_sets", mySetsTaken - helpTaken);
        startActivity(intent);
    }

     protected void runTimer() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                stopwatch.update();
                String text1 = stopwatch.getTime();
                stoper.setText(text1);
                handler.postDelayed(this, 1000);
            }
        });
    }

    protected void updateCardsLeft() {
        cardsLeft.setText(String.format("%d cards left in the deck", deckSize - firstUnvisibleCard));
    }

    public static Integer[] generateSequence() {
        List<Integer> ans;
        do {
            int ds;
            if (MainActivity.gameMode == CardView.P7)
                ds = 128;
            else
                ds = 64;
            ans = new ArrayList<>();
            for (int i = 0; i < ds; i++)
                ans.add(i);
            Collections.shuffle(ans);
        } while (!checkListForXor(ans.subList(0, NR_OF_CARDS)));
        return ans.toArray(new Integer[0]);
    }

    public boolean checkForXor() {
        return checkListForXor(adapter.getVisibleValues());
    }

    public static boolean checkListForXor(List<Integer> list) {
        return getXorFour(list) != null;
    }

    public static Integer[] getXorFour(List<Integer> list) {
        for (int i = 3; i < 12; i++) for (int j = 2; j < i; j++) for (int k = 1; k < j; k++) for (int l = 0; l < k; l++) {
            int a = list.get(i);
            int b = list.get(j);
            int c = list.get(k);
            int d = list.get(l);
            if ((a ^ b ^ c ^ d) == 0)
                return new Integer[]{i, j, k, l};
        }
        return null;
    }

}
