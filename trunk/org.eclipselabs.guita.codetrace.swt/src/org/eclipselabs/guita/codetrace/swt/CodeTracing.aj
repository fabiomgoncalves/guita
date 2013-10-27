package org.eclipselabs.guita.codetrace.swt;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.acl.Group;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.reflect.SourceLocation;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipselabs.guita.codetrace.common.Request;


public privileged aspect CodeTracing {

	private static final int PORT1 = 8080; // Receive paint and unpaint requestsList
	private static final int PORT2 = 8090; // Send number of painted widgets
	private ServerSocket serverSocket;

	private Map<String , List<Control>> widgetsTable = new HashMap<String, List<Control>>();

	private Map<Control, Color> paintedWidgetsTable = new HashMap<Control, Color>();

	private List<Request> requestsList = new ArrayList<Request>();

	protected pointcut scope() : !within(CodeTracing);


	public CodeTracing(){
		System.out.println("GUITA: Code Trace Active");
		try {
			serverSocket = new ServerSocket(PORT1);
		} catch (IOException e) {
			System.exit(1);
		}

		receiveStartuprequestsList();

		new RequestHandler().start();
	}

	private class RequestHandler extends Thread {
		@Override
		public void run() {
			Socket socket = null;
			ObjectInputStream ois = null;

			while(true) {
				try {
					socket = serverSocket.accept();
					ois = new ObjectInputStream(socket.getInputStream());
					boolean end = false;
					while(!end) {
						Object obj = ois.readObject();

						if(obj == null)
							end = true;
						else {
							Request request = (Request) obj;

							if(request.isClassRequest())
								searchClass(request);
							else{
								if(search(request))
									requestsList.add(request);
								else
									requestsList.remove(request);
							}
						}
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				} finally {
					if(ois != null){
						try {
							ois.close(); } catch(IOException e){}
					}
					if(socket != null){
						try {
							socket.close(); } catch(IOException e){}
					}
				}
			}
		}
	}

	private boolean search(Request request){
		int numberpaintedWidgetsTable = 0;
		List<Control> aux = new ArrayList<Control>();

		if(widgetsTable.containsKey(request.getLocation())){
			aux = widgetsTable.get(request.getLocation());
			RGB color = request.isToRemove() ? null : getColor(request);

			if(request.getTotalVarsOfType() == 1) {

				for(Control c : aux)
					if(c.getClass().getSimpleName().equals(request.getType())) {
						SearchRunnable runnable = new SearchRunnable(c, color);
						c.getDisplay().syncExec(runnable);
						numberpaintedWidgetsTable++;
					}
				
			}
			else {
				Control g = aux.get(request.getOrder());
				SearchRunnable runnable = new SearchRunnable(g, color);
				g.getDisplay().syncExec(runnable);
				numberpaintedWidgetsTable = 1;
			}
		}

		boolean addTorequestsList = !request.isToRemove();

		if(addTorequestsList)
			sendNumberOfpaintedWidgets(numberpaintedWidgetsTable, request);

		return addTorequestsList;
	}

	private void searchClass(Request request){
		Iterator<String> iterator = widgetsTable.keySet().iterator();
		while(iterator.hasNext()){
			String key = iterator.next();
			Iterator<Control> iterator2 = widgetsTable.get(key).iterator();
			while(iterator2.hasNext()){
				Widget w = iterator2.next();
				if(w.getClass().getSimpleName().equals(request.getLocation())){
					SearchRunnable runnable = new SearchRunnable((Control) w, getColor(request));
					w.getDisplay().syncExec(runnable);
				}
			}
		}
	}

	private class SearchRunnable implements Runnable{
		private Control widget;
		private RGB color;

		public SearchRunnable(Control widget, RGB color){
			this.widget = widget;
			this.color = color;
		}

		@Override
		public void run() {
			if(paintedWidgetsTable.containsKey(widget) && color == null)
				removePaint(widget);
			else
				paint(widget, color);
		}

	}

	private RGB getColor(Request request){
		Request.Color color = request.getColor();
		return new RGB(color.getR(), color.getG(), color.getB());
	}

	private boolean isBackgroundPainting(Control g){
		return g.getClass().equals(Shell.class) || Composite.class.isAssignableFrom(g.getClass()) 
				|| Group.class.isAssignableFrom(g.getClass());
	}

	private PaintListener listener = new PaintListener(){
		@Override
		public void paintControl(PaintEvent e) {
			if(paintedWidgetsTable.containsKey(e.widget)) {
				e.gc.setForeground(paintedWidgetsTable.get(e.widget));
				e.gc.setLineWidth(2);
				e.gc.drawOval(0, 0, e.width-1, e.height-1);
			}
		}
	};

	private void paint(Control g, RGB color){
		if(isBackgroundPainting(g)){
			if(!paintedWidgetsTable.containsKey(g))
				paintedWidgetsTable.put(g, g.getBackground());

			g.setBackground(new Color(g.getDisplay(), color));
		}
		else {
			if(paintedWidgetsTable.containsKey(g)){
				g.removePaintListener(listener);
				g.redraw();
			}

			paintedWidgetsTable.put(g, new Color(g.getDisplay(), color.red, color.green, color.blue)); //ou null

			g.addPaintListener(listener);
			g.redraw();
		}
	}

	private void removePaint(Control g){
		if(isBackgroundPainting(g))
			g.setBackground(paintedWidgetsTable.get(g));
		else {
			g.removePaintListener(listener);
			g.redraw();
		}

		paintedWidgetsTable.remove(g);
	}

	private void sendNumberOfpaintedWidgets(int number, Request request){
		Socket socket = null;
		ObjectOutputStream oos = null;
		try {
			socket = new Socket("localhost", PORT2);
			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject(number);
			oos.writeObject(request.getLocation());
			oos.writeObject(request.getType());
			oos.writeObject(request.getName());

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(oos != null){
				try {
					oos.close(); } catch(IOException e){}
			}
			if(socket != null){
				try {
					socket.close(); } catch(IOException e){}
			}
		}
	}

	private void receiveStartuprequestsList(){
		final int PORT3 = 8100; // Receive startup list
		Socket socket = null;
		ObjectInputStream ois = null;
		ObjectOutputStream oos = null;

		try {	
			socket = new Socket("localhost", PORT3);
			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject("OK");

			ois = new ObjectInputStream(socket.getInputStream());
			@SuppressWarnings("unchecked")
			final List<Request> startuprequestsList = (List<Request>) ois.readObject();
			requestsList = startuprequestsList;

		}
		catch(ConnectException ce) {
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(ois != null){
				try {
					ois.close(); } catch(IOException e){}
			}
			if(oos != null){
				try {
					oos.close(); } catch(IOException e){}
			}
			if(socket != null){
				try {
					socket.close(); } catch(IOException e){}
			}
		}
	}

	private void captureObjects(Control g, SourceLocation loc, Object[] args){
		String key = loc.getFileName() + ":" + loc.getLine();
		List<Control> values = widgetsTable.get(key);

		if(values == null) {
			values = new ArrayList<Control>();
			widgetsTable.put(key, values);
		}

		for(Object arg  : args)
			if(arg != null && Control.class.isAssignableFrom(arg.getClass()))
				values.add((Control) arg);


		if(!values.contains(g))
			values.add(g);

		if(!requestsList.isEmpty()){
			for(Request r : requestsList) {
				if(r.getLocation().equals(key))
					search(r);
			}
		}
	}

	after() returning (final Control g): call(Control+.new(..)) && scope() {
		SourceLocation loc = thisJoinPoint.getSourceLocation();
		Object[] args = thisJoinPoint.getArgs();
		captureObjects(g, loc, args);

		g.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {

				Iterator<String> iterator = widgetsTable.keySet().iterator();
				while(iterator.hasNext()){
					String key = iterator.next();
					Iterator<Control> iterator2 = widgetsTable.get(key).iterator();
					while(iterator2.hasNext()){
						Widget w = iterator2.next();
						if(w.equals(g))
							iterator2.remove();
					}
					if(widgetsTable.get(key).isEmpty())
						iterator.remove();
				}

			}
		});
	}

	after(): call(* *.*(..)) && target(Control+)  && scope() {
		Control g = (Control) thisJoinPoint.getTarget();
		SourceLocation loc = thisJoinPoint.getSourceLocation();
		Object[] args = thisJoinPoint.getArgs();
		captureObjects(g, loc, args);
	}
}
