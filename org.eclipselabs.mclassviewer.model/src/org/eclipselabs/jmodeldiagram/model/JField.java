package org.eclipselabs.jmodeldiagram.model;

import static org.eclipselabs.jmodeldiagram.model.Util.checkNotNull;

public final class JField extends StereotypedElement {
	private static final long serialVersionUID = 1L;
	
	private final JClass owner;
	private final JType type;
	private Visibility visibility;
	
	public JField(JClass owner, String name, JType type) {
		super(name);
		checkNotNull(owner, name, type);
		this.owner = owner;
		this.type = type;
		visibility = Visibility.PRIVATE;
		owner.addField(this);
	}
	
	public JType getType() {
		return type;
	}
	
	public Visibility getVisibility() {
		return visibility;
	}
	
	
}
