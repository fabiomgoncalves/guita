package pt.iscte.dcti.umlviewer.network.external;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;

public class ClassDataTransferObject implements Serializable {

	private final byte[] classBytes;
	private final Collection<String> classMethods;

	public ClassDataTransferObject(byte[] classBytes, Collection<String> classMethods) {
		this.classBytes = classBytes;
		this.classMethods = classMethods;
	}

	public byte[] getClassBytes() {
		return classBytes;
	}

	public Collection<String> getClassMethods() {
		return classMethods;
	}

}