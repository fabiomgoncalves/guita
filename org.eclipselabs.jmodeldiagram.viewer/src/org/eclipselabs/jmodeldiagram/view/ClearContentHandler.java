package org.eclipselabs.jmodeldiagram.view;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import pt.iscte.dcti.umlviewer.service.ServiceHelper;

public class ClearContentHandler extends AbstractHandler {

	private JModelViewer service;

	public ClearContentHandler() {
		service = ServiceHelper.getService();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		service.clear();
		return null;
	}

}