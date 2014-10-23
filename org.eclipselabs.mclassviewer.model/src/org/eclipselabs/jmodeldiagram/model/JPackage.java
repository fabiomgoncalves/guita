package org.eclipselabs.jmodeldiagram.model;

import java.util.Collections;
import java.util.Set;

public final class JPackage extends NamedElement {
	
	private static final long serialVersionUID = 1L;

	public JPackage(String name) {
		super(name);
	}
	
	// TODO
	public Set<JPackage> calculateDependencies() {
		return Collections.emptySet();
	}
}
