package org.eclipselabs.guita.paintwidgets.view;

public class WidgetReference {

	private String name;
	private String type;
	private String localization;
	private String color;

	public WidgetReference(String name, String type, String localization, String color){
		this.name = name;
		this.type = type;
		this.localization = localization;
		this.color = color;
	}
	
	public String getName(){
		return name;
	}
	
	public String getType(){
		return type;
	}
	
	public String getLocalization(){
		return localization;
	}
	
	public String getColor(){
		return color;
	}
	
	public void setColor(String color){
		this.color = color;
	}
}
