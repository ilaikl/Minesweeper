package com.example.ilai.minesweeper;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.example.ilai.minesweeper.Logic.Board;
import com.example.ilai.minesweeper.Logic.Game;
import com.example.ilai.minesweeper.Logic.GameStatus;
import com.example.ilai.minesweeper.Logic.Level;

public class GameActivity extends AppCompatActivity {
    private Level level;
    private TextView mGameTextView;
    private Game mGame;
    private GridView mGrid;
    private TextView mTimeText;
    private Thread timeThread;
    private int time=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        level =(Level) getIntent().getBundleExtra("m_bundle").getSerializable("string_level");
        mGame=new Game(level);
        mGrid = (GridView) findViewById(R.id.GridLayout1);

        mGrid.setAdapter(new TileAdapter(getApplicationContext(),mGame.getmBoard()));

        mGrid.setNumColumns(mGame.getmBoard().getDimension());

        mGrid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mGame.flagUnflagTile(position / mGame.getmBoard().getDimension(),
                        position % mGame.getmBoard().getDimension());

                ((TileAdapter) mGrid.getAdapter()).notifyDataSetChanged();
                return true;
            }
        });

        mGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                mGame.selectTile(position / mGame.getmBoard().getDimension(),
                        position % mGame.getmBoard().getDimension());

                ((TileAdapter) mGrid.getAdapter()).notifyDataSetChanged();

                if(mGame.getmGameStatus()== GameStatus.WON || mGame.getmGameStatus()== GameStatus.LOST)
                {
                    Intent i = new Intent(GameActivity.this, ScoreActivity.class);
                    Bundle b= new Bundle();
                    b.putSerializable("won_lost", mGame.getmGameStatus());
                    b.putInt("timekey", time);
                    i.putExtra("m_bundle2",b);
                    startActivity(i);
                }
            }

        });


        mTimeText = (TextView) findViewById(R.id.time_text);
        timeThread=new Thread(new Runnable() {
            @Override
            public void run() {
                    while ((mGame.getmGameStatus() == GameStatus.STARTED) || (mGame.getmGameStatus() == GameStatus.NOT_YET_STARTED)) {
                        if (mGame.getmGameStatus() == GameStatus.STARTED) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            time++;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTimeText.setText("time: " + time);
                                }
                            });

                        }
                    }

            }
        });

        timeThread.start();

    }
}
