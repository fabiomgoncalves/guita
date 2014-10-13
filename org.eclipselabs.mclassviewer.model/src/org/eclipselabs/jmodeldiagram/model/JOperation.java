package org.eclipselabs.jmodeldiagram.model;

import java.util.List;

import static org.eclipselabs.jmodeldiagram.model.Util.*;


public class JOperation extends NamedElement {

	private JType owner;
	private Visibility visibility;
	private boolean isAbstract;
	private boolean isStatic;
	private List<JClass> params;
	private JType returnType;
	
	public JOperation(JType owner, String name) {
		super(name);
		checkNotNull(owner);
		this.owner = owner;
		owner.addOperation(this);
		visibility = Visibility.PUBLIC;
		isAbstract = false;
		isStatic = false;
	}
	
	public JType getOwner() {
		return owner;
	}
	
	public Visibility getVisibility() {
		return visibility;
	}

	public boolean isAbstract() {
		return isAbstract;
	}
	
	public boolean isStatic() {
		return isStatic;
	}
	
	// TODO
	@Override
	boolean equalsInternal(Object obj) {
		return super.equalsInternal(obj);
	}
	
	@Override
	int hashCodeInternal() {
		return super.hashCodeInternal();
	}
}

