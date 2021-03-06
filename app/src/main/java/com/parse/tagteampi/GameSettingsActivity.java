package com.parse.tagteampi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.lang.reflect.Field;
import java.util.List;


public class GameSettingsActivity extends Activity {
    private NumberPicker gameRadiusNumberPicker;
    private RadioGroup tagRadiusRadioGroup;
    private Spinner timeLimitSpinner;
    private RadioGroup tagLimitGroup;
    private RadioGroup avatarRadioGroup;
    private Button apply;
    //integer values for various uses
    private int GAME_RADIUS = 300;
    private int TAG_RADIUS = 10;
    private int TAG_LIMIT = 0;
    private int SECONDS = 0;
    private String AVATAR = "crab";
    private String PRINTED_MESSAGE;
    private int PICKER_RANGE = 50; //set for quick editing
    private String[] RADIUS_INTERVALS = new String[38];
    private String GAME_OBJECT_ID;


    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GAME_OBJECT_ID = this.getIntent().getExtras().getString("gameObjectId");
        setContentView(R.layout.activity_create_game);

        //Initialize the apply button, which currently pushes settings' values to the screen through Toast
        //TODO: Pass user info (user name, ID, etc.) through game settings to lobby activity through extra
        apply = (Button) findViewById(R.id.done_button);
        apply.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

//                PRINTED_MESSAGE = "The values as found:\nTag Distance: " + TAG_RADIUS
//                        + "\nGameplay Radius: " + GAME_RADIUS
//                        + "\nTag Limit: " + TAG_LIMIT
//                        + "\nTime limit: " + SECONDS
//                        + "\nAvatar: " + AVATAR;
//                Toast.makeText(GameSettingsActivity.this, PRINTED_MESSAGE, Toast.LENGTH_LONG).show();

