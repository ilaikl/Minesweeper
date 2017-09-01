package com.example.ilai.minesweeper;

/**
 * Created by Ilai on 21/08/2017.
 */
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

public class TileView extends FrameLayout {

    public TextView text;

    public TileView(Context context) {
        super(context);

        setBackgroundResource(R.drawable.unexposed_tile);
        text = new TextView(context);

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        text.setLayoutParams(layoutParams);

        text.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        text.setGravity(Gravity.CENTER_VERTICAL);
        text.setTextSize(21);
        text.setTextColor(Color.BLACK);

        this.addView(text);

    }
}
