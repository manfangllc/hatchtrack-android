package com.hatchtrack.app;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class PeepItemViewHolder extends RecyclerView.ViewHolder {

    CardView cv;
    TextView title;

    int dbId;
    PeepListFragment.PeepClickListener clickListener;

    public PeepItemViewHolder(View itemView, PeepListFragment.PeepClickListener listener) {
        super(itemView);
        this.cv = itemView.findViewById(R.id.hatchCardId);
        this.title = itemView.findViewById(R.id.name);

        if(listener != null) {
            this.clickListener = listener;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PeepItemViewHolder.this.clickListener.OnPeepClicked(PeepItemViewHolder.this.dbId);
                }
            });
        }
    }
}
