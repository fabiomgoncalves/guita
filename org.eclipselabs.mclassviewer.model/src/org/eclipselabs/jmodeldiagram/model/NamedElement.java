package org.eclipselabs.jmodeldiagram.model;

import static org.eclipselabs.jmodeldiagram.model.Util.checkNotNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public abstract class NamedElement implements Serializable {
	private static final long serialVersionUID = 1L;

	private final String name;
	private Map<String, String> props;
	
	NamedElement(String name) {
		checkNotNull(name);
		this.name = name;
		props = Collections.emptyMap();
	}
	
	public String getName() {
		return name;
	}
	
//	@Override
//	public final String toString() {
//		return getClass().getSimpleName() + " " + name;
//	}
	
	public String getProperty(String key) {
		checkNotNull(key);
		return props.get(key);
	}
	
	public boolean hasProperty(String key) {
		checkNotNull(key);
		return props.containsKey(key);
	}
	
	public void setProperty(String key, String value) {
		checkNotNull(key);
		checkNotNull(value);
		
		if(props.isEmpty())
			props = new HashMap<String, String>();
		
		props.put(key, value);
	}
	
	
	@Override
	public final boolean equals(Object obj) {
		return
				obj != null &&
				getClass().equals(obj.getClass()) && 
				((NamedElement) obj).name.equals(name) &&
				equalsInternal(obj);
	}
	
	boolean equalsInternal(Object obj) {
		return true;
	}
	
	// cuidado método
	@Override
	public final int hashCode() {
		return name.hashCode() + hashCodeInternal();
	}
	
	int hashCodeInternal() {
		return 0;
	}
}
