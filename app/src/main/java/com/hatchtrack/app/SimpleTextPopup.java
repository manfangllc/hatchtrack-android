package com.hatchtrack.app;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class SimpleTextPopup extends DialogFragment {
    private static final String TAG = DialogChooseSpecies.class.getSimpleName();

    private Dialog dialog;
    private TextView textView;
    private String text;
    private String title;

    @Override
    public void setArguments(@Nullable Bundle args) {
        super.setArguments(args);
        this.title = args.getString("Title", "");
        this.text = args.getString("Text", "<nothing>");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.textView.setText(this.text);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if(this.dialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.HatchTrackDialogThemeAnim);
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View rootView = inflater.inflate(R.layout.dialog_simple_text, null);
            this.textView = rootView.findViewById(R.id.textView);
            this.textView.setMovementMethod(new ScrollingMovementMethod());
            this.textView.setHorizontallyScrolling(true);
            this.textView.setText(this.text);
            builder.setMessage(this.title)
                    .setView(rootView)
                    .setNegativeButton("close", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            this.dialog = builder.create();
        }
        return(dialog);
    }
}
