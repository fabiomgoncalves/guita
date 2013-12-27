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
import java.util.WeakHashMap;

import org.aspectj.lang.reflect.SourceLocation;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Widget;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import org.eclipselabs.guita.uitrace.common.*;


public privileged aspect GUIEnhancer {

	private Map<Widget, Trace> traceTable;
	private Map<Widget, Widget> underTrace;
	private BiMap<Widget, Integer> idTable;

	private Map<Integer, Widget> callees;

	private final int portOut;
	private final int portIn;

	private boolean firstTime;

	private int nextId = 1;

	private Widget armed;

	private boolean altOn;
	private boolean shiftOn;

	private ServerSocket serverSocket = null;

	protected pointcut scope() : !within(GUIEnhancer);

	public GUIEnhancer() {
		System.out.println("GUITA: GUI Trace Active");
		this.portIn = Defaults.APPLICATION_PORT;
		this.portOut = Defaults.ECLIPSE_PORT;

		traceTable = new HashMap<Widget, Trace>();
		underTrace = new HashMap<Widget, Widget>();
		idTable = HashBiMap.create();

		callees = new WeakHashMap<Integer, Widget>();
		firstTime = true;

		try {
			serverSocket = new ServerSocket(portIn);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		new RequestHandler().start();
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

					for(Entry<Widget,Trace> entry : traceTable.entrySet()) {
						if(entry.getValue().first().sameAs(loc)) {
							Widget w = entry.getKey();
							triggerNavigation(w, true);
						}
					}
					ois.close();
					is.close();
					sock.close();					
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
				if(altOn)
					shot = shotSurrounding();			
			}
		});

	}

	after() returning(final Item item) :
		call(Item+.new(..)) && scope() {

		item.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {				
				if(altOn) {
					//					System.out.println("selected - " + item);
					triggerNavigation(item, false);
				}
			}		
		});				
	}		

	//	private final Cursor cursor = new Cursor(Display.getDefault(), SWT.CURSOR_HAND);

	after(Widget control) : 
		call(* *(..)) && target(control) && scope() {

		if(traceTable.containsKey(control)) {
			SourceLocation loc = thisJoinPointStaticPart.getSourceLocation();

			//			int size = traceTable.get(control).size();
			int id = idTable.get(control);
			Location traceLoc = traceTable.get(control).addLocation(loc, id);

			//			if(traceTable.get(control).size() > size && underTrace.containsKey(control))
			//				triggerNavigation(control);

			Object thisObj = thisJoinPoint.getThis();
			if(thisObj instanceof Widget) {
				callees.put(traceLoc.id(), (Widget) thisObj);
				System.out.println(traceLoc.id() + " -> " + thisObj);
			}
		}
	}

	after() returning(final Widget control) :
		call(Widget+.new(..)) && scope() {

		if(firstTime)
			addAltListener(control);

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

		boolean parentAvailable = 
				widget instanceof Control &&
				traceTable.containsKey(((Control) widget).getParent());
		
		traceTable.put(widget, new Trace(widget.getClass().getSimpleName(), false, loc, id, parentAvailable));

		nextId++;

		if(widget instanceof Shell) {			
			((Shell) widget).addListener(SWT.MouseDown, new Listener() {

				@Override
				public void handleEvent(Event event) {
					if(altOn)
						triggerNavigation(widget, false);
				}
			});


		}
		else if(widget instanceof TableColumn) {
			((TableColumn) widget).addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					if(altOn)
						triggerNavigation(widget, false);
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {

				}
			});
		}
		else if(widget instanceof Control) {
			widget.addListener(SWT.MouseDown, new Listener() {

				@Override
				public void handleEvent(Event event) {
					if(altOn)
						triggerNavigation(widget, false);
				}
			});
		}
		
		
		widget.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				traceTable.remove(widget);
				underTrace.remove(widget);
			}
		});
		
		
	}





	private void addAltListener(final Widget control) {
		final Display display = control.getDisplay();
		display.addFilter(SWT.KeyDown, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if(event.keyCode == SWT.ALT) {
					altOn = true;
					if((event.stateMask & SWT.SHIFT) != 0)
						shiftOn = true;
				}
			}				
		});

		display.addFilter(SWT.KeyUp, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if(event.keyCode == SWT.ALT) {
					altOn = false;
					shiftOn = false;
				}
			}
		});

		firstTime = false;
	}

	private static class GetParentRunnable implements Runnable {
		Control control;
		Composite parent;
		
		GetParentRunnable(Control control) {
			this.control = control;
		}
		public void run() {
			parent = control.getParent();
		}
	}
	
	private void triggerNavigation(Widget widget, boolean parent) {
		System.out.println("# " + traceTable.size());
		if(widget instanceof TabFolder) {
			TabFolder folder = (TabFolder) widget;
			TabItem[] items = folder.getSelection();
			if(items.length > 0)
				widget = items[0];
		}

		if(parent || widget instanceof Control && shiftOn) {  // && !widget.isDisposed()
			GetParentRunnable runnable = new GetParentRunnable((Control) widget);
			Display.getDefault().syncExec(runnable);
			widget = runnable.parent; 
		}

		Trace trace = traceTable.get(widget);
		if(trace == null) {
			System.err.println("Control not found");
			return;
		}

		Socket clientSocket = null;
		try {
			clientSocket = new Socket("localhost", portOut);	
			OutputStream os = clientSocket.getOutputStream();  
			ObjectOutputStream oos = new ObjectOutputStream(os);  
			oos.writeObject(trace);
			oos.close();
			os.close();
			clientSocket.close();

			Image image = makeScreenshot(widget);			

			ImageData data = image.getImageData();
			ImageLoader loader = new ImageLoader();
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
		catch (IOException ex) {
			System.out.println("Accept failed: " + portOut);
		} 
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
		int quarter = SHOT_WIDTH/4;
		gc.copyArea(image, loc.x - SHOT_WIDTH/4, loc.y - SHOT_HEIGHT/2);
		return image;
	}
}
