package org.eclipselabs.guita.codetrace.plugin.view;

import org.eclipse.swt.graphics.RGB;
import org.eclipselabs.guita.codetrace.common.VariableInfo;

public class TableWidgetReference {

	private String name;
	private VariableInfo info;
	private String location;
	private String color;
	private int numberOfPaintedWidgets;
	private RGB colorRGB;

	public TableWidgetReference(String name, VariableInfo info, String location, String color, int numberOfPaintedWidgets){
		this.name = name;
		this.info = info;
		this.location = location;
		this.color = color;
		this.numberOfPaintedWidgets = numberOfPaintedWidgets;

		setColorRGB(color);
	}

	public String getName(){
		return name;
	}

	public String getType(){
		return info.getType();
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
	
	public int getOrder(){
		return info.getLineExecutionOrder();
	}
	
	public RGB getColorRGB(){
		return colorRGB;
	}
	
	public VariableInfo getInfo(){
		return info;
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
