package com.hatchtrack.app;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;

class EggCountPicker extends NumberPicker {

    private int textColor = Color.BLACK;
    private int textSize = 16;
    private int min = 0;
    private int max = 10;
    private int defaultCount = 0;

    public EggCountPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        if(attrs != null) {
            TypedArray array = getContext().obtainStyledAttributes(
                    attrs,
                    R.styleable.EggCountPicker
            );
            this.textColor = array.getColor(R.styleable.EggCountPicker_textColor, Color.BLACK);
            this.textSize = array.getInt(R.styleable.EggCountPicker_textSizeSp, 16);
            this.min = array.getInt(R.styleable.EggCountPicker_min, 0);
            this.max = array.getInt(R.styleable.EggCountPicker_max, 10);
            this.defaultCount = array.getInt(R.styleable.EggCountPicker_eggs, 0);
            array.recycle();
        }
        this.setMinValue(this.min);
        this.setMaxValue(this.max);
        this.setValue(this.defaultCount);
    }

//    @Override
//    protected void onAttachedToWindow() {
//        super.onAttachedToWindow();
//        super.setMinValue(this.min);
//        super.setMaxValue(this.max);
//        invalidate();

//        super.setValue(this.defaultCount);
//    }

//    @Override
//    public int getValue() {
//        return(this.defaultCount);
//    }
//
//    @Override
//    public int getMinValue() {
//        return(this.min);
//    }
//
//    @Override
//    public int getMaxValue() {
//        return(this.max);
//    }

//    @Override
//    protected void onFinishInflate() {
//        super.onFinishInflate();
//        this.setMinValue(this.min);
//        this.setMaxValue(this.max);
////        this.setValue(this.defaultCount);
//    }

    @Override
    public void addView(View child) {
        super.addView(child);
        this.updateView(child);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        super.addView(child, params);
        this.updateView(child);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        this.updateView(child);
    }

    private void updateView(View view) {
        if(view instanceof EditText){
            ((EditText) view).setTextSize(this.textSize);
            ((EditText) view).setTextColor(this.textColor);
        }
    }
}
