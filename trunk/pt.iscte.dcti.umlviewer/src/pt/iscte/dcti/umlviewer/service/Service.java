package pt.iscte.dcti.umlviewer.service;

import java.util.Collection;
import java.util.Map;

public interface Service {
	
	public void showFragment(Map<Class<?>, Collection<String>> fragment_classes);
	
	//public void showFragment(Collection<Class<?>> classes, MethodFilter filter);
}
