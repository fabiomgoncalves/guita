package org.eclipselabs.guita.uitrace.common;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.aspectj.lang.reflect.SourceLocation;

public class Trace implements Serializable, Iterable<Location> {
	private static final long serialVersionUID = 1L;

	private enum TraceType implements Serializable {
		NORMAL(""), WIDGET_DISPOSED("Widget is disposed"), WIDGET_NOT_FOUND("Widget not found");
		
		private final String message;
		
		private TraceType(String message) {
			this.message = message;
		}
	}
	
	public static final Trace WIDGET_DISPOSED = new Trace(TraceType.WIDGET_DISPOSED);

	public static final Trace WIDGET_NOT_FOUND = new Trace(TraceType.WIDGET_NOT_FOUND);
	
//	private final transient Widget widget;
	private final String widgetType;
	private final List<Location> trace;
	private final TraceType type;
	
	private boolean parentAvailable;
	
	private Trace(TraceType type) {
//		widget = null;
		widgetType = null;
		trace = Collections.emptyList();
		this.type = type;
	}
	
	public Trace(String widgetType, SourceLocation createLocation, int id) {
//		this.widget = widget;
		this.widgetType = widgetType;
		trace = new ArrayList<Location>();
		trace.add(new Location(createLocation, id));
		type = TraceType.NORMAL;
		parentAvailable = false;
	}

	
//	private static String removeAnonymousClassTail(String type) {
//		return type.indexOf('$') != -1 ? 
//				type.substring(0, type.indexOf('$')) : type;
//	}
	
//	public Widget getWidget() {
//		return widget;
//	}
	
	public String getWidgetName() {
		return widgetType;
	}
	
	public TraceType getType() {
		return type;
	}
	
	public boolean isNormalTrace() {
		return type.equals(TraceType.NORMAL);
	}
	
	public boolean isWidgetDisposed() {
		return type.equals(TraceType.WIDGET_DISPOSED);
	}
	
	public boolean isWidgetNotFound() {
		return type.equals(TraceType.WIDGET_NOT_FOUND);
	}
	
	public String getMessage() {
		return type.message;
	}
	
	public void setParentAvailable() {
		parentAvailable = true;
	}
	
	public boolean isParentAvailable() {
		return parentAvailable;
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


	@Override
	public Iterator<Location> iterator() {
		return trace.iterator();
	}
	
	
}
