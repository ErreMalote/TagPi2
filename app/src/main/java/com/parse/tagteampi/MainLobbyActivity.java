package com.parse.tagteampi;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
import com.parse.SaveCallback;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Reynaldo on 11/21/2014.
 */
public class MainLobbyActivity extends Activity {

    private ListView listView;
    private Button createGame;
    private double latitude;
    private double longitude;
    // Adapter for the Parse query
    private ParseQueryAdapter<TagGame> gamesQueryAdapter;
    private ArrayList<String> arr = new ArrayList<String>();
    private ArrayList<String> arr1 = new ArrayList<String>();
    private String gameId;
    private int loc;


    //private Button suspensionTestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_lobby);
        listView = (ListView) findViewById(R.id.GamesListView);


        createGame = (Button) findViewById(R.id.button_create_game);

        createGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), GameSettingsActivity.class);
                startActivity(i);
            }
        });
        /*
        suspensionTestButton = (Button) findViewById(R.id.button_test_suspension);

        suspensionTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getBaseContext(), SuspensionTesterActivity.class);
                startActivity(i);
            }
        });
*/
        //Listview stuff
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location lastLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);


        if (lastLocation != null) {
            latitude = lastLocation.getLatitude();
            longitude = lastLocation.getLongitude();

            final ParseGeoPoint geoPoint = new ParseGeoPoint(latitude, longitude);

            ParseQuery<TagGame> gamesQuery = ParseQuery.getQuery("Game");


            gamesQuery.findInBackground(new FindCallback<TagGame>() {
                @Override
                public void done(List<TagGame> list, ParseException e) {
                    for (TagGame aList : list) {
                        arr.add(aList.getUser());
                        arr1.add(aList.getObjectId());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, arr);
                    listView.setAdapter(adapter);
                }
            });

            listView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                    Toast.makeText(getApplicationContext(), "Hii" + position, Toast.LENGTH_SHORT).show();

                    final Intent toGame = new Intent(getBaseContext(), InGameActivity.class);


                    //Creates ActiveUser parseObject and relates it to Game parseObject
                    TagPlayer player1 = new TagPlayer();
                    player1.setPlayer(ParseUser.getCurrentUser().getUsername());
                    player1.setGame(arr1.get(position));
                    player1.setLocation(geoPoint);
                    player1.setTagCount(0);
                    toGame.putExtra("gameObjectId", arr1.get(position));
                    player1.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                //Saves the TagPlayer objectId to keys that can be

                                startActivity(toGame);
                            }
                        }
                    });


                }
            });
        }
        else{
            //Can not get current location - Advise User
            Toast.makeText(getBaseContext(), "Can not find GPS location.", Toast.LENGTH_LONG).show();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_logout:
                ParseUser.logOut();
                Intent intent = new Intent(this, DispatchActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK );
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    /*
    // Set up a customized query
        ParseQueryAdapter.QueryFactory<TagGame> factory =
                new ParseQueryAdapter.QueryFactory<TagGame>() {
                    public ParseQuery<TagGame> create() {
                        ParseQuery<TagGame> query = TagGame.getQuery();

                        return query;
                    }
                };

        // Set up the query adapter
        gamesQueryAdapter = new ParseQueryAdapter<TagGame>(this, factory) {
            @Override
            public View getItemView(TagGame games, View view, ViewGroup parent) {
                if (view == null) {
                    view = View.inflate(getContext(), R.layout.anywall_post_item, null);
                }
                ListView contentView = (ListView) view.findViewById(R.id.GamesListView);
                TextView usernameView = (TextView) view.findViewById(R.id.username_view);
                contentView.setAdapter(games.getUser());

                return view;
            }
        };

    query.whereWithinKilometers("location", geoPointFromLocation(myLoc), radius * METERS_PER_FEET / METERS_PER_KILOMETER);
                query.setLimit(MAX_POST_SEARCH_RESULTS);

*/

}