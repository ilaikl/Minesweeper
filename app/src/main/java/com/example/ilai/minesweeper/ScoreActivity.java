package com.example.ilai.minesweeper;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ilai.minesweeper.Logic.GameStatus;

public class ScoreActivity extends AppCompatActivity {

    private GameDbHelper mDbHelper;
    private String mGameTime;
    private String mLocation;
    private String mMode;
    private String mPlayerName;
    private Bundle b;
    private TextView won_lost_tv;
    private Dialog mWorthyDialog;
    private AlertDialog mVerificationDialog;
    private EditText playerNameEditable;
    private GameStatus gs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        b = getIntent().getBundleExtra("m_bundle2");

        gs=(GameStatus) b.getSerializable("won_lost");
        int time= b.getInt("timekey");
        playerNameEditable = (EditText)findViewById(R.id.edit_text_name);
        mPlayerName = "";
        if(b.size() > 2){
            mDbHelper = new GameDbHelper(this);
            mGameTime = Integer.toString(time);
            mLocation = b.getString("address_key");
            mMode = b.getString("level_key");

            playerNameEditable.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable editable) {
                    mPlayerName = editable.toString();
                }
            });
        }

        won_lost_tv = (TextView) findViewById(R.id.won_lost_text);
        if(gs==GameStatus.LOST) {
            won_lost_tv.setText("You Lost!");
            playerNameEditable.setVisibility(View.INVISIBLE);
        }

        else if(gs==GameStatus.WON)
            won_lost_tv.setText("You Won! Your score is: "+time);

        Button backButton=(Button)findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(b.size()>2) {
                    mDbHelper.insertGame(mPlayerName, mGameTime, mLocation, mMode);
                        Intent i = new Intent(ScoreActivity.this, MainActivity.class);
                        startActivity(i);

                }else{
                    Intent i = new Intent(ScoreActivity.this, MainActivity.class);
                    startActivity(i);
                }


            }
        });

    }




}
