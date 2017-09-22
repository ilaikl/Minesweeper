package com.example.ilai.minesweeper;

import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ilai.minesweeper.Logic.Game;
import com.example.ilai.minesweeper.Logic.GameStatus;
import com.example.ilai.minesweeper.Logic.Level;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.Random;

public class GameActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, RotateService.RotationListener{
    private Level level;
    private Game mGame;
    private GridView mGrid;
    private TextView mTimeText;
    private Thread timeThread;
    private int time=0;
    private TextView mMinesRemainsText;
    protected Location mFinalLocation;
    private AddressResultReceiver mResultReceiver;
    private boolean mAddressRequested = false;
    private String mAddressOutput;
    private GoogleApiClient mGoogleApiClient;
    private boolean mAnimationsAreFinished = false;
    private boolean mIntentServiceNoLongerNeeded = false;
    private boolean mShouldUpdateDatabase = false;
    private ServiceConnection mConnection;
    RotateService.RotateBinder mBinder;
    boolean isBound = false;
    int mFinalTilePosition = Constants.POSITION_DOES_NOT_EXIST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        level =(Level) getIntent().getBundleExtra("m_bundle").getSerializable("string_level");
        mGame=new Game(level);
        mGrid = (GridView) findViewById(R.id.GridLayout1);

        mGrid.setAdapter(new TileAdapter(mGame.getmBoard()));

        mGrid.setNumColumns(mGame.getmBoard().getDimension());

        mMinesRemainsText = (TextView) findViewById(R.id.mines_remained_text);
        mMinesRemainsText.setText("remaining mines: "+mGame.getMinesLeft());


