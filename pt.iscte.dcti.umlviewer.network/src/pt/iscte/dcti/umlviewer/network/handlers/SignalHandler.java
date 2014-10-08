package pt.iscte.dcti.umlviewer.network.handlers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import pt.iscte.dcti.umlviewer.network.external.ClassDataTransferObject;
import pt.iscte.dcti.umlviewer.network.util.ByteClassLoader;
import pt.iscte.dcti.umlviewer.service.Service;
import pt.iscte.dcti.umlviewer.service.ServiceHelper;

public class SignalHandler extends AbstractHandler {

	private static final boolean SHOW_REPORTS = false;

	private static final int DEFAULT_PORT = 8080;

	private Service service;
	private ByteClassLoader classLoader;

	public SignalHandler() {
		service = ServiceHelper.getService();
		classLoader = new ByteClassLoader(this.getClass().getClassLoader());

		if(SHOW_REPORTS) {
			System.out.println("SIGNAL READY");
		}
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		if(SHOW_REPORTS) {
			System.out.println("SIGNAL PRESSED");
		}

		new RecordAgent().start();

		return null;
	}

	private class RecordAgent extends Thread {

		private InetAddress addr;
		private Socket socket;
		private ObjectOutputStream outputStream;
		private ObjectInputStream inputStream;

		private Map<String, ClassDataTransferObject> data = null;

		public void run() {
			//spamming the "option click" will trigger various threads.
			try {
				addr = InetAddress.getByName(null);
				socket = new Socket(addr, DEFAULT_PORT);
				outputStream = new ObjectOutputStream(socket.getOutputStream());
				inputStream = new ObjectInputStream(socket.getInputStream());
				handleConnection();
			}
			catch (UnknownHostException e) { e.printStackTrace(); } 
			catch (IOException e) { e.printStackTrace(); } 
			finally {
				if(inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) { e.printStackTrace(); }
				}
				if(outputStream != null) {
					try {
						outputStream.close();
					} catch (IOException e) { e.printStackTrace(); }
				}
				if(socket != null) {
					try {
						socket.close();
					} catch (IOException e) { e.printStackTrace(); }
				}
			}

			if (data != null) {
				Map<Class<?>, Collection<Method>> fragmentClasses = loadClassesFromData();
				service.showFragment(fragmentClasses);
			}
		}

		private void handleConnection() {
			try {
				//the first message sent by aspect is the current status on recording.
				//both this class and aspect need to synchronize their status.
				Integer status = (Integer)inputStream.readObject();

				if(status.intValue() == 1) {
					outputStream.writeObject(new Integer(0));
					data = (Map<String, ClassDataTransferObject>)inputStream.readObject(); //#1

					if(SHOW_REPORTS) {
						System.out.println("STOPPED RECORDING");
					}
				}
				else if(status.intValue() == 0) {
					outputStream.writeObject(new Integer(1));
					status = (Integer)inputStream.readObject(); //The report, is recording or not.

					if(SHOW_REPORTS) {
						String aux = (status.intValue() == 1) ? "STARTED RECORDING" : "RECORDING NOT STARTED";
						System.out.println(aux);
					}
				}
				else {
					outputStream.writeObject(new Integer(-1)); //end session
					inputStream.readObject(); //consume ack

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

		private Map<Class<?>, Collection<Method>> loadClassesFromData() {
			Map<Class<?>, Collection<Method>> fragmentClasses = new HashMap<Class<?>, Collection<Method>>();

			classLoader.setData(data);

			Class<?> clazz = null;
			Collection<Method> methods = null;

			for(String className: classLoader.getAvailableClasses()) {
				try {
					if(SHOW_REPORTS) {
						System.out.println("LOADING CLASS " + className);
					}

					clazz = classLoader.loadClass(className);

					//workaround begin
					Collection<String> classMethods = classLoader.getClassMethods(className);
					if(classMethods != null && !classMethods.isEmpty()) {

						methods = new LinkedList<Method>();

						for(String methodName: classMethods) {

							for(Method aux: clazz.getMethods()) {
								if(aux.getName().equals(methodName) && !methods.contains(aux)) {
									methods.add(aux);
								}
							}

						}
					}
					//workaround end

					fragmentClasses.put(clazz, methods);

					if(SHOW_REPORTS) {
						System.out.println("LOADED CLASS " + clazz.getName());
					}	
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}

			return fragmentClasses;
		}

	}

}