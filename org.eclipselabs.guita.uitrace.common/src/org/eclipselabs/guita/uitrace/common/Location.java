package org.eclipselabs.guita.uitrace.common;

import java.io.Serializable;
import java.util.Arrays;

import org.aspectj.lang.reflect.SourceLocation;

public class Location implements Serializable {
	private static final long serialVersionUID = 1L;

	private final byte[] className;
	private final int line;
	private final int id;

	public Location(SourceLocation loc, int id) {			
		String className = removeAnonymousClassTail(loc.getWithinType().getName());		
		this.className = className.getBytes();
		this.line = loc.getLine();
		this.id = id;
	}
	

	public boolean sameAs(Location loc) {
		return 
		id == loc.id &&
		line == loc.line &&
		Arrays.equals(className, loc.className);
	}	

	
	
	public String className() {		
		return new String(className);
	}
	
	public String fileName() {
		String className = className();
		int lastDot = className.lastIndexOf('.');
		return className.replace('.', '/').substring(lastDot+1) + ".java";
	}
	
	public int lineNumber() {
		return line;
	}
	
	
	public int id() {
		return id;
	}

	@Override
	public String toString() {
		return new String(className);
	}

	private static String removeAnonymousClassTail(String type) {
		return type.indexOf('$') != -1 ? 
				type.substring(0, type.indexOf('$')) : type;
	}
}