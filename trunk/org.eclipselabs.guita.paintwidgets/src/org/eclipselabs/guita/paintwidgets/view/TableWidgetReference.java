package org.eclipselabs.guita.paintwidgets.view;

import org.eclipse.swt.graphics.RGB;

public class TableWidgetReference {

	private String name;
	private String type;
	private String location;
	private String color;
	private int numberOfPaintedWidgets;
	private RGB colorRGB;

	public TableWidgetReference(String name, String type, String location, String color, int numberOfPaintedWidgets){
		this.name = name;
		this.type = type;
		this.location = location;
		this.color = color;
		this.numberOfPaintedWidgets = numberOfPaintedWidgets;

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
	
	public int getNumberOfPaintedWidgets(){
		return numberOfPaintedWidgets;
	}
	
	public RGB getColorRGB(){
		return colorRGB;
	}

	public void setColor(String color){
		this.color = color;
		setColorRGB(color);
	}
	
	public void setNumberPaintedWidgets(int number){
		this.numberOfPaintedWidgets = number;
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
