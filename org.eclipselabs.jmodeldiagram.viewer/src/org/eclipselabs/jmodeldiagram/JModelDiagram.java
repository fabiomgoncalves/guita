package org.eclipselabs.jmodeldiagram;

import org.eclipselabs.jmodeldiagram.model.JModel;
import org.eclipselabs.jmodeldiagram.view.JModelViewer;

public class JModelDiagram {

	public static void display(JModel model) {
		JModelViewer.getInstance().displayModel(model, false);
	}
	
	public static void displayIncremental(JModel model) {
		JModelViewer.getInstance().displayModel(model, true);
	}
	
	public static void clearDiagram() {
		JModelViewer.getInstance().clear();
	}
	
	public static void addListener(DiagramListener listener) {
		JModelViewer.getInstance().addClickHandler(listener);
	}
	
	public static void removeListener(DiagramListener listener) {
		JModelViewer.getInstance().addClickHandler(listener);
	}

}