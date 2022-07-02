package com.jjaworska.p7;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import room.Result;


public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder> {

    private Context context;
    private List<Result> resultList;

    public ResultsAdapter(Context context) {
        this.context = context;
    }

    public void setResultList(List<Result> resultList) {
        this.resultList = resultList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ResultsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.result_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.date_field.setText(HighestScoresActivity.dateFormat.format(this.resultList.get(position).date));
        holder.own_sets.setText(Integer.toString(this.resultList.get(position).setsByYourself));
        holder.time.setText(Stopwatch.stringFromSeconds(this.resultList.get(position).timeInSeconds));
    }

    @Override
    public int getItemCount() {
        return  this.resultList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView date_field;
        TextView own_sets;
        TextView time;

        public ViewHolder(View view) {
            super(view);
            date_field = view.findViewById(R.id.date_field);
            own_sets = view.findViewById(R.id.own_sets);
            time = view.findViewById(R.id.time);
        }
    }
}
