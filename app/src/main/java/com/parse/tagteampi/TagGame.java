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

    public int getRadious() {
        return getInt("gameRadius");
    }

    public void setRadious(int value) {
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
        put("TagLimit", value);
    }

    public int getTagRadious() {
        return getInt("tagRadius");
    }

    public void setTagRadious(int value) {
        put("TagRadius", value);
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
