package pt.iscte.dcti.umlviewer.service;

import java.lang.reflect.Method;

public interface ClickHandler {
	void classClicked(Class<?> c);
	
	void methodClicked(Method m);
	
}
