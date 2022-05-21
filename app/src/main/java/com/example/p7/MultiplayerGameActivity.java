package com.example.p7;

import static android.os.SystemClock.sleep;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.p7.databinding.ActivityMultiplayerGameBinding;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import bluetooth.BluetoothChatService;
import bluetooth.Constants;

public class MultiplayerGameActivity extends AppCompatActivity implements RecyclerAdapter.ItemClickListener {
    final static int NR_OF_CARDS = 12;
    RecyclerAdapter adapter;
    List<CardView> clickedCards = new LinkedList<>();
    Stopwatch stopwatch = new Stopwatch();
    boolean paused = false;
    int setsTaken = 0;
    int helpTaken = 0;
    int xorOfCards = 0;
    int deckSize;
    int firstUnvisibleCard = NR_OF_CARDS;
    Integer [] data;

    private ActivityMultiplayerGameBinding binding;

    private MediaPlayer mpClick;
    private MediaPlayer mpSet;
    private BluetoothChatService mChatService;

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case Constants.MESSAGE_READ:
                    byte[] buf = (byte[]) msg.obj;
                    Log.i("Bluetooth", String.format("received: %d\n",
                            (int)(buf[0])));
                    if (buf[0] == Constants.GAME_SETUP) {
                        data = new Integer[msg.arg1 - 2];
                        for (int i = 0; i < msg.arg1 - 2; i++) {
                            data[i] = (int) buf[i + 1];
                        }
                        deckSize = data.length;
                        binding.cardsLeft.setText(String.format("%d cards left in the deck", deckSize - NR_OF_CARDS));
                        setRecyclerView(Arrays.copyOfRange(data, 0, NR_OF_CARDS));
                    }
                    if (buf[0] == Constants.NEW_SET) {
                        Log.i("Bluetooth", String.format("Very much new set"));
                        // construct a string from the valid bytes in the buffer
                        if (MainActivity.soundEffects)
                            mpSet.start();
                        setsTaken++;
                        if (firstUnvisibleCard == deckSize) {
                            endGame(true);
                            return;
                        }
                        clickedCards.clear();
                        Log.i("Bluetooth", String.format(
                                "Received %d %d %d %d", buf[1], buf[2], buf[3], buf[4]
                        ));
                        for (CardView cardview : adapter.getVisibleCards()) {
                            byte x = (byte) cardview.getValue();
                            for (int i = 0; i < 4; i++)
                                if (x == buf[i + 1])
                                    cardview.setValue(data[firstUnvisibleCard + i]);
                        }
                        firstUnvisibleCard += 4;
                        binding.cardsLeft.setText(String.format("%d cards left in the deck", deckSize - firstUnvisibleCard));
                    }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMultiplayerGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarSingle.getRoot());
        Utils.dealWithToolbar(binding.toolbarSingle.getRoot(), getApplicationContext());
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        binding.frame.setMeasureAllChildren(true);
        binding.pause.setVisibility(View.GONE);

        mChatService = MultiplayerActivity.getAdapter();
        Log.i("Bluetooth", "Updating handler");
        mChatService.updateHandler(mHandler);

        if (mChatService.amIServer()) {
            /* Wake up the others */
            mChatService.write(new byte[1]);
            if (MainActivity.gameMode == CardView.P7)
                deckSize = 128;
            else
                deckSize = 64;

            data = SinglePlayerActivity.generateSequence();

            binding.cardsLeft.setText(String.format("%d cards left in the deck", deckSize - NR_OF_CARDS));

            setRecyclerView(Arrays.copyOfRange(data, 0, NR_OF_CARDS));

            byte[] setupMsg = new byte[deckSize + 2];
            setupMsg[0] = Constants.GAME_SETUP;
            for (int i = 1; i <= deckSize; i++)
                setupMsg[i] = data[i - 1].byteValue();
            setupMsg[deckSize + 1] = Constants.END_OF_MESSAGE;
            Log.i("Bluetooth", "Sending welcome message");
            sleep(500);
            mChatService.write(setupMsg);
        }

        mpClick = MediaPlayer.create(this, R.raw.click);
        mpSet = MediaPlayer.create(this, R.raw.set);

        runTimer();

        Log.i("MultiplayerGameActivity", mChatService.amIServer() ? "I am server" : "I am not server");
    }

    private void setRecyclerView(Integer[] values) {
        RecyclerView recyclerView = binding.rvNumbers;
        int numberOfColumns = 3;
        recyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        adapter = new RecyclerAdapter(this, values);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
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
        } else {
            clickedCards.add(cv);
            xorOfCards ^= cv.getValue();
        }
        if (MainActivity.soundEffects)
            mpClick.start();
        cv.click();
        /* SET! */
        if (clickedCards.size() == 4 && xorOfCards == 0) {
            sleep(150);
            if (MainActivity.soundEffects)
                mpSet.start();
            setsTaken++;
            /* Inform other players */
            byte[] msg = new byte[5];
            msg[0] = Constants.NEW_SET;
            for (int i = 0; i < 4; i++) {
                CardView cardview = clickedCards.get(i);
                msg[i + 1] = (byte) cardview.getValue();
            }
            mChatService.write(msg);
            if (firstUnvisibleCard == deckSize) {
                endGame(true);
                return;
            }
            for (CardView clicked : clickedCards) {
                clicked.click();
                clicked.setValue(data[firstUnvisibleCard++]);
            }
            clickedCards.clear();
            binding.cardsLeft.setText(String.format("%d cards left in the deck", deckSize - firstUnvisibleCard));
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
