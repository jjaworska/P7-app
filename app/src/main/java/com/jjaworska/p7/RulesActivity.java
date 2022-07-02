package com.jjaworska.p7;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.jjaworska.p7.databinding.ActivityRulesBinding;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class RulesActivity extends AppCompatActivity implements RecyclerAdapter.ItemClickListener {

    static final int NR_OF_CARDS = 9;
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
        } while (!CardView.checkForXor(data));

        RecyclerView recyclerView = binding.rvNumbers;
        int numberOfColumns = 3;
        recyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        RecyclerAdapter adapter = new RecyclerAdapter(this, data);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
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

    @Override
    public void onItemClick(View view, int position) {
        CardView cv = view.findViewById(R.id.card_image);
        if (cv.isClicked()) clicked--;
        else clicked++;
        xor_of_cards ^= data[position];
        binding.xor.setValue(xor_of_cards);
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
