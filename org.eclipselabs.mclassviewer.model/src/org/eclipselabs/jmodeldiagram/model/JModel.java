package org.eclipselabs.jmodeldiagram.model;

import static org.eclipselabs.jmodeldiagram.model.Util.checkNotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class JModel implements Iterable<JType>, Serializable {
	private static final long serialVersionUID = 1L;
	
	private final Set<JPackage> packages;
	
	private final Map<String, JType> types;
	private String description;
	
	public JModel() {
		types = new HashMap<String, JType>();
		packages = new HashSet<JPackage>();
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
	
	public Iterator<JPackage> getPackages() {
		return Collections.unmodifiableSet(packages).iterator();
	}
	
	public Iterator<JClass> classes() {
		
		return new Iterator<JClass>() {
			
			private JType[] array = types.values().toArray(new JType[types.values().size()]);
			private int i = 0;
			
			@Override
			public boolean hasNext() {
				if(i == array.length)
					return false;
				else
					for(int j = i; j < array.length; j++)
						if(array[j] instanceof JClass)
							return true;
				
				return false;
			}

			@Override
			public JClass next() {
				if(!hasNext())
					throw new IllegalStateException("iterator !hasNext");
				
				while(!(array[i] instanceof JClass) && i != array.length)
					i++;
				
				return (JClass) array[i];
			}
			
		};
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
	
	public Collection<JType> merge(JModel model) {
		Collection<JType> excluded = new ArrayList<JType>();
		for(JType ft : model) {
			String qName = ft.getQualifiedName();
			if(hasType(qName)) {
				JType t = getType(qName);
				if(t.equals(ft))
					t.merge(ft);
				else
					excluded.add(ft);
			}
			else {
				addType(ft);
			}
		}
		return excluded;
	}
	
	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		
		for(JType t : types.values()) {
			s.append(t + "\n");
			for(JOperation o : t)
				s.append("\t" + o + "\n");
			s.append("\n");
		}
		return s.toString();
	}
	
	// TODO
	public String getCommonSubPackageName() {
		return "";
	}

}
