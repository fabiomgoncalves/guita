package org.eclipselabs.guita.paintwidgets;

import java.io.Serializable;

public class Request implements Serializable{

	private String location;
	private String color;

	public Request(String location, String color){
		this.location = location;
		this.color = color;
	}
}
