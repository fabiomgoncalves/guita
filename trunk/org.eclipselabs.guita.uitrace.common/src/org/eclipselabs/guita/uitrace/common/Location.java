package org.eclipselabs.guita.uitrace.common;

import java.io.Serializable;
import java.util.Arrays;

import org.aspectj.lang.reflect.SourceLocation;

public class Location implements Serializable {
	private static final long serialVersionUID = 1L;

//	public final String className;
	private final byte[] path;
	private final int line;
	private final int id;

	public Location(SourceLocation loc, int id) {			
		String className = removeAnonymousClassTail(loc.getWithinType().getName());		
		this.path = className.getBytes();
		this.line = loc.getLine();
		this.id = id;
	}

	public boolean sameAs(Location loc) {
		return 
		id == loc.id &&
		line == loc.line &&
		Arrays.equals(path, loc.path);
	}	

	private int indexOfSeparator() {
		int i = path.length - 1;
		while(i > 0 && (path[i] & 0xff) != ':')
			i--;
		return i;
	}
	
	public String className() {		
		return new String(path);
	}
	
	public String fileName() {
		String className = className();
		int lastDot = className.lastIndexOf('.');
		return className.replace('.', '/').substring(lastDot+1) + ".java";
	}
	
	public int lineNumber() {
//		int sep = indexOfSeparator();		
//		return Integer.parseInt(new String(path, sep + 1, path.length - sep + 1));
		return line;
	}
	
//	public String key() {
//		return "" ;//file + ":" + line;
//	}

	public int id() {
		return id;
	}

	@Override
	public String toString() {
		return new String(path);
	}

	private static String removeAnonymousClassTail(String type) {
		return type.indexOf('$') != -1 ? 
				type.substring(0, type.indexOf('$')) : type;
	}
}