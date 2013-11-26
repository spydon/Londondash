package com.londonsales.londondash.client;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>StatsService</code>.
 */
public interface StatsServiceAsync {
    void getDataTable(String company, String stmt,
            AsyncCallback<String> callback);
    void getString(String company, String stmt,
            AsyncCallback<String> callback);
    void getRegions(String company,
            AsyncCallback<HashMap<String, Integer>> callback);
    void getStores(String company,
            AsyncCallback<HashMap<String, String>> callback);
    void getProducts(String company,
            AsyncCallback<ArrayList<String>> callback);
    void getUsers(String company,
            AsyncCallback<ArrayList<String>> callback);
    void getStands(String company,
            AsyncCallback<ArrayList<String>> callback);
}
