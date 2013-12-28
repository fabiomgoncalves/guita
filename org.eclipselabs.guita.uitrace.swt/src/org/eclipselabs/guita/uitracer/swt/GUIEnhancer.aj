package org.eclipselabs.guita.uitracer.swt;


import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.aspectj.lang.reflect.SourceLocation;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipselabs.guita.uitrace.common.Defaults;
import org.eclipselabs.guita.uitrace.common.Location;
import org.eclipselabs.guita.uitrace.common.Trace;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;


public privileged aspect GUIEnhancer {

	private Map<Widget, Trace> traceTable;
	private Map<Widget, Widget> underTrace;
	private BiMap<Widget, Integer> idTable;

	private final int portOut;
	private final int portIn;

	private int nextId = 1;

	//	private Widget armed;

	private ServerSocket serverSocket = null;

	protected pointcut scope() : !within(GUIEnhancer);

	public GUIEnhancer() {
		System.out.println("GUITA: GUI Trace Active");
		this.portIn = Defaults.APPLICATION_PORT;
		this.portOut = Defaults.ECLIPSE_PORT;

		traceTable = new HashMap<Widget, Trace>();
		underTrace = new HashMap<Widget, Widget>();
		idTable = HashBiMap.create();

		try {
			serverSocket = new ServerSocket(portIn);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		new RequestHandler().start();
	}


	private static boolean altKeyPressed(Event e) {
		return (e.stateMask & SWT.ALT) != 0;
	}

	private static boolean shiftKeyPressed(Event e) {
		return (e.stateMask & SWT.SHIFT) != 0;
	}

	public class RequestHandler extends Thread {
		@Override
		public void run() {
			Socket sock;
			while(true) {
				try {
					sock = serverSocket.accept();
					InputStream is = sock.getInputStream();  
					ObjectInputStream ois = new ObjectInputStream(is);  
					Location loc = (Location) ois.readObject();  

					Widget widget = null;
					for(Entry<Widget,Trace> entry : traceTable.entrySet()) {
						if(entry.getValue().first().sameAs(loc)) {
							widget = entry.getKey();
						}
					}
					ois.close();
					is.close();
					sock.close();
					triggerNavigation(widget, true, true);
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private Image shot;

	after() returning(final MenuItem item) :
		call(MenuItem+.new(..)) && scope() {

		item.addArmListener(new ArmListener() {			
			@Override
			public void widgetArmed(ArmEvent e) {
				System.out.println("ARMED");
				//				if(altKeyPressed(e.))
				shot = shotSurrounding();			
			}
		});

	}

	after() returning(final Item item) :
		call(Item+.new(..)) && scope() {

		item.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {				
				if(altKeyPressed(event))
					triggerNavigation(item, event);
			}		
		});				
	}		

	private Cursor cursor = null;

	after(Widget control) : 
		call(* *(..)) && target(control) && scope() {

		if(traceTable.containsKey(control)) {
			SourceLocation loc = thisJoinPointStaticPart.getSourceLocation();

			int size = traceTable.get(control).size();
			int id = idTable.get(control);
			traceTable.get(control).addLocation(loc, id);

			if(traceTable.get(control).size() > size && underTrace.containsKey(control))
				triggerNavigation(control, false, false);
		}
	}

	after() returning(final Widget control) :
		call(Widget+.new(..)) && scope() {
		//		System.out.println("ADDED: " + control);
		handleWidget(control, thisJoinPointStaticPart.getSourceLocation());
	}

	after() returning(final TableViewer viewer) :
		call(TableViewer+.new(..)) && scope() {

		handleWidget(viewer.getTable(), thisJoinPointStaticPart.getSourceLocation());
	}

	after() returning(final TableViewerColumn col) :
		call(TableViewerColumn+.new(..)) && scope() {

		handleWidget(col.getColumn(), thisJoinPointStaticPart.getSourceLocation());
	}


	private void handleWidget(final Widget widget, final SourceLocation loc) {
		int id = nextId;

		idTable.put(widget, id);


		traceTable.put(widget, new Trace(widget.getClass().getSimpleName(), loc, id));

		nextId++;

		if(widget instanceof Shell) {			
			widget.addListener(SWT.MouseDown, new Listener() {

				@Override
				public void handleEvent(Event event) {
					if(altKeyPressed(event))
						triggerNavigation(widget, event);
				}
			});


		}
		else if(widget instanceof TableColumn) {
			widget.addListener(SWT.MouseDown, new Listener() {

				@Override
				public void handleEvent(Event event) {
					if(altKeyPressed(event))
						triggerNavigation(widget, event);
				}
			});

			//			((TableColumn) widget).addSelectionListener(new SelectionAdapter() {
			//				@Override
			//				public void widgetSelected(SelectionEvent e) {
			//					if(altKeyPressed(e))
			//						triggerNavigation(widget, false);
			//				}
			//			});
		}
		else if(widget instanceof Control) {
			final Control control = (Control) widget;
			widget.addListener(SWT.MouseDown, new Listener() {

				@Override
				public void handleEvent(Event event) {
					if(altKeyPressed(event))
						triggerNavigation(widget, event);
				}
			});
			if(cursor == null)
				cursor = new Cursor(Display.getDefault(), SWT.CURSOR_HAND);

			control.setCursor(cursor);
		}

		widget.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				traceTable.remove(widget);
				underTrace.remove(widget);
			}
		});
	}







	private static class GetParentRunnable implements Runnable {
		Control control;
		Composite parent;

		GetParentRunnable(Control control) {
			this.control = control;
		}
		public void run() {
			if(control != null && !control.isDisposed())
				parent = control.getParent();
		}
	}

	private void checkIfParentAvailable(Control control, Trace trace) {
		GetParentRunnable runnable = new GetParentRunnable(control);
		Display.getDefault().syncExec(runnable);
		Widget parentWidget = runnable.parent;

		if(parentWidget != null && traceTable.containsKey(parentWidget)) {
			trace.setParentAvailable();
		}
	}
	
	private void triggerNavigation(Widget widget, Event event) {
		triggerNavigation(widget, shiftKeyPressed(event), false);
	}
	
	private void triggerNavigation(Widget widget, boolean parent, boolean fromIDE) {
		//		System.out.println("trace request - " + widget + (parent ? "(parent)" : ""));

		if(widget instanceof TabFolder) {
			TabFolder folder = (TabFolder) widget;
			TabItem[] items = folder.getSelection();
			if(items.length > 0)
				widget = items[0];
		}

		Trace trace = null;

		if(parent) {
			GetParentRunnable runnable = new GetParentRunnable((Control) widget);
			Display.getDefault().syncExec(runnable);
			Widget parentWidget = runnable.parent;

			if(parentWidget != null)
				trace = traceTable.get(parentWidget);

			if(trace == null) {
				trace = Trace.WIDGET_DISPOSED;
				if(!fromIDE)
					showMessage(SWT.ICON_INFORMATION, "Not Available", "Parent widget not available");
			}
			else {
				widget = parentWidget;
			}
		}
		else {
			trace = traceTable.get(widget);
		}

		if(trace == null) {
			showMessage(SWT.ICON_INFORMATION, "Not Available", "Control is disposed");
			trace = Trace.WIDGET_DISPOSED;
		}

		if(trace.isNormalTrace())
			checkIfParentAvailable((Control) widget, trace);
		else if(!fromIDE)
			return;

		Socket clientSocket = null;
		try {
			clientSocket = new Socket("localhost", portOut);	
			OutputStream os = clientSocket.getOutputStream();  
			ObjectOutputStream oos = new ObjectOutputStream(os);  
			oos.writeObject(trace);
			oos.close();
			os.close();
			clientSocket.close();

			if(trace.isNormalTrace()){
				Image image = makeScreenshot(widget);			

				ImageData data = image.getImageData();
				ImageLoader loader = new ImageLoader(	);
				loader.data = new ImageData[] {data};	

				clientSocket = new Socket("localhost", portOut);
				os = clientSocket.getOutputStream();  
				loader.save(os, SWT.IMAGE_PNG);
				os.close();
				clientSocket.close();
				image.dispose();

				if(!underTrace.containsKey(widget))
					underTrace.put(widget, widget);
			}
		} 
		catch (IOException ex) {
			showMessage(SWT.ICON_ERROR, "Connection Error", "Could not connect to Eclipse on port: " + portOut);
			//			System.out.println("Accept failed: " + portOut);
		} 
	}

	private void showMessage(final int icon, final String title, final String text) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				MessageBox message = new MessageBox(new Shell(Display.getDefault()), icon);
				message.setText(title);
				message.setMessage(text);
				message.open();
			}
		});
	}

	private static String sourceLocationToString(SourceLocation loc) {
		return loc.getWithinType().getName() + ":" + loc.getLine();
	}


	private static boolean equal(SourceLocation a, SourceLocation b) {
		return 
				a.getWithinType().equals(a.getWithinType()) && 
				a.getLine() == b.getLine();
	}


	private static class MakeShotRunnable implements Runnable {
		Control control;
		Image image;

		MakeShotRunnable(Control control) {
			this.control = control;
		}

		public void run() {
			if(!control.isDisposed()) {
				GC gc = new GC((Control) control);
				int width = ((Control) control).getBounds().width;
				int height = ((Control) control).getBounds().height;
				image = new Image(control.getDisplay(), width, height);
				gc.copyArea(image, 0, 0);
				gc.dispose();
			}
		}

	}


	private Image makeScreenshot(Widget widget) {
		Image image = null;

		if(widget instanceof Control) {
			Control control = (Control) widget;
			MakeShotRunnable runnable = new MakeShotRunnable(control);
			Display.getDefault().syncExec(runnable);
			image = runnable.image;
		}
		else if(widget instanceof MenuItem) {
			return shot;
		}
		else if(widget instanceof Item) {
			image = shotSurrounding();
		}

		if(image == null) {
			image = new Image(Display.getDefault(), SHOT_WIDTH, SHOT_HEIGHT);
		}

		return image;
	}


	private static final int SHOT_WIDTH = 225;
	private static final int SHOT_HEIGHT = 75;

	private Image shotSurrounding() {
		GC gc = new GC(Display.getDefault());
		Image image = new Image(Display.getDefault(), SHOT_WIDTH, SHOT_HEIGHT);
		Point loc = Display.getDefault().getCursorLocation();
		gc.copyArea(image, loc.x - SHOT_WIDTH/4, loc.y - SHOT_HEIGHT/2);
		return image;
	}

	//	private void addAltListener(final Widget control) {
	//		System.out.println("add ALT");
	//		final Display display = control.getDisplay();
	//		display.addFilter(SWT.KeyDown, new Listener() {
	//
	//			@Override
	//			public void handleEvent(Event event) {
	//				System.out.println(event.keyCode);
	//				if(event.keyCode == SWT.ALT) {
	//					System.out.println("ALT pressed");
	//					altOn = true;
	//					if((event.stateMask & SWT.SHIFT) != 0)
	//						shiftOn = true;
	//				}
	//			}				
	//		});
	//
	//		display.addFilter(SWT.KeyUp, new Listener() {
	//
	//			@Override
	//			public void handleEvent(Event event) {
	//				if(event.keyCode == SWT.ALT) {
	//					System.out.println("ALT released");
	//					altOn = false;
	//					shiftOn = false;
	//				}
	//			}
	//		});
	//
	//		firstTime = false;
	//	}
}
