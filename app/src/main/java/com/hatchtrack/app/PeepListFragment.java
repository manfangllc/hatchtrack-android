package com.hatchtrack.app;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PeepListFragment extends Fragment implements Braggable {
    private static final String TAG = PeepListFragment.class.getSimpleName();

    public interface PeepClickListener {
        void onPeepClicked(int dbId);
    }

    private FloatingActionButton fab;
    private CoordinatorLayout mainCoordinator;

    PeepClickListener clickListener;

    public PeepListFragment() {
    }

    public static PeepListFragment newInstance(PeepClickListener listener, FloatingActionButton fab, CoordinatorLayout mc) {
        PeepListFragment fragment = new PeepListFragment();
        fragment.clickListener = listener;
        fragment.fab = fab;
        fragment.mainCoordinator = mc;
        return(fragment);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_peep_list, container, false);
        RecyclerView peepListView = rootView.findViewById(R.id.peepListId);
        peepListView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        peepListView.setAdapter(new PeepRvAdapter(this.getActivity(), this.getActivity().getSupportLoaderManager(), this.clickListener));
        this.fab.hide();
        return(rootView);
    }

    @Override
    public void onVisible() {
        this.fab.hide();
    }
}
