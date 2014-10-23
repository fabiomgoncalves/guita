package org.eclipselabs.jmodeldiagram.model;

import static org.eclipselabs.jmodeldiagram.model.Util.checkNotNull;

import java.util.List;


public class JOperation extends NamedElement {

	private static final long serialVersionUID = 1L;

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
	
	JOperation copyTo(JType type) {
		JOperation op = new JOperation(type, getName());
		op.visibility = visibility;
		op.isAbstract = isAbstract;
		op.isStatic = isStatic;
//		op.params = ne
		op.returnType = returnType;
		return op;
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
	
	@Override
	public String toString() {
		return visibility.symbol() + " " + getName() + "(...)";
	}
}

