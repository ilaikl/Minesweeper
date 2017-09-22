package com.example.ilai.minesweeper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GameDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Leaderboard.db";
    public static final String TOP_GAMES_TABLE = "TopGamesTable";
    public static final String GAME_ID = "GameID"; //Primary Key
    public static final String PLAYER_NAME = "PlayerName";
    public static final String GAME_TIME = "GameTime";
    public static final String LOCATION = "Location";
    public static final String GAME_MODE = "GameMode";
    public static final int MAX_ROWS_PER_MODE = 10;
    public static final int MAX_PLAYER_NAME_LENGTH = 14;

    public GameDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TOP_GAMES_TABLE + " (" +
                GAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PLAYER_NAME + " TEXT, " +
                GAME_TIME + " TEXT, " +
                LOCATION + " TEXT, " +
                GAME_MODE + " TEXT)");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TOP_GAMES_TABLE);
        onCreate(db);
    }


    public boolean insertGame(String playerName, String gameTime, String location, String gameMode) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PLAYER_NAME, playerName);
        contentValues.put(GAME_TIME, gameTime);
        contentValues.put(LOCATION, location);
        contentValues.put(GAME_MODE, gameMode);
        long result = db.insert(TOP_GAMES_TABLE, null, contentValues);
        if(result == -1) return false;

        Cursor cursor = db.rawQuery("select * from " + TOP_GAMES_TABLE +
                " where " + GAME_MODE + " = '" + gameMode +
                "' order by " + GAME_TIME + " ASC", null);
        if(cursor.getCount() == MAX_ROWS_PER_MODE + 1) {
            cursor.moveToLast();
            String worstGameID = cursor.getString(0);
            db.delete(TOP_GAMES_TABLE, GAME_ID + " = ?", new String[] {worstGameID});
        }
        return true;
    }

    public boolean isWorthy(String gameTime, String gameMode){

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select " + GAME_TIME +
                " from " + TOP_GAMES_TABLE +
                " where " + GAME_MODE + " = '" + gameMode +
                "' order by " + GAME_TIME + " ASC", null);
        if(cursor.getCount() < MAX_ROWS_PER_MODE)
            return true;

        cursor.moveToLast();
        if(cursor.getString(0).compareTo(gameTime) > 0)
            return true;

        return false;

    }

    public Cursor getGamesSortedByGT(String gameMode) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select " + PLAYER_NAME + ", " + GAME_TIME + ", " + LOCATION +
                " from " + TOP_GAMES_TABLE +
                " where " + GAME_MODE + " = '" + gameMode +
                "' order by " + GAME_TIME + " ASC", null);
    }
}

