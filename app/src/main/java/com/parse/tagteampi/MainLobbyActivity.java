package com.parse.tagteampi;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
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
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
import com.parse.SaveCallback;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Reynaldo on 11/21/2014.
 */
public class MainLobbyActivity extends Activity {

    private ListView listView;
    private Button createGame;
    private Button suspensionTestButton;
    private ArrayList<ActiveGame> arr = new ArrayList<ActiveGame>();
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
                Intent toHostSettings = new Intent(getBaseContext(), GameSettingsActivity.class);
                toHostSettings.putExtra("isHost", true);
                startActivity(toHostSettings);
            }
        });

//        suspensionTestButton = (Button) findViewById(R.id.button_suspension_test);
//
//        suspensionTestButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent i = new Intent(getBaseContext(), SuspensionTesterActivity.class);
//                startActivity(i);
//            }
//        });

        // Customized Query: Gets all games which have been created, but haven't started yet,
        // in ascending order of creation time.
        ParseQueryAdapter.QueryFactory<TagGame> gamefactory = new ParseQueryAdapter.QueryFactory<TagGame>() {
            @Override
            public ParseQuery<TagGame> create() {

                ParseQuery<TagGame> query = TagGame.getQuery();
                query.whereEqualTo("startTime", null);
                query.orderByAscending("createdAt");
                return query;
            }
        };

        // Set up the query adapter
        final ParseQueryAdapter<TagGame> gameQueryAdapter = new ParseQueryAdapter<TagGame>(this, gamefactory) {
            @Override
            public View getItemView(TagGame game, View view, ViewGroup parent) {
                if (view == null) {
                    view = View.inflate(getContext(), R.layout.activity_main_lobby_gameview, null);
                }
                TextView hostuserView = (TextView) view.findViewById(R.id.hostuser_view);
                final TextView numplayersView = (TextView) view.findViewById(R.id.numplayers_view);
                hostuserView.setText(game.getHostUser());

                int numplayers;
                ParseQuery<TagPlayer> pquery = TagPlayer.getQuery();
                pquery.whereEqualTo("gameId", game.getObjectId());
                pquery.findInBackground(new FindCallback<TagPlayer>() {
                    @Override
                    public void done(List<TagPlayer> plist, ParseException e) {
                        if (plist != null) {
                            int num = 0;
                            for (int i = 0; i < plist.size(); i++) {
                                num++;
                            }
                            numplayersView.setText("Players:  " + num);
                        }

                    }
                });



                return view;
            }
        };

        gameQueryAdapter.setAutoload(true);

        // Attach the query adapter to the view
        ListView opengamesListView = (ListView) findViewById(R.id.GamesListView);
        opengamesListView.setAdapter(gameQueryAdapter);

        opengamesListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                //Toast.makeText(getApplicationContext(), "Hii" + position, Toast.LENGTH_SHORT).show();

                // class name
                final TagGame item = gameQueryAdapter.getItem(position);
                final String selectedGameObjectId = item.getObjectId();

                // check to see if the player is already in a game and thus shouldn't be trying to enter a new one
                //TODO: Uncomment this portion once we're done testing everything
                //TODO: If this query finds a game the player is already in, they shouldn't be allowed to enter the selected game
//                final ParseQuery<TagPlayer> pquery = ParseQuery.getQuery("Player");
//                pquery.findInBackground(new FindCallback<TagPlayer>() {
//                    public void done(final List<TagPlayer> playerList, ParseException e) {
//                        if (e == null) {
//                            for (int i = 0; i < playerList.size(); i++) {
//                                if( playerList.get(i).getString("playerId").equalsIgnoreCase(ParseUser.getCurrentUser().getUsername())){
//                                    Toast.makeText(getApplicationContext(), "You are already in an active game!", Toast.LENGTH_LONG).show();
//                                }
//                            }
//                        } else {
//                            Log.d("parseError", e.toString());
//                            // There was an error.
//                        }
//                    }
//                });


                final Intent toPlayerSettings = new Intent(getBaseContext(), GameSettingsActivity.class);
                toPlayerSettings.putExtra("gameObjectId", selectedGameObjectId);
                startActivity(toPlayerSettings);

            }
        });
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
}


class ActiveGame {
    private String gameHost;
    private String gameObjectId;

    public ActiveGame(String hostName, String gameObjId) {
        this.gameHost = hostName;
        this.gameObjectId = gameObjId;
    }

    public String getGameHost() {
        return this.gameHost;
    }

    public String getGameObjectId() {
        return this.gameObjectId;
    }
}