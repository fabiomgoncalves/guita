package org.eclipselabs.guita.paintwidgets.view;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class WidgetReference {

	private String name;
	private String type;
	private String localization;
	private BufferedImage img;

	public WidgetReference(String name, String type, String localization){
		this.name = name;
		this.type = type;
		this.localization = localization;
		try {                
			img = ImageIO.read(new File("Icons/Delete.png"));
		} catch (IOException ex) {
		}
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
	
	public BufferedImage getImage(){
		return img;
	}
}
