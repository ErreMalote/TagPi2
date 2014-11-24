package com.parse.tagteampi;


import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by Owner on 11/22/2014.
 */
@ParseClassName("ActiveUsers")
public class ActiveUsers extends ParseObject {

    public void setUserId(String value) {
        put("userId", value);
    }

    public String getUserId() {
        return getString("userId");
    }

    public void setGameId(String value) {
        put("gameId", value);
    }

    public String getGameId() {
        return getString("gameId");
    }

    public static ParseQuery<ActiveUsers> getQuery() {
        return ParseQuery.getQuery(ActiveUsers.class);
    }
}
