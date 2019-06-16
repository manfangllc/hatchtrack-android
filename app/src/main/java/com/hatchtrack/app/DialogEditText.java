package com.hatchtrack.app;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.hatchtrack.app.database.Data;

public class DialogEditText extends DialogFragment implements TextView.OnEditorActionListener {
    private static final String TAG = DialogChooseSpecies.class.getSimpleName();

    interface EditTextListener {
        void onText(String text);
    }

    private Dialog dialog;
    private EditTextListener listener;
    private String title;
    private String value;
    private EditText editText;

    void setEditTextListener(EditTextListener ecl) {
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
            View rootView = inflater.inflate(R.layout.dialog_edit_text, null);
            this.editText = rootView.findViewById(R.id.editText);
            this.editText.setText(this.value);
            this.editText.setOnEditorActionListener(this);
            this.editText.requestFocus();
            InputMethodManager imm = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            builder.setMessage(this.title)
                    .setView(rootView)
                    .setNegativeButton(R.string.choose_species_negative, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            InputMethodManager imm = (InputMethodManager) DialogEditText.this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(DialogEditText.this.editText.getWindowToken(), 0);
                        }
                    })
                    .setPositiveButton(R.string.choose_peeps_positive, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (DialogEditText.this.listener != null) {
                                InputMethodManager imm = (InputMethodManager) DialogEditText.this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(DialogEditText.this.editText.getWindowToken(), 0);
                                DialogEditText.this.value = DialogEditText.this.editText.getText().toString();
                                DialogEditText.this.listener.onText(DialogEditText.this.value);
                            }
                        }
                    });
            this.dialog = builder.create();
        }
        return (this.dialog);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        Log.i(TAG, "onEditorAction()");
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            InputMethodManager imm = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(this.editText.getWindowToken(), 0);
            this.value = v.getText().toString();
            if(this.listener != null){
                this.listener.onText(this.value);
            }
        }
        return(false);
    }

    public void setValue(String v){
        this.value = v;
        if(this.editText != null){
            this.editText.setText(this.value);
        }
    }

    public void setTitle(String v){
        this.title = v;
     }
}
