package org.eclipselabs.jmodeldiagram.model;

import static org.eclipselabs.jmodeldiagram.model.Util.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public  abstract class JType extends StereotypedElement implements Iterable<JOperation> {
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
		int i = qualifiedName.lastIndexOf('.'); 
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
	
	public void merge(JType type) {
		checkNotNull(type);
		if(isClass() && !type.isClass())
			throw new IllegalArgumentException("incompatible merge");
		
		if(!type.equals(type))
			throw new IllegalArgumentException("incompatible merge - types must be the same");
		
		for(JOperation o : type.operations)
			o.copyTo(this);
		
		internalMerge(type);
	}
	
	void internalMerge(JType type) {
		
	}

	@Override
	public Iterator<JOperation> iterator() {
		return Collections.unmodifiableList(operations).iterator();
	}
	
	@Override
	public String toString() {
		return qualifiedName + " " + (isClass() ? "[class]" : "[interface]");
	}
	
}
