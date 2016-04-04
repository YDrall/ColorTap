package com.no.donttap;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class GameActivity extends Activity {

    private static final String Tag="GameEngine";

    // To store game high score
    private SharedPreferences gameData;

    // key for high score (sharePreferences)
    private  static final String gameDataKey="highScore";

    //represents number of row in game grid
    private final int gridColumnCount=2;

    //to store width of device
    public static int deviceWidth;

    //to store height of device
    public static int deviceHeight;

    //denotes number of stripes (rectangular blocks in game grid
    private int stripeCount=6;

    // true if you unable a timeout occurs
    private boolean timeOut=false;

    //denotes how fast color will be inflated to each stripe i.e:game speed(Time in milliseconds)
    private int inflateTime =600;

    // denotes time to wait on a strip before raising a timeOut( time in milliseconds)
    private int waitTime=900;

    //game score
    public volatile int tapCount=0;

    //false on gameOver
    private Boolean life = true;

    //this array is used to select any random stripe on game grid(only inactive ones)
    public volatile int[] random;

    //represents pivot position in #random array
    public volatile int randomLast=stripeCount-1;

    // StripeView array
    public Stripes[] stripe=new Stripes[stripeCount];

    // index of culprit stripe in #strip array
    private int failedIndex=-1;

    //Color bank
    private final int[] colorArray= {Color.rgb(244, 67, 54),Color.rgb(63, 81, 181),Color.rgb(76, 175, 80),Color.rgb(255, 193, 7),
            Color.rgb(255, 87, 34),Color.rgb(121, 85, 72),Color.rgb(66, 66, 66),Color.rgb(250, 250, 250)};

    // total number of colors (in #colorArray)
    private final int totalColorCount=8;

    // number of friend colors (starting at 0 ... n, 2 means 0,1,2 => 3)
    public int friendColorCount=2;

    private int colorPivot=totalColorCount-1;

    public InflaterThread inflaterThread;

    private RelativeLayout parentLayout;
    public StatusView statusView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //Turn of window title
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.content_game);


        initDimensions();
        /* starting game in the beginning without showing any splash screen or home screen
            because i am not in mood of creating in any fancy designing stuff...*/
        initGameGrid();

    }

    @Override
    public void onPause()
    {
        super.onPause();
        life=false;
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }


/*
* */
    private void initDimensions() {
        parentLayout= (RelativeLayout) findViewById(R.id.parent);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        deviceWidth=displayMetrics.widthPixels;
        deviceHeight=displayMetrics.heightPixels;
    }

    private void initGameGrid()
    {
        parentLayout.removeAllViews();
        setGameParameters();
        addStripes();
        prepareStatusView();
        tapToStartScreen();
    }

    private void prepareStatusView() {
        statusView = new StatusView(this);
        RelativeLayout.LayoutParams sv= new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sv.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
        sv.topMargin=10;
        statusView.setLayoutParams(sv);
        parentLayout.addView(statusView);
    }


    private void setGameParameters()
    {
        random= new int[]{0,1,2,3,4,5};
        tapCount=0;
        life = true;
        stripeCount=6;
        randomLast=stripeCount-1;
        friendColorCount=2;
        colorPivot=totalColorCount-1;
        timeOut=false;
        failedIndex=-1;
        statusView=null;
        makeFriendsAndEnemies();
    }

    private void makeFriendsAndEnemies()
    {
        int random,temp;
        for(int i=0;i<=friendColorCount;i++)
        {
            random=(int)(Math.random()*colorPivot);

            temp=colorArray[random];
            colorArray[random]=colorArray[colorPivot];
            colorArray[colorPivot]=temp;
            colorPivot--;
        }
    }

    private void addStripes() {
        GridLayout gridLayout=new GridLayout(this);
        RelativeLayout.LayoutParams gl=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        gridLayout.setColumnCount(gridColumnCount);
        gridLayout.setLayoutParams(gl);

        parentLayout.addView(gridLayout);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(deviceWidth/2,deviceHeight/3);
        Log.i(Tag,"addStripes called");

        for (int i = 0; i < stripeCount; i++) {
            stripe[i] = new Stripes(this,i);
            stripe[i].setLayoutParams(layoutParams);
            gridLayout.addView(stripe[i]);
        }

        addColors();
    }

    private void addColors()
    {
        int increment=0;
        for (int i = 0; i < stripeCount; i++) {
            if (increment <= friendColorCount) {
                stripe[i].stripeColor=colorArray[colorPivot + increment + 1];
                stripe[i].setBackgroundColor(stripe[i].stripeColor);

                increment++;
            } else {
                increment = 0;
                stripe[i].stripeColor=colorArray[colorPivot + increment + 1];
                stripe[i].setBackgroundColor(stripe[i].stripeColor);
                increment++;
            }
        }
    }

    private void tapToStartScreen()
    {
        ImageView semiBack = new ImageView(this);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        semiBack.setLayoutParams(lp);
        semiBack.setAlpha(0.0f);
        parentLayout.addView(semiBack);
        final TextView tap= new TextView(this);
        RelativeLayout.LayoutParams tp=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tp.topMargin=deviceHeight/2;
        tp.leftMargin= deviceWidth/3;
        tap.setLayoutParams(tp);
        tap.setText("TAP TO START..");
        tap.setTextColor(Color.WHITE);
        tap.setBackgroundColor(Color.BLACK);
        parentLayout.addView(tap);
        semiBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.GONE);
                tap.setVisibility(View.GONE);
                startInflatingColors();
            }
        });
    }




    public void startInflatingColors()
    {
        inflaterThread=new InflaterThread(this);
        inflaterThread.start();

    }

    public void gameOverScreen()
    {
        RelativeLayout readyBack = new RelativeLayout(this);
        RelativeLayout.LayoutParams rl=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        readyBack.setLayoutParams(rl);
        readyBack.setBackgroundColor(Color.TRANSPARENT);
        parentLayout.addView(readyBack);

        //Game Over
        //retry button
        //Home Button
        //score Field
        //Best Score Field



        TextView gameOver=new TextView(this);
        LinearLayout.LayoutParams go=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        go.topMargin=90;
        gameOver.setGravity(Gravity.CENTER);
        gameOver.setTextSize(TypedValue.COMPLEX_UNIT_DIP,50);
        if(timeOut)
            gameOver.setText("Too late");
        else
            gameOver.setText("Wrong color");
        // gameOver.setText(" Game Over");
        gameOver.setTextColor(Color.WHITE);
        gameOver.setBackgroundColor(Color.BLACK);
        gameOver.setTypeface(Typeface.SERIF);
        gameOver.setLayoutParams(go);
        readyBack.addView(gameOver);


        GridLayout scoreBoard= new GridLayout(this);
        RelativeLayout.LayoutParams sb= new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sb.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE);
        scoreBoard.setColumnCount(2);
        scoreBoard.setLayoutParams(sb);
        readyBack.addView(scoreBoard);

        TextView scoreLabel = new TextView(this);
        RelativeLayout.LayoutParams sl=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        scoreLabel.setText("Score: ");
        scoreLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP,40);
        scoreLabel.setLayoutParams(sl);
        scoreBoard.addView(scoreLabel);

        TextView score=new TextView(this);
        RelativeLayout.LayoutParams tp=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        score.setTextSize(TypedValue.COMPLEX_UNIT_DIP,40);
        score.setText(String.valueOf(tapCount));
        score.setLayoutParams(tp);
        scoreBoard.addView(score);

        TextView hscoreLabel= new TextView(this);
        RelativeLayout.LayoutParams hsl=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hscoreLabel.setText("Best:");
        hscoreLabel.setLayoutParams(hsl);
        hscoreLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP,40);
        scoreBoard.addView(hscoreLabel);

        TextView hscore= new TextView(this);
        RelativeLayout.LayoutParams hs=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        gameData = getSharedPreferences(gameDataKey,Context.MODE_PRIVATE);
        hscore.setText(Integer.toString(gameData.getInt(gameDataKey,0)));
        hscore.setLayoutParams(hs);
        hscore.setTextSize(TypedValue.COMPLEX_UNIT_DIP,40);
        scoreBoard.addView(hscore);

        Button retry= new Button(this);
        RelativeLayout.LayoutParams rt=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rt.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
        rt.topMargin=3*deviceHeight/4;
        rt.rightMargin=deviceWidth/2;
        retry.setLayoutParams(rt);
        retry.setText("Retry");
        retry.setBackgroundColor(Color.BLACK);
        retry.setTextSize(TypedValue.COMPLEX_UNIT_DIP,40);
        retry.setTextColor(Color.WHITE);
        retry.setBackgroundResource(R.drawable.button);
        readyBack.addView(retry);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnimatorSet animatorSet=new AnimatorSet();
                ObjectAnimator animatecolor= ObjectAnimator.ofObject(v,"backgroundColor",new ArgbEvaluator(),Color.BLACK,Color.RED);
                animatorSet.setDuration(150);
                animatorSet.playTogether(animatecolor);
                animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorSet.start();
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        initGameGrid();
                    }
                });

            }
        });

    }




    public synchronized int[] selectRandom()
    {
        if(randomLast>0){
            int randomPosition;
            randomPosition= (int) (Math.random()*randomLast+1);

            swap(randomPosition,randomLast);
            randomLast--;
            return (new int[]{random[randomLast+1], (randomLast + 1)});
        }
        return new int[]{-1};
    }

    public synchronized void pushToSafeList(int arrayIndex,int stripeIndex)
    {
        randomLast++;
        stripe[randomLast].arrayIndex=arrayIndex;
        swap(arrayIndex,randomLast);
    }

    private void swap(int a, int b) {
        int temp=random[a];
        random[a]=random[b];
        random[b]=temp;
    }

    public synchronized boolean hasLife()
    {
        return life;
    }

    public synchronized void makeLifeShort()
    {
        life=false;
    }

    public boolean setFailedIndex(int index) {
        if(failedIndex<0)
        {
            failedIndex = index;
            return true;
        }
        return false;
    }

    public void finishGameSession() {
        gameData = getSharedPreferences(gameDataKey,Context.MODE_PRIVATE);
        if(tapCount>gameData.getInt(gameDataKey,0))
        {
            SharedPreferences.Editor editor= gameData.edit();
            editor.putInt(gameDataKey,tapCount);
            editor.apply();
        }
        if(timeOut)
        {
            statusView.showGameOver(stripe[failedIndex].stripeColor);
        }
        AnimateView av=new AnimateView(this,stripe[failedIndex]);
        RelativeLayout.LayoutParams rt=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        av.setLayoutParams(rt);
        parentLayout.addView(av);
        av.blink();
    }

    public int getAFriend() {return colorArray[((int)(Math.random()*(friendColorCount+1))+colorPivot+1)];}

    public int getAnEnemy() { return colorArray[(int)(Math.random()*(colorPivot+1))];}


    /* *Handling inflation of color to random stripes
   /   and waits for @inflateTime milliseconds
   /   also create threads and each of these threads monitors a single stripes
    */
    private class InflaterThread extends Thread {
        GameActivity gameActivity;
        public InflaterThread(GameActivity gameActivity)
        {
            this.gameActivity=gameActivity;
        }
        @Override
        public void run()
        {
            while(gameActivity.life)
            {

                final int[] i=selectRandom();
                if(i[0]>=0)
                {
                    //stripe[i[0]].isEnemy=(int)(Math.random()*10)<6;
                    if(((int)(Math.random()*10)<6)) {
                        Log.i(Tag,"A new enemy"+Integer.toString(stripe[i[0]].getIndex())+" "+Integer.toString(stripe[i[0]].arrayIndex));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    stripe[i[0]].setActive();
                                }
                            });

                        stripe[i[0]].arrayIndex = i[1];
                        MonitorThread m = new MonitorThread(gameActivity, stripe[i[0]].getIndex());
                        stripe[i[0]].thread_id = m.getId();
                        m.start();
                    }
                    else {
                        Log.i(Tag,"A new freind"+Integer.toString(stripe[i[0]].getIndex()));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    stripe[i[0]].arrayIndex = i[1];//array index should be changed before setunactive().
                                    stripe[i[0]].setUnActive();

                                }
                            });
                    }
                    try {
                            Thread.sleep(inflateTime);
                        } catch (InterruptedException e) {
                            Log.i(Tag,"Thread interrupted");
                        }
                    }

                else{
                    gameActivity.makeLifeShort();
                }

            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finishGameSession();
                }
            });
        }
    }

    /*Monitors if an active view is touched or not
    / It waits for @waitTime and then alter the view on UIThread
    */
    private class MonitorThread extends Thread {

        private GameActivity gameActivity;
        private int index;

        public MonitorThread(GameActivity ga,int index)
        {
            this.gameActivity=ga;
            this.index=index;
        }
        @Override
        public void run()
        {
            Log.i(Tag,"Waiting on Stripenumber:"+Integer.toString(index));
            try {
                Thread.sleep(gameActivity.waitTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            if(gameActivity.stripe[index].active && this.getId()==stripe[index].thread_id && gameActivity.hasLife())
            {
                Log.i(Tag,"timeout on Stripenumber"+Integer.toString(index));
                gameActivity.makeLifeShort();
                Log.i(Tag,"makeLifeShort() on Stripenumber"+Integer.toString(index));
                if(gameActivity.setFailedIndex(index))
                {
                    gameActivity.timeOut = true;
                    failedIndex = index;
                    Log.i(Tag,"failed index Stripenumber"+Integer.toString(index));
                }
            }

            }
        }
}


