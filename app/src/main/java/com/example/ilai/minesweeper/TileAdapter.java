package com.example.ilai.minesweeper;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.ilai.minesweeper.Logic.Board;
import com.example.ilai.minesweeper.Logic.Tile;

/**
 * Created by Ilai on 21/08/2017.
 */

public class TileAdapter extends BaseAdapter {

    private Board mBoard;
    private Context mContext;

    public TileAdapter(Context context, Board board) {

        mBoard = board;
        mContext = context;

    }

    @Override
    public int getCount() {
        return mBoard.getDimension()*mBoard.getDimension();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        TileView tileView;
        tileView = (TileView)convertView;
        if(tileView == null)
            tileView = new TileView(mContext);


        //tileView.text.setText(getItem(position).);
       // tileView.setBackgroundResource(R.drawable.filename);


        //Resetting the tile's look:
        tileView.setBackgroundColor(Color.GRAY);
        tileView.text.setText("");

        //If not revealed:
        if(!getItem(position).ismIsSelected()){

            //if this tile isn't revealed, all we have to do is check if its flagged:
            if(getItem(position).ismIsFlaged()){
                tileView.setBackgroundResource(R.drawable.flag32);
            }

        }
        else { //The tile is revealed:

            tileView.setBackgroundColor(Color.WHITE);

            //checking if there's an "unjustified" flag:
            if(!getItem(position).ismIsFlaged()){

                if(getItem(position).ismIsMined()){
                    tileView.setBackgroundResource(R.drawable.mine32); // revealed and mined.
                }
                else{ //Revealed, but not mined and not flagged:
                    int surroundingMines = mBoard.getNumberOfSurroundingMines(position/mBoard.getDimension(),
                            position%mBoard.getDimension());
                    if(surroundingMines > 0) { //The tile should show the number of surrounding mines if there are any.
                        tileView.text.setText("" + surroundingMines);
                    }
                }

            }

        }




        return tileView;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Tile getItem(int position) {
        return mBoard.getTile(position/mBoard.getDimension(),
                position%mBoard.getDimension());
    }
}
