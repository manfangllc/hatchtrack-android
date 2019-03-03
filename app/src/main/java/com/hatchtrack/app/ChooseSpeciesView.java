package com.hatchtrack.app;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class ChooseSpeciesView extends RecyclerView {
    private static final String TAG = ChooseSpeciesView.class.getSimpleName();

    public interface ChooseSpeciesListener {
        void onSpeciesChosen(int speciesId);
    }

    private int speciesId;
    private String[] speciesNames = new String[0];
    private float[] speciesDays = new float[0];
    private int[] speciesIds = new int[0];
    private Map<Integer, String> speciesPicMap = new HashMap<>();
    private ChooseSpeciesView.ChooseSpeciesListener listener;

    private class SpeciesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View view;
        ImageView imageView;
        TextView textName;
        TextView textDays;
        int dbId;
        Integer position;

        public SpeciesViewHolder(View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.speciesImage);
            this.textName = itemView.findViewById(R.id.speciesName);
            this.textDays = itemView.findViewById(R.id.speciesDays);
            this.view = itemView;
        }
        @Override
        public void onClick(View v) {
            Log.i(TAG, "onClick(): view=" + v.toString() + ", position=" + this.position);
            ChooseSpeciesView.this.speciesId = ChooseSpeciesView.this.speciesIds[this.position];
            if(ChooseSpeciesView.this.listener != null) {
                ChooseSpeciesView.this.listener.onSpeciesChosen(ChooseSpeciesView.this.speciesId);
            }
        }
    }

    private class Adapter extends RecyclerView.Adapter<ChooseSpeciesView.SpeciesViewHolder> {

        @NonNull
        @Override
        public ChooseSpeciesView.SpeciesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //Inflate the layout, initialize the View Holder
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chooser_species_item, parent, false);
            ChooseSpeciesView.SpeciesViewHolder holder = new ChooseSpeciesView.SpeciesViewHolder(v);
            return(holder);
        }
        @Override
        public void onBindViewHolder(@NonNull ChooseSpeciesView.SpeciesViewHolder holder, int position) {
            holder.textName.setText(ChooseSpeciesView.this.speciesNames[position]);
            holder.textDays.setText(Float.toString(ChooseSpeciesView.this.speciesDays[position]));
            holder.view.setOnClickListener(holder);
            holder.dbId = ChooseSpeciesView.this.speciesIds[position];
            holder.position = position;
            String uriStr = ChooseSpeciesView.this.speciesPicMap.get(holder.dbId);
            if(uriStr != null){
                holder.imageView.setImageURI(Uri.parse(uriStr));
            }
            else {
                holder.imageView.setImageResource(R.drawable.hatch_1);
            }
        }
        @Override
        public int getItemCount() {
            return((ChooseSpeciesView.this.speciesIds == null) ? 0 : ChooseSpeciesView.this.speciesIds.length);
        }
    }

    public ChooseSpeciesView(Context context) {
        super(context);
    }

    public ChooseSpeciesView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ChooseSpeciesView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    void setArguments(int[] ids, String[] names, float[] days,  Map<Integer, String> pics, ChooseSpeciesView.ChooseSpeciesListener listen){
        this.speciesIds = ids;
        this.speciesNames = names;
        this.speciesDays =days;
        this.speciesPicMap = pics;
        this.listener = listen;
        this.setLayoutManager(new LinearLayoutManager(this.getContext()));
        this.setAdapter(new ChooseSpeciesView.Adapter());
    }
}
