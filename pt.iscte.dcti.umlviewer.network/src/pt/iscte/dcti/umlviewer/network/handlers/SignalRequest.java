package pt.iscte.dcti.umlviewer.network.handlers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import pt.iscte.dcti.umlviewer.network.util.ByteClassLoader;
import pt.iscte.dcti.umlviewer.service.Service;
import pt.iscte.dcti.umlviewer.service.ServiceHelper;

public class SignalRequest extends AbstractHandler {

	private static final int DEFAULT_PORT = 8080;

	private static final boolean SHOW_REPORTS = true;

	private Service service;

	public SignalRequest() {
		service = ServiceHelper.getService();
		if(SHOW_REPORTS) {
			System.out.println("SIGNAL READY");
		}
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		new RecordAgent().start();
		return null;
	}

	private class RecordAgent extends Thread {
		
		private InetAddress addr;
		private Socket socket;
		private ObjectOutputStream output_stream;
		private ObjectInputStream input_stream;

		private Map<String, byte[]> data = null;

		public void run() {
			//spamming the "option click" will trigger various threads.
			try {
				addr = InetAddress.getByName(null);
				socket = new Socket(addr, DEFAULT_PORT);
				output_stream = new ObjectOutputStream(socket.getOutputStream());
				input_stream = new ObjectInputStream(socket.getInputStream());
				handleConnection();
			}
			catch (UnknownHostException e) { } 
			catch (IOException e) { } 
			finally {
				if(input_stream != null) {
					try {
						input_stream.close();
					} catch (IOException e) { }
				}
				if(output_stream != null) {
					try {
						output_stream.close();
					} catch (IOException e) { }
				}
				if(socket != null) {
					try {
						socket.close();
					} catch (IOException e) { }
				}
			}
			if (data != null) {
				Set<Class<?>> classes = loadClassesFromData(data);
				service.showFragment(classes, null);
			}
		}

		private void handleConnection() {
			try {			
				Integer status = (Integer)input_stream.readObject();
				//the first message sent by aspect is the current status on recording.
				//both this class and aspect need to synchronize their status.
				if(status.intValue() == 1) {
					output_stream.writeObject(new Integer(0));
					data = (Map<String, byte[]>)input_stream.readObject();
					if(SHOW_REPORTS) {
						System.out.println("STOPPED RECORDING");
					}
				}
				else if(status.intValue() == 0) {
					output_stream.writeObject(new Integer(1));
					status = (Integer)input_stream.readObject(); //The report, is recording or not.
					if(SHOW_REPORTS) {
						String aux = (status.intValue() == 1) ? "STARTED RECORDING" : "RECORDING NOT STARTED";
						System.out.println(aux);
					}
				}
				else {
					output_stream.writeObject(new Integer(-1)); //end session
					input_stream.readObject(); //consume ack
					if(SHOW_REPORTS) {
						System.out.println("ASPECT ENDED SESSION");
					}
					//ending session also resets the recording on aspect???
					//this ensures that server will not become permanently recording.
				}
			} 
			catch (ClassNotFoundException e) { }
			catch (IOException e) { } 
		}

	}

	private Set<Class<?>> loadClassesFromData(Map<String, byte[]> data) {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		ByteClassLoader classLoader = new ByteClassLoader(this.getClass().getClassLoader(), data);
		for(String className : classLoader.getAvailableClasses()) {
			Class<?> c = null;
			try {
				c = classLoader.loadClass(className);
				classes.add(c);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return classes;
	}

}
