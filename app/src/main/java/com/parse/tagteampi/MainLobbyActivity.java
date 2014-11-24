package com.parse.tagteampi;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
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
    private ArrayList<String> arr = new ArrayList<String>();

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
        final ParseQuery<ParseObject> createdGames = ParseQuery.getQuery("Game");

        createdGames.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                for (int i = 0; i < list.size(); i++) {
                    arr.add(list.get(i).getString("host_user"));
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, arr);

                listView.setAdapter(adapter);
            }
        });


        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "Hii" + position, Toast.LENGTH_SHORT).show();

                // class name
                final String gameName = arr.get(position);

                //Creates ActiveUser parseObject and relates it to Game parseObject
                final ActiveUsers activeUser = new ActiveUsers();
                activeUser.setUserId(ParseUser.getCurrentUser().getUsername());
                /*
                activeUser.setGameId(createdGames.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public String done(List<ParseObject> list, ParseException e) {
                        for (int i = 0; i < list.size(); i++) {
                            if(list.get(i).getString("host_user").equals(gameName)){
                                return list.get(i).getObjectId();
                            }else{
                                return "";
                            }
                        }
                    };
                }));

                activeUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            //Saves the ActiveUsers objectId to keys that can be
                            // accessed by the activity (InGameLobbyActivity??)
                            // that follows.
                            String activeUserObjectId = activeUser.getObjectId();
                            toLobby.putExtra("activeUserObjectId", activeUserObjectId);
                            startActivity(toLobby);
                        }
                    }
                });
                Intent i = new Intent(MainLobbyActivity.this, LobbyActivity.class);

                startActivity(i);*/
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