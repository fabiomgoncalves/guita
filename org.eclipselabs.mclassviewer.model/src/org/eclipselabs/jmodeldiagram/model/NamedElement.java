package org.eclipselabs.jmodeldiagram.model;

import static org.eclipselabs.jmodeldiagram.model.Util.*;

import java.io.Serializable;


public abstract class NamedElement implements Serializable {
	private static final long serialVersionUID = 1L;

	private final String name;
	
	NamedElement(String name) {
		checkNotNull(name);
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public final String toString() {
		return getClass().getSimpleName() + " " + name;
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
