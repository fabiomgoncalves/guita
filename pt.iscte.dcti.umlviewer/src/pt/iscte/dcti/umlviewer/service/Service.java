package pt.iscte.dcti.umlviewer.service;

import java.lang.reflect.Method;
import java.util.Set;

public interface Service {
	
	public void showFragment(Set<Class<?>> classes, Set<Method> methods);
	
}
