package com.parse.tagteampi;

import android.location.Criteria;

import com.parse.LocationCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * Data model for a post.
 */
@ParseClassName("Game")
public class TagGame extends ParseObject {

    public String getName() {
        return getString("text");
    }

    public void setName(String value) {
        put("text", value);
    }

    public ParseUser getUser() {
        return getParseUser("user");
    }

    public void setUser(ParseUser value) {
        put("user", value);
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
