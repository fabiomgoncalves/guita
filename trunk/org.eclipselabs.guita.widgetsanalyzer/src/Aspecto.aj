import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.reflect.SourceLocation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;


public aspect Aspecto {

	public static final int PORT_IN = 8080;
	private ServerSocket serverSocket;

	private Map<String , ArrayList<Widget>> list = new HashMap<String, ArrayList<Widget>>();

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
			Socket socket = null;
			ObjectInputStream ois = null;
			ObjectOutputStream oos = null;
			while(true) {
				try {
					socket = serverSocket.accept();
					ois = new ObjectInputStream(socket.getInputStream());
					String request = (String)ois.readObject();

					if(list.containsKey(request)){	
						oos = new ObjectOutputStream(socket.getOutputStream());
						oos.writeObject(list.get(request));
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				} finally {
					if(oos != null){
						try {
							oos.close();
						} catch(IOException e){
						}
					}
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
