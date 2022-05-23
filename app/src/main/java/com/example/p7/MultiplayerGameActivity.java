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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.p7.databinding.ActivityMultiplayerGameBinding;

import java.math.BigInteger;
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
    private Map<Integer, String> nicks = new HashMap<>();
    private Map<Integer, Integer> results = new HashMap<>();
    private Map<Integer, TextView> textviews = new HashMap<>();
    private ActivityMultiplayerGameBinding binding;
    LinearLayout container;
    Menu optionsMenu;

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
        results.put(myId, 0);
        TextView toAdd = new TextView(getApplicationContext());
        toAdd.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        toAdd.setText(mChatService.nick + " (me): 0 sets");
        container.addView(toAdd);
        textviews.put(myId, toAdd);

        if (mChatService.amIServer()) {
            stopwatch.pause();
            /* Wake up the others */
            mChatService.write(new byte[1]);
            byte[] setupMsg = new byte[deckSize + 6];
            setupMsg[0] = Constants.GAME_SETUP;
            for (int i = 1; i <= deckSize; i++)
                setupMsg[i] = data[i - 1].byteValue();
            writeMyId(setupMsg, deckSize + 1);
            sleep(500);
            mChatService.write(setupMsg);
            stopwatch.resume();
        }

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
        BigInteger bigInt = BigInteger.valueOf(myId);
        byte[] bytesId = bigInt.toByteArray();
        for (int i = 0; i < 4; i++)
            array[offset + i] = bytesId[i];
        array[offset + 4] = Constants.END_OF_MESSAGE;
    }

    public static boolean isOkId(int x) {
        for (int i = 0; i < 4; i++)
            if ((byte) (x >>> (24 - 8 * i)) == Constants.END_OF_MESSAGE)
                return false;
        return true;
    }

    @Override
    public void dealWithViews() {
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
        frame = binding.frame;
        pause = binding.pause;
        cardsLeft = binding.cardsLeft;
        rvNumbers = binding.rvNumbers;
        stoper = binding.stoper;
        container = binding.container;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_single_player, menu);
        optionsMenu = menu;
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
                byte[] msgFinish = new byte[6];
                msgFinish[0] = Constants.STOP_GAME;
                writeMyId(msgFinish, 1);
                mChatService.write(msgFinish);
                endGame(true);
                return(true);
            case R.id.pause:
                byte[] msgPause = new byte[6];
                msgPause[0] = Constants.PAUSE_GAME;
                writeMyId(msgPause, 1);
                mChatService.write(msgPause);
                return togglePauseButton(item, myId);
            case android.R.id.home:
                MultiplayerActivity.finishService();
                Intent intent2 = new Intent(this, MainActivity.class);
                startActivity(intent2);
                return(true);
        }
        return(super.onOptionsItemSelected(item));
    }

    private boolean togglePauseButton(MenuItem item, int whoDidIt) {
        if (!paused) {
            Utils.fadeAnimation(rvNumbers, pause, 500);
            binding.pausedTextView.setText("Paused by " + nicks.get(whoDidIt));
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
    }

    void newSetFor(int id) {
        int res = results.get(id);
        results.put(id, ++res);
        TextView tv = textviews.get(id);
        String nick = nicks.get(id);
        if (id == myId){
            tv.setText(nick + " (me): " + res);
        } else {
            tv.setText(nick + ": " + res);
        }
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
        newSetFor(myId);
    }

    @Override
    public void endGame(boolean finished) {
        Intent intent = new Intent(this, MultiplayerFinishActivity.class);
        intent.putExtra("time_int", stopwatch.getIntTime());
        StringBuilder summaryBuilder = new StringBuilder();
        for (int id : results.keySet()) {
            String nick = nicks.get(id);
            int res = results.get(id);
            summaryBuilder.append(nick);
            summaryBuilder.append(" - ");
            summaryBuilder.append(res);
            summaryBuilder.append('\n');
        }
        String summary = summaryBuilder.toString();
        intent.putExtra("summary", summary);
        startActivity(intent);
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.MESSAGE_READ) {
                byte[] buf = (byte[]) msg.obj;
                int sender = msg.arg2;
                Log.i("Bluetooth", String.format("received from %d\n", sender));
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
                    newSetFor(sender);
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
                    String newNick = new String(buf, 4, msg.arg1 - 4, StandardCharsets.UTF_8);
                    Log.i("Bluetooth", newNick + " just introduced themselves");
                    nicks.put(sender, newNick);
                    TextView toAdd = new TextView(getApplicationContext());
                    toAdd.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
                    toAdd.setText(newNick + ": 0 sets");
                    container.addView(toAdd);
                    textviews.put(sender, toAdd);
                    results.put(sender, 0);
                }
                if (buf[0] == Constants.PAUSE_GAME) {
                    togglePauseButton(optionsMenu.findItem(R.id.pause), sender);
                }
                if (buf[0] == Constants.STOP_GAME) {
                    endGame(true);
                }
            }
        }
    };

}
