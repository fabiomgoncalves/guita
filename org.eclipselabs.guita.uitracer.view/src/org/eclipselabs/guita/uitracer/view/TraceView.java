package org.eclipselabs.guita.uitracer.view;

import java.util.HashMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.eclipselabs.guita.uitrace.common.Location;
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
			existing = new TraceItem(folder, trace, image, this);					
		}
		else {
			existing.reload(trace, image);
		}

		folder.setSelection(existing);
		navigate(trace.first());
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

	IFile f = null;

	void navigate(final Location loc) {
		//		System.out.println("NAV: " + loc.file + " " + loc.line);
		IFile file = locate(loc);

		if(file != null) {

			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					try {
						IMarker marker = f.createMarker(IMarker.TEXT);
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put(IMarker.LINE_NUMBER, new Integer(loc.lineNumber()));				
						marker.setAttributes(map);
						IDE.openEditor(page, marker); 
						marker.delete();
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}
			});
		}
//		else {
//			locateTab(loc.getTrace()).setFileNotFound();
//		}
	}

	IFile locate(final Location loc) {
		IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();

		f = null;
		try {
			wsRoot.accept(new IResourceVisitor() { 
				public boolean visit(IResource resource) { 
					if (resource.getType() != IResource.FILE)
						return true; 
					
					String className = loc.className();
					if(className.lastIndexOf('.') == -1)
						return false;
					
					String[] path = className.substring(0,className.lastIndexOf('.')).split("\\.");
					IContainer c = resource.getParent();
					
					for(int i = path.length - 1; i >= 0; i--) {
						if(c == null || !c.getName().equals(path[i]))
							return false;
						
						c = c.getParent();
					}

					String file = loc.fileName();
					
					if (resource.getName().equals(file)) {
						f = (IFile) resource;
					}
					return true; 
				} 
			});
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		return f;
	}

	public void removeAll() {
		for(CTabItem tab : folder.getItems())
			tab.dispose();
	}

}
