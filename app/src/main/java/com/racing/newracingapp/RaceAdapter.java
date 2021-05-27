package com.racing.newracingapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;

import java.util.ArrayList;
import java.util.List;

public class RaceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private List<RaceModel> moviesList = new ArrayList<>();
    private Context context;

    public RaceAdapter(List<RaceModel> moviesList, Context context) {

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
        RaceModel result = moviesList.get(position);
        String roomId = result.getRaceId();
        CategoryViewHolder holder = (CategoryViewHolder) vholder;
        holder.raceId_tv.setText(result.getRaceId());
        holder.raceTitle_tv.setText(result.getRaceTitle());
        holder.raceItem_rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(context,MapsActivity.class);
                i.putExtra("raceId", result.getRaceId());
                context.startActivity(i);
            }
        });
        holder.raceShare_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, roomId);
                sendIntent.setType("text/plain");
                context.startActivity(sendIntent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return moviesList.size();
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {

        private TextView raceId_tv, raceTitle_tv;
        private ImageView raceShare_iv;
        private RelativeLayout raceItem_rl;
        public CategoryViewHolder(View itemView) {
            super(itemView);

            raceId_tv = itemView.findViewById(R.id.raceId_tv);
            raceTitle_tv = itemView.findViewById(R.id.raceTitle_tv);
            raceShare_iv = itemView.findViewById(R.id.race_share_iv);
            raceItem_rl = itemView.findViewById(R.id.raceItem_rl);
        }
    }


}

