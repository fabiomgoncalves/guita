package org.eclipselabs.guita.paintwidgets.view;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class ViewTable extends ViewPart{

	private TableViewer table;
	
	@Override
	public void createPartControl(Composite parent) {
		//table = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		
	}

	@Override
	public void setFocus() {
		
	}
}