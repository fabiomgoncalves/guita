package pt.iscte.dcti.umlviewer.service;

import pt.iscte.dcti.umlviewer.view.UMLViewer;

public class ServiceHelper {

	public static Service getService() {
		return UMLViewer.getInstance();
	}

}