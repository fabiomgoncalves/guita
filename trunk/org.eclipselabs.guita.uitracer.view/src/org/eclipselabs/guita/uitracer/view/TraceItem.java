package org.eclipselabs.guita.uitracer.view;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipselabs.guita.uitrace.common.Defaults;
import org.eclipselabs.guita.uitrace.common.Location;
import org.eclipselabs.guita.uitrace.common.Trace;


public class TraceItem extends CTabItem {

	private Trace trace;
	private Composite area;	
	private Scale timeline;
	private Canvas canvas;

	//	private Text debug;

	private static final int PADDING = 5;

	public TraceItem(CTabFolder parent, final Trace trace, final Image image) {
		super(parent, SWT.CLOSE);
		this.trace = trace;

		setText(trace.getWidgetName());

		area = new Composite(parent, SWT.NONE);
		RowLayout layout = new RowLayout(SWT.VERTICAL);
		layout.spacing = 10;
		layout.marginTop = 10;
		area.setLayout(layout);

		Composite bar = new Composite(area, SWT.NONE);
		bar.setLayout(new RowLayout(SWT.HORIZONTAL));
		loadTimeline(bar);
		addParentButton(bar, trace);

		loadImage(image);

		setControl(area);

	}

	private void loadImage(final Image image) {
		if(canvas != null)
			canvas.dispose();

		ImageData data = image.getImageData();


		canvas = new Canvas(area, SWT.BORDER);
		int width = data.width + PADDING*2;
		int height = data.height + PADDING*2;

		data.alpha = 128;

		final Image blur = new Image(area.getDisplay(), data);
		canvas.setLayoutData(new RowData(width, height));
		canvas.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				e.gc.drawImage(blur, PADDING-1, PADDING-1);
			}
		});
	}

	private void loadTimeline(Composite parent) {
		if(timeline != null) {
			int max = Math.max(1, trace.size()-1);
			timeline.setMaximum (max);
		}
		else {

			timeline = new Scale (parent, SWT.BORDER);
			timeline.setMinimum(0);
			int max = Math.max(1, trace.size()-1);
			timeline.setMaximum (max);
			timeline.setPageIncrement (1);
			timeline.setSelection(0);
			timeline.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					navigateToSelection();
				}
			});	

			Button backwardButton = new Button(parent, SWT.PUSH);

			Image image = Activator.getInstance().getImage("icons/nav_backward.gif");
			backwardButton.setImage(image);
			backwardButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if(timeline.getSelection() > 0) {
						timeline.setSelection(timeline.getSelection() - 1);
						navigateToSelection();
					}
				}
			});

			Button forwardButton = new Button(parent, SWT.PUSH);
			image = Activator.getInstance().getImage("icons/nav_forward.gif");
			forwardButton.setImage(image);
			forwardButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if(timeline.getSelection() < timeline.getMaximum()) {
						timeline.setSelection(timeline.getSelection() + 1);
						navigateToSelection();
					}
				}
			});
		}
	}



	private void addParentButton(Composite parent, final Trace trace) {
		Button parentButton = new Button(parent, SWT.PUSH);
		Image image = Activator.getInstance().getImage("icons/up_nav.gif");
		parentButton.setImage(image);
		parentButton.setEnabled(trace.isParentAvailable());
		parentButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					Socket socket = new Socket("localhost", Defaults.APPLICATION_PORT);
					OutputStream os = socket.getOutputStream();  
					ObjectOutputStream oos = new ObjectOutputStream(os);  
					oos.writeObject(trace.first());
					oos.close();
				}
				catch(IOException ex) {
					ex.printStackTrace();
				}
			}
		});
	}


	public void navigateToSelection() {
		Location loc = trace.at(trace.size() == 1 ? 0 : timeline.getSelection());

		//		debug.setText(loc.toString() + " : " + loc.lineNumber());
		//		debug.pack();
		area.layout();

		navigate(loc);

	}

	public void reload(Trace trace, Image image) {
		this.trace = trace;
		loadTimeline(area);
		loadImage(image);
		area.layout();
	}



	public boolean hasTrace(Trace trace) {
		return this.trace.sameAs(trace);
	}


	void navigate(final Location loc) {
		final IFile file = locate(loc);

		if(file != null) {

			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				public void run() {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					try {
						IMarker marker = file.createMarker(IMarker.TEXT);
						HashMap<String, Object> map = new HashMap<String, Object>();
						int offset = 0;
						if(offsets.containsKey(loc))
							offset = offsets.get(loc);
						map.put(IMarker.LINE_NUMBER, new Integer(loc.lineNumber() + offset));				
						marker.setAttributes(map);
						IEditorPart editorPart = IDE.openEditor(page, marker); 
						handle(editorPart, loc);
						marker.delete();
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	private Set<IEditorPart> editorsHooked = new HashSet<IEditorPart>();
	private Map<Location,Integer> offsets = new HashMap<Location, Integer>();

	private void handle(IEditorPart part, final Location loc) {
		if(part instanceof AbstractTextEditor && !editorsHooked.contains(part)) {
			editorsHooked.add(part);
			AbstractTextEditor editor = (AbstractTextEditor) part;
			editor.getDocumentProvider().getDocument(editor.getEditorInput())
			.addDocumentListener(new IDocumentListener() {
				int lines;

				@Override
				public void documentChanged(DocumentEvent event) {

					if(lines != event.getDocument().getNumberOfLines()) {
						int diff = event.getDocument().getNumberOfLines() - lines;

						if(diff != 0) {
							System.out.println("DIFF: " + diff);
							try {

								int line = event.getDocument().getLineOfOffset(event.getOffset()) + 1;
								System.out.println("LINE: " + line);
								for(Location loc : trace) {
									if(loc.lineNumber() > line) {
										if(!offsets.containsKey(loc))
											offsets.put(loc, diff);
										else
											offsets.put(loc, offsets.get(loc) + diff);

										//										loc.move(diff);
										System.out.println("move " + diff);
									}
								}
							} catch (BadLocationException e) {
								e.printStackTrace();
							}
						}
					}
				}

				@Override
				public void documentAboutToBeChanged(DocumentEvent event) {
					lines = event.getDocument().getNumberOfLines();
				}
			});

			// deregister listener
		}
	}

	private static class Visitor implements IResourceVisitor {
		Location loc;
		IFile file;

		Visitor(Location loc) {
			this.loc = loc;
		}

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

			if (resource.getName().equals(loc.fileName())) {
				file = (IFile) resource;
			}
			return true; 
		} 
	}


	IFile locate(final Location loc) {
		IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();

		try {
			Visitor visitor = new Visitor(loc);
			wsRoot.accept(visitor);
			return visitor.file;
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	
//	private void loadQuestions() {
//	Link link = new Link(area, SWT.NONE);
//	link.setText("<a>Which object did this?</a>");
//	link.addListener(SWT.Selection, new Listener() {
//
//		@Override
//		public void handleEvent(Event event) {
//
//			Socket clientSocket = null;
//			try {
//				clientSocket = new Socket("localhost", Defaults.APPLICATION_PORT);	
//				OutputStream os = clientSocket.getOutputStream();  
//				ObjectOutputStream oos = new ObjectOutputStream(os);
//				oos.writeObject(trace.at(timeline.getSelection()));
//				oos.close();
//				os.close();
//				clientSocket.close();
//			}
//			catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//	});
//
//}

}
