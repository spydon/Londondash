package com.londonsales.londondash.client;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("stats")
public interface StatsService extends RemoteService {
	String getDataTable(String company, String stmt);
	String getString(String company, String stmt);
	HashMap<String, Integer> getRegions(String company);
	HashMap<String, String> getStores(String company);
    ArrayList<String> getProducts(String company);
}
