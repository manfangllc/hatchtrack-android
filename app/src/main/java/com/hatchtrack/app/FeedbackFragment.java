package com.hatchtrack.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FeedbackFragment extends Fragment {
    private static final String TAG = FeedbackFragment.class.getSimpleName();

    private Context context;
    private CollapsingToolbarLayout toolbarLayout;
    private AppBarLayout appBarLayout;

    public FeedbackFragment() {
    }

    public static FeedbackFragment newInstance(CollapsingToolbarLayout ctl, AppBarLayout abl) {
        FeedbackFragment fragment = new FeedbackFragment();
        fragment.toolbarLayout = ctl;
        fragment.appBarLayout = abl;
        return(fragment);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this.getContext();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.toolbarLayout.setTitle("Feedback");
        this.appBarLayout.setExpanded(true);
        View rootView = inflater.inflate(R.layout.frag_feedback, container, false);
        rootView.findViewById(R.id.emailId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri u = Uri.parse("mailto:feedback@hatchtrack.com?subject=com.hatchtrack.app Feedback");
                Log.i(TAG, "uri=" + u.toString());
                FeedbackFragment.this.getActivity().startActivity(new Intent("android.intent.action.SENDTO", Uri.parse("mailto:feedback@hatchtrack.com?subject=com.hatchtrack.app Feedback")));
            }
        });
        rootView.findViewById(R.id.rateId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FeedbackFragment.this.getActivity().startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=net.emirac.chromania")));
            }
        });
        return(rootView);
    }

}
