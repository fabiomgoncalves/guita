package pt.iscte.dcti.umlviewer.tester.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ClassesToBytes {
	
	public static Map<String, byte[]> getClassBytes(Collection<Class<?>> classes) {
		Map<String, byte[]> data = new HashMap<String, byte[]>();
		for(Class<?> c : classes) {
			try {
				byte[] classBytes = getClassBytes(c);
				data.put(c.getName(), classBytes);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return data;
	}
	
	private static byte[] getClassBytes(Class<?> clazz) throws IOException {
		String name = clazz.getName().replace('.', '/') + ".class";
		InputStream iStream = clazz.getClassLoader().getResourceAsStream(name);
		try {
			byte[] classBytes = new byte[iStream.available()];			
			iStream.read(classBytes);
			return classBytes;
		}
		finally
		{
			iStream.close();
		}
	}

}
