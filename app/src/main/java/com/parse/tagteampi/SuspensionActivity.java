package com.parse.tagteampi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

import com.parse.ParseUser;


/**
 * PiTag: Suspension List
 * Activated when a user is placed on the suspension list after failing to push an update to
 * Parse. Starts a 60-second countdown, which the suspended user can interrupt by pushing
 * a successful update. If time expires, suspended user is removed from the active game.
 */
public class SuspensionActivity extends Activity {

    private CountDownTimer timer;
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suspension);

        text = (TextView) findViewById(R.id.suspendTextView);

        timer = new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                text.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                text.setText("done!");
            }
        }.start();
    }
}
