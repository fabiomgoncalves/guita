package org.eclipselabs.guita.request;
import java.io.Serializable;




public class Request implements Serializable {
	private static final long serialVersionUID = 1L;
	
	
	private String location;
//	private String color;
	private Color colorRGB;
	
	public static class Color implements Serializable {
		private static final long serialVersionUID = 1L;

		private final int r,g,b;
		public Color(int r, int g, int b) {
			this.r = r;
			this.g = g;
			this.b = b;
		}
		
		public static final Color RED = new Color(255, 0, 0);
		public static final Color BLUE = new Color(0, 0, 255);;
		public static final Color GREEN = new Color(0, 255, 0);;
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
		this.colorRGB = color;

//		if(color != null)
//			setColorRGB(color);
	}

	public String getLocation(){
		return location;
	}

	public boolean isToRemove(){
		return colorRGB == null;
	}

	public Color getColorRGB(){
		return colorRGB;
	}

//	public void setColorRGB(String color){
//		if(color.equals("Red")){
//			colorRGB = new RGB(255, 0, 0);
//		}else if(color.equals("Blue")){
//			colorRGB = new RGB(0, 0, 255);
//		}else if(color.equals("Green")){
//			colorRGB = new RGB(0, 255, 0);
//		}else if(color.equals("Yellow")){
//			colorRGB = new RGB(255, 255, 0);
//		}else{
//			colorRGB = new RGB(255, 0, 255);
//		}
//	}
}
