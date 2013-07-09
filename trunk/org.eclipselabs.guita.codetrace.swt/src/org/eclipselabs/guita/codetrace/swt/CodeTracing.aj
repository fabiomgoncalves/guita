package org.eclipselabs.guita.codetrace.swt;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.eclipselabs.guita.codetrace.common.Request;


public privileged aspect CodeTracing {

	private static final int PORT1 = 8080; // Receber pedidos de paint e unpaint
	private static final int PORT2 = 8090; // Mandar numero de objectos pintados
	private ServerSocket serverSocket;

	private Map<String , List<Control>> widgetsList = new HashMap<String, List<Control>>();

	private Map<Control, Color> paintedWidgets = new HashMap<Control, Color>();

	private List<Request> requests = new ArrayList<Request>();

	protected pointcut scope() : !within(CodeTracing);


	public CodeTracing(){
		System.out.println("GUITA: Code Trace Active");
		try {
			serverSocket = new ServerSocket(PORT1);
		} catch (IOException e) {
			System.exit(1);
		}

		receiveStartupRequests();

		new RequestHandler().start();
	}

	public class RequestHandler extends Thread {
		@Override
		public void run() {
			Socket socket = null;
			ObjectInputStream ois = null;

			while(true) {
				try {
					socket = serverSocket.accept();
					ois = new ObjectInputStream(socket.getInputStream());
					Request request = (Request) ois.readObject();

					if(search(request))
						requests.add(request);
					else
						requests.remove(request);

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
		int numberPaintedWidgets = 0;
		List<Control> aux = new ArrayList<Control>();

		if(widgetsList.containsKey(request.getLocation())){
			aux = widgetsList.get(request.getLocation());
			RGB color = request.isToRemove() ? null : getColor(request);

			if(request.getTotalVarsOfType() == 1) {

				for(Control c : aux)
					if(c.getClass().getSimpleName().equals(request.getType())) {
						SearchRunnable runnable = new SearchRunnable(c, color);
						c.getDisplay().syncExec(runnable);
						numberPaintedWidgets++;
					}
			}
			else {
				Control g = aux.get(request.getOrder());
				SearchRunnable runnable = new SearchRunnable(g, color);
				g.getDisplay().syncExec(runnable);
				numberPaintedWidgets = 1;
			}
		}

		boolean addToRequests = !request.isToRemove();

		if(addToRequests)
			sendNumberOfPaintedWidgets(numberPaintedWidgets, request);

		return addToRequests;
	}

	private class SearchRunnable implements Runnable {
		private Control widget;
		private RGB color;

		public SearchRunnable(Control widget, RGB color){
			this.widget = widget;
			this.color = color;
		}

		@Override
		public void run() {
			if(paintedWidgets.containsKey(widget) && color == null) {
				removePaint(widget);
			}
			else{
				paint(widget, color);
			}
		}

	}

	private RGB getColor(Request request) {
		Request.Color color = request.getColor();
		return new RGB(color.getR(), color.getG(), color.getB());
	}

	public void paint(Control g, final RGB color){
		if(g.getClass().getSimpleName().equals("Shell") || g.getClass().getSimpleName().equals("Composite"))
			g.setBackground(new Color(null, color));
		else{
			g.addPaintListener(new PaintListener() {

				@Override
				public void paintControl(PaintEvent e) {
					e.gc.setForeground(new Color(null, color.red, color.green, color.blue));
					e.gc.drawOval(0, 0, e.width-1, e.height-1);
				}
			});
			g.redraw();
		}

		if(!paintedWidgets.containsKey(g))
			paintedWidgets.put(g, new Color(null, color.red, color.green, color.blue));
	}

	public void removePaint(Control g){
		if(g.getClass().getSimpleName().equals("Shell") || g.getClass().getSimpleName().equals("Composite"))
			g.setBackground(paintedWidgets.get(g));
		else{
			g.removePaintListener(null);
			g.redraw();
		}

		paintedWidgets.remove(g);
	}

	private void sendNumberOfPaintedWidgets(int number, Request request){
		Socket socket = null;
		ObjectOutputStream oos = null;
		try {
			socket = new Socket("localhost", PORT2);
			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject(number);
			oos.writeObject(request.getLocation());
			oos.writeObject(request.getType());
			oos.writeObject(request.getOrder());

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

	public void receiveStartupRequests(){
		final int PORT3 = 8100; // Receber lista inicial
		Socket socket = null;
		ObjectInputStream ois = null;
		ObjectOutputStream oos = null;

		try {	
			socket = new Socket("localhost", PORT3);
			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject("OK");

			ois = new ObjectInputStream(socket.getInputStream());
			@SuppressWarnings("unchecked")
			final List<Request> startupRequests = (List<Request>) ois.readObject();
			requests = startupRequests;

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
		String aux = loc.getFileName() + ":" + loc.getLine();
		List<Control> list = widgetsList.get(aux);

		if(list == null) {
			list = new ArrayList<Control>();
			widgetsList.put(aux, list);
		}

		for(Object arg  : args)
			if(arg != null && Control.class.isAssignableFrom(arg.getClass()))
				list.add((Control) arg);


		if(!list.contains(g))
			list.add(g);

		if(!requests.isEmpty()){
			for(Request r : requests) {
				if(r.getLocation().equals(aux))
					search(r);
			}
		}
	}

	after() returning (final Control g): call(Widget+.new(..)) && scope() {
		SourceLocation loc = thisJoinPoint.getSourceLocation();
		Object[] args = thisJoinPoint.getArgs();
		captureObjects(g, loc, args);

		g.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {

				Iterator<String> iterator = widgetsList.keySet().iterator();
				while(iterator.hasNext()){
					String key = iterator.next();
					Iterator<Control> iterator2 = widgetsList.get(key).iterator();
					while(iterator2.hasNext()){
						Widget w = iterator2.next();
						if(w.equals(g))
							iterator2.remove();
					}
					if(widgetsList.get(key).isEmpty())
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

	//	after(Widget w) : set(Widget+ *.*) && args(w) && scope() {
	//		//		System.out.println(thisJoinPoint.getSourceLocation());
	//	}
	//
	//	after() : get(Widget+ *.*) && scope() {
	//		//		System.out.println("GET" + thisJoinPoint.getSourceLocation());
	//	}
}
