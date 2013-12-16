package com.londonsales.londondash.shared;

import java.io.Serializable;

public class Tuple<X, Y> implements Serializable {
	private static final long serialVersionUID = 1L;
	public X x;
	public Y y;
	
	public Tuple() {}

	public Tuple(X x, Y y) {
		this.x = x;
		this.y = y;
	}
}
