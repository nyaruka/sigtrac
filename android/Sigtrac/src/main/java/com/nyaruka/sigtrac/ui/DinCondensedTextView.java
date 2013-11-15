package com.nyaruka.sigtrac.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class DinCondensedTextView extends TextView {

    public DinCondensedTextView(Context context) {
        super(context);
        if(!isInEditMode()) {
            setFont();
        }

    }


    public DinCondensedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if(!isInEditMode()) {
            setFont();
        }
    }

    public DinCondensedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if(!isInEditMode()) {
            setFont();
        }
    }

    private void setFont() {
        Typeface dinCondensed = Typeface.createFromAsset(getContext().getAssets(), "fonts/din_condensed.ttf");
        setTypeface(dinCondensed);
    }
}
