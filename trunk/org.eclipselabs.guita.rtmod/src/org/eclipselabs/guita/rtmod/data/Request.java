package org.eclipselabs.guita.rtmod.data;

import java.io.Serializable;

public class Request implements Serializable {
	private static final long serialVersionUID = 1L;

	private Location location;
	private Object[] parameters;
	private String name;
	
	public Request(Location location, Object[] parameters, String name) {
		this.location = location;
		this.parameters = parameters;
		this.name = name;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}	
	
	public Object[] getParameters() {
		return parameters;
	}

	public void setParameters(Object[] parameters) {
		this.parameters = parameters;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
