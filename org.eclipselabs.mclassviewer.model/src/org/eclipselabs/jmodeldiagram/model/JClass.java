package org.eclipselabs.jmodeldiagram.model;

import static org.eclipselabs.jmodeldiagram.model.Util.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class JClass extends JType {
	
	private static final long serialVersionUID = 1L;
	
	private boolean isAbstract;
	private JClass superclass;
	private Set<JInterface> supertypes;
	private List<JField> fields;
	private Set<Association> associations;
	
	public JClass(String name) {
		super(name);
		isAbstract = false;
		superclass = null;
		supertypes = Collections.emptySet();
		fields = Collections.emptyList();
		associations = Collections.emptySet();
	}
	
	public static JClass create(String name) {
		JClass c = new JClass(name);
		return c;
	}
	
	public JClass setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
		return this;
	}
	
	public JClass setSuperclass(JClass superclass) {
		this.superclass = superclass;
		return this;
	}
	
	public JClass addStereotype(Stereotype stereotype) {
		addStereotypeInternal(stereotype);
		return this;
	}
	
	public JClass setVisibility(Visibility visibility) {
		setVisibilityInternal(visibility);
		return this;
	}
	
	void addField(JField field) {
		checkNotNull(field);
		if(fields.isEmpty())
			fields = new ArrayList<JField>(5);
		
		fields.add(field);
	}
	
	
	public void addInterface(JInterface type) {
		checkNotNull(type);
		if(supertypes.isEmpty())
			supertypes = new HashSet<JInterface>(3);
		
		supertypes.add(type);
		
	}
	
	void addAssociation(Association a) {
		checkNotNull(a);
		if(associations.isEmpty())
			associations = new HashSet<Association>(5);
		
		associations.add(a);
	}
	
	public boolean isAbstract() {
		return isAbstract;
	}
	
	public boolean hasSuperclass() {
		return superclass != null;
	}
	
	public JClass getSuperclass() {
		return superclass;
	}
	
	public boolean implementsInterfaces() {
		return !supertypes.isEmpty();
	}

	public Iterable<JInterface> getInterfaces() {
		return Collections.unmodifiableSet(supertypes);
	}

	public Iterable<Association> getAssociations() {
		return Collections.unmodifiableSet(associations);
	}

	@Override
	void internalMerge(JType type) {
		JClass ctype = (JClass) type;
		// merge fields
		
	}

	
	
}
