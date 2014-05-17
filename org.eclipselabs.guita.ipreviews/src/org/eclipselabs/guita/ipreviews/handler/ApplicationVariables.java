package org.eclipselabs.guita.ipreviews.handler;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Layout;

public class ApplicationVariables {
	private Layout layout;
	private boolean toggle_debug;
	private static ApplicationVariables observer;
	
	private ApplicationVariables(){
		layout = new FillLayout();
		toggle_debug = false;
	}
	
	public static ApplicationVariables getInstance(){
		if(observer == null){
			observer = new ApplicationVariables();
		}
		return observer;
	}
	
	public void setLayout(Layout layout){
		this.layout = layout;
	}
	
	public Layout getLayout(){
		return layout;
	}
	
	public boolean getDebug(){
		return toggle_debug;
	}
	
	public void toggleDebug(){
		toggle_debug = !toggle_debug;
	}
	
	
	
	public void resetVariables(boolean reset_toggle) {
		if(reset_toggle)
			toggle_debug = false;
		try {
			layout = layout.getClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
