package org.eclipselabs.guita.ipreviews.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipselabs.guita.ipreviews.handler.MenuLayoutVariables;

public class DefaultLayoutChanger extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		System.out.println(((Event)event.getTrigger()).widget);
		if(((Event)event.getTrigger()).widget instanceof MenuItem){
			MenuItem item = (MenuItem) ((Event)event.getTrigger()).widget;
			MenuLayoutVariables aux = MenuLayoutVariables.getInstance();
			switch(item.getText()){
				case "RowLayout":
					aux.setLayout(new RowLayout());
					break;
				case "GridLayout":
					aux.setLayout(new GridLayout());
					break;
				case "FillLayout":
					aux.setLayout(new FillLayout());
					break;
			}
		}
		return null;
	}

}
