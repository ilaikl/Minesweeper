package com.example.ilai.minesweeper.Logic;

import java.util.Random;

/**
 * Created by Ilai on 20/08/2017.
 */
public class Board {

    private Tile[][] tilesArr = null;
    private int mines;
    private int dimension;
    private int playableTilesLeft;

    public Board(int dimension, int numMines) {
        mines = numMines;
        this.dimension=dimension;
        tilesArr=new Tile[dimension][dimension];

        for(int i = 0; i < dimension; i++) {
            for(int j = 0; j < dimension; j++) {
                tilesArr[i][j] = new Tile();
            }
        }


        playableTilesLeft=dimension*dimension - mines;


        int minesRemained = mines;
        Random rnd = new Random();

        while(minesRemained > 0) {
            int randomRow = rnd.nextInt(dimension -1);
            int randomCol = rnd.nextInt(dimension -1);

            if(!tilesArr[randomRow][randomCol].ismIsMined()){
                tilesArr[randomRow][randomCol].setmIsMined(true);
                minesRemained--;
            }
        }
    }

    public Tile getTile(int i,int j) {
        return tilesArr[i][j];
    }


    public int getPlayableTilesLeft() {
        return playableTilesLeft;
    }

    public boolean revealTile(int i, int j) {

        if(tilesArr[i][j].ismIsSelected() || tilesArr[i][j].ismIsFlaged()) {
            return false;
        }

        tilesArr[i][j].setmIsSelected(true);
        playableTilesLeft--;

        if(!tilesArr[i][j].ismIsMined() && getNumberOfSurroundingMines(i, j)==0) {

            for(int a = i-1; a <= i+1; a++){
                for(int b = j-1; b <= j+1; b++){

                    if(a>=0 && a< dimension && b>=0 && b< dimension){
                        if(a!=i || b!=j){
                            revealTile(a, b);
                        }
                    }
                }
            }
        }
        return true;

    }


    public int getNumberOfSurroundingMines(int positionI, int positionJ) {

        int sum = 0;

        for(int i = positionI-1; i <= positionI+1; i++){
            for(int j = positionJ-1; j <= positionJ+1; j++){

                if(i>=0 && i< dimension && j>=0 && j< dimension){

                    if(i!=positionI || j!=positionJ){

                        if(tilesArr[i][j].ismIsMined())
                            sum++;
                    }

                }
            }
        }
        return sum;
    }

    public int getDimension() {
        return dimension;
    }
}