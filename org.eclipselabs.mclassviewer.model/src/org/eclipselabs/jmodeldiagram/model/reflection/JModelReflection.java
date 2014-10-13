package org.eclipselabs.jmodeldiagram.model.reflection;

import java.lang.reflect.Method;

import org.eclipselabs.jmodeldiagram.model.JClass;
import org.eclipselabs.jmodeldiagram.model.JInterface;
import org.eclipselabs.jmodeldiagram.model.JOperation;

public class JModelReflection {

	public static JClass createClass(Class<?> c) {
		if(c.isInterface())
			throw new IllegalArgumentException("not a class - " + c.getName());
		
		JClass jc = new JClass(c.getName());

		for(Method m : c.getDeclaredMethods()) {
			new JOperation(jc, m.getName());
		}
		return jc;
	}
	
	
	public static JInterface createInterface(Class<?> c) {
		if(!c.isInterface())
			throw new IllegalArgumentException("not an interface - " + c.getName());
		
		JInterface ji = new JInterface(c.getName());
		for(Method m : c.getDeclaredMethods()) {
			new JOperation(ji, m.getName());
		}
		
		return ji;
	}
	
	
}
