
package com.parse.tagteampi;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.System.exit;

/**
 * Lobby Screen
 * Created on 11/20/2014
 * Hello.
 */
public class LobbyActivity extends Activity {

    private int GAME_RADIUS;
    private int TAG_RADIUS;
    private int TAG_LIMIT;
    private int MINUTES;
    private long USER_ID;
    private boolean joinable;
    private ArrayList<String> bogey;

    Button gameStart;
    Button gameLeave;
    ListView players;
    int timeCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        //retrieve all set values from the game settings screen and display rules to the player
        //GAME_RADIUS = this.getIntent().getExtras().getInt("GAME_RADIUS");
        //TAG_RADIUS = this.getIntent().getExtras().getInt("TAG_RADIUS");
        //TAG_LIMIT = this.getIntent().getExtras().getInt("TAG_LIMIT");
        //MINUTES = this.getIntent().getExtras().getInt("MINUTES");
        //----------------------temp bogey array--------------------------//
        bogey = new ArrayList<String>();
        bogey.add("Ron");
        bogey.add("Paul");
        bogey.add("Kuntakente");
        bogey.add("Toby");
        //-----------------------------//
        TextView gameRadius = (TextView) findViewById(R.id.gameRadius);
        gameRadius.setText(String.valueOf(GAME_RADIUS));
        TextView tagRadius = (TextView) findViewById(R.id.tagRadius);
        TextView tagLimit = (TextView) findViewById(R.id.tagLimit);
        TextView timeLimit = (TextView) findViewById(R.id.timeLimit);
        tagRadius.setText(String.valueOf(TAG_RADIUS));
        tagLimit.setText(String.valueOf(TAG_LIMIT));
        timeLimit.setText(String.valueOf(MINUTES) + " mins");
        //-----------Set up the ListView to populate by the AL-----------//
        players = (ListView) findViewById(R.id.playerList);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                bogey
        );
        players.setAdapter(adapter);


        gameLeave = (Button) findViewById(R.id.exitButton);
        gameStart = (Button) findViewById(R.id.startButton);

        gameStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LobbyActivity.this, "Coming soon(TM)!", Toast.LENGTH_SHORT).show();
                //TODO: insert the check to count the current player count, configure Toast
                //TODO: change PostActivity to GameActivity when ready
            }
        });

        gameLeave.setOnClickListener(new View.OnClickListener() {
            //TODO: make sure this button acts like the back button, or remove entirely
            @Override
            public void onClick(View v) {
                //TODO: Change the destination class to the main screen
                exit(0);
            }
        });

        //If this section breaks the app, just comment it out.  Otherwise yay!
        /*Looper.prepare();
        //Having fun with timed intervals
        int interval = 2000; //2000 milliseconds
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                Toast.makeText(LobbyActivity.this, "You've been here for " + String.valueOf(timeCount) + " seconds.", Toast.LENGTH_SHORT).show();
                //java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()
            }
        }, 0delay of zero, interval);
    }



    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        menu.findItem(R.id.action_settings).setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(LobbyActivity.this, SettingsActivity.class));
                return true;
            }
        });
        return true;
    }
*/
    }
}