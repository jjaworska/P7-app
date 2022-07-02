package com.jjaworska.p7;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private final Integer[] mData;
    private final LayoutInflater mInflater;
    private final List<CardView> visibleCards = new LinkedList<>();
    private ItemClickListener mClickListener;

    RecyclerAdapter(Context context, Integer[] data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycler_view_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.myImageView.setValue(mData[position]);
    }

    @Override
    public int getItemCount() {
        return mData.length;
    }

    public List<CardView> getVisibleCards() {
        return visibleCards;
    }

    /* had to do it the lame way to have better min SDK level */
    public List<Integer> getVisibleValues() {
        List<Integer> ans = new ArrayList<>();
        for (CardView x : visibleCards)
            ans.add(x.getValue());
        return ans;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView myImageView;

        ViewHolder(View itemView) {
            super(itemView);
            myImageView = itemView.findViewById(R.id.card_image);
            visibleCards.add(myImageView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAbsoluteAdapterPosition());
        }
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
