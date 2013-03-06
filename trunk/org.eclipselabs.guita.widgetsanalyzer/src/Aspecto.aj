import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.reflect.SourceLocation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;


public aspect Aspecto {

	public static final int PORT_IN = 5658;
	private ServerSocket serverSocket;

	private Map<String , ArrayList<Widget>> list = new HashMap<String, ArrayList<Widget>>();
	private ArrayList<Widget> previousWidget = new ArrayList<Widget>();

	private final Painter p = new Painter();

	protected pointcut scope() : !within(Aspecto) && !within(Painter);


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
			Socket sock;
			while(true) {
				try {
					sock = serverSocket.accept();
					InputStream is = sock.getInputStream();  
					ObjectInputStream ois = new ObjectInputStream(is);
					final String request = (String)ois.readObject();

					if(list.containsKey(request)){
						if(!previousWidget.equals(null))
							removePainting();
						for(Widget g: list.get(request)){
							previousWidget.add(g);
							final Widget actualWidget = g;
							actualWidget.getDisplay().syncExec(new Runnable() {

								@Override
								public void run() {
									p.paint(actualWidget);
								}
							});
						}
					}

					ois.close();
					is.close();
				}
				catch(Exception e) {
					System.err.println("problem");
					e.printStackTrace();
				}
			}
		}
	}

	public void removePainting(){
		for(final Widget aux: previousWidget)
			aux.getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					p.dispaint(aux);
				}
			});
	}

	after() returning (Widget g): call(Widget+.new(..)) && scope() {
		if(!g.getClass().equals(Shell.class)){
			SourceLocation loc = thisJoinPoint.getSourceLocation();
			String aux = loc.getFileName() + ":" + loc.getLine();
			if(list.containsKey(aux))
				list.get(aux).add(g);
			else{
				ArrayList<Widget> auxList = new ArrayList<Widget>();
				auxList.add(g);
				list.put(aux, auxList);
			}
		}
	}

	after(Widget g): call(* Widget+.*(..)) && target(g)  && scope() {
		if(!g.getClass().equals(Shell.class)){
			SourceLocation loc = thisJoinPoint.getSourceLocation();
			String aux = loc.getFileName() + ":" + loc.getLine();
			if(list.containsKey(aux))
				list.get(aux).add(g);
			else{
				ArrayList<Widget> auxList = new ArrayList<Widget>();
				auxList.add(g);
				list.put(aux, auxList);
			}

			System.out.println(list.toString());
		}
	}

}
