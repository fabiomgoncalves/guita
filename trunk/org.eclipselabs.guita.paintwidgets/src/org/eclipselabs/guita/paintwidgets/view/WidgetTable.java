package org.eclipselabs.guita.paintwidgets.view;

import org.eclipse.swt.graphics.RGB;

public class WidgetTable {

	private String name;
	private String type;
	private String location;
	private String color;
	private RGB colorRGB;

	public WidgetTable(String name, String type, String location, String color){
		this.name = name;
		this.type = type;
		this.location = location;
		this.color = color;

		setColorRGB(color);
	}

	public String getName(){
		return name;
	}

	public String getType(){
		return type;
	}

	public String getLocation(){
		return location;
	}

	public String getColor(){
		return color;
	}
	
	public RGB getColorRGB(){
		return colorRGB;
	}

	public void setColor(String color){
		this.color = color;
	}

	public void setColorRGB(String color){
		if(color.equals("Red")){
			colorRGB = new RGB(255, 0, 0);
		}else if(color.equals("Blue")){
			colorRGB = new RGB(0, 0, 255);
		}else if(color.equals("Green")){
			colorRGB = new RGB(0, 255, 0);
		}else if(color.equals("Yellow")){
			colorRGB = new RGB(255, 255, 0);
		}else{
			colorRGB = new RGB(255, 0, 255);
		}
	}
}
