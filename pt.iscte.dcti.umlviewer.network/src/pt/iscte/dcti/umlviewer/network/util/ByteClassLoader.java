package pt.iscte.dcti.umlviewer.network.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import pt.iscte.dcti.umlviewer.network.external.ClassDataTransferObject;

public class ByteClassLoader extends ClassLoader{
	
	private Map<String, Class<?>> loaded;
	
	private Map<String, ClassDataTransferObject> data;
	
	public ByteClassLoader(ClassLoader parent) {
		super(parent);
		loaded = new HashMap<String, Class<?>>();
	}
	
	public void setData(Map<String, ClassDataTransferObject> data) {
		this.data = data;
	}

	public Class<?> loadClass(String class_name) throws ClassNotFoundException {
		Class<?> clazz = loaded.get(class_name);
		
		if(clazz == null) {
			try {
				if(class_name.startsWith("java.") || class_name.startsWith("sun.reflect") || 
						class_name.startsWith("org.aspectj")) { 
					clazz = getParent().loadClass(class_name);
				}
				else {		
					if(data.get(class_name) == null || data.get(class_name).getClassBytes() == null) {
						throw new ClassNotFoundException(class_name);
					}
					byte[] class_bytes = data.get(class_name).getClassBytes();
					clazz = defineClass(class_name, class_bytes, 0, class_bytes.length);
					loaded.put(clazz.getName(), clazz);
				}
			}
			catch(SecurityException securityException) {
				return super.loadClass(class_name);
			}
		}
		return clazz;
	}
	
	public Set<String> getAvailableClasses() {
		return Collections.unmodifiableSet(data.keySet());
	}
	
	public Collection<String> getClassMethods(String class_name) {
		return ((data.get(class_name) != null) ? data.get(class_name).getClassMethods() : null);
	}

}
