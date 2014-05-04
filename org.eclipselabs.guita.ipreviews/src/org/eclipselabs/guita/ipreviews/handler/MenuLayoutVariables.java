package org.eclipselabs.guita.ipreviews.handler;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Layout;

public class MenuLayoutVariables {
	private Layout layout;
	private static MenuLayoutVariables observer;
	
	private MenuLayoutVariables(){
		layout = new FillLayout();
	}
	
	public static MenuLayoutVariables getInstance(){
		if(observer == null){
			observer = new MenuLayoutVariables();
		}
		return observer;
	}
	
	public void setLayout(Layout layout){
		this.layout = layout;
	}
	
	public Layout getLayout(){
		return layout;
	}
}
