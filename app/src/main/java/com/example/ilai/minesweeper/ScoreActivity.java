package com.example.ilai.minesweeper;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.ilai.minesweeper.Logic.GameStatus;
import com.example.ilai.minesweeper.Logic.Level;

public class ScoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);


        GameStatus gs=(GameStatus) getIntent().getBundleExtra("m_bundle2").getSerializable("won_lost");
        int time=getIntent().getBundleExtra("m_bundle2").getInt("timekey");

        TextView won_lost_tv = (TextView) findViewById(R.id.won_lost_text);
        if(gs==GameStatus.LOST)
            won_lost_tv.setText("You Lost!");
        else if(gs==GameStatus.WON)
            won_lost_tv.setText("You Won! Your score is: "+time);

        Button backButton=(Button)findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(ScoreActivity.this, MainActivity.class);
                startActivity(i);
            }
        });

    }
}
