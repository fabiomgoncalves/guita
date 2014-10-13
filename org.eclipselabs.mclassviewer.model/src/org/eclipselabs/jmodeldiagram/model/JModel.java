package org.eclipselabs.jmodeldiagram.model;

import static org.eclipselabs.jmodeldiagram.model.Util.checkNotNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JModel implements Iterable<JType>, Serializable {

	private final Map<String, JType> types;
	private String description;
	
	public JModel() {
		types = new HashMap<String, JType>();
	}
	
	public JModel(String description) {
		this();
		this.description = description;
	}
	
	public void addType(JType type) {
		checkNotNull(type);
		types.put(type.getQualifiedName(), type);
	}
	
	@Override
	public Iterator<JType> iterator() {
		return Collections.unmodifiableCollection(types.values()).iterator();
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean hasDescription() {
		return description != null;
	}

	public JType getType(String qualifiedName) {
		return types.get(qualifiedName);
	}

	public boolean hasType(String qualifiedName) {
		return types.containsKey(qualifiedName);
	}
	
	

}
