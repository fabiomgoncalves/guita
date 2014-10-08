package pt.iscte.dcti.umlviewer.network.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import pt.iscte.dcti.umlviewer.network.external.ClassDataTransferObject;

public class ByteClassLoader extends ClassLoader {

	private Map<String, Class<?>> loaded;

	private Map<String, ClassDataTransferObject> data;

	public ByteClassLoader(ClassLoader parent) {
		super(parent);
		loaded = new HashMap<String, Class<?>>();
	}

	public void setData(Map<String, ClassDataTransferObject> data) {
		this.data = data;
	}

	public Set<String> getAvailableClasses() {
		return Collections.unmodifiableSet(data.keySet());
	}

	public Class<?> loadClass(String className) throws ClassNotFoundException {
		Class<?> clazz = loaded.get(className);

		if(clazz == null) {
			try {
				if(className.startsWith("java.") || className.startsWith("sun.reflect") || 
						className.startsWith("org.aspectj")) { 
					clazz = getParent().loadClass(className);
				}
				else {		
					if(data.get(className) == null || data.get(className).getClassBytes() == null) {
						throw new ClassNotFoundException(className);
					}

					byte[] classBytes = data.get(className).getClassBytes();
					clazz = defineClass(className, classBytes, 0, classBytes.length);

					loaded.put(clazz.getName(), clazz);
				}
			}
			catch(SecurityException securityException) {
				return super.loadClass(className);
			}
		}

		return clazz;
	}

	public Collection<String> getClassMethods(String className) {
		return ((data.get(className) != null) ? data.get(className).getClassMethods() : null);
	}

}