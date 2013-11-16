package pt.iscte.dcti.umlviewer.tester.aspects;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import org.aspectj.lang.reflect.MethodSignature;

import pt.iscte.dcti.umlviewer.network.external.ClassDataTransferObject;
import pt.iscte.dcti.umlviewer.tester.util.ClassesToBytes;

public aspect Aspect1 {
	
	//Atenção, o aspecto deve saber quando a aplicação foi fechada, para fechar o servidor também.

	private static final int DEFAULT_PORT = 8080;

	private Set<Class<?>> app_classes;

	private boolean recording;
	private Map<Class<?>, Collection<String>> fragment_classes;

	public Aspect1() {
		app_classes = new HashSet<Class<?>>();
		recording = false;
		fragment_classes = null;
		new Server().start();
	}

	private Integer getCurrentStatus() {
		return recording ? new Integer(1) : new Integer(0);
	}

	private void startRecord() {
		fragment_classes = new HashMap<Class<?>, Collection<String>>();
		recording = true;
	}

	private void endRecord() {
		recording = false;
	}

	private Map<Class<?>, Collection<String>> getFragmentClasses() {
		return fragment_classes;
	}

	after() : staticinitialization(pt.iscte.dcti.umlviewer.tester.model..*) {
		app_classes.add(thisJoinPoint.getSignature().getDeclaringType());
	}

	after(Object o): execution(* pt.iscte.dcti.umlviewer.tester.model.*.* (..)) && target(o) {
		if(recording) {
			MethodSignature sig = (MethodSignature) thisJoinPoint.getSignature();
			Class<?> clazz = sig.getDeclaringType();
			if(fragment_classes.get(clazz) == null) { //#1
				fragment_classes.put(clazz, new LinkedList<String>());
			}
			fragment_classes.get(clazz).add(sig.getMethod().getName()); //#2
			//#3
			for(Field f: clazz.getDeclaredFields()) {
				if(app_classes.contains(f.getType()) && !fragment_classes.containsKey(f.getType())) { //#4
					fragment_classes.put(f.getType(), null);
				}
			}
		}
	}
	
	//#1 - !containsKey || (containsKey && value == null)
	//#2 - Em vez de guardar o nome do método, guardar o próprio método em si? Ou guardar uma String "completa"?
	//#3 - EM FALTA: Processar extends/implements e dependencias em métodos (return type e parametros de entrada)
	//#4 - EM FALTA: Colecções e Arrays de arrays. Atenção a colecções de colecções, e arrays de arrays.

	private class Server extends Thread {

		private ServerSocket server_socket;

		public void run() {
			try {
				server_socket = new ServerSocket(DEFAULT_PORT);
				while(true) {
					Socket socket = server_socket.accept();
					new RequestHandler(socket).start();
				}
			} 
			catch(IOException e) { e.printStackTrace(); }
			finally {
				if(server_socket != null) {
					try {
						server_socket.close();
					} catch(IOException e) { e.printStackTrace(); }
				}
			}
		}

	}

	private class RequestHandler extends Thread {

		private Socket socket;
		private ObjectInputStream input_stream;
		private ObjectOutputStream output_stream;

		private RequestHandler(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			try {
				input_stream = new ObjectInputStream(socket.getInputStream());
				output_stream = new ObjectOutputStream(socket.getOutputStream());
				handleRequest();
			}
			catch (ClassNotFoundException e) { e.printStackTrace(); }
			catch (IOException e) { e.printStackTrace(); }
			finally {
				if(output_stream != null) {
					try {
						output_stream.close();
					} catch (IOException e) { e.printStackTrace(); }
				}
				if(input_stream != null) {
					try {
						input_stream.close();
					} catch (IOException e) { e.printStackTrace(); }
				}
				if(socket != null) {
					try {
						socket.close();
					} catch (IOException e) { e.printStackTrace(); }
				}
			}
		}

		private void handleRequest() throws IOException, ClassNotFoundException {
			synchronized(Aspect1.this) {
				Integer status = getCurrentStatus();
				output_stream.writeObject(status);
				Integer request = (Integer)input_stream.readObject();
				if(status.intValue() == 0 && request.intValue() == 1) {
					startRecord();
					status = getCurrentStatus();
					output_stream.writeObject(status);
				}
				else if(status.intValue() == 1 && request.intValue() == 0) {
					endRecord();
					Map<String, ClassDataTransferObject> data = ClassesToBytes.getClassBytes(getFragmentClasses());
					output_stream.writeObject(data);
				}
				else if(request.intValue() == -1) {
					//END SESSION REQUEST
					output_stream.writeObject(new Integer(-1));
				}
				else {
					//OUT_OF_SYNC -> Ignore request
					output_stream.writeObject(new Integer(-1));
				}
			}
		}

	}

}
