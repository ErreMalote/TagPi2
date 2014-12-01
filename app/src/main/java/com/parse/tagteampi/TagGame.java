package com.parse.tagteampi;


import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;


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

    public String getUser() {
        return getString("host_user");
    }

    public void setUser(String value) {
        put("host_user", value);
    }

    public long getTime() {
        return getLong("gameDuration");
    }

    public void setTime(long value) {
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

    public static ParseQuery<TagGame> getQuery() {
        return ParseQuery.getQuery(TagGame.class);
    }
}
