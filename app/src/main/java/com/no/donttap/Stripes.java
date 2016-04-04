package com.no.donttap;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;


public class Stripes extends View implements View.OnClickListener{

    private static final String Tag="Stripe";
    private final GameActivity game;
    public volatile Boolean  active=false;
    private int stripeIndex;
    public int arrayIndex;
    public long thread_id;
    public int stripeColor;

    public Stripes(GameActivity gameActivity,int StripeIndex) {
        super(gameActivity);
        this.game=gameActivity;
        this.stripeIndex = StripeIndex;
        setOnClickListener(this);
        Log.i(Tag,"Stripe created stripeindex:"+Integer.toString(stripeIndex));
    }
    @Override
    public void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
    }


    @Override
    public void onClick(View v)
    {
        if(game.hasLife()) {
            if (this.active) {
                Log.i(Tag,"Stripe clicked stripeindex:"+Integer.toString(stripeIndex));
                this.game.statusView.showPlusOne(this.stripeColor);
                this.setUnActive();
                this.game.tapCount++;
            } else {
                this.game.makeLifeShort();
                Log.i(Tag,"makeLifeShort() on Stripenumber"+Integer.toString(stripeIndex));
                this.game.statusView.showWrongTap(this.stripeColor);
                game.setFailedIndex(this.stripeIndex);

            }
        }
    }

    public synchronized void setActive()
    {
        Log.i(Tag,"Stripe Activated stripeindex:"+Integer.toString(stripeIndex));
        this.active = true;
        this.stripeColor=game.getAnEnemy();
        this.setBackgroundColor(this.stripeColor);
    }

    public synchronized void setUnActive()
    {
        Log.i(Tag,"Stripe unactivated stripeindex:"+Integer.toString(stripeIndex));


        game.pushToSafeList(arrayIndex,stripeIndex);

        this.active = false;
        this.stripeColor = game.getAFriend();
        this.setBackgroundColor(this.stripeColor);


    }

    private void color_animate()
    {
        ObjectAnimator animate= ObjectAnimator.ofObject(this,"backgroundColor",new ArgbEvaluator(),Color.BLACK,this.stripeColor);
        animate.setInterpolator(new LinearInterpolator());
        animate.setDuration(40);
        animate.start();
    }

    public int getIndex()
    {
        return stripeIndex;
    }
}
