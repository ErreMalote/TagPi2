package com.parse.tagteampi;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseGeoPoint;
<<<<<<< HEAD
import com.parse.ParseUser;
=======
import com.parse.ParseObject;
>>>>>>> origin/master

import java.lang.String;


public class GameSettingsActivity extends Activity {
    private NumberPicker gameRadiusNumberPicker;
    private RadioGroup tagRadiusRadioGroup;
    private Spinner timeLimitSpinner;
    private RadioGroup tagLimitGroup;
    private Button apply;
    private ParseGeoPoint point = new ParseGeoPoint();
    //integer values for various uses
    private int GAME_RADIUS = 300;
    private int TAG_RADIUS = 10;
    private int TAG_LIMIT = 0;
    private int MINUTES = 0;
    //private String USER_NAME;
    private String PRINTED_MESSAGE;
    private int PICKER_RANGE = 50; //set for quick editing
    private String[] RADIUS_INTERVALS = new String[38];
    //private String USER_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO: connect user information with game settings as extras
        //USER_NAME = this.getIntent().getExtras().getString("Username");
        setContentView(R.layout.activity_create_game);
        //Add TagGame ParseObject registration
        ParseObject.registerSubclass(TagGame.class);

        //Intent intent = getIntent();
       // Location location = intent.getParcelableExtra(Application.INTENT_EXTRA_LOCATION);
       // point = new ParseGeoPoint(location.getLatitude(), location.getLongitude());

        //Initialize the apply button, which currently pushes settings' values to the screen through Toast
        //TODO: Pass user info (user name, ID, etc.) through game settings to lobby activity through extra
        apply = (Button) findViewById(R.id.done_button);
        apply.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                GAME_RADIUS = Integer.parseInt(RADIUS_INTERVALS[gameRadiusNumberPicker.getValue()]);
                PRINTED_MESSAGE = "The values as found:\nTag Distance: " + TAG_RADIUS
                        + "\nGameplay Radius: " + GAME_RADIUS
                        + "\nTag Limit: " + TAG_LIMIT
                        + "\nTime limit: " + MINUTES;
                Toast.makeText(GameSettingsActivity.this, PRINTED_MESSAGE, Toast.LENGTH_LONG).show();

                //TODO: Create the lobby screen and activate the following code:
                Intent toLobby = new Intent(GameSettingsActivity.this, LobbyActivity.class);
                toLobby.putExtra("gameRadiousS", GAME_RADIUS);
                toLobby.putExtra("TAG_RADIUS", TAG_RADIUS);
                toLobby.putExtra("tagLimit", TAG_LIMIT);
                toLobby.putExtra("gameDuration", MINUTES);

                TagGame tg = new TagGame();
                tg.setRadious(GAME_RADIUS);
                tg.setTagLimit(TAG_LIMIT);
                tg.setTagRadious(TAG_RADIUS);
                tg.setTime(MINUTES);
                tg.setLocation(point);
                tg.setUser(ParseUser.getCurrentUser().getUsername());

                ActiveUsers au = new ActiveUsers();
                au.setGameId(ParseUser.getCurrentUser().getObjectId());
                au.setUserId(ParseUser.getCurrentUser().getUsername());
                startActivity(toLobby);

            }
        });

        //Initialize the gameplay radius number picker
        gameRadiusNumberPicker = (NumberPicker) findViewById(R.id.gpRadius);
        //Make picking a radius a little bit easier :)
        for(int i=0; i<38; i++) //start at i=2 to start at 150
            RADIUS_INTERVALS[i] = String.valueOf(PICKER_RANGE * (i+3));

        //Maximum and minimum values for the number picker
        gameRadiusNumberPicker.setMaxValue(37);
        gameRadiusNumberPicker.setMinValue(0);
        gameRadiusNumberPicker.setDisplayedValues(RADIUS_INTERVALS);
        gameRadiusNumberPicker.setValue(GAME_RADIUS); //initial set

        //Initialize the tag radius button group
        tagRadiusRadioGroup = (RadioGroup) findViewById(R.id.tag_radiogroup);
        tagRadiusRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                setTagRadiusById(checkedId); //uses the button string to set the integer value
            }
        });

        //Initialize the tag limit button group
        tagLimitGroup = (RadioGroup)  findViewById(R.id.limit_radiogroup);
        tagLimitGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                setTagLimitById(checkedId); //uses the button string to set the integer value
            }
        });

        //Time Limit Spinner
        timeLimitSpinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.times_array, android.R.layout.simple_spinner_item);
        //set the layout for the spinner
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeLimitSpinner.setAdapter(adapter);

        //Controls the Spinner object
        timeLimitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {
                    case (0): {//15 MINUTES
                        MINUTES = 15;
                        apply.setClickable(true);
                        break;
                    }
                    case (1): {//30 MINUTES
                        MINUTES = 30;
                        apply.setClickable(true);
                        break;
                    }
                    case (2): {//1 hour
                        MINUTES = 60;
                        apply.setClickable(true);
                        break;
                    }
                    case (3): {//2 HOURS
                        MINUTES = 120;
                        apply.setClickable(true);
                        break;
                    }
                    case (4): {//4 HOURS
                        MINUTES = 240;
                        apply.setClickable(true);
                        break;
                    }
                    case (5): {//8 HOURS
                        MINUTES = 480;
                        apply.setClickable(true);
                        break;
                    }
                    case (6): {//24 HOURS
                        MINUTES = 1440;
                        apply.setClickable(true);
                        break;
                    }
                    default: { //may be 0, may be 9, not sure
                        MINUTES = 0;
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
        timeLimitSpinner.setSelection(4);

        //Initialize the default button
        Button defaults = (Button) findViewById(R.id.default_settings_button);
        /**Default settings can be changed inside this onClick method*/
        defaults.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                tagLimitGroup.check(R.id.five);
                tagRadiusRadioGroup.check(R.id.ten_meters);
                gameRadiusNumberPicker.setValue(300); //apparently this works
                GAME_RADIUS = 300; //just to make sure
                timeLimitSpinner.setSelection(8); //gotta check that this is "unselected"
                MINUTES = 0;
            }
        });
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
        if(TAG_LIMIT == 0) {
            if(MINUTES == 0)
                apply.setClickable(false);
        }
        else //at least one condition met for game rules
            apply.setClickable(true);
    }

    //uses the button ID to set the tag radius by checking the string value
    public void setTagRadiusById(int buttonId) {
        RadioButton button = (RadioButton) findViewById(buttonId);
        String label = button.getText().toString();
        TAG_RADIUS = Integer.parseInt(label);
    }

}
