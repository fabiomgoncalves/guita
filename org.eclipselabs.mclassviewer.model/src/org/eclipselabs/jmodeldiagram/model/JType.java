package org.eclipselabs.jmodeldiagram.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.eclipselabs.jmodeldiagram.model.Util.*;

public  class JType extends StereotypedElement implements Iterable<JOperation> {
	private static final long serialVersionUID = 1L;

	private final String qualifiedName;
	private Visibility visibility;
	
	private List<JOperation> operations;
	
	JType(String qualifiedName) {
		super(qualifiedName);
		checkNotNull(qualifiedName);
		// TODO check pattern
		this.qualifiedName = qualifiedName;
		visibility = Visibility.PUBLIC;
		operations = Collections.emptyList();
	}

	public String getQualifiedName() {
		return qualifiedName;
	}
	
	public String getName() {
		int i = qualifiedName.indexOf('.'); 
		if(i == -1)
			return qualifiedName;
		else
			return qualifiedName.substring(i+1);
	}
	
	public String getPackage() {
		int i = qualifiedName.indexOf('.'); 
		if(i == -1)
			return "";
		else
			return qualifiedName.substring(0, i);
	}
	
	public boolean hasPackage() {
		return qualifiedName.indexOf('.') != -1;
	}
	
	
	void addOperation(JOperation operation) {
		checkNotNull(operation);
		
		if(operations.isEmpty())
			operations = new ArrayList<JOperation>();
		
		operations.add(operation);
	}
	
//	public Iterable<MMethod> getOperations() {
//		return Collections.unmodifiableList(operations);
//	}

	void setVisibilityInternal(Visibility visibility) {
		checkNotNull(visibility);
		
		this.visibility = visibility;
	}
	
	public Visibility getVisibility() {
		return visibility;
	}
	
	public boolean isInterface() {
		return getClass().equals(JInterface.class);
	}

	public boolean isClass() {
		return getClass().equals(JClass.class);
	}
	
	
	@Override
	public Iterator<JOperation> iterator() {
		return Collections.unmodifiableList(operations).iterator();
	}
	
}
