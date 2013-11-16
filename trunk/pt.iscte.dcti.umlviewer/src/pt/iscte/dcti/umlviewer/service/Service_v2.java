package pt.iscte.dcti.umlviewer.service;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

public interface Service_v2 {
	
	public void showFragment(Map<Class<?>, Collection<Method>> fragment_classes);

}
