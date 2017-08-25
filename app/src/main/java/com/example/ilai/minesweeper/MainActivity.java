package com.example.ilai.minesweeper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.view.View;

import com.example.ilai.minesweeper.Logic.Level;

import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    public static final String DEFAULT = "N/A";
    private Level level=null;
    private RadioButton mRadioButtonEasy,mRadioButtonMedium,mRadioButtonHard;
    private Button mButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRadioButtonEasy = (RadioButton) findViewById(R.id.radioButtonEasy);
        mRadioButtonMedium = (RadioButton) findViewById(R.id.radioButtonMedium);
        mRadioButtonHard = (RadioButton) findViewById(R.id.radioButtonHard);


        SharedPreferences sharedPreferences = getSharedPreferences("MinesweeperData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String chosenDifficulty = sharedPreferences.getString("chosenDifficulty", DEFAULT);

        if(chosenDifficulty.equals(DEFAULT)){
            editor.putString("chosenDifficulty", Level.EASY.name());
            editor.commit();
            mRadioButtonEasy.setChecked(true);
        }
        else{
            if(chosenDifficulty.equals(Level.EASY.name()))
                mRadioButtonEasy.setChecked(true);

            if(chosenDifficulty.equals(Level.MEDIUM.name()))
                mRadioButtonMedium.setChecked(true);

            if(chosenDifficulty.equals(Level.HARD.name()))
                mRadioButtonHard.setChecked(true);
        }


        mButton = (Button) findViewById(R.id.button);




            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mRadioButtonEasy.isChecked())
                        level=Level.EASY;
                    else if (mRadioButtonMedium.isChecked())
                        level=Level.MEDIUM;
                    else if (mRadioButtonHard.isChecked())
                        level=Level.HARD;

                    if(level!=null)
                        {
                            SharedPreferences sharedPreferences = getSharedPreferences("MinesweeperData", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("chosenDifficulty",level.name());
                            editor.commit();

                            Intent i = new Intent(MainActivity.this, GameActivity.class);
                            Bundle b= new Bundle();
                            b.putSerializable("string_level", level);
                            i.putExtra("m_bundle",b);
                            startActivity(i);
                            finish();
                        }
                }
            });
    }

}
