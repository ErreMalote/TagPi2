package com.parse.tagteampi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.RadioButton;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class SuspensionTesterActivity extends Activity{

    // GPS
    protected LocationManager locMan;
    protected Location location;
    // Parse
    protected TagGame gameData;
    protected TagPlayer playerData;
    // Layout
    protected TextView text1;
    protected RadioButton radio;
    // Game Tick
    protected Handler tickHandler = new Handler();
    protected Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {

            // Shows that Game Ticks are running steadily
            if (radio.isSelected()) {
                radio.setText("Tock");
                radio.setSelected(false);
            } else {
                radio.setText("Tick");
                radio.setSelected(true);
            }

            // Game Tick functionality goes here

            if (radio.getText().equals("Tick")) {   // every 2 seconds
                // Update player location
                location = locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location == null) {
                    // Error case - suspension time!


                }
                playerData.setLocation(new ParseGeoPoint(location.getLatitude(), location.getLongitude()));
                playerData.saveInBackground();

                // Get new data for all players in current game
                ParseQuery<TagPlayer> query = ParseQuery.getQuery("Player");
                query.whereEqualTo("gameId", gameData.getObjectId());
                query.findInBackground(new FindCallback<TagPlayer>() {
                    public void done(List<TagPlayer> tp, ParseException e) {
                        if (e == null) {
                            if (playerData.isItt()) {
                                for (int i = 0; i < tp.size(); i++) {
                                    // do Itt player stuff
                                }
                            } else {
                                for (int i = 0; i < tp.size(); i++) {
                                    // do NotItt player stuff
                                }
                            }
                        } else {
                            // Error case - suspension time!
                        }
                    }
                });

            }




            tickHandler.postDelayed(this, 1000);
        }
    };

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suspension_tester);

        // Initialize layout
        text1 = (TextView) findViewById(R.id.suspensionTestText1);
        radio = (RadioButton) findViewById(R.id.suspensionTestRadio);

        // Initialize GeoLocation information
        locMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        location = locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        // Register and Initialize TagGame to create a Game on Parse
        ParseObject.registerSubclass(TagGame.class);
        gameData = new TagGame();
        gameData.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e != null) {
                    new AlertDialog.Builder(SuspensionTesterActivity.this)
                            .setTitle("GameBuiild Error")
                            .setMessage(e.toString())
                            .setPositiveButton("OK", null)
                            .show();
                }
            }
        });

        // Wait for game to be created so player doesn't try to use a null value
        while (gameData.getObjectId() == null){
            // Here we sit until gameData gets its act together
        }

        // Register and Initialize TagPlayer to create a Player on Parse
        ParseObject.registerSubclass(TagPlayer.class);

        while (location == null) {
            //Retry
            location = locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        playerData = new TagPlayer();
        playerData.setPlayer(ParseUser.getCurrentUser());
        playerData.setGame(gameData.getObjectId());
        playerData.setLocation(new ParseGeoPoint(location.getLatitude(), location.getLongitude()));
        playerData.setTagCount(0);
        playerData.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e != null) {
                    new AlertDialog.Builder(SuspensionTesterActivity.this)
                            .setTitle("PlayerBuild Error")
                            .setMessage(e.toString())
                            .setPositiveButton("OK", null)
                            .show();
                }
            }
        });

        text1.setText("You're in a game!");
        radio.setText("Tick");

        tickHandler.postDelayed(tickRunnable, 1000);




    }

}