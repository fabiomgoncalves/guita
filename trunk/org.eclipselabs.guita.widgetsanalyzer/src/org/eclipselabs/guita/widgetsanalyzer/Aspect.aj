package org.eclipselabs.guita.widgetsanalyzer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.eclipselabs.guita.request.Request;

public aspect Aspect {

	public static final int PORT1 = 8080; // Receber pedidos de paint e unpaint
	public static final int PORT2 = 8090; // Mandar numero de objectos pintados
	private ServerSocket serverSocket;

	private Map<String , List<Widget>> widgetsList = new HashMap<String, List<Widget>>();

	private Map<Widget, Color> paintedWidgets = new HashMap<Widget, Color>();

	private List<Request> pendingRequests = new ArrayList<Request>();

	protected pointcut scope() : !within(Aspect);


	public Aspect(){
		System.out.println("HI from TRACER!");
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
						pendingRequests.add(request);
					else
						pendingRequests.remove(request);

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
		SearchRunnable runnable = null;
		boolean sendNumberOfWidgets = true;
		boolean addToPending = true;
		int numberPaintedWidgets = 0;
		List<Widget> aux = new ArrayList<Widget>();

		if(widgetsList.containsKey(request.getLocation())){	//FUNCIONA PARA UM WIDGET, MAS E SE FOR UM CICLO? COMO SE FAZ COM A ORDEM?
			aux = widgetsList.get(request.getLocation());
			Widget g = aux.get(request.getOrder());
			runnable = new SearchRunnable(g, request);
			g.getDisplay().syncExec(runnable);

			//for(Widget g: widgetsList.get(request.getLocation())){
			//runnable = new SearchRunnable(g, request);
			//g.getDisplay().syncExec(runnable);
			//}

			sendNumberOfWidgets = runnable.getSendNumberOfWidgets();
			addToPending = runnable.getAddToPending();
			numberPaintedWidgets = runnable.getNumberPaintedWidgets();
		}

		if(sendNumberOfWidgets)
			sendNumberOfPaintedWidgets(numberPaintedWidgets, request);
		
		return addToPending;
	}

	private class SearchRunnable implements Runnable {
		private Widget widget;
		private Request request;
		private int numberPaintedWidgets;
		private boolean addToPending = true;
		private boolean sendNumberOfWidgets = true;

		public SearchRunnable(Widget widget, Request request){
			this.widget = widget;
			this.request = request;
		}

		@Override
		public void run() {
			if(paintedWidgets.containsKey(widget) && request.isToRemove()){
				removePaint(widget);
				sendNumberOfWidgets = false;
				addToPending = false;
			}else{
				paint(widget, getColor(request));
				numberPaintedWidgets++;
			}
		}

		public int getNumberPaintedWidgets(){
			return numberPaintedWidgets;
		}

		public boolean getAddToPending(){
			return addToPending;
		}

		public boolean getSendNumberOfWidgets(){
			return sendNumberOfWidgets;
		}
	}

	private RGB getColor(Request request) {
		org.eclipselabs.guita.request.Request.Color color = request.getColor();
		return new RGB(color.getR(), color.getG(), color.getB());
	}

	public void paint(Widget g, RGB color){
		Control c = (Control)g;

		if(!paintedWidgets.containsKey(g))
			paintedWidgets.put(g, c.getBackground());

		c.setBackground(new Color(null, color));
	}

	public void removePaint(Widget g){
		Control c = (Control)g;

		c.setBackground(paintedWidgets.get(g));

		paintedWidgets.remove(g);
	}

	public void sendNumberOfPaintedWidgets(int number, Request request){
		Socket socket = null;
		ObjectOutputStream oos = null;
		try {
			socket = new Socket("localhost", PORT2);
			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject(number);
			oos.writeObject(request.getLocation());
			oos.writeObject(request.getType());

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
			pendingRequests = startupRequests;

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

	after() returning (final Widget g): call(Widget+.new(..)) && scope() {
		SourceLocation loc = thisJoinPoint.getSourceLocation();
		String aux = loc.getFileName() + ":" + loc.getLine();
		List<Widget> list = widgetsList.get(aux);

		if(list == null) {
			list = new ArrayList<Widget>();
			widgetsList.put(aux, list);
		}

		//		if(!list.contains(g))
		list.add(g);

		if(!pendingRequests.isEmpty()){
			for(Request r : pendingRequests) {
				if(r.getLocation().equals(aux))
					search(r);
			}
		}

		g.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {

				Iterator<String> iterator = widgetsList.keySet().iterator();
				while(iterator.hasNext()){
					String key = iterator.next();
					Iterator<Widget> iterator2 = widgetsList.get(key).iterator();
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

	after(Widget g): call(* Widget+.*(..)) && target(g)  && scope() {
		Object[] args = thisJoinPoint.getArgs();
		for(Object arg  : args)
			if(arg != null && Control.class.isAssignableFrom(arg.getClass()))
				System.out.println("ARG: " +  arg);

		SourceLocation loc = thisJoinPoint.getSourceLocation();
		String aux = loc.getFileName() + ":" + loc.getLine();
		List<Widget> list = widgetsList.get(aux);

		if(list == null) {
			list = new ArrayList<Widget>();
			widgetsList.put(aux, list);
		}

		//		if(!list.contains(g))
		list.add(g);

		if(!pendingRequests.isEmpty()){
			for(Request r : pendingRequests) {
				if(r.getLocation().equals(aux))
					search(r);
			}
		}
	}

	after(Widget w) : set(Widget+ *.*) && args(w) && scope() {
		//		System.out.println(thisJoinPoint.getSourceLocation());
	}

	after() : get(Widget+ *.*) && scope() {
		//		System.out.println("GET" + thisJoinPoint.getSourceLocation());
	}
}
