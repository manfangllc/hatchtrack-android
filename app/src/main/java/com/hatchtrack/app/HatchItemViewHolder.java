package com.hatchtrack.app;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class HatchItemViewHolder extends RecyclerView.ViewHolder {

    CardView cv;
    TextView nameView;
    TextView startDate;
    TextView endDate;

    int dbId;
    String name;
    HatchListFragment.HatchClickListener clickListener;

    public HatchItemViewHolder(View itemView, HatchListFragment.HatchClickListener listener) {
        super(itemView);
        this.cv = itemView.findViewById(R.id.hatchCardId);
        this.nameView = itemView.findViewById(R.id.name);
        this.startDate = itemView.findViewById(R.id.startDateId);
        this.endDate = itemView.findViewById(R.id.endDateId);

        if(listener != null) {
            this.clickListener = listener;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HatchItemViewHolder.this.clickListener.onHatchClicked(HatchItemViewHolder.this.dbId);
                }
            });
        }
    }

    void setDbId(int i){
        this.dbId = i;
    }

    void refresh(){
        this.nameView.setText(this.name);
    }
}
