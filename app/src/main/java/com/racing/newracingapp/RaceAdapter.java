package com.racing.newracingapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RaceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private List<String> moviesList = new ArrayList<>();
    private Context context;

    public RaceAdapter(List<String> moviesList, Context context) {

        this.moviesList = moviesList;
        this.context = context;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.race_item,parent,false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vholder, int position) {
        String result = moviesList.get(position);
        CategoryViewHolder holder = (CategoryViewHolder) vholder;
        holder.race_item_tv.setText(result);
        holder.race_item_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(context,MapsActivity.class);
                i.putExtra("raceId", result);
                context.startActivity(i);
            }
        });



    }

    @Override
    public int getItemCount() {
        return moviesList.size();
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {

        private TextView race_item_tv;
        public CategoryViewHolder(View itemView) {
            super(itemView);

            race_item_tv = itemView.findViewById(R.id.race_item_tv);
        }
    }


}

