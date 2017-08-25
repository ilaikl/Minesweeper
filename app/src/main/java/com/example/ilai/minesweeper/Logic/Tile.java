package com.example.ilai.minesweeper.Logic;

/**
 * Created by Ilai on 21/08/2017.
 */

public class Tile {
    private boolean mIsSelected=false;
    private boolean mIsFlaged=false;
    private boolean mIsMined=false;

    public boolean ismIsSelected() {
        return mIsSelected;
    }

    public void setmIsSelected(boolean mIsSelected) {
        this.mIsSelected = mIsSelected;
    }

    public boolean ismIsFlaged() {
        return mIsFlaged;
    }

    public void setmIsFlaged(boolean mIsFlaged) {
        this.mIsFlaged = mIsFlaged;
    }

    public boolean ismIsMined() {
        return mIsMined;
    }

    public void setmIsMined(boolean mIsMined) {
        this.mIsMined = mIsMined;
    }
}
