package org.eclipselabs.guita.request;

import java.io.Serializable;

import org.eclipselabs.guita.variableanalyzer.service.VariableInfo;


public class Request implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private VariableInfo info;
	private String location;
	private Color color;
	
	public static class Color implements Serializable {
		private static final long serialVersionUID = 1L;

		private final int r,g,b;
		
		public Color(int r, int g, int b) {
			this.r = r;
			this.g = g;
			this.b = b;
		}
		
		public static final Color RED = new Color(255, 0, 0);
		public static final Color GREEN = new Color(0, 255, 0);;
		public static final Color BLUE = new Color(0, 0, 255);;
		public static final Color YELLOW = new Color(255, 255, 0);
		public static final Color PINK = new Color(255, 0, 255);

		public int getR() {
			return r;
		}
		
		public int getG() {
			return g;
		}
		
		public int getB() {
			return b;
		}
	}

	public static Request newPaintRequest(String location, VariableInfo info, Color color) {
		return new Request(location, info, color);
	}
	
	public static Request newUnpaintRequest(String location, VariableInfo info) {
		return new Request(location, info, null);
	}
		
	private Request(String location, VariableInfo info, Color color){
		this.location = location;
		this.info = info;
		this.color = color;
	}

	public String getLocation(){
		return location;
	}
	
	public String getType(){
		return info.getType();
	}

	public boolean isToRemove(){
		return color == null;
	}

	public Color getColor(){
		return color;
	}
	
	public int getOrder(){
		return info.getLineExecutionOrder();
	}
	
	public int getTotalVars(){
		return info.getTotalVars();
	}
	
	public int getTotalVarsOfType(){
		return info.getNumberOfVarsWithSameType();
	}
	
	public int getTotalVarsOfName(){
		return info.getNumberOfVarsWithSameName();
	}
}
