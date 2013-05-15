package pt.iscte.dcti.umlviewer.tester.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import pt.iscte.dcti.umlviewer.network.external.ClassDataTransferObject;

public class ClassesToBytes {
	
	public static Map<String, ClassDataTransferObject> getClassBytes(Map<Class<?>, Collection<String>> fragment_classes) {
		Map<String, ClassDataTransferObject> data = new HashMap<String, ClassDataTransferObject>();
		for(Entry<Class<?>, Collection<String>> entry: fragment_classes.entrySet()) {
			try {
				byte[] class_bytes = getClassBytes(entry.getKey());
				ClassDataTransferObject aux = new ClassDataTransferObject(class_bytes, entry.getValue());
				data.put(entry.getKey().getName(), aux);
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
			byte[] class_bytes = new byte[iStream.available()];			
			iStream.read(class_bytes);
			return class_bytes;
		}
		finally
		{
			iStream.close();
		}
	}

}
