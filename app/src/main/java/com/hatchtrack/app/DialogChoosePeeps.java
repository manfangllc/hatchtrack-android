package com.hatchtrack.app;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.ArrayList;
import java.util.List;

public class DialogChoosePeeps extends DialogFragment {

    private int hatchId;
    private String[] peepNames = new String[0];
    private int[] peepIds = new int[0];
    private boolean[] isPeepInHatch = new boolean[0];
    private final List<Integer> selectList = new ArrayList<>();

    public interface ChoosePeepsDialogListener {
        void onPeepsChosen(int hatchId, List<Integer> peepIds);
    }

    private ChoosePeepsDialogListener listener;

    private class PeepViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener{

        CheckBox checkBox;
        int dbId;
        Integer position;

        public PeepViewHolder(View itemView) {
            super(itemView);
            this.checkBox = itemView.findViewById(R.id.peepCheckBox);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked) {
                DialogChoosePeeps.this.selectList.add(this.position);
            } else {
                DialogChoosePeeps.this.selectList.remove(this.position);
            }
        }
    }

    private class Adapter extends RecyclerView.Adapter<DialogChoosePeeps.PeepViewHolder> {

        Adapter(){
            super();
        }

        @NonNull
        @Override
        public DialogChoosePeeps.PeepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //Inflate the layout, initialize the View Holder
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chooser_peep_item, parent, false);
            PeepViewHolder holder = new PeepViewHolder(v);
            return(holder);
        }

        @Override
        public void onBindViewHolder(@NonNull DialogChoosePeeps.PeepViewHolder holder, int position) {
            holder.checkBox.setText(DialogChoosePeeps.this.peepNames[position] + "(peepId=" + DialogChoosePeeps.this.peepIds[position] + ")");
            holder.checkBox.setChecked(DialogChoosePeeps.this.isPeepInHatch[position]);
            holder.checkBox.setOnCheckedChangeListener(holder);
            holder.dbId = DialogChoosePeeps.this.peepIds[position];
            holder.position = position;
        }

        @Override
        public int getItemCount() {
            return((DialogChoosePeeps.this.peepIds == null) ? 0 : DialogChoosePeeps.this.peepIds.length);
        }
    }

    @Override
    public void setArguments(@Nullable Bundle args) {
        super.setArguments(args);
        this.hatchId = args.getInt(Globals.KEY_HATCH_ID, 0);
        this.peepNames = args.getStringArray(Globals.KEY_PEEP_NAMES);
        this.peepIds = args.getIntArray(Globals.KEY_PEEP_IDS);
        this.isPeepInHatch = args.getBooleanArray(Globals.KEY_PEEP_IN_HATCH);
        this.selectList.clear();
        for( int i = 0; i < this.isPeepInHatch.length; i++){
            if(this.isPeepInHatch[i]){
                this.selectList.add(i);
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.HatchTrackDialogThemeAnim);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.dialog_choose_peeps, null);
        RecyclerView peepRecycler = rootView.findViewById(R.id.peepList);
        peepRecycler.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        peepRecycler.setAdapter(new DialogChoosePeeps.Adapter());
        builder.setMessage(R.string.choose_peeps)
                .setView(rootView)
                .setPositiveButton(R.string.choose_peeps_positive, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        List<Integer> selectedPeepIds = new ArrayList<>(DialogChoosePeeps.this.selectList.size());
                        int i = 0;
                        for(Integer position: DialogChoosePeeps.this.selectList){
                            selectedPeepIds.add(DialogChoosePeeps.this.peepIds[position]);
                        }
                        DialogChoosePeeps.this.listener.onPeepsChosen(DialogChoosePeeps.this.hatchId, selectedPeepIds);
                    }
                })
                .setNegativeButton(R.string.choose_peeps_negative, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // verify that the host activity implements the callback interface
        try {
            listener = (ChoosePeepsDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(this.getActivity().toString() + " must implement ChoosePeepsDialogListener");
        }
    }
}
