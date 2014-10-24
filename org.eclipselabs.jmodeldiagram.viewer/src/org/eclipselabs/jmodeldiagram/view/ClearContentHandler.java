package org.eclipselabs.jmodeldiagram.view;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipselabs.jmodeldiagram.JModelDiagram;


public class ClearContentHandler extends AbstractHandler {

	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		JModelDiagram.clearDiagram();
		return null;
	}

}