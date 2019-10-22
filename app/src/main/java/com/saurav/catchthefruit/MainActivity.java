package com.saurav.catchthefruit;

import android.content.Intent;
import android.graphics.Point;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    private TextView scoreLabel;
    private TextView startLabel;
    private ImageView bird;
    private ImageView orange;
    private ImageView pink;
    private ImageView bullet;

    // Size
    private int frameHeight;
    private int birdSize;
    private int screenWidth;
    private int screenHeight;

    // Position
    private int birdY;
    private int orangeX;
    private int orangeY;
    private int pinkX;
    private int pinkY;
    private int bulletX;
    private int bulletY;

    // Speed
    private int birdSpeed;
    private int orangeSpeed;
    private int pinkSpeed;
    private int bulletSpeed;

    // Score
    private int score = 0;

    // Initialize Class
    private Handler handler = new Handler();
    private Timer timer = new Timer();
    private SoundPlayer sound;


    // Status Check
    private boolean action_flg = false;
    private boolean start_flg = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sound = new SoundPlayer(this);

        scoreLabel = (TextView) findViewById(R.id.scoreLabel);
        startLabel = (TextView) findViewById(R.id.startLabel);
        bird = (ImageView) findViewById(R.id.bird);
        orange = (ImageView) findViewById(R.id.orange);
        pink = (ImageView) findViewById(R.id.pink);
        bullet = (ImageView) findViewById(R.id.bullet);


        // Get screen size.
        WindowManager wm = getWindowManager();
        Display disp = wm.getDefaultDisplay();
        Point size = new Point();
        disp.getSize(size);

        screenWidth = size.x;
        screenHeight = size.y;

        // Now
        // Nexus4 Width: 768 Height:1280
        // Speed bird:20 Orange:12 Pink:20 bullet:16

        birdSpeed = Math.round(screenHeight / 55);  // 1280 / 60 = 21.333... => 21
        orangeSpeed = Math.round(screenWidth / 60); // 768 / 60 = 12.8 => 13
        pinkSpeed = Math.round(screenWidth / 45);   // 768 / 36 = 21.333... => 21
        bulletSpeed = Math.round(screenWidth / 65); // 768 / 45 = 17.06... => 17

        //Log.v("SPEED_bird", birdSpeed + "");
        //Log.v("SPEED_ORANGE", orangeSpeed + "");
        //Log.v("SPEED_PINK", pinkSpeed + "");
        //Log.v("SPEED_bullet", bulletSpeed + "");


        // Move to out of screen.
        orange.setX(-80);
        orange.setY(-80);
        pink.setX(-80);
        pink.setY(-80);
        bullet.setX(-70);
        bullet.setY(-70);

        scoreLabel.setText("Score : 0");


    }


    public void changePos() {

        hitCheck();

        // Orange
        orangeX -= orangeSpeed;
        if (orangeX < 0) {
            orangeX = screenWidth + 20;
            orangeY = (int) Math.floor(Math.random() * (frameHeight - orange.getHeight()));
        }
        orange.setX(orangeX);
        orange.setY(orangeY);


        // bullet
        bulletX -= bulletSpeed;
        if (bulletX < 0) {
            bulletX = screenWidth + 10;
            bulletY = (int) Math.floor(Math.random() * (frameHeight - bullet.getHeight()));
        }
        bullet.setX(bulletX);
        bullet.setY(bulletY);


        // Pink
        pinkX -= pinkSpeed;
        if (pinkX < 0) {
            pinkX = screenWidth + 5000;
            pinkY = (int) Math.floor(Math.random() * (frameHeight - pink.getHeight()));
        }
        pink.setX(pinkX);
        pink.setY(pinkY);


        // Move bird
        if (action_flg == true) {
            // Touching
            birdY -= birdSpeed;

        } else {
            // Releasing
            birdY += birdSpeed;
        }

        // Check bird position.
        if (birdY < 0) birdY = 0;

        if (birdY > frameHeight - birdSize) birdY = frameHeight - birdSize;

        bird.setY(birdY);

        scoreLabel.setText("Score : " + score);

    }


    public void hitCheck() {

        // If the center of the ball is in the bird, it counts as a hit.

        // Orange
        int orangeCenterX = orangeX + orange.getWidth() / 2;
        int orangeCenterY = orangeY + orange.getHeight() / 2;

        // 0 <= orangeCenterX <= birdWidth
        // birdY <= orangeCenterY <= birdY + birdHeight

        if (0 <= orangeCenterX && orangeCenterX <= birdSize &&
                birdY <= orangeCenterY && orangeCenterY <= birdY + birdSize) {

            score += 10;
            orangeX = -10;
            sound.playHitSound();

        }

        // Pink
        int pinkCenterX = pinkX + pink.getWidth() / 2;
        int pinkCenterY = pinkY + pink.getHeight() / 2;

        if (0 <= pinkCenterX && pinkCenterX <= birdSize &&
                birdY <= pinkCenterY && pinkCenterY <= birdY + birdSize) {

            score += 30;
            pinkX = -10;
            sound.playHitSound();

        }

        // bullet
        int bulletCenterX = bulletX + bullet.getWidth() / 2;
        int bulletCenterY = bulletY + bullet.getHeight() / 2;

        if (0 <= bulletCenterX && bulletCenterX <= birdSize &&
                birdY <= bulletCenterY && bulletCenterY <= birdY + birdSize) {

            // Stop Timer!!
            timer.cancel();
            timer = null;

            sound.playOverSound();

            // Show Result
            Intent intent = new Intent(getApplicationContext(), Result.class);
            intent.putExtra("SCORE", score);
            startActivity(intent);

        }

    }


    public boolean onTouchEvent(MotionEvent me) {

        if (start_flg == false) {

            start_flg = true;

            // Why get frame height and bird height here?
            // Because the UI has not been set on the screen in OnCreate()!!

            FrameLayout frame = (FrameLayout) findViewById(R.id.frame);
            frameHeight = frame.getHeight();
            birdY = (int)bird.getY();

            // The bird is a square.(height and width are the same.)
            birdSize = bird.getHeight();


            startLabel.setVisibility(View.GONE);

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            changePos();
                        }
                    });
                }
            }, 0, 20);


        } else {
            if (me.getAction() == MotionEvent.ACTION_DOWN) {
                action_flg = true;

            } else if (me.getAction() == MotionEvent.ACTION_UP) {
                action_flg = false;
            }
        }

        return true;
    }


    // Disable Return Button
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }

}