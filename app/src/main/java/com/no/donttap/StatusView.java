package com.no.donttap;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;


public class StatusView extends TextView{

    private AnimatorSet animatorSet;


    public StatusView(Context context) {
        super(context);
        init();
    }



    public StatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StatusView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init() {
        animatorSet=new AnimatorSet();
        this.setBackgroundResource(R.drawable.statusback);
        this.setTextSize(TypedValue.COMPLEX_UNIT_DIP,30);
        this.setAlpha(0);
        this.setGravity(Gravity.CENTER);
    }

    public void showPlusOne(int stripeColor)
    {
        this.setText("+1");
        changeBackground(stripeColor);
        ObjectAnimator objectAnimator=ObjectAnimator.ofFloat(this,ALPHA,1.0f,0.0f);
        startAnimation(1500,objectAnimator);
    }

    public void showGameOver(int stripeColor)
    {
        this.setText("Too late!");
        changeBackground(stripeColor);
        ObjectAnimator objectAnimator=ObjectAnimator.ofFloat(this,ALPHA,1.0f,0.0f);
        startAnimation(1500,objectAnimator);
    }

    public void expand() {
        ObjectAnimator scaleX=ObjectAnimator.ofFloat(this,SCALE_X,1.0f,1.2f);
        ObjectAnimator scaleY=ObjectAnimator.ofFloat(this,SCALE_Y,1.0f,1.2f);
        startAnimation(700,scaleX,scaleY);
    }

    public void showWrongTap(int stripeColor) {
        this.setText("Wrong Color");
        changeBackground(stripeColor);
        ObjectAnimator objectAnimator=ObjectAnimator.ofFloat(this,ALPHA,1.0f,0.0f);
        startAnimation(1500,objectAnimator);
    }

    private void startAnimation(long time, ObjectAnimator... items)
    {
       // Log.i("AnimateStatus","animate alpha called");
        if(animatorSet.isRunning()) {
            animatorSet.cancel();
        }

        animatorSet.setDuration(time);
        animatorSet.playTogether(items);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }

    public void changeBackground(int color)
    {
        GradientDrawable gd= (GradientDrawable) this.getBackground();
        gd.setColor(color);

        if(color== Color.WHITE || color== Color.rgb(222,255,0))
        {
            this.setTextColor(Color.BLACK);
        }
        else
        {
            this.setTextColor(Color.WHITE);
        }

        this.setAlpha(1);
    }



}
