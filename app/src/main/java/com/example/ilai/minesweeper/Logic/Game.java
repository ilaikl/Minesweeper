package com.example.ilai.minesweeper.Logic;

/**
 * Created by Ilai on 21/08/2017.
 */

public class Game {
    private Board mBoard;
    private Level level;
    private GameStatus mGameStatus;

    public Game(Level level){
        mGameStatus=GameStatus.NOT_YET_STARTED;
        this.level=level;
        if(level==Level.EASY)
            mBoard=new Board(10,5);
        if(level==Level.MEDIUM)
            mBoard=new Board(10,10);
        if(level==Level.HARD)
            mBoard=new Board(5,10);
    }

    public void flagUnflagTile(int i,int j){
        if(!mBoard.getTile(i,j).ismIsSelected())
            mBoard.getTile(i,j).setmIsFlaged(!mBoard.getTile(i,j).ismIsFlaged());
    }

    public void selectTile(int i,int j){
        if (mGameStatus==GameStatus.NOT_YET_STARTED)
            mGameStatus=GameStatus.STARTED;
        if(!mBoard.getTile(i,j).ismIsSelected() && !mBoard.getTile(i,j).ismIsFlaged())
        {
            mBoard.revealTile(i,j);
            if( mBoard.getTile(i,j).ismIsMined()){
                mGameStatus=GameStatus.LOST;
            }else {
                if(mBoard.getPlayableTilesLeft()==0)
                    mGameStatus=GameStatus.WON;
            }

        }

    }

    public GameStatus getmGameStatus() {
        return mGameStatus;
    }

    public Board getmBoard() {
        return mBoard;
    }

    public Level getLevel() {
        return level;
    }
}
