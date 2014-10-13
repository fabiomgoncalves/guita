package org.eclipselabs.jmodeldiagram.model;

public class JField extends StereotypedElement {

	private final String name;
	private final JClass type;
	private Visibility visibility;
	
	public JField(String name, JClass type) {
		super(name);
		this.name = name;
		this.type = type;
	}
}