        mGrid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if(mGame.getmGameStatus() != GameStatus.WON && mGame.getmGameStatus() != GameStatus.LOST){
                    mGame.flagUnflagTile(position / mGame.getmBoard().getDimension(),
                            position % mGame.getmBoard().getDimension());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMinesRemainsText.setText("remaining mines: "+mGame.getMinesLeft());
                        }
                    });

                    ((TileAdapter) mGrid.getAdapter()).notifyDataSetChanged();
                    return true;
                }

                return false;

            }
        });

        mGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(mGame.getmGameStatus() != GameStatus.WON && mGame.getmGameStatus() != GameStatus.LOST){

                    mGame.selectTile(position / mGame.getmBoard().getDimension(),
                            position % mGame.getmBoard().getDimension());
                    ((TileAdapter) mGrid.getAdapter()).notifyDataSetChanged();

                    if(mGame.getmGameStatus() == GameStatus.WON || mGame.getmGameStatus() == GameStatus.LOST)
                    {
                        timeThread.interrupt();
                        mFinalTilePosition = position;
                        if(mGame.getmGameStatus() == GameStatus.WON){
                            GameDbHelper dbHelper = new GameDbHelper(GameActivity.this);
                            if(dbHelper.isWorthy(mTimeText.getText().toString(), mGame.getLevel().toString())){
                                mShouldUpdateDatabase = true;
                                if (mGoogleApiClient.isConnected() && mFinalLocation != null) {
                                    startIntentService();
                                }
                                mAddressRequested = true;

                            }
                            else
                                mIntentServiceNoLongerNeeded = true;
                            activateWinAnimation();
                        }
                        else{
                            mIntentServiceNoLongerNeeded = true;
                            activateLoseAnimation();
                        }
                        final Runnable r = new Runnable() {
                            public void run() {
                                mAnimationsAreFinished = true;
                                if(mIntentServiceNoLongerNeeded)
                                    startConclusionActivity();
                            }
                        };
                        final Handler handler = new Handler();
                        handler.postDelayed(r, 4000);
                    }
                }
            }

        });


        mTimeText = (TextView) findViewById(R.id.time_text);
        timeThread=new Thread(new Runnable() {
            @Override
            public void run() {
                while ((mGame.getmGameStatus() != GameStatus.WON) && (mGame.getmGameStatus() != GameStatus.LOST)) {
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

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d("Service Connection", "bound to service");
                mBinder = (RotateService.RotateBinder)service;
                mBinder.registerListener(GameActivity.this);
                Log.d("Service Connection", "registered as listener");
                isBound = true;
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
                isBound = false;
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        // connect:
        mGoogleApiClient.connect();
        // bind service:
        Intent intent = new Intent(this, RotateService.class);
        Log.d("On start", "binding to service...");
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void startConclusionActivity(){
        Intent i = new Intent(GameActivity.this, ScoreActivity.class);
        Bundle b= new Bundle();
        b.putSerializable("won_lost", mGame.getmGameStatus());
        b.putInt("timekey", time);
        if(mShouldUpdateDatabase){
            b.putCharSequence("address_key", mAddressOutput);
            b.putCharSequence("level_key", mGame.getLevel().toString());
        }
        i.putExtra("m_bundle2",b);
        startActivity(i);
        finish();
    }
    @Override
    protected void onStop() {
        super.onStop();
        // disconnect:
        mGoogleApiClient.disconnect();
        // unbind service:
        if (isBound) {
            unbindService(mConnection);
            isBound = false;
        }
        mGame.exit();
    }

    @Override
    public void thereIsHighAngleDeviation() {
        if(mGame.getmGameStatus() != GameStatus.LOST && mGame.getmGameStatus() != GameStatus.WON){
            mGame.punishPlayer();
            ((TileAdapter)mGrid.getAdapter()).notifyDataSetChanged();
            mMinesRemainsText.setText("remaining mines: " + mGame.getMinesLeft());
            Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibe.vibrate(600);
            final Toast toast = Toast.makeText(getApplicationContext(), "ADDING MINES!", Toast.LENGTH_SHORT);
            toast.show();
            Handler toastHandler = new Handler();
            toastHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.cancel();
                }
            }, 1000);
        }
    }




    private void activateLoseAnimation() {
        Log.e("----------","Lose Animation...");
        Random rnd = new Random();
        final float[] fallingRate = new float[mGrid.getCount()];
        for(int i = 0; i < mGrid.getCount(); i++) {
            fallingRate[i] = rnd.nextFloat()/5 + 1;
        }
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        final float screenHeight = displaymetrics.heightPixels;
        ValueAnimator fallingAnimator = ValueAnimator.ofFloat(0, screenHeight);
        fallingAnimator.setInterpolator(new LinearInterpolator());
        fallingAnimator.setDuration(5000L);
        fallingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                for(int i = 0; i < mGrid.getCount(); i++) {
                    TileView tile = (TileView) mGrid.getChildAt(i);
                    if(tile != null) {
                        tile.setTranslationY(value * fallingRate[i]);
                    }
                }
            }
        });

        fallingAnimator.start();

        if(mFinalTilePosition >= 0) {
            final TileView explodingTile = (TileView) mGrid.getChildAt(mFinalTilePosition);
            explodingTile.setElevation(30);
            final int frameLength = 130;
            final int numberOfFrames = 14;
            ValueAnimator explodeAnimation = ValueAnimator.ofFloat(0, frameLength*numberOfFrames);
            explodeAnimation.setDuration(frameLength*numberOfFrames);
            explodeAnimation.setInterpolator(new LinearInterpolator());
            explodeAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    if (value < frameLength) explodingTile.setBackgroundResource(R.drawable.explosion1);
                    else if (value < frameLength*2) explodingTile.setBackgroundResource(R.drawable.explosion2);
                    else if (value < frameLength*3) explodingTile.setBackgroundResource(R.drawable.explosion3);
                    else if (value < frameLength*4) explodingTile.setBackgroundResource(R.drawable.explosion4);
                    else if (value < frameLength*5) explodingTile.setBackgroundResource(R.drawable.explosion5);
                    else if (value < frameLength*6) explodingTile.setBackgroundResource(R.drawable.explosion6);
                    else if (value < frameLength*7) explodingTile.setBackgroundResource(R.drawable.explosion7);
                    else if (value < frameLength*8) explodingTile.setBackgroundResource(R.drawable.explosion8);
                    else if (value < frameLength*9) explodingTile.setBackgroundResource(R.drawable.explosion9);
                    else if (value < frameLength*10) explodingTile.setBackgroundResource(R.drawable.explosion10);
                    else if (value < frameLength*11) explodingTile.setBackgroundResource(R.drawable.explosion11);
                    else if (value < frameLength*12) explodingTile.setBackgroundResource(R.drawable.explosion12);
                    else if (value < frameLength*13) explodingTile.setBackgroundResource(R.drawable.explosion13);
                    else explodingTile.setBackgroundResource(R.drawable.explosion14);
                }
            });
            explodeAnimation.start();
        }

    }


    private void activateWinAnimation() {
        TextView wonText = (TextView)findViewById(R.id.won_text);
        wonText.setText("YOU WON");

        final View top = findViewById(R.id.topLayout);
        ValueAnimator lightsOutAnimation = ValueAnimator.ofFloat(1,0);
        lightsOutAnimation.setDuration(5000L);
        lightsOutAnimation.setInterpolator(new AccelerateInterpolator());
        lightsOutAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mGrid.setAlpha(value);
                top.setAlpha(value);
            }
        });
        lightsOutAnimation.start();
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) !=
                PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    Integer.parseInt(android.Manifest.permission.ACCESS_COARSE_LOCATION));
        }
        mFinalLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mFinalLocation != null) {
            if (!Geocoder.isPresent()) {
                return;
            }
            if (mAddressRequested) {
                startIntentService();
            }
        }
    }

    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        mResultReceiver = new AddressResultReceiver(new Handler());
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mFinalLocation);
        startService(intent);
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            if (resultCode == Constants.SUCCESS_RESULT) {
                mIntentServiceNoLongerNeeded = true;
                if(mAnimationsAreFinished)
                    startConclusionActivity();
            }
        }
    }

}