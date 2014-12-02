package com.parse.tagteampi;


import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Date;


/**
 * Data model for a post.
 */
@ParseClassName("Game")
public class TagGame extends ParseObject {

    public int getMapRadius() {
        return getInt("gameRadius");
    }

    public void setMapRadius(int value) {
        put("gameRadius", value);
    }

    public String getHostUser() {
        return getString("host_user");
    }

    public void setHostUser(String value) {
        put("host_user", value);
    }

    public long getGameDuration() {
        return getLong("gameDuration");
    }

    public void setGameDuration(long value) {
        put("gameDuration", value);
    }

    public int getTagLimit() {
        return getInt("tagLimit");
    }

    public void setTagLimit(int value) {
        put("tagLimit", value);
    }

    public int getTagRadius() {
        return getInt("tagRadius");
    }

    public void setTagRadius(int value) {
        put("tagRadius", value);
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint("location");
    }

    public void setLocation(ParseGeoPoint value) {
        put("location", value);
    }

    public Date getStartTime() { return getDate("startTime"); }

    public void setStartTime(Date value) { put("startTime", value); }

    public static ParseQuery<TagGame> getQuery() {
        return ParseQuery.getQuery(TagGame.class);
    }
}