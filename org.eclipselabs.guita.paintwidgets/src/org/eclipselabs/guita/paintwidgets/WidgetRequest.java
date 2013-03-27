package org.eclipselabs.guita.paintwidgets;

import org.eclipse.swt.graphics.RGB;

public class WidgetRequest {

	private String location;
	private RGB colorRGB;

	public WidgetRequest(String location, String color){
		this.location = location;
		
		if(color.equals("Red")){
			colorRGB = new RGB(255, 0, 0);
		}else if(color.equals("Blue")){
			colorRGB = new RGB(0, 0, 255);
		}else if(color.equals("Green")){
			colorRGB = new RGB(0, 255, 0);
		}else if(color.equals("Yellow")){
			colorRGB = new RGB(255, 255, 0);
		}else if(color.equals("Pink")){
			colorRGB = new RGB(255, 0, 255);
		}else{
			colorRGB = null;
		}
	}

	public String getLocation(){
		return location;
	}

	public RGB getColorRGB(){
		return colorRGB;
	}
}
