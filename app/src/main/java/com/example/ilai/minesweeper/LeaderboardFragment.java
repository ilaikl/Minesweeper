package com.example.ilai.minesweeper;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.ilai.minesweeper.Logic.Level;

public class LeaderboardFragment extends Fragment {

    public static final String LEADERBOARD_FRAGMENT_TAG = "LeaderboardFragmentTag";

    public OnGameSelectedListener mCallback;

    private Level mMode;
    private int mPickedGame;
    private ListView mList;

    public static LeaderboardFragment newInstance() {
        return new LeaderboardFragment();
    }

    public LeaderboardFragment() {
    }

    // Container must implement this interface
    public interface OnGameSelectedListener {
        public void onGameSelected(int position);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container has implemented the callback interface.
        // If not, it throws an exception.
        try {
            mCallback = (OnGameSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnGameSelectedListener");
        }

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initializing fields:
        mMode = Level.EASY;
        mPickedGame = 0; //No game is picked.

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_leaderboard, container, false);

        //Setting up the list:
        setupList(rootView);

        //Returning inflater:
        return rootView;

    }


    private void setupList(View rootView) {

        //Getting the listView:
        mList = (ListView)rootView.findViewById(R.id.leaderboard_list);

        //Setting the adapter for the listView:
        mList.setAdapter(new RowAdapter(getActivity(), mMode));

        //Setting a click listener for the listView:
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                int gamePicked = position + 1;

                if(mPickedGame != gamePicked){
                    mPickedGame = gamePicked;
                    ((RowAdapter)mList.getAdapter()).setPickedGame(gamePicked);
                    ((RowAdapter)mList.getAdapter()).notifyDataSetChanged();

                    // Send the event to the host activity:
                    mCallback.onGameSelected(position);

                }

            }
        });

    }

    public void updateMode(Level mode) {

        //update mode:
        mMode = mode;
        ((RowAdapter)mList.getAdapter()).setMode(mode);

        //Update picked game:
        mPickedGame = 0;
        ((RowAdapter)mList.getAdapter()).setPickedGame(0);

        //Notify listView:
        ((RowAdapter)mList.getAdapter()).notifyDataSetChanged();
        mList.smoothScrollToPosition(0);

    }

    public void updateSelectedGame(int position) {

        //Update picked game:
        mPickedGame = position;
        ((RowAdapter)mList.getAdapter()).setPickedGame(position);

        //Notify listView:
        ((RowAdapter)mList.getAdapter()).notifyDataSetChanged();
        mList.smoothScrollToPosition(position);

    }


}

