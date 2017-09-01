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


        tileView.setBackgroundResource(R.drawable.unexposed_tile);
        tileView.text.setText("");

        if(!getItem(position).ismIsSelected()){

            if(getItem(position).ismIsFlaged()){
                tileView.setBackgroundResource(R.drawable.flag32);
            }

        }
        else {

            tileView.setBackgroundColor(Color.TRANSPARENT);

            if(!getItem(position).ismIsFlaged()){

                if(getItem(position).ismIsMined()){
                    tileView.setBackgroundResource(R.drawable.mine);
                }
                else{
                    int surroundingMines = mBoard.getNumberOfSurroundingMines(position/mBoard.getDimension(),
                            position%mBoard.getDimension());
                    if(surroundingMines > 0) {
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
