package pt.iscte.dcti.umlviewer.network.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ByteClassLoader extends ClassLoader{

	private Map<String, byte[]> data;
	private Map<String, Class<?>> loaded;
	
	public ByteClassLoader(ClassLoader parent, Map<String, byte[]> data) {
		super(parent);
		this.data = data;
		loaded = new HashMap<String, Class<?>>();
	}

	public Class<?> loadClass(String name) throws ClassNotFoundException {
		Class<?> clazz = loaded.get(name);
		
		if(clazz == null) {
			try {
				if(name.startsWith("java.") || name.startsWith("sun.reflect")) { 
					clazz = getParent().loadClass(name);
				}
				else {		
					byte[] classData = data.get(name);
					if(classData == null)
						throw new ClassNotFoundException(name);

					clazz = defineClass(name, classData, 0, classData.length);
					loaded.put(name, clazz);
				}
			}
			catch(SecurityException securityException) {
				return super.loadClass(name);
			}
		}
		return clazz;
	}
	
	public Set<String> getAvailableClasses() {
		return Collections.unmodifiableSet(data.keySet());
	}
}
