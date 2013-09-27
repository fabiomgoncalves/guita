package org.eclipselabs.guita.codetrace.common;

import java.io.Serializable;



public class Request implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private VariableInfo info;
	private String name;
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
		
		public static final Color WHITE = new Color(255, 255, 255);
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

	public static Request newPaintRequest(String location, VariableInfo info, Color color, String name) {
		return new Request(location, info, color, name);
	}
	
	public static Request newUnpaintRequest(String location, VariableInfo info, String name) {
		return new Request(location, info, null, name);
	}
	
	public static Request newPaintClassRequest(String className, Color color) {
		return new Request(className, null, color, null);
	}
		
	private Request(String location, VariableInfo info, Color color, String name){
		this.location = location;
		this.info = info;
		this.color = color;
		this.name = name;
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
	
	public String getName(){
		return name;
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
	
	public boolean isVariableRequest() {
		return info != null;
	}
	
	public boolean isClassRequest() {
		return info == null;
	}
	
	public boolean isPaintRequest() {
		return color != null;
	}
	
}
