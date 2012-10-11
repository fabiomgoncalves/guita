package org.eclipselabs.guita.uitrace.common;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.reflect.SourceLocation;

public class Trace implements Serializable {
	private static final long serialVersionUID = 1L;

	private final String widgetType;
	private final boolean disposed;
	private final List<Location> trace;
	
	public Trace(String widgetType, boolean disposed, SourceLocation createLocation, int id) {
		this.widgetType = widgetType;
		this.disposed = disposed;
		trace = new ArrayList<Location>();
		trace.add(new Location(createLocation, id));
	}

	
	private static String removeAnonymousClassTail(String type) {
		return type.indexOf('$') != -1 ? 
				type.substring(0, type.indexOf('$')) : type;
	}
	
	public String getWidgetName() {
		return widgetType;
	}
	
	public boolean isWidgetDisposed() {
		return disposed;
	}
	
	public int size() {
		return trace.size();
	}
	
	public Location first() {
		return trace.get(0);
	}
	
	public Location last() {
		return trace.get(trace.size() - 1);
	}
	
	public Location at(int index) {
		return trace.get(index);
	}
	
	public Location addLocation(SourceLocation loc, int id) {
		Location l = new Location(loc, id);
		if(!l.sameAs(last()))
			trace.add(l);
		
		return l;
	}
	
	public boolean sameAs(Trace trace) {
		return first().sameAs(trace.first());
	}
	
	
}
