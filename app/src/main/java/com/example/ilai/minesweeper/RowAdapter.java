package com.example.ilai.minesweeper;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.ilai.minesweeper.Logic.Level;

public class RowAdapter extends BaseAdapter {

    private Level mMode;
    private int mPickedGame;
    private Context mContext;
    private GameDbHelper mDbHelper;

    private static final int MAX_LOCATION_LENGTH = 16;


    public RowAdapter(Context context, Level mode) {

        mMode = mode;
        mContext = context;
        mPickedGame = 0;
        mDbHelper = new GameDbHelper(mContext);

    }


    @Override
    public int getCount() {
        return GameDbHelper.MAX_ROWS_PER_MODE;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        RowView rowView;
        rowView = (RowView)convertView;

        if(rowView == null) {
            rowView = new RowView(mContext);
            Log.v("Tile Adapter","creating new view for index " + position);
        } else {
            Log.e("Tile Adapter","RECYCLING view for index "+ position);
        }

        //Set NO. text:
        rowView.numberText.setText(String.format("%3s", position + 1 + "."));

        //If database has data suitable for this position, set the right texts:
        if(getItem(position) != null){

            rowView.nameText.setText(getItem(position)[0]);
            rowView.timeText.setText(getItem(position)[1]);

            //If the location text is too long, cut it:
            if(getItem(position)[2].length() > MAX_LOCATION_LENGTH) {
                rowView.locationText.setText(getItem(position)[2].substring(0, MAX_LOCATION_LENGTH - 1) + "...");
            }
            else {//Otherwise, set normally
                rowView.locationText.setText(getItem(position)[2]);
            }

        }

        // Otherwise, write nothing:
        else {
            rowView.nameText.setText("");
            rowView.timeText.setText("");
            rowView.locationText.setText("");
        }

        //If the row is selected, mark it.
        if(mPickedGame == position + 1) {
            rowView.setBackgroundColor(Color.LTGRAY);
        }
        else { //Otherwise, don't.
            rowView.setBackgroundColor(Color.WHITE);
        }

        return rowView;

    }


    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public String[] getItem(int position) {

        Cursor cursor = mDbHelper.getGamesSortedByGT(mMode.toString());

        if (cursor.getCount() <= position){
            return null; //The row for this position is empty.
        }

        String[] row = new String[cursor.getColumnCount()];
        cursor.moveToPosition(position);

        for(int i = 0; i < row.length; i++){
            row[i] = cursor.getString(i);
        }

        return row;

    }


    public void setPickedGame(int pickedGame){
        mPickedGame = pickedGame;
        Log.d("Row Adapter","Picked Game is now " + mPickedGame);
    }

    public void setMode(Level mode){
        mMode = mode;
        Log.d("Row Adapter","Mode is now " + mMode.toString());
    }


}
