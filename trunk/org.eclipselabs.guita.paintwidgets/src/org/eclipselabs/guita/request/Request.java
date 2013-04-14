package org.eclipselabs.guita.request;

import java.io.Serializable;


public class Request implements Serializable {
	private static final long serialVersionUID = 1L;
	
	
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

	public static Request newPaintRequest(String location, Color color) {
		return new Request(location, color);
	}
	
	public static Request newUnpaintRequest(String location) {
		return new Request(location, null);
	}
		
	private Request(String location, Color color){
		this.location = location;
		this.color = color;
	}

	public String getLocation(){
		return location;
	}

	public boolean isToRemove(){
		return color == null;
	}

	public Color getColor(){
		return color;
	}
}
