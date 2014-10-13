package pt.iscte.dcti.umlviewer.service;

import org.eclipselabs.jmodeldiagram.model.JOperation;
import org.eclipselabs.jmodeldiagram.model.JType;

public interface ClickHandler {
	void classClicked(JType type);
	
	void methodClicked(JOperation operation);
	
}
