package com.no.donttap;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

public class AnimateView extends View{

    private GameActivity game;
    private Stripes failedStripe;

    private Paint p;
    private int radius;
    private float x=0;
    private float y=0;


    public AnimateView(GameActivity game,Stripes failedStripe) {
        super(game);
        this.game=game;
        this.failedStripe=failedStripe;
        init();
    }


    @Override
    public void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        canvas.drawCircle(x,y,radius,p);
    }

    private void init()
    {
        radius=GameActivity.deviceHeight/6;
        p=new Paint();
        p.setColor(Color.TRANSPARENT);

        y=GameActivity.deviceHeight/6 + (failedStripe.getIndex() /2)*GameActivity.deviceHeight/3;
        x=GameActivity.deviceWidth/4 + (failedStripe.getIndex() %2)*GameActivity.deviceWidth/2;
    }

    public void startAnimation()
    {
        Log.i("animate","animation started at: "+Integer.toString(failedStripe.getIndex()));
        ValueAnimator vl=ValueAnimator.ofInt(radius,(int)(1.5*GameActivity.deviceHeight));
        vl.setInterpolator(new AccelerateDecelerateInterpolator());
        vl.setDuration(200);
        vl.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                radius+=60;

                invalidate();
            }
        });
        vl.start();
        vl.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setBackgroundColor(failedStripe.stripeColor);

            }
        });

    }

    public void blink()
    {

        ObjectAnimator animate= ObjectAnimator.ofObject(failedStripe,"backgroundColor",new ArgbEvaluator(), Color.RED,failedStripe.stripeColor);
        animate.setInterpolator(new LinearInterpolator());
        animate.setRepeatMode(ValueAnimator.RESTART);
        animate.setRepeatCount(4);
        animate.setDuration(300);
        animate.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                p.setColor(failedStripe.stripeColor);
                startAnimation();
                game.gameOverScreen();
            }
        });
        animate.start();


    }

}
