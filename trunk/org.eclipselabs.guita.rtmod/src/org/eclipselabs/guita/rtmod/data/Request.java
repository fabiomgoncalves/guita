package org.eclipselabs.guita.rtmod.data;

import java.io.Serializable;
import java.util.LinkedList;

public class Request implements Serializable {
	private static final long serialVersionUID = 1L;

	private Location location;
	private LinkedList<String> parametros;
	private String metodo;
	
	public Request(Location location, LinkedList<String> parametros, String metodo) {
		this.location = location;
		this.setParametros(parametros);
		this.metodo = metodo;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public String getMetodo() {
		return metodo;
	}

	public void setMetodo(String metodo) {
		this.metodo = metodo;
	}

	public LinkedList<String> getParametros() {
		return parametros;
	}

	public void setParametros(LinkedList<String> parametros) {
		this.parametros = parametros;
	}	
}
