package org.eclipselabs.guita.uitracer.view;


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.eclipselabs.guita.uitrace.common.Trace;



public class TraceView extends ViewPart {
	public static final String ID = "org.eclipselabs.guita.uitracer.view";
	private static TraceView instance;
	
	private CTabFolder folder;

	public TraceView() {
		instance = this;
	}

	public static TraceView getInstance() {
		return instance;
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		folder = new CTabFolder(parent, SWT.BORDER);
		folder.setSimple(false);
		folder.setUnselectedImageVisible(true);
		folder.setUnselectedCloseVisible(false);
		folder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				((TraceItem) folder.getItem(folder.getSelectionIndex())).navigateToSelection();
			}
		});
			
	}

	@Override
	public void setFocus() {

	}

	public void load(final Trace trace, final Image image) {
		TraceItem existing = locateTab(trace);

		if(existing == null) {
			existing = new TraceItem(folder, trace, image);					
		}
		else {
			existing.reload(trace, image);
		}

		folder.setSelection(existing);
		existing.navigate(trace.first());
	}

	private TraceItem locateTab(final Trace trace) {
		TraceItem existing = null;
		CTabItem[] items = folder.getItems();

		for(int i = 0; i < items.length && existing == null; i++) {
			TraceItem titem = (TraceItem) items[i];
			if(titem.hasTrace(trace)) { 				
				existing = titem;
			}
		}
		return existing;
	}


	public void removeAll() {
		for(CTabItem tab : folder.getItems())
			tab.dispose();
	}

}
