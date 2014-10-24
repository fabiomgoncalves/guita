package org.eclipselabs.jmodeldiagram;

import org.eclipselabs.jmodeldiagram.model.Association;
import org.eclipselabs.jmodeldiagram.model.JModel;
import org.eclipselabs.jmodeldiagram.model.JOperation;
import org.eclipselabs.jmodeldiagram.model.JType;

public interface DiagramListener {
	
	void diagramEvent(JModel model, Event event);
	
	void classEvent(JType type, Event event);
	
	void operationEvent(JOperation operation, Event event);
	
	void associationEvent(Association association, Event event);
	
	public static enum Event {
		ADD,
		CLEAR,
		SELECT,
		DOUBLE_CLICK;
	}
	
	public static class Adapter implements DiagramListener {

		@Override
		public void diagramEvent(JModel model, Event event) {
		
		}
		
		@Override
		public void classEvent(JType type, Event event) {
			
		}

		@Override
		public void operationEvent(JOperation operation, Event event) {
			
		}

		@Override
		public void associationEvent(Association association, Event event) {
			
		}
	}
	
	public static class EventFilter implements DiagramListener {
		private DiagramListener listener;
		private Event event;
		
		public EventFilter(DiagramListener listener, Event event) {
			if(listener == null || event == null)
				throw new NullPointerException("args cannot be null");
			this.event = event;
			this.listener = listener;
		}
		
		@Override
		public void diagramEvent(JModel model, Event event) {
			if(event.equals(this.event))
				listener.diagramEvent(model, event);
		}

		@Override
		public void classEvent(JType type, Event event) {
			if(event.equals(this.event))
				listener.classEvent(type, event);
			
		}

		@Override
		public void operationEvent(JOperation operation, Event event) {
			if(event.equals(this.event))
				listener.operationEvent(operation, event);
		}

		@Override
		public void associationEvent(Association association, Event event) {
			if(event.equals(this.event))
				listener.associationEvent(association, event);
		}
		
	}
	
	public static class EventAdapter implements DiagramListener {
		
		@Override
		public void diagramEvent(JModel model, Event event) {
			
		}

		@Override
		public void classEvent(JType type, Event event) {
			
		}

		@Override
		public void operationEvent(JOperation operation, Event event) {
		
		}

		public void operationEvent(JOperation operation) {
			
		}

		@Override
		public void associationEvent(Association association, Event event) {
			
		}
	}
}
