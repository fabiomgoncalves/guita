package org.eclipselabs.guita.request;

import java.io.Serializable;


public class Request implements Serializable {
	private static final long serialVersionUID = 1L;
	
	
	private String location;
	private String type;
	private Color color;
	private int order;
	
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

	public static Request newPaintRequest(String location, String type, Color color, int order) {
		return new Request(location, type, color, order);
	}
	
	public static Request newUnpaintRequest(String location, String type) {
		return new Request(location, type, null, 0);
	}
		
	private Request(String location, String type, Color color, int order){
		this.location = location;
		this.type = type;
		this.color = color;
	}

	public String getLocation(){
		return location;
	}
	
	public String getType(){
		return type;
	}

	public boolean isToRemove(){
		return color == null;
	}

	public Color getColor(){
		return color;
	}
	
	public int getOrder(){
		return order;
	}
}
