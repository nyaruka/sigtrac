package com.nyaruka.sigtrac.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class DinTextView extends TextView {

    public DinTextView(Context context) {
        super(context);
        if(!isInEditMode()) {
            setFont();
        }
    }

    public DinTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if(!isInEditMode()) {
            setFont();
        }
    }

    public DinTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if(!isInEditMode()) {
            setFont();
        }
    }

    private void setFont() {
        Typeface din = Typeface.createFromAsset(getContext().getAssets(), "fonts/din.ttf");
        setTypeface(din);
    }
}
