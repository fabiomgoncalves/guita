package org.eclipselabs.guita.uitracer.view;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class RemoveActionDelegate implements IViewActionDelegate {

	@Override
	public void run(IAction action) {
		TraceView.getInstance().removeAll();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {

	}

	@Override
	public void init(IViewPart view) {

	}

}
