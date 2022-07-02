package com.jjaworska.p7;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.jjaworska.p7.databinding.ActivityMultiplayerGameBinding;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import bluetooth.BluetoothService;
import bluetooth.Constants;


public class MultiplayerGameActivity extends SinglePlayerActivity {

    private BluetoothService mChatService;
    private final Map<Integer, String> nicks = new HashMap<>();
    private final Map<Integer, Integer> results = new HashMap<>();
    private final Map<Integer, TextView> textviews = new HashMap<>();
    private ActivityMultiplayerGameBinding binding;
    /* To check whether we should wait for the other side to join */
    boolean introductionReceived = false;
    LinearLayout container;
    Menu optionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* We will resume the stopwatch once we get a message from the second side */
        stopwatch.pause();
        binding.rvNumbers.setVisibility(View.INVISIBLE);

        mChatService = MultiplayerActivity.getAdapter();
        mChatService.updateHandler(mHandler);

        /* An ID cannot contain a "END_OF_MESSAGE" byte */
        do {
            myId = new Random().nextInt();
        } while (!isOkId(myId));

        Log.i("MultiplayerGame", "MyId = " + myId);
        Log.i("MultiplayerGame", "My nick is " + mChatService.nick);
        nicks.put(myId, mChatService.nick);
        results.put(myId, 0);
        textviews.put(myId, newTextView(mChatService.nick + " (me)"));

        /*
         * Not a super elegant solution, but we can ensure
         */
        if (MultiplayerActivity.getAdapter().amIServer()) {
            Handler handler = new Handler(Looper.getMainLooper());
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (!introductionReceived) {
                        sendSetupMessage();
                        introduceYourself();
                        handler.postDelayed(this, 500);
                    }
                }
            };
            handler.post(runnable);
        }

    }

    @NonNull
    private TextView newTextView(String nick) {
        TextView toAdd = new TextView(getApplicationContext());
        toAdd.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        toAdd.setText(nick + ": 0 sets");
        toAdd.setTextSize(18);
        container.addView(toAdd);
        return toAdd;
    }

    private void sendSetupMessage() {
        /* Wake up the others */
        mChatService.write(new byte[1]);
        byte[] setupMsg = new byte[deckSize + 6];
        setupMsg[0] = Constants.GAME_SETUP;
        for (int i = 1; i <= deckSize; i++)
            setupMsg[i] = data[i - 1].byteValue();
        writeMyId(setupMsg, deckSize + 1);
        mChatService.write(setupMsg);
    }

    private void introduceYourself() {
        String nickToSend = (char) Constants.INTRODUCTION + mChatService.nick;
        byte[] nickToBytes = nickToSend.getBytes(StandardCharsets.UTF_8);
        byte[] msg = new byte[nickToBytes.length + 6];
        msg[0] = Constants.INTRODUCTION;
        System.arraycopy(nickToBytes, 0, msg, 1, nickToBytes.length);
        writeMyId(msg, nickToBytes.length + 1);
        mChatService.write(msg);
    }

    public void writeMyId(byte[] array, int offset) {
        BigInteger bigInt = BigInteger.valueOf(myId);
        byte[] bytesId = bigInt.toByteArray();
        System.arraycopy(bytesId, 0, array, offset, 4);
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
            if (item.getItemId() == R.id.question) {
                for (CardView c : clickedCards)
                    c.click();
                clickedCards.clear();
                xorOfCards = 0;
                Integer[] xorFour = getXorFour(adapter.getVisibleValues());
                List<CardView> visibleCards = adapter.getVisibleCards();
                for (int i : xorFour)
                    visibleCards.get(i).highlight();
                return (true);
            }
            if (item.getItemId() == R.id.finish) {
                byte[] msgFinish = new byte[6];
                msgFinish[0] = Constants.STOP_GAME;
                writeMyId(msgFinish, 1);
                mChatService.write(msgFinish);
                endGame(true);
                return (true);
            }
            if (item.getItemId() == R.id.pause) {
                byte[] msgPause = new byte[6];
                msgPause[0] = Constants.PAUSE_GAME;
                writeMyId(msgPause, 1);
                mChatService.write(msgPause);
                return togglePauseButton(item, myId);
            }
            if (item.getItemId() == android.R.id.home) {
                MultiplayerActivity.finishService();
                Intent intent2 = new Intent(this, MainActivity.class);
                startActivity(intent2);
                return (true);
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

    /*
     * The logic of user communication is implemented here
     */
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.MESSAGE_READ) {
                byte[] buf = (byte[]) msg.obj;
                int sender = msg.arg2;
                Log.i("Bluetooth", String.format("Received message from %d\n", sender));
                if (buf[0] == Constants.GAME_SETUP) {
                    Log.i("Multiplayer Activity", "Setup message received");
                    data = new Integer[msg.arg1 - 1];
                    for (int i = 0; i < msg.arg1 - 1; i++) {
                        data[i] = (int) buf[i + 1];
                    }
                    deckSize = data.length;
                    setRecyclerView();
                    updateCardsLeft();
                    introduceYourself();
                }
                if (buf[0] == Constants.NEW_SET) {
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
                    /* acknowledge the new user */
                    if (!nicks.containsKey(sender)) {
                        nicks.put(sender, newNick);
                        textviews.put(sender, newTextView(newNick));
                        results.put(sender, 0);
                        /* The communication was successfully established, start the game */
                        Log.i("Multiplayer Activity", "Introduction received");
                        introductionReceived = true;
                        stopwatch.resume();
                        binding.rvNumbers.setVisibility(View.VISIBLE);
                    }
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
