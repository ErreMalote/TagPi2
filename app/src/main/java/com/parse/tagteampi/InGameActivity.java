package com.parse.tagteampi;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import java.util.Set;
import java.util.TimeZone;

import android.content.Context;
import android.location.LocationManager;
import android.support.v4.view.MenuItemCompat;
import android.text.format.Time;
import android.widget.Toast;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;


public class InGameActivity extends FragmentActivity implements LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    /*
     * Define a request code to send to Google Play services This code is returned in
     * Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /*
     * Constants for location update parameters
     */
    // Initial game timer value
    private static final int DUMMY_GAME_TIMER_VALUE = 10000000;  // ten million seconds = no game duration OR uninitialized
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    private Button startGameButton;
    // The update interval
    private static final int UPDATE_INTERVAL_IN_SECONDS = 2;

    // A fast interval ceiling
    private static final int FAST_CEILING_IN_SECONDS = 1;

    // Update interval in milliseconds
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
            * UPDATE_INTERVAL_IN_SECONDS;

    // A fast ceiling of update intervals, used when the app is visible
    private static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
            * FAST_CEILING_IN_SECONDS;
    /*
     * Constants for handling location results
     */
    // Conversion from feet to meters
    private static final float METERS_PER_FEET = 0.3048f;

    // Conversion from kilometers to meters
    private static final int METERS_PER_KILOMETER = 1000;

    // Initial offset for calculating the map bounds
    private static final double OFFSET_CALCULATION_INIT_DIFF = 1.0;

    // Accuracy for calculating the map bounds
    private static final float OFFSET_CALCULATION_ACCURACY = 0.01f;

    // Maximum results returned from a Parse query
    private static final int MAX_POST_SEARCH_RESULTS = 20;

    // Maximum post search radius for map in kilometers
    private static final int MAX_POST_SEARCH_DISTANCE = 100;

    /*
     * Other class member variables
     */
    // Map fragment
    private SupportMapFragment mapFragment;

    // Represents the circle around a map
    private Circle mapCircle;

    // Fields for the map radius in feet
    private float radius;
    private float lastRadius;

    // Game fields
    private String gameObjectId;
    private int tagLimit;
    private long duration = DUMMY_GAME_TIMER_VALUE;
    private float tagRadius;
    private boolean isHostUser;
    private ParseGeoPoint centerCircle;
    private boolean gameStarted = false;
    private boolean enoughPlayers = false;
    private TagGame game;                   // for storing results of Game Query at game start
    private TextView durationTimerTextView;



    // Fields for helping process map and location changes
    private final Map<String, Marker> mapMarkers = new HashMap<String, Marker>();
    private int mostRecentMapUpdate;
    private boolean hasSetUpInitialLocation;
    private String selectedPostObjectId;
    private Location lastLocation;
    private Location currentLocation;

    // A request to connect to Location Services
    private LocationRequest locationRequest;

    // Stores the current instantiation of the location client in this object
    private LocationClient locationClient;

    // Adapter for the Parse query
    private ParseQueryAdapter<TagPlayer> postsQueryAdapter;

    // Fields for updating current player position to Parse
    private String playerObjectId;
    private LocationManager lm;
    private double latitude;
    private double longitude;
    private ParseGeoPoint playerGeoPoint;

    // Fields for updating itt
    private boolean itt;
    private boolean initItt = false;
    private TagPlayer ittPlayer;


    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ONCREATE                                                                                     ONCREATE
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gameObjectId = getIntent().getExtras().getString("gameObjectId");
        playerObjectId = getIntent().getExtras().getString("playerObjectId");

        ParseQuery<TagGame> gameSettings = ParseQuery.getQuery("Game");

        gameSettings.findInBackground(new FindCallback<TagGame>() {
            @Override
            public void done(List<TagGame> list, ParseException e) {
                for (TagGame aList : list) {
                    if(aList.getObjectId().equalsIgnoreCase(gameObjectId)){
                        radius = aList.getMapRadius();
                        tagLimit = aList.getTagLimit();
                        tagRadius = aList.getTagRadius();
                        duration = aList.getGameDuration();
                        centerCircle = aList.getLocation();
                        if (aList.getHostUser().equalsIgnoreCase(ParseUser.getCurrentUser().getUsername())) {
                            isHostUser = true;
                        } else {
                            isHostUser = false;
                        }
                        if(aList.getStartTime() != null){
                            gameStarted = true;
                        }else{
                            gameStarted = false;
                        }

                    }
                }
            }
        });

        setContentView(R.layout.activity_main);

        locationRequest = LocationRequest.create();                         // Create a new global location parameters object
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);       // Set the update interval
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);    // Use high accuracy
        locationRequest.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLISECONDS);  // Set the interval ceiling to one minute
        locationClient = new LocationClient(this, this, this);      // Create a new location client, using the enclosing class to handle callbacks.

        ParseQueryAdapter.QueryFactory<TagPlayer> factory = new ParseQueryAdapter.QueryFactory<TagPlayer>() {
            public ParseQuery<TagPlayer> create() {

                ParseQuery<TagPlayer> query = TagPlayer.getQuery();
                query.whereEqualTo("gameId", gameObjectId);
                query.orderByDescending("createdAt");
                return query;
            }
        };

        // Set up the query adapter
        postsQueryAdapter = new ParseQueryAdapter<TagPlayer>(this, factory) {
            @Override
            public View getItemView(TagPlayer player, View view, ViewGroup parent) {
                if (view == null) {
                    view = View.inflate(getContext(), R.layout.anywall_post_item, null);
                }
                TextView contentView = (TextView) view.findViewById(R.id.content_view);
                TextView usernameView = (TextView) view.findViewById(R.id.username_view);
                contentView.setText(player.getPlayer());
                if(player.isItt()) {
                    usernameView.setText("itt");
                }else{
                    usernameView.setText("Not itt");
                }
                return view;
            }
        };

        // Disable automatic loading when the adapter is attached to a view.
        postsQueryAdapter.setAutoload(false);

        // Disable pagination, we'll manage the query limit ourselves
        postsQueryAdapter.setPaginationEnabled(false);

        // Attach the query adapter to the view
        ListView playersListView = (ListView) findViewById(R.id.players_listview);
        playersListView.setAdapter(postsQueryAdapter);

        // Set up the handler for an item's selection
        playersListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final TagPlayer item = postsQueryAdapter.getItem(position);
                selectedPostObjectId = item.getObjectId();
                mapFragment.getMap().animateCamera(
                        CameraUpdateFactory.newLatLng(new LatLng(item.getLocation().getLatitude(), item
                                .getLocation().getLongitude())), new CancelableCallback() {
                            public void onFinish() {
                                Marker marker = mapMarkers.get(item.getObjectId());

                                if (marker != null) {
                                    marker.showInfoWindow();
                                }
                            }

                            public void onCancel() {
                            }
                        });
                Marker marker = mapMarkers.get(item.getObjectId());
                if (marker != null) {
                    marker.showInfoWindow();
                }
            }
        });

        // Set up the map fragment
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        // Enable the current location "blue dot"
        mapFragment.getMap().setMyLocationEnabled(false);
        // Set up the camera change handler
        mapFragment.getMap().setOnCameraChangeListener(new OnCameraChangeListener() {
            public void onCameraChange(CameraPosition position) {
                // When the camera changes, update the query
                doMapQuery();

            }
        });

        // Initialize start button so we can destroy it for all non-hosts
        startGameButton = (Button) findViewById(R.id.startGameButton);
        /*if (!isHostUser) {
            startGameButton.setVisibility(View.GONE);
        } else {
            startGameButton.setVisibility(View.VISIBLE);
        }*/


        ////////////////////////////////////////////////////////////////////////////////////////////
        // GAME DURATION TIMER Setup
        ////////////////////////////////////////////////////////////////////////////////////////////
        durationTimerTextView = (TextView) findViewById(R.id.in_game_timer);
        if (duration != DUMMY_GAME_TIMER_VALUE) {
            durationTimerTextView.setText(secondsToString(duration));
            durationTimerTextView.setVisibility(View.VISIBLE);
        }
        if (!initItt) {
            initItt = initializeItt();
        }
        ////////////////////////////////////////////////////////////////////////////////////////////
        // GAME TICK Start - after everything is set up, start the tickRunnable
        ////////////////////////////////////////////////////////////////////////////////////////////
        tickHandler.postDelayed(tickRunnable, 1000);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // TICK FUNCTIONS                                                                               TICK FUNCTIONS
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    protected Handler tickHandler = new Handler();
    protected Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {

            // STEP 1: Check if game has ended
            //TODO: Add stuff for checking if tag limit has been reached
            //TODO: Get score info before deleting game
            if (duration <= 0) {
                if (isHostUser) {
                    ParseQuery<TagGame> finalquery = TagGame.getQuery();
                    finalquery.getInBackground(gameObjectId, new GetCallback<TagGame>() {
                        @Override
                        public void done(TagGame tagGame, ParseException e) {
                            tagGame.deleteInBackground();   // Bye bye game!
                        }
                    });
                }

                Toast.makeText(getApplicationContext(), "Game is over, everyone go home.", Toast.LENGTH_LONG).show();
                finish();

            }

            // STEP 2: Decrement duration (every second)
            if (duration != DUMMY_GAME_TIMER_VALUE) {
                if (gameStarted) {
                    duration--;
                    durationTimerTextView.setText(secondsToString(duration));
                } else {
                    if (durationTimerTextView.getVisibility() == View.INVISIBLE) {
                        durationTimerTextView.setText(secondsToString(duration));
                        durationTimerTextView.setVisibility(View.VISIBLE);
                    }

                }
            }

            /*
            * Used to update personal location to Parse everytime the runnable executes.
            */
            lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            Location playerLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (playerLoc != null) {
                latitude = playerLoc.getLatitude();
                longitude = playerLoc.getLongitude();
                playerGeoPoint = new ParseGeoPoint(latitude, longitude);

                ParseQuery<TagPlayer> player = ParseQuery.getQuery("Player");
                player.getInBackground(playerObjectId, new GetCallback<TagPlayer>() {
                    @Override
                    public void done(TagPlayer tagPlayer, ParseException e) {
                        if (e == null) {
                            tagPlayer.setLocation(playerGeoPoint);
                            tagPlayer.saveInBackground();
                        }
                    }
                });
            }



            // STEP 2: Pregame Stuff
            if (!gameStarted) {
                // HOST USER pregame stuff to do
                if (isHostUser) {
                    startGameButton.setVisibility(View.VISIBLE);
                    startGameButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ParseQuery<ParseObject> playerCount = ParseQuery.getQuery("Player");
                            playerCount.whereEqualTo("gameId", gameObjectId);
                            playerCount.findInBackground(new FindCallback<ParseObject>() {
                                @Override
                                public void done(List<ParseObject> playerList, ParseException e) {
                                    if (e == null) {
                                        if (playerList.size() < 2) {
                                            Toast.makeText(InGameActivity.this, "Need 3 players to start",
                                                    Toast.LENGTH_LONG).show();
                                        } else if (playerList.size() > 8) {
                                            Toast.makeText(InGameActivity.this, "Player maximum is 8.",
                                                    Toast.LENGTH_LONG).show();
                                        } else {
                                            enoughPlayers = true;
                                        }
                                    }
                                }
                            });

                            if (enoughPlayers) {
                                ParseQuery<TagGame> gameStartQuery = ParseQuery.getQuery("Game");
                                gameStartQuery.getInBackground(gameObjectId, new GetCallback<TagGame>() {
                                    @Override
                                    public void done(TagGame tg, ParseException e) {
                                        if (e == null) {
                                            Date date = new Date();
                                            date.getTime();
                                            tg.setStartTime(date);
                                            tg.put("startedAt", date);
                                            tg.saveInBackground();

                                            duration = tg.getGameDuration();
                                            game = tg;
                                        }
                                    }
                                });
                            }

                            // Button folds out of layout after being pressed.
                            if (enoughPlayers) {
                                Button button = (Button) v;
                                button.setVisibility(View.GONE);
                            }

                            //Game starts
                            gameStarted = true;
                        }
                    });

                    // PLAYER USER pregame stuff to do
                } else {
                    startGameButton.setVisibility(View.GONE);
                    ParseQuery<TagGame> gameSettings = ParseQuery.getQuery("Game");
                    gameSettings.findInBackground(new FindCallback<TagGame>() {
                        @Override
                        public void done(List<TagGame> list, ParseException e) {
                            for (TagGame aList : list) {
                                if (aList.getObjectId().equalsIgnoreCase(gameObjectId)) {
                                    if (aList.getStartTime() != null) {
                                        game = aList;
                                        duration = aList.getGameDuration();
                                        gameStarted = true;

                                    }
                                }
                            }
                        }
                    });
                }
                // STEP 2 alternate: After game has started
            } else {
                startGameButton.setVisibility(View.GONE);
                // STEP 3: EVERY 2 SECONDS - Get all Players in the game
                // TODO: make every 2 seconds
                ParseQuery<TagPlayer> gameSettings = ParseQuery.getQuery("Player");
                gameSettings.whereEqualTo("gameId", gameObjectId);
                gameSettings.findInBackground(new FindCallback<TagPlayer>() {
                    @Override
                    public void done(List<TagPlayer> list, ParseException e) {
                        for (TagPlayer aList : list) {
                            if (aList.getGame().equalsIgnoreCase(gameObjectId) && ParseUser.getCurrentUser().getUsername().equalsIgnoreCase(aList.getPlayer())) {

                                itt = aList.isItt();
                            }
                        }

                        if (itt) {
                            isItt();
                            //Toast.makeText(InGameActivity.this, "checking for people", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                // TODO: STEP 4: ITT ONLY, EVERY 2 SECONDS - Check for taggable players

                // TODO: STEP 6: Decrement duration

            }




            // FINALLY, call the next tick
            tickHandler.postDelayed(this, 1000);
        }
    };

    /*
     * Called when the Activity is no longer visible at all. Stop updates and disconnect.
     */
    @Override
    public void onStop() {
        // If the client is connected
        if (locationClient.isConnected()) {
            stopPeriodicUpdates();
        }

        // After disconnect() is called, the client is considered "dead".
        locationClient.disconnect();

        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tickHandler.removeCallbacks(tickRunnable);
        final Intent toMainLobby = new Intent(getBaseContext(), MainLobbyActivity.class);
        startActivity(toMainLobby);

    }

    /*
     * Called when the Activity is restarted, even before it becomes visible.
     */
    @Override
    public void onStart() {
        super.onStart();

        // Connect to the location services client
        locationClient.connect();
    }

    /*
     * Called when the Activity is resumed. Updates the view.
     */
    @Override
    protected void onResume() {
        super.onResume();

        Application.getConfigHelper().fetchConfigIfNeeded();

        // Get the latest search distance preference
        radius = Application.getSearchDistance();
        // Checks the last saved location to show cached data if it's available
        if (lastLocation != null) {
            LatLng myLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            LatLng circleLatLng = new LatLng(centerCircle.getLatitude(),centerCircle.getLongitude());
            // If the search distance preference has been changed, move
            // map to new bounds.
            if (lastRadius != radius) {
                updateZoom(myLatLng);
            }
            // Update the circle map
            updateCircle(circleLatLng);
        }
        // Save the current radius
        lastRadius = radius;
        // Query for the latest data to update the views.
        doMapQuery();
        doListQuery();
    }

    /*
    * Inflates the Action Bar.
    */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main, menu);


        menu.findItem(R.id.action_settings).setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(InGameActivity.this, SettingsActivity.class));
                return true;
            }
        });

        return true;
    }



    /*
     * Handle results returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed() in
     * LocationUpdateRemover and LocationUpdateRequester may call startResolutionForResult() to start
     * an Activity that handles Google Play services problems. The result of this call returns here,
     * to onActivityResult.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // Choose what to do based on the request code
        switch (requestCode) {

            // If the request code matches the code sent in onConnectionFailed
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:

                switch (resultCode) {
                    // If Google Play services resolved the problem
                    case Activity.RESULT_OK:

                        if (Application.APPDEBUG) {
                            // Log the result
                            Log.d(Application.APPTAG, "Connected to Google Play services");
                        }

                        break;

                    // If any other result was returned by Google Play services
                    default:
                        if (Application.APPDEBUG) {
                            // Log the result
                            Log.d(Application.APPTAG, "Could not connect to Google Play services");
                        }
                        break;
                }

                // If any other request code was received
            default:
                if (Application.APPDEBUG) {
                    // Report that this Activity received an unknown requestCode
                    Log.d(Application.APPTAG, "Unknown request code received for the activity");
                }
                break;
        }
    }

    /*
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            if (Application.APPDEBUG) {
                // In debug mode, log the status
                Log.d(Application.APPTAG, "Google play services available");
            }
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), Application.APPTAG);
            }
            return false;
        }
    }

    /*
     * Called by Location Services when the request to connect the client finishes successfully. At
     * this point, you can request the current location or start periodic updates
     */
    public void onConnected(Bundle bundle) {
        if (Application.APPDEBUG) {
            Log.d("Connected to location services", Application.APPTAG);
        }
        currentLocation = getLocation();
        startPeriodicUpdates();
    }

    /*
     * Called by Location Services if the connection to the location client drops because of an error.
     */
    public void onDisconnected() {
        if (Application.APPDEBUG) {
            Log.d("Disconnected from location services", Application.APPTAG);
        }
    }

    /*
     * Called by Location Services if the attempt to Location Services fails.
     */
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Google Play services can resolve some errors it detects. If the error has a resolution, try
        // sending an Intent to start a Google Play services activity that can resolve error.
        if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

            } catch (IntentSender.SendIntentException e) {

                if (Application.APPDEBUG) {
                    // Thrown if Google Play services canceled the original PendingIntent
                    Log.d(Application.APPTAG, "An error occurred when connecting to location services.", e);
                }
            }
        } else {
            // If no resolution is available, display a dialog to the user with the error.
            showErrorDialog(connectionResult.getErrorCode());
        }
    }


    /*
     * Report location updates to the UI.
     */
    public void onLocationChanged(Location location) {
        currentLocation = location;
        if (lastLocation != null
                && geoPointFromLocation(location)
                .distanceInKilometersTo(geoPointFromLocation(lastLocation)) < 0.001) {
            // If the location hasn't changed by more than 10 meters, ignore it.
            return;
        }
        lastLocation = location;
        LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        LatLng circleLatLng = new LatLng(centerCircle.getLatitude(),centerCircle.getLongitude());
        if (!hasSetUpInitialLocation) {
            // Zoom to the current location.
            updateZoom(myLatLng);
            hasSetUpInitialLocation = true;
        }
        // Update map radius indicator
        updateCircle(circleLatLng);
        doMapQuery();
        doListQuery();
    }

    /*
     * In response to a request to start updates, send a request to Location Services
     */
    private void startPeriodicUpdates() {
        locationClient.requestLocationUpdates(locationRequest, this);
    }

    /*
     * In response to a request to stop updates, send a request to Location Services
     */
    private void stopPeriodicUpdates() {
        locationClient.removeLocationUpdates(this);
    }

    /*
     * Get the current location
     */
    private Location getLocation() {
        // If Google Play Services is available
        if (servicesConnected()) {
            // Get the current location
            return locationClient.getLastLocation();
        } else {
            return null;
        }
    }

    /*
     * Set up a query to update the list view
     */
    private void doListQuery() {
        Location myLoc = (currentLocation == null) ? lastLocation : currentLocation;
        // If location info is available, load the data
        if (myLoc != null) {
            // Refreshes the list view with new data based
            // usually on updated location data.
            postsQueryAdapter.loadObjects();
        }
    }

    /*
     * ??????????????????????????????????????
     * Set up the query to update the map view
     */
    private void doMapQuery() {
        final int myUpdateNumber = ++mostRecentMapUpdate;
        Location myLoc = (currentLocation == null) ? lastLocation : currentLocation;
        // If location info isn't available, clean up any existing markers
        if (myLoc == null) {
            cleanUpMarkers(new HashSet<String>());
            return;
        }

        //Converts the user's location to a ParseGeoPoint to run in the Parse query.
        final ParseGeoPoint myPoint = geoPointFromLocation(myLoc);
        // Create the map Parse query
        ParseQuery<TagPlayer> mapQuery = TagPlayer.getQuery();
        // Set up additional query filters
        mapQuery.whereWithinKilometers("location", myPoint, MAX_POST_SEARCH_DISTANCE);
        mapQuery.whereEqualTo("gameId", gameObjectId);
        mapQuery.include("user");
        mapQuery.orderByDescending("createdAt");
        mapQuery.setLimit(MAX_POST_SEARCH_RESULTS);
        // Kick off the query in the background
        mapQuery.findInBackground(new FindCallback<TagPlayer>() {
            @Override
            public void done(List<TagPlayer> objects, ParseException e) {                               /*?????????????????????*/
                if (e != null) {
                    if (Application.APPDEBUG) {
                        Log.d(Application.APPTAG, "An error occurred while querying for map posts.", e);
                    }
                    return;
                }
        /*
         * Make sure we're processing results from
         * the most recent update, in case there
         * may be more than one in progress.
         */
                if (myUpdateNumber != mostRecentMapUpdate) {
                    return;
                }
                // Posts to show on the map
                Set<String> toKeep = new HashSet<String>();
                // Loop through the results of the search
                for (TagPlayer post : objects) {
                    // Add this post to the list of map pins to keep
                    toKeep.add(post.getObjectId());
                    // Check for an existing marker for this post
                    Marker oldMarker = mapMarkers.get(post.getObjectId());

                    //Set markers location
                    MarkerOptions markerOpts = new MarkerOptions().position(new LatLng(post.getLocation().getLatitude(),
                            post.getLocation().getLongitude()));

                    if(post.isItt()) {
                        if(oldMarker != null) {
                            if(oldMarker.getSnippet() == null) {
                                continue;
                            }
                            else {
                                oldMarker.remove();
                            }
                        }

                        markerOpts = markerOpts
                                .title("Itt")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.red_shark_icon));
                    }

                    else {
                        if(oldMarker != null) {
                            if(oldMarker.getSnippet() != null) {
                                continue;
                            }
                            else {
                                oldMarker.remove();
                            }
                        }


                        if(post.getAvatar() == 1) {
                            markerOpts = markerOpts
                                    .title(String.valueOf(post.getPlayer()))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.orange_crab));

                        }

                        if(post.getAvatar() == 2) {
                            markerOpts = markerOpts
                                    .title(String.valueOf(post.getPlayer()))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.orange_jellyfish));
                        }

                        if(post.getAvatar() == 3) {
                            markerOpts = markerOpts
                                    .title(String.valueOf(post.getPlayer()))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.orange_octopus));
                        }

                        if(post.getAvatar() == 4) {
                            markerOpts = markerOpts
                                    .title(String.valueOf(post.getPlayer()))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.orange_seahorse));
                        }

                        if(post.getAvatar() == 5) {
                            markerOpts = markerOpts
                                    .title(String.valueOf(post.getPlayer()))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.orange_sponge));
                        }

                        if(post.getAvatar() == 6) {
                            markerOpts = markerOpts
                                    .title(String.valueOf(post.getPlayer()))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.orange_starfish));
                        }

                        if(post.getAvatar() == 7) {
                            markerOpts = markerOpts
                                    .title(String.valueOf(post.getPlayer()))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.orange_this_is_bait));
                        }

                        if(post.getAvatar() == 8) {
                            markerOpts = markerOpts
                                    .title(String.valueOf(post.getPlayer()))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.orange_turtle));
                        }
                    }

                    // Set up the marker properties based on if it is within the search radius
                    //  if (post.getLocation().distanceInKilometersTo(myPoint) > radius * METERS_PER_FEET
                    //        / METERS_PER_KILOMETER) {


                    // Add a new marker
                    Marker marker = mapFragment.getMap().addMarker(markerOpts);
                    mapMarkers.put(post.getObjectId(), marker);
                    if (post.getObjectId().equals(selectedPostObjectId)) {
                        marker.showInfoWindow();
                        selectedPostObjectId = null;
                    }
                }
                // Clean up old markers.
                cleanUpMarkers(toKeep);
            }
        });
    }

    /*
     * Helper method to clean up old markers
     */
    private void cleanUpMarkers(Set<String> markersToKeep) {
        for (String objId : new HashSet<String>(mapMarkers.keySet())) {
            if (!markersToKeep.contains(objId)) {
                Marker marker = mapMarkers.get(objId);
                marker.remove();
                mapMarkers.get(objId).remove();
                mapMarkers.remove(objId);
            }
        }
    }

    /*
     * Helper method to get the Parse GEO point representation of a location
     */
    private ParseGeoPoint geoPointFromLocation(Location loc) {
        return new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
    }

    /*
     * Displays a circle on the map representing the search radius
     */
    private void updateCircle(LatLng myLatLng) {
        if (mapCircle == null) {
            mapCircle =
                    mapFragment.getMap().addCircle(
                            new CircleOptions().center(myLatLng ).radius(radius * METERS_PER_FEET));
            int baseColor = Color.DKGRAY;
            mapCircle.setStrokeColor(baseColor);
            mapCircle.setStrokeWidth(2);
            mapCircle.setFillColor(Color.argb(50, Color.red(baseColor), Color.green(baseColor),
                    Color.blue(baseColor)));
        }
        mapCircle.setCenter(myLatLng);
        mapCircle.setRadius(radius * METERS_PER_FEET); // Convert radius in feet to meters.
    }

    /*@Override
    public void onBackPressed() {

    }*/

    /*
         * Zooms the map to show the area of interest based on the search radius
         */
    private void updateZoom(LatLng myLatLng) {
        // Get the bounds to zoom to
        LatLngBounds bounds = calculateBoundsWithCenter(myLatLng);
        // Zoom to the given bounds
        mapFragment.getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 5));
    }

    /*
     * Helper method to calculate the offset for the bounds used in map zooming
     */
    private double calculateLatLngOffset(LatLng myLatLng, boolean bLatOffset) {
        // The return offset, initialized to the default difference
        double latLngOffset = OFFSET_CALCULATION_INIT_DIFF;
        // Set up the desired offset distance in meters
        float desiredOffsetInMeters = radius * METERS_PER_FEET;
        // Variables for the distance calculation
        float[] distance = new float[1];
        boolean foundMax = false;
        double foundMinDiff = 0;
        // Loop through and get the offset
        do {
            // Calculate the distance between the point of interest
            // and the current offset in the latitude or longitude direction
            if (bLatOffset) {
                Location.distanceBetween(myLatLng.latitude, myLatLng.longitude, myLatLng.latitude
                        + latLngOffset, myLatLng.longitude, distance);
            } else {
                Location.distanceBetween(myLatLng.latitude, myLatLng.longitude, myLatLng.latitude,
                        myLatLng.longitude + latLngOffset, distance);
            }
            // Compare the current difference with the desired one
            float distanceDiff = distance[0] - desiredOffsetInMeters;
            if (distanceDiff < 0) {
                // Need to catch up to the desired distance
                if (!foundMax) {
                    foundMinDiff = latLngOffset;
                    // Increase the calculated offset
                    latLngOffset *= 2;
                } else {
                    double tmp = latLngOffset;
                    // Increase the calculated offset, at a slower pace
                    latLngOffset += (latLngOffset - foundMinDiff) / 2;
                    foundMinDiff = tmp;
                }
            } else {
                // Overshot the desired distance
                // Decrease the calculated offset
                latLngOffset -= (latLngOffset - foundMinDiff) / 2;
                foundMax = true;
            }
        } while (Math.abs(distance[0] - desiredOffsetInMeters) > OFFSET_CALCULATION_ACCURACY);
        return latLngOffset;
    }

    /*
     * Helper method to calculate the bounds for map zooming
     */
    LatLngBounds calculateBoundsWithCenter(LatLng myLatLng) {
        // Create a bounds
        LatLngBounds.Builder builder = LatLngBounds.builder();

        // Calculate east/west points that should to be included
        // in the bounds
        double lngDifference = calculateLatLngOffset(myLatLng, false);
        LatLng east = new LatLng(myLatLng.latitude, myLatLng.longitude + lngDifference);
        builder.include(east);
        LatLng west = new LatLng(myLatLng.latitude, myLatLng.longitude - lngDifference);
        builder.include(west);

        // Calculate north/south points that should to be included
        // in the bounds
        double latDifference = calculateLatLngOffset(myLatLng, true);
        LatLng north = new LatLng(myLatLng.latitude + latDifference, myLatLng.longitude);
        builder.include(north);
        LatLng south = new LatLng(myLatLng.latitude - latDifference, myLatLng.longitude);
        builder.include(south);

        return builder.build();
    }

    /*
     * Show a dialog returned by Google Play services for the connection error code
     */
    private void showErrorDialog(int errorCode) {
        // Get the error dialog from Google Play services
        Dialog errorDialog =
                GooglePlayServicesUtil.getErrorDialog(errorCode, this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);

            // Show the error dialog in the DialogFragment
            errorFragment.show(getSupportFragmentManager(), Application.APPTAG);
        }
    }

    /*
     * Define a DialogFragment to display the error dialog generated in showErrorDialog.
     */
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;

        /**
         * Default constructor. Sets the dialog field to null
         */
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        /*
         * Set the dialog to display
         *
         * @param dialog An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    // Converts time given in seconds to hh:mm:ss formatted string
    private String secondsToString(long seconds) {

        int millis = (int) (seconds * 1000);
        TimeZone tz = TimeZone.getTimeZone("UTC");
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(tz);
        String time = df.format(new Date(millis));
        return time;
    }

    private boolean initializeItt() {
        ParseQuery<TagPlayer> player = ParseQuery.getQuery("Player");
        player.getInBackground(playerObjectId, new GetCallback<TagPlayer>() {
            @Override
            public void done(TagPlayer tagPlayer, ParseException e) {
                if (isHostUser) {
                    tagPlayer.setItt();
                } else {
                    tagPlayer.setNotItt();
                }
                tagPlayer.saveInBackground();
            }
        });
        return true;
    }

    private void isItt () {

        ParseQuery<TagPlayer> ittPlay = ParseQuery.getQuery("Player");
        ittPlay.getInBackground(playerObjectId, new GetCallback<TagPlayer>() {
            @Override
            public void done(final TagPlayer ittPlayerLoc, ParseException e) {
                ittPlayer = ittPlayerLoc;
            }
        });

        ParseQuery<TagPlayer> itt = ParseQuery.getQuery("Player");
        itt.whereEqualTo("gameId",gameObjectId);
        itt.whereNotEqualTo("playerId", ParseUser.getCurrentUser().getUsername());
        itt.findInBackground(new FindCallback<TagPlayer>() {
            @Override
            public void done(List<TagPlayer> tagPlayers, ParseException e) {
                for(TagPlayer tagplayer: tagPlayers){

                    double distance = tagplayer.getLocation().distanceInKilometersTo(ittPlayer.getLocation());
                    if (distance <= 0.01 * tagRadius) {
                        ittPlayer.setNotItt();
                        tagplayer.setItt();
                        ittPlayer.saveInBackground();
                        tagplayer.saveInBackground();
                        break;
                    }
                }
            }
        });

    }

    /*
    private void autoTag(String gameObjectId, ParseUser currentUser) {
        final String gameIdFinal = gameObjectId;
        final ParseUser thisUser = currentUser;
        final ParseGeoPoint location = currentUser.getParseGeoPoint("location");
        final ParseQuery<ParseObject> self = ParseQuery.getQuery("Player");
        self.whereEqualTo("PlayerId", currentUser.getUsername());
        self.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                //query Parse for all players with a gameID equal to the current game's
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Player");
                query.whereEqualTo("gameId", gameIdFinal);
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> playerList, ParseException e) {
                        if (e == null) {
                            Log.d("score", "Player retrieved: " + playerList.size());
                            ParseObject taggedPlayer = null;
                            double lowDistance = radius;
                            for(ParseObject player: playerList) {
                                double distance = (1000 * player.getParseGeoPoint("location").distanceInKilometersTo(location));
                                if(distance > 0 && distance <= lowDistance) {
                                    taggedPlayer = player;
                                    lowDistance = distance;
                                }
                                if(taggedPlayer != null) {
                                    player.put("Itt", true);
                                    thisUser.put("Itt", false);
                                }

                            }
                        } else {
                            Log.d("score", "Error: " + e.getMessage());
                        }
                    }
                });
            }
        });
     */
}