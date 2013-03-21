package org.eclipselabs.guita.paintwidgets.view;

public class WidgetReference {

	private String name;
	private String type;
	private String localization;

	public WidgetReference(String name, String type, String localization){
		this.name = name;
		this.type = type;
		this.localization = localization;
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
}
