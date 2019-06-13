package com.hatchtrack.app;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.hatchtrack.app.database.Data;

import java.util.HashMap;
import java.util.Map;

public class DialogChooseSpecies extends DialogFragment implements ChooseSpeciesView.ChooseSpeciesListener {
    private static final String TAG = DialogChooseSpecies.class.getSimpleName();

    private int hatchId;
    private String[] speciesNames = new String[0];
    private float[] speciesDays = new float[0];
    private int[] speciesIds = new int[0];
    private Map<Integer, String> speciesPicMap = new HashMap<>();
    private Dialog dialog;
    private ChooseSpeciesView.ChooseSpeciesListener relayListener;

    void setOnSpeciesListener(ChooseSpeciesView.ChooseSpeciesListener listener){
        this.relayListener = listener;
    }

    @Override
    public void setArguments(@Nullable Bundle args) {
        super.setArguments(args);
        this.hatchId = args.getInt(Globals.KEY_HATCH_ID, 0);
        this.speciesNames = args.getStringArray(Globals.KEY_SPECIES_NAMES);
        this.speciesDays = args.getFloatArray(Globals.KEY_SPECIES_DAYS);
        this.speciesIds = args.getIntArray(Globals.KEY_SPECIES_IDS);
        int[] ids = args.getIntArray(Globals.KEY_SPECIES_PICS_IDS);
        String[] names = args.getStringArray(Globals.KEY_SPECIES_PICS_STRINGS);
        for(int i = 0; i < ids.length; i++){
            this.speciesPicMap.put(ids[i], names[i]);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if(this.dialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.HatchTrackDialogThemeAnim);
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View rootView = inflater.inflate(R.layout.dialog_choose_species, null);
            ChooseSpeciesView speciesRecycler = rootView.findViewById(R.id.speciesList);
            speciesRecycler.setArguments(this.speciesIds, this.speciesNames, this.speciesDays, this.speciesPicMap, this);
            Log.i(TAG, "onCreateDialog(): ChooseSpeciesView=" + speciesRecycler.toString());
            builder.setMessage(R.string.choose_species)
                    .setView(rootView)
                    .setNegativeButton(R.string.choose_species_negative, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            this.dialog = builder.create();
        }
        return(this.dialog);
    }

    @Override
    public void onSpeciesChosen(int speciesId, String speciesName, float speciesDays) {
        Log.i(TAG, "onSpeciesChosen(): hatchId=" + hatchId + ", speciesId=" + speciesId);
        if(this.relayListener != null){
            this.relayListener.onSpeciesChosen(speciesId, speciesName, speciesDays);
        }
        if(this.hatchId > 0) {
            Data.setHatchSpecies(this.getContext(), this.hatchId, speciesId);
        }
        this.dialog.dismiss();
    }

}
