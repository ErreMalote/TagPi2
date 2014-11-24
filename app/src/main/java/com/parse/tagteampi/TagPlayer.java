package com.parse.tagteampi;


import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Date;
import java.util.List;


/**
 * Data model for a Player in a game. Players update this table every 2 seconds, or face
 * suspension.
 */
@ParseClassName("Player")
public class TagPlayer extends ParseObject {
    // gameId
    public String getGame() {
        return getString("gameId");
    }

    public void setGame(String value) {
        put("gameId", value);
    } // testing only, should be handled automatically

    // playerId
    public ParseUser getPlayer() {
        return getParseUser("playerId");
    }

    public void setPlayer(ParseUser value) {
        put("playerId", value.getUsername());
    }

    // location
    public ParseGeoPoint getLocation() {
        return getParseGeoPoint("location");
    }

    public void setLocation(ParseGeoPoint value) {
        put("location", value);
    }

    // tagCount
    public int getTagCount() {
        return getInt("tagCount");
    }

    public void setTagCount(int value) {
        put("tagCount", value);
    }

    // itt
    public boolean isItt() {
        return getBoolean("itt");
    }

    public void setItt() {
        put("itt", true);
    }

    public void setNotItt() {
        put("itt", false);
    }

    public void tagItt(String gameId, ParseUser player) {
        final String taggedplayer = player.getUsername();

        ParseQuery<TagPlayer> query = ParseQuery.getQuery("Player");
        query.whereEqualTo("gameId", gameId);
        query.findInBackground(new FindCallback<TagPlayer>() {
            public void done(List<TagPlayer> tp, ParseException e) {
                if (e == null) {
                    Log.d("gameId", "Retrieved " + tp.size() + "players.\n");
                    for (int i = 0; i < tp.size(); i++) {
                        if (tp.get(i).getPlayer().getUsername().equals(taggedplayer)) {
                            tp.get(i).getPlayer().put("itt", true); // Tag, you're itt!
                            Log.d("playerId", "Tagged " + tp.get(i).getPlayer() + "\n");
                        } else {
                            tp.get(i).getPlayer().put("itt", false);// Not itt!
                        }
                    }
                } else {
                    Log.d("gameId", "Tag Failed.");
                }
            }
        });
    }

    // createdAt
    public Date getCreatedAt() {
        return getDate("createdAt");
    }

    // updatedAt
    public Date getUpdatedAt() {
        return getDate("updatedAt");
    }

    public static ParseQuery<TagPlayer> getQuery() {
        return ParseQuery.getQuery(TagPlayer.class);
    }
}