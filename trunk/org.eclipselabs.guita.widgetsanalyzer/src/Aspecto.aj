import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.lang.reflect.SourceLocation;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;


public aspect Aspecto {

	public static final int PORT_IN = 8082;
	private ServerSocket serverSocket;
	Socket socket = null;
	ObjectInputStream ois = null;
	
	private Color normalBackground = new Color(null, 240, 240, 240);

	private Map<String , Set<Widget>> widgetsList = new HashMap<String, Set<Widget>>();
	private List<Widget> paintedWidgets = new ArrayList<Widget>();

	protected pointcut scope() : !within(Aspecto);


	public Aspecto(){
		System.out.println("HI from TRACER!");
		try {
			serverSocket = new ServerSocket(PORT_IN);
		} catch (IOException e) {
			System.exit(1);
		}
		new RequestHandler().start();
	}

	public class RequestHandler extends Thread {
		@Override
		public void run() {	
			while(true) {
				try {
					socket = serverSocket.accept();
					ois = new ObjectInputStream(socket.getInputStream());
					String request = (String)ois.readObject();
					final String color = (String)ois.readObject();

					if(widgetsList.containsKey(request)){	
						for(Widget g: widgetsList.get(request)){
							final Widget actualWidget = g;
							actualWidget.getDisplay().syncExec(new Runnable() {

								@Override
								public void run() {
									if(paintedWidgets.contains(actualWidget) && color.equals("")){
										removePaint(actualWidget);
									}else{
										paint(actualWidget, color);
									}
								}
							});
						}
					}
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

	public void paint(Widget g, String color){
		Control c = (Control)g;

		if(color.equals("Red")){
			c.setBackground(new Color(null, 255, 0, 0));
		} else if(color.equals("Blue")){
			c.setBackground(new Color(null, 0, 0, 255));
		} else if(color.equals("Green")){
			c.setBackground(new Color(null, 0, 255, 0));
		} else if(color.equals("Yellow")){
			c.setBackground(new Color(null, 255, 255, 0));
		} else {
			c.setBackground(new Color(null, 255, 0, 255));
		}
		
		paintedWidgets.add(g);
	}

	public void removePaint(Widget g){
		Control c = (Control)g;
		c.setBackground(normalBackground);
	}

	after() returning (Widget g): call(Widget+.new(..)) && scope() {
		SourceLocation loc = thisJoinPoint.getSourceLocation();
		String aux = loc.getFileName() + ":" + loc.getLine();
		if(widgetsList.containsKey(aux))
			widgetsList.get(aux).add(g);
		else{
			Set<Widget> auxList = new HashSet<Widget>();
			auxList.add(g);
			widgetsList.put(aux, auxList);
		}
	}

	after(Widget g): call(* Widget+.*(..)) && target(g)  && scope() {
		SourceLocation loc = thisJoinPoint.getSourceLocation();
		String aux = loc.getFileName() + ":" + loc.getLine();
		if(widgetsList.containsKey(aux))
			widgetsList.get(aux).add(g);
		else{
			Set<Widget> auxList = new HashSet<Widget>();
			auxList.add(g);
			widgetsList.put(aux, auxList);
		}
	}

}
