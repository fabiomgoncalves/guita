package org.eclipselabs.jmodeldiagram.model;

import static org.eclipselabs.jmodeldiagram.model.Util.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class StereotypedElement extends NamedElement {

	private static final long serialVersionUID = 1L;
	
	private List<Stereotype> stereotypes;
	
	StereotypedElement(String name) {
		super(name);
		stereotypes = Collections.emptyList();
	}
	
	void addStereotypeInternal(Stereotype stereotype) {
		checkNotNull(stereotype);
		
		if(stereotypes.isEmpty())
			stereotypes = new ArrayList<Stereotype>(3);
		
		stereotypes.add(stereotype);
	}
}
