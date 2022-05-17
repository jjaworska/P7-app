package com.example.p7;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.p7.databinding.ActivityRulesBinding;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class RulesActivity extends AppCompatActivity implements RecyclerAdapter.ItemClickListener {
    static final int NR_OF_CARDS = 9;
    private RecyclerAdapter adapter;
    private boolean[] wasClicked = new boolean[NR_OF_CARDS];
    private int clicked = 0;
    private Integer xor_of_cards = 0;
    private Integer[] data = new Integer[NR_OF_CARDS];

    private ActivityRulesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRulesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setTitle("How to play");
        setSupportActionBar(binding.toolbarRules.getRoot());
        Utils.dealWithToolbar(binding.toolbarRules.getRoot(), getApplicationContext());
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Generating the fours
        List<Integer> rng = new LinkedList<>();
        for (int i = 0; i < 128; i++)
            rng.add(i);
        do {
            Collections.shuffle(rng);
            data = rng.subList(0, 9).toArray(new Integer[0]);
        } while (CardView.checkForXor(data) == false);

        RecyclerView recyclerView = binding.rvNumbers;
        int numberOfColumns = 3;
        recyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        adapter = new RecyclerAdapter(this, data);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
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

    @Override
    public void onItemClick(View view, int position) {
        if (wasClicked[position]) {
            clicked--;
            xor_of_cards ^= data[position];
            wasClicked[position] = false;
        } else {
            clicked++;
            xor_of_cards ^= data[position];
            wasClicked[position] = true;
        }
        binding.xor.setValue(xor_of_cards);
        // TODO: animations
        ((CardView) view.findViewById(R.id.card_image)).click();
        if (clicked == 4 && xor_of_cards == 0) {
            binding.congrats.setText("Congrats, it's a set!!");
            binding.congrats.setTextColor(Color.RED);
        } else {
            binding.congrats.setText("Not a set");
            binding.congrats.setTextColor(Color.BLACK);
        }
    }

}
