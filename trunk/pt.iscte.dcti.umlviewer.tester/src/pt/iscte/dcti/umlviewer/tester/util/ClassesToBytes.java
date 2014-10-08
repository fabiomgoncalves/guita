package pt.iscte.dcti.umlviewer.tester.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import pt.iscte.dcti.umlviewer.common.ClassDataTransferObject;


public class ClassesToBytes {

	public static Map<String, ClassDataTransferObject> getClassBytes(Map<Class<?>, Collection<String>> fragmentClasses) {
		Map<String, ClassDataTransferObject> data = new HashMap<String, ClassDataTransferObject>();

		for(Entry<Class<?>, Collection<String>> entry: fragmentClasses.entrySet()) {
			try {
				byte[] classBytes = getClassBytes(entry.getKey());
				ClassDataTransferObject classDTO = new ClassDataTransferObject(classBytes, entry.getValue());

				data.put(entry.getKey().getName(), classDTO);
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
		finally {
			if(iStream != null) {
				iStream.close();
			}
		}
	}

}