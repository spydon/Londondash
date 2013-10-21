package com.londonsales.londondash.client;

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
}
