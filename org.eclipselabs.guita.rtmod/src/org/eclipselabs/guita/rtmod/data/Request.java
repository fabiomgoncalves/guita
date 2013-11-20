package org.eclipselabs.guita.rtmod.data;

import java.io.Serializable;

public class Request implements Serializable {
	private static final long serialVersionUID = 1L;

	private Location location;
	private String valor;
	private String metodo;
	
	public Request(Location location, String valor, String metodo) {
		this.location = location;
		this.valor = valor;
		this.metodo = metodo;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	public String getMetodo() {
		return metodo;
	}

	public void setMetodo(String metodo) {
		this.metodo = metodo;
	}	
}
