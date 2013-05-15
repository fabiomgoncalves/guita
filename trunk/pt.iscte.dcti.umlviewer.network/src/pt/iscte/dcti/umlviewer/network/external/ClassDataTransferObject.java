package pt.iscte.dcti.umlviewer.network.external;

import java.io.Serializable;
import java.util.Collection;

public class ClassDataTransferObject implements Serializable {
	
	private final byte[] class_bytes;
	private final Collection<String> class_methods;
	
	public ClassDataTransferObject(final byte[] class_bytes, 
			final Collection<String> class_methods) {
		this.class_bytes = class_bytes;
		this.class_methods = class_methods;
	}
	
	public byte[] getClassBytes() {
		return class_bytes;
	}
	
	public Collection<String> getClassMethods() {
		return class_methods;
	}

}
