package pt.iscte.dcti.umlviewer.network.handlers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import pt.iscte.dcti.umlviewer.network.external.ClassDataTransferObject;
import pt.iscte.dcti.umlviewer.network.util.ByteClassLoader;
import pt.iscte.dcti.umlviewer.service.Service;
import pt.iscte.dcti.umlviewer.service.ServiceHelper;

public class SignalHandler extends AbstractHandler {
	
	//this Implementing Runnable?
	
	private static final boolean SHOW_REPORTS = true;

	private static final int DEFAULT_PORT = 8080;

	private Service service;
	
	private ByteClassLoader class_loader;

	public SignalHandler() {
		service = ServiceHelper.getService();
		class_loader = new ByteClassLoader(this.getClass().getClassLoader());
		if(SHOW_REPORTS) {
			System.out.println("SIGNAL READY");
		}
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		if(SHOW_REPORTS) {
			System.out.println("SIGNAL PRESSED");
		}
		new RecordAgent().start(); //Execute wraping on a thread?
		return null;
	}

	private class RecordAgent extends Thread {

		private InetAddress addr;
		private Socket socket;
		private ObjectOutputStream output_stream;
		private ObjectInputStream input_stream;

		private Map<String, ClassDataTransferObject> data = null;

		public void run() {
			//spamming the "option click" will trigger various threads.
			try {
				addr = InetAddress.getByName(null);
				socket = new Socket(addr, DEFAULT_PORT);
				output_stream = new ObjectOutputStream(socket.getOutputStream());
				input_stream = new ObjectInputStream(socket.getInputStream());
				handleConnection();
			}
			catch (UnknownHostException e) { e.printStackTrace(); } 
			catch (IOException e) { e.printStackTrace(); } 
			finally {
				if(input_stream != null) {
					try {
						input_stream.close();
					} catch (IOException e) { e.printStackTrace(); }
				}
				if(output_stream != null) {
					try {
						output_stream.close();
					} catch (IOException e) { e.printStackTrace(); }
				}
				if(socket != null) {
					try {
						socket.close();
					} catch (IOException e) { e.printStackTrace(); }
				}
			}
			if (data != null) {
				Map<Class<?>, Collection<String>> fragment_classes = loadClassesFromData();
				service.showFragment(fragment_classes);
			}
		}

		private void handleConnection() {
			try {			
				Integer status = (Integer)input_stream.readObject();
				//the first message sent by aspect is the current status on recording.
				//both this class and aspect need to synchronize their status.
				if(status.intValue() == 1) {
					output_stream.writeObject(new Integer(0));
					data = (Map<String, ClassDataTransferObject>)input_stream.readObject(); //#1
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
			catch (ClassNotFoundException e) { e.printStackTrace(); }
			catch (IOException e) { e.printStackTrace(); } 
		}

		//#1 - CONTROLAR: aspecto pode enviar sinal de terminar sessão em vez do fragmento.

		private Map<Class<?>, Collection<String>> loadClassesFromData() {
			Map<Class<?>, Collection<String>> fragment_classes = new HashMap<Class<?>, Collection<String>>();
			class_loader.setData(data);
			for(String class_name : class_loader.getAvailableClasses()) {
				Class<?> clazz = null;
				try {
					if(SHOW_REPORTS) {
						System.out.println("LOADING CLASS " + class_name);
					}

					clazz = class_loader.loadClass(class_name); //#2
					Collection<String> class_methods = class_loader.getClassMethods(class_name);
					fragment_classes.put(clazz, class_methods);

					if(SHOW_REPORTS) {
						System.out.println("LOADED SUCCESSFULLY " + clazz.getName());
					}	
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			return fragment_classes;
		}
		
		//#2 - loadClass() poderá devolver null e causar lançamento de 
		//IllegalArgumentException quando faz put()?

	}

}