                //LocationManager used to get the current location of the user.  Will be used for
                //setting the center of the game play area.
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Location lastLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (lastLocation != null) {

                    latitude = lastLocation.getLatitude();
                    longitude = lastLocation.getLongitude();

                    final ParseGeoPoint geoPoint = new ParseGeoPoint(latitude, longitude);

                    // CREATE NEW GAME: if no existing gameObjectId was passed from the Main Lobby
                    if (GAME_OBJECT_ID == null) {

                        //Creates Game parseObject for the current user to host a game.
                        final TagGame game = new TagGame();
                        game.setHostUser(ParseUser.getCurrentUser().getString("username"));
                        game.setGameDuration(SECONDS);
                        game.setMapRadius(Integer.parseInt(RADIUS_INTERVALS[gameRadiusNumberPicker.getValue()]));
                        game.setTagRadius(TAG_RADIUS);
                        game.setTagLimit(TAG_LIMIT);
                        game.setLocation(geoPoint);

                        game.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    //Saves the game settings data and game objectId to key that
                                    // can be accessed by the activity (InGameLobbyActivity??)
                                    // that follows.
                                    final String gameObjectId = game.getObjectId();
                                    final Intent toGame = new Intent(GameSettingsActivity.this,
                                            InGameActivity.class);
                                    toGame.putExtra("gameObjectId", gameObjectId);

                                    //Create Player parseObject and relate it to Game parseObject
                                    final TagPlayer player = new TagPlayer();
                                    player.setGame(gameObjectId);
                                    player.setPlayer(ParseUser.getCurrentUser().getString("username"));
                                    player.setLocation(geoPoint);
                                    player.setAvatar(TagPlayer.getAvatarNumber(AVATAR));
                                    player.setTagCount(0);

                                    player.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                //Saves the ActiveUsers objectId to keys that can be
                                                // accessed by the activity (InGameLobbyActivity??)
                                                // that follows.
                                                String playerObjectId = player.getObjectId();
                                                toGame.putExtra("playerObjectId", playerObjectId);
                                                startActivity(toGame);
                                            }
                                        }
                                    });
                                }
                            }
                        });

                        // JOIN EXISTING GAME: if a gameObjectId was passed from the Main Lobby
                    } else {
                        // Need to figure out if the selected avatar has already been used
                        ParseQuery<TagPlayer> pquery = TagPlayer.getQuery();
                        pquery.whereEqualTo("gameId", GAME_OBJECT_ID);
                        pquery.findInBackground(new FindCallback<TagPlayer>() {
                            @Override
                            public void done(List<TagPlayer> plist, ParseException e) {

                                if (e == null) {
                                    int[] usedAvatars = new int[8];

                                    for (int i = 0; i < plist.size(); i++) {
                                        usedAvatars[(plist.get(i).getAvatar()) - 1] = 1;
                                    }

                                    if (usedAvatars[TagPlayer.getAvatarNumber(AVATAR) - 1] != 0) {
                                        // Avatar already used, make toast.
                                        Toast.makeText(getApplicationContext(), "That avatar has already been taken!", Toast.LENGTH_LONG).show();
                                    } else {
                                        //Saves the game settings data and game objectId to key that
                                        // can be accessed by the activity (InGameLobbyActivity??)
                                        // that follows.
                                        final Intent toGame = new Intent(GameSettingsActivity.this,
                                                InGameActivity.class);
                                        toGame.putExtra("gameObjectId", GAME_OBJECT_ID);

                                        //Create Player parseObject and relate it to Game parseObject
                                        final TagPlayer player = new TagPlayer();
                                        player.setGame(GAME_OBJECT_ID);
                                        player.setPlayer(ParseUser.getCurrentUser().getString("username"));
                                        player.setLocation(geoPoint);
                                        player.setAvatar(TagPlayer.getAvatarNumber(AVATAR));
                                        player.setTagCount(0);

                                        player.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    //Saves the ActiveUsers objectId to keys that can be
                                                    // accessed by the activity (InGameLobbyActivity??)
                                                    // that follows.
                                                    String playerObjectId = player.getObjectId();
                                                    toGame.putExtra("playerObjectId", playerObjectId);
                                                    startActivity(toGame);
                                                }
                                            }
                                        });
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                } else {
                    //Can not get current location - Advise User
                    Toast.makeText(GameSettingsActivity.this, "Can not find GPS location.", Toast.LENGTH_LONG).show();
                }
            }
        });

        // GAME SETTINGS WIDGETS

        //Initialize the gameplay radius number picker
        gameRadiusNumberPicker = (NumberPicker) findViewById(R.id.gpRadius);
        //Make picking a radius a little bit easier :)
        for (int i = 0; i < 38; i++) //start at i=2 to start at 150
            RADIUS_INTERVALS[i] = String.valueOf(PICKER_RANGE * (i + 3));

        //Maximum and minimum values for the number picker
        gameRadiusNumberPicker.setMaxValue(37);
        gameRadiusNumberPicker.setMinValue(0);
        gameRadiusNumberPicker.setDisplayedValues(RADIUS_INTERVALS);
        gameRadiusNumberPicker.setValue(GAME_RADIUS); //initial set
        setNumberPickerTextColor(gameRadiusNumberPicker, Color.parseColor("white"));
        gameRadiusNumberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        //Initialize the tag radius button group
        tagRadiusRadioGroup = (RadioGroup) findViewById(R.id.tag_radiogroup);
        tagRadiusRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                setTagRadiusById(checkedId); //uses the button string to set the integer value
            }
        });

        //Initialize the tag limit button group
        tagLimitGroup = (RadioGroup) findViewById(R.id.limit_radiogroup);
        tagLimitGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                setTagLimitById(checkedId); //uses the button string to set the integer value
            }
        });

        //Time Limit Spinner
        timeLimitSpinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.times_array, R.layout.settings_spinner);
        //set the layout for the spinner
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeLimitSpinner.setAdapter(adapter);

        //Controls the Spinner object
        timeLimitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {
                    case (0): {//5 MINUTES
                        SECONDS = 300;
                        apply.setClickable(true);
                        break;
                    }
                    case (1): {//15 MINUTES
                        SECONDS = 900;
                        apply.setClickable(true);
                        break;
                    }
                    case (2): {//30 MINUTES
                        SECONDS = 1800;
                        apply.setClickable(true);
                        break;
                    }
                    case (3): {//1 hour
                        SECONDS = 3600;
                        apply.setClickable(true);
                        break;
                    }
                    case (4): {//2 HOURS
                        SECONDS = 7200;
                        apply.setClickable(true);
                        break;
                    }
                    case (5): {//4 HOURS
                        SECONDS = 14400;
                        apply.setClickable(true);
                        break;
                    }
                    case (6): {//8 HOURS
                        SECONDS = 28800;
                        apply.setClickable(true);
                        break;
                    }
                    case (7): {//24 HOURS
                        SECONDS = 86400;
                        apply.setClickable(true);
                        break;
                    }
                    default: { //may be 0, may be 9, not sure
                        SECONDS = 10000000;     // this is the value InGameActivity uses as a "null" value
                        if (TAG_LIMIT == 0)
                            apply.setClickable(false);
                    }
                }
            }

            //If the spinner has no selected value, there will be no time limit.
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        timeLimitSpinner.setSelection(8);

        // PLAYER SETTINGS WIDGETS

        //Initialize the avatar radio group
        avatarRadioGroup = (RadioGroup) findViewById(R.id.avatar_radiogroup);
        avatarRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                setAvatarById(checkedId); //uses the button string to set the integer value
            }
        });

        //Initialize the default button
        Button defaults = (Button) findViewById(R.id.default_settings_button);
        /**Default settings can be changed inside this onClick method*/
        defaults.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (GAME_OBJECT_ID == null) {
                    tagLimitGroup.check(R.id.five);
                    tagRadiusRadioGroup.check(R.id.ten_meters);
                    gameRadiusNumberPicker.setValue(300); //apparently this works
                    GAME_RADIUS = 300; //just to make sure
                    timeLimitSpinner.setSelection(8); //gotta check that this is "unselected"
                    SECONDS = 0;
                }
                RadioButton crabRadio = (RadioButton) findViewById(R.id.crab);
                crabRadio.setChecked(true);

            }
        });


        // If user is joining an existing game, need to disable all layout related to Game Settings
        if (GAME_OBJECT_ID != null) {
            TextView gameSettingsTextView = (TextView) findViewById(R.id.gamesettings_textview);
            gameSettingsTextView.setVisibility(View.GONE);

            TextView gameRadiusTextView = (TextView) findViewById(R.id.gpradius_textview);
            gameRadiusTextView.setVisibility(View.GONE);

            TextView timeLimitTextView = (TextView) findViewById(R.id.time_textView);
            timeLimitTextView.setVisibility(View.GONE);

            gameRadiusNumberPicker.setVisibility(View.GONE);

            timeLimitSpinner.setVisibility(View.GONE);

            TextView tagRadiusTextView = (TextView) findViewById(R.id.tag_textview);
            tagRadiusTextView.setVisibility(View.GONE);

            tagRadiusRadioGroup.setVisibility(View.GONE);

            TextView tagLimitTextView = (TextView) findViewById(R.id.limit_textView);
            tagLimitTextView.setVisibility(View.GONE);

            tagLimitGroup.setVisibility(View.GONE);



        }
    }



    /*
        //Allow the user to back out through the settings menu
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.main, menu);

            menu.findItem(R.id.action_settings).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    startActivity(new Intent(GameSettingsActivity.this, SettingsActivity.class));
                    return true;
                }
            });
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
                return true;
            }

            return super.onOptionsItemSelected(item);
        }
    */
    //uses the button ID to set the tag limit  by checking the string value
    //If tag limit set to 0 when time limit is also 0, this will disable the Apply button
    public void setTagLimitById(int buttonId) {
        RadioButton button = (RadioButton) findViewById(buttonId);
        String buttonText = button.getText().toString();
        TAG_LIMIT = Integer.parseInt(buttonText);
        if (TAG_LIMIT == 0) {
            if (SECONDS == 0)
                apply.setClickable(false);
        } else //at least one condition met for game rules
            apply.setClickable(true);
    }

    //uses the button ID to set the tag radius by checking the string value
    public void setTagRadiusById(int buttonId) {
        RadioButton button = (RadioButton) findViewById(buttonId);
        String label = button.getText().toString();
        TAG_RADIUS = Integer.parseInt(label);
    }

    //uses the button ID to set the avatar by checking the string value
    public void setAvatarById(int buttonId) {
        RadioButton button = (RadioButton) findViewById(buttonId);
        AVATAR = button.getTag().toString();
    }

    public static boolean setNumberPickerTextColor(NumberPicker numberPicker, int color)
    {
        final int count = numberPicker.getChildCount();
        for(int i = 0; i < count; i++){
            View child = numberPicker.getChildAt(i);
            if(child instanceof EditText){
                try{
                    Field selectorWheelPaintField = numberPicker.getClass()
                            .getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    ((Paint)selectorWheelPaintField.get(numberPicker)).setColor(color);
                    ((EditText)child).setTextColor(color);
                    numberPicker.invalidate();
                    return true;
                }
                catch(NoSuchFieldException e){
                    Log.w("setNumberPickerTextColor", e);
                }
                catch(IllegalAccessException e){
                    Log.w("setNumberPickerTextColor", e);
                }
                catch(IllegalArgumentException e){
                    Log.w("setNumberPickerTextColor", e);
                }
            }
        }
        return false;
    }

}