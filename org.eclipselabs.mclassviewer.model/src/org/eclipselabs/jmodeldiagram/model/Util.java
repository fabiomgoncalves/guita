package org.eclipselabs.jmodeldiagram.model;

class Util {
	
	static void checkNotNull(Object o) {
		if(o == null)
			throw new NullPointerException("arg cannot be null");
	}
}
