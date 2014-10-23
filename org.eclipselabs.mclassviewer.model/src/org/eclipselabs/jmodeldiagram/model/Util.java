package org.eclipselabs.jmodeldiagram.model;

class Util {
	
	static void checkNotNull(Object ...v) {
		for(Object o : v)
			if(o == null)
				throw new NullPointerException("arg cannot be null");
		
	}
}
