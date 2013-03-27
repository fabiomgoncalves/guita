package org.eclipselabs.guita.paintwidgets.view;

public class WidgetReference {

	private String name;
	private String type;
	private String location;
	private String color;

	public WidgetReference(String name, String type, String location, String color){
		this.name = name;
		this.type = type;
		this.location = location;
		this.color = color;
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
	
	public void setColor(String color){
		this.color = color;
	}
}
