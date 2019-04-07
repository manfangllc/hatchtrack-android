package com.hatchtrack.app;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

public class DialogEggCount extends DialogFragment implements android.widget.NumberPicker.OnValueChangeListener {
    private static final String TAG = DialogChooseSpecies.class.getSimpleName();

    interface EggCountListener {
        void onEggCount(int count);
    }

    private Dialog dialog;
    private EggCountListener listener;
    private int count;
    private NumberPicker numberPicker;

    void setEggCountListener(EggCountListener ecl) {
        this.listener = ecl;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (this.dialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.HatchTrackDialogThemeAnim_NoMinWidth);
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View rootView = inflater.inflate(R.layout.dialog_egg_count, null);
            this.numberPicker = rootView.findViewById(R.id.numberPicker);
            this.numberPicker.setMinValue(0);
            this.numberPicker.setMaxValue(100);
            this.numberPicker.setOnValueChangedListener(this);
            this.numberPicker.setValue(this.count);
            builder.setMessage(R.string.egg_count)
                    .setView(rootView)
                    .setNegativeButton(R.string.choose_species_negative, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    })
                    .setPositiveButton(R.string.choose_peeps_positive, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (DialogEggCount.this.listener != null) {
                                DialogEggCount.this.listener.onEggCount(DialogEggCount.this.count);
                            }
                        }
                    });
            this.dialog = builder.create();
        }
        return (this.dialog);
    }

    @Override
    public void onValueChange(android.widget.NumberPicker picker, int oldVal, int newVal) {
        this.count = newVal;
    }

    public void setValue(int i){
        this.count = i;
    }
}
