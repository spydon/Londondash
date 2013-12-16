package com.londonsales.londondash.shared;

import java.io.Serializable;
import java.util.HashMap;

public class Columnable implements Serializable {
	private static final long serialVersionUID = 1L;
	private HashMap<String, String> structure = new HashMap<String, String>();
	
	public Columnable() {}

	public String getValue(String key) {
		return structure.get(key);
	}
	
	public void setValue(String key, String value) {
		structure.put(key, value);
	}
	
	public HashMap<String, String> getStructure() {
		return structure;
	}
}
