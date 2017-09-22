package com.example.ilai.minesweeper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.view.View;

import com.example.ilai.minesweeper.Logic.Level;

public class MainActivity extends AppCompatActivity {

    public static final String DEFAULT = "N/A";
    private Level level=null;
    private RadioButton mRadioButtonEasy,mRadioButtonMedium,mRadioButtonHard;
    private Button mButton;
    private Button mScoresButton;
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


        mScoresButton = (Button) findViewById(R.id.scores_button);


        mScoresButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, LeaderboardActivity.class);
                startActivity(i);
            }
        });

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
                        }
                }
            });
        checkForPermissions();
    }

    private void checkForPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    Constants.MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
                else {
                    createVerificationDialog();
                }
                return;
            }
        }
    }

    private void createVerificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("this app needs your permission...")
                .setNegativeButton("Go Back", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                Constants.MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                    }
                }).setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                finish();
            }
        }).setCancelable(false)
                .create()
                .show();
    }
}
