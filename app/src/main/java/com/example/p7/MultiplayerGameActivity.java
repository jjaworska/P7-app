package com.example.p7;

import static android.os.SystemClock.sleep;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.p7.databinding.ActivityMultiplayerGameBinding;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import bluetooth.BluetoothChatService;
import bluetooth.Constants;


public class MultiplayerGameActivity extends SinglePlayerActivity {

    private BluetoothChatService mChatService;
    private TextView player0, player1;
    private Map<Integer, String> nicks = new HashMap<>();
    // private String nick0, nick1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mChatService = MultiplayerActivity.getAdapter();
        mChatService.updateHandler(mHandler);

        do {
            myId = new Random().nextInt();
        } while (!isOkId(myId));
        Log.i("MultiplayerGame", "MyId = " + Integer.toString(myId));
        Log.i("MultiplayerGame", "My nick is " + mChatService.nick);
        nicks.put(myId, mChatService.nick);

        if (mChatService.amIServer()) {
            /* Wake up the others */
            mChatService.write(new byte[1]);
            byte[] setupMsg = new byte[deckSize + 6];
            setupMsg[0] = Constants.GAME_SETUP;
            for (int i = 1; i <= deckSize; i++)
                setupMsg[i] = data[i - 1].byteValue();
            writeMyId(setupMsg, deckSize + 1);
            sleep(500);
            mChatService.write(setupMsg);
        }
        // nick0 = mChatService.nick;
        /* Introduce yourself */
        String nickToSend = Character.toString((char) Constants.INTRODUCTION) + mChatService.nick;
        byte[] nickToBytes = nickToSend.getBytes(StandardCharsets.UTF_8);
        byte[] msg = new byte[nickToBytes.length + 6];
        msg[0] = Constants.INTRODUCTION;
        for (int i = 0; i < nickToBytes.length; i++)
            msg[i + 1] = nickToBytes[i];
        writeMyId(msg, nickToBytes.length + 1);
        mChatService.write(msg);
    }

    public void writeMyId(byte[] array, int offset) {
        for (int i = 0; i < 4; i++)
            array[offset + i] = (byte) (myId >>> (24 - 8 * i));
        array[offset + 4] = Constants.END_OF_MESSAGE;
    }

    public static boolean isOkId(int x) {
        for (int i = 0; i < 4; i++)
            if ((byte) (x >>> (24 - 8 * i)) == Constants.END_OF_MESSAGE)
                return false;
        return true;
    }

    private void dealWithViews() {
        ActivityMultiplayerGameBinding binding = ActivityMultiplayerGameBinding.inflate(getLayoutInflater());
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
        player0 = binding.player0;
        player1 = binding.player1;
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
                Intent intent2 = new Intent(this, MainActivity.class);
                startActivity(intent2);
                return(true);
        }
        return(super.onOptionsItemSelected(item));
    }

    @Override
    protected void takeSet(int whose) {
        /* Inform other players */
        byte[] msg = new byte[14];
        List<CardView> clickedCardsCopy = new ArrayList<>(clickedCards);
        msg[0] = Constants.NEW_SET;
        for (int i = 0; i < 4; i++) {
            CardView cardview = clickedCards.get(i);
            msg[i + 1] = (byte) cardview.getValue();
        }
        /* If this was the last set, we inform others here because it will end the activity */
        if (firstUnvisibleCard == deckSize) {
            writeMyId(msg, 5);
            mChatService.write(msg);
        }
        super.takeSet(myId);
        /* Otherwise, provide information about the new cards */
        for (int i = 0; i < 4; i++) {
            CardView cardview = clickedCardsCopy.get(i);
            msg[i + 5] = (byte) cardview.getValue();
        }
        writeMyId(msg, 9);
        mChatService.write(msg);
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.MESSAGE_READ) {
                byte[] buf = (byte[]) msg.obj;
                Log.i("Bluetooth", String.format("received from %d\n", msg.arg2));
                if (buf[0] == Constants.GAME_SETUP) {
                    data = new Integer[msg.arg1 - 1];
                    for (int i = 0; i < msg.arg1 - 1; i++) {
                        data[i] = (int) buf[i + 1];
                    }
                    deckSize = data.length;
                    Log.i("MultiplayerGameAct", String.format("deckSize = %d", deckSize));
                    updateCardsLeft();
                    setRecyclerView();
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
                    for (CardView cv : clickedCards)
                        cv.click();
                    clickedCards.clear();
                    Log.i("Bluetooth", String.format(
                            "Received %d %d %d %d", buf[1], buf[2], buf[3], buf[4]
                    ));
                    for (CardView cardview : adapter.getVisibleCards()) {
                        byte x = (byte) cardview.getValue();
                        for (int i = 0; i < 4; i++)
                            if (x == buf[i + 1])
                                cardview.setValue((int) buf[i + 5]);
                    }
                    firstUnvisibleCard += 4;
                    updateCardsLeft();
                }
                if (buf[0] == Constants.INTRODUCTION) {
                    String n = new String(buf, 1, msg.arg1 - 1);
                    Log.i("Bluetooth", n + " just introduced themselves");
                    nicks.put(msg.arg2, n);
                }
            }
        }
    };

}
