package pt.iscte.dcti.umlviewer.tester.aspects;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pt.iscte.dcti.umlviewer.tester.util.ClassesToBytes;



public aspect Aspect1 {

	private static final int DEFAULT_PORT = 8080;

	private boolean recording;

	private Set<Class<?>> allClasses;
	private Set<Class<?>> classes;

	public Aspect1() {
		recording = false;
		allClasses = new HashSet<Class<?>>();
		classes = null;
		new Server().start();
	}

	private Integer getCurrentStatus() {
		return recording ? new Integer(1) : new Integer(0);
	}

	private void startRecord() {
		
		classes = new HashSet<Class<?>>();
		recording = true;
	}

	private void endRecord() {
		recording = false;
	}
	
	after() : staticinitialization( pt.iscte.dcti.umlviewer.tester.model..* ) {
		allClasses.add(thisJoinPoint.getSignature().getDeclaringType());
	}
	
	after(Object o): execution(* pt.iscte.dcti.umlviewer.tester.model.*.* (..)) && target(o) {
		if(recording) {
			addClass(o.getClass());
		}
	}

	private void addClass(Class<?> c) {
		classes.add(c);
		for(Field f : c.getDeclaredFields()) {
			if(allClasses.contains(f.getType()))
				classes.add(f.getType());
		}
	}

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
			catch(IOException e) { }
			finally {
				if(server_socket != null) {
					try {
						server_socket.close();
					} catch(IOException e) { }
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
			catch (ClassNotFoundException e) { }
			catch (IOException e) { }
			finally {
				if(output_stream != null) {
					try {
						output_stream.close();
					} catch (IOException e) { }
				}
				if(input_stream != null) {
					try {
						input_stream.close();
					} catch (IOException e) { }
				}
				if(socket != null) {
					try {
						socket.close();
					} catch (IOException e) { }
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
					Map<String, byte[]> data = ClassesToBytes.getClassBytes(classes);
					output_stream.writeObject(data);
				}
				else if(request.intValue() == -1) {
					//END SESSION
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
