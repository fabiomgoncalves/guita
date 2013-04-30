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

	public static final int PORT1 = 8081;
	public static final int PORT2 = 8091;
	private ServerSocket serverSocket;

	private Map<String , List<Widget>> widgetsList = new HashMap<String, List<Widget>>();

	private Map<Widget, Color> paintedWidgets = new HashMap<Widget, Color>();

	private List<Request> pendingRequests = new ArrayList<Request>();

	private int numberPaintedWidgets;
	private boolean sendNumberWidgets;

	protected pointcut scope() : !within(Aspect);


	public Aspect(){
		System.out.println("HI from TRACER!");
		try {
			serverSocket = new ServerSocket(PORT1);
		} catch (IOException e) {
			System.exit(1);
		}
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
					final Request request = (Request) ois.readObject();

					search(request);
				}
				catch(Exception e) {
					e.printStackTrace();
				} finally {
					if(ois != null){
						try {
							ois.close();
						} catch(IOException e){
						}
					}
					if(socket != null){
						try {
							socket.close();
						} catch(IOException e){
						}
					}
				}
			}
		}
	}

	private void search(final Request request){
		numberPaintedWidgets = 0;
		sendNumberWidgets = true;

		if(widgetsList.containsKey(request.getLocation())){	
			for(Widget g: widgetsList.get(request.getLocation())){
				final Widget actualWidget = g;
				actualWidget.getDisplay().syncExec(new Runnable() {

					@Override
					public void run() {
						if(paintedWidgets.containsKey(actualWidget) && request.isToRemove()){
							removePaint(actualWidget);
							pendingRequests.remove(request);
							sendNumberWidgets = false;
						}else{
							paint(actualWidget, getColor(request));
							pendingRequests.add(request);
							numberPaintedWidgets++;
						}
					}
				});
			}
		}else{
			pendingRequests.add(request);
		}
		if(sendNumberWidgets){
			sendNumberOfPaintedWidgets(numberPaintedWidgets);
		}
	}

	private RGB getColor(Request request) {
		org.eclipselabs.guita.request.Request.Color color = request.getColor();
		return new RGB(color.getR(), color.getG(), color.getB());
	}

	public void sendNumberOfPaintedWidgets(int size){
		Socket socket2 = null;
		ObjectOutputStream oos = null;
		try {
			socket2 = new Socket("localhost", PORT2);
			oos = new ObjectOutputStream(socket2.getOutputStream());
			oos.writeObject(size);

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(oos != null){
				try {
					oos.close(); } catch(IOException e){}
			}
			if(socket2 != null){
				try {
					socket2.close(); } catch(IOException e){}
			}
		}
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

	public void receiveStartupRequests(){
		Socket socket = null;
		ObjectInputStream ois = null;

		try {
			socket = new Socket("localhost", PORT2);
			ois = new ObjectInputStream(socket.getInputStream());

			@SuppressWarnings("unchecked")
			final List<Request> startupRequests = (List<Request>) ois.readObject();

			pendingRequests = startupRequests;

		} catch (UnknownHostException e) {
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
			if(socket != null){
				try {
					socket.close(); } catch(IOException e){}
			}
		}
	}

	after() returning (final Widget g): call(Widget+.new(..)) && scope() {
		SourceLocation loc = thisJoinPoint.getSourceLocation();
		String aux = loc.getFileName() + ":" + loc.getLine();
		if(widgetsList.containsKey(aux))
			widgetsList.get(aux).add(g);
		else{
			List<Widget> auxList = new ArrayList<Widget>();
			auxList.add(g);
			widgetsList.put(aux, auxList);
		}

		Iterator<Request> iterator = pendingRequests.iterator();
		while(iterator.hasNext()){
			Request r = iterator.next();
			if(r.getLocation().equals(aux)){
				search(r);
			}
		}

		g.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {

				//ALGO

				System.out.println(g);
			}
		});
	}

	after(Widget g): call(* Widget+.*(..)) && target(g)  && scope() {
		SourceLocation loc = thisJoinPoint.getSourceLocation();
		String aux = loc.getFileName() + ":" + loc.getLine();
		if(widgetsList.containsKey(aux))
			widgetsList.get(aux).add(g);
		else{
			List<Widget> auxList = new ArrayList<Widget>();
			auxList.add(g);
			widgetsList.put(aux, auxList);
		}

		Iterator<Request> iterator = pendingRequests.iterator();
		while(iterator.hasNext()){
			Request r = iterator.next();
			if(r.getLocation().equals(aux)){
				search(r);
			}
		}
	}
}
