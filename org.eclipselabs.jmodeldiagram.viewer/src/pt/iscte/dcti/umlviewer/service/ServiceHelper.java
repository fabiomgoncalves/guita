package pt.iscte.dcti.umlviewer.service;

import org.eclipselabs.jmodeldiagram.view.JModelViewer;

public class ServiceHelper {

	public static JModelViewer getService() {
		return JModelViewer.getInstance();
	}

}