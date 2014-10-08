package pt.iscte.dcti.umlviewer.tester.aspects;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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

	private static final int DEFAULT_PORT = 8080;

	private Set<Class<?>> appClasses;
	private boolean recording;
	private Map<Class<?>, Collection<String>> fragmentClasses;

	public Aspect1() {
		appClasses = new HashSet<Class<?>>();
		recording = false;
		fragmentClasses = null;

		new Server().start();
	}

	private Integer getCurrentStatus() {
		return recording ? new Integer(1) : new Integer(0);
	}

	private void startRecord() {
		fragmentClasses = new HashMap<Class<?>, Collection<String>>();
		recording = true;
	}

	private void endRecord() {
		recording = false;
	}

	private Map<Class<?>, Collection<String>> getFragmentClasses() {
		return fragmentClasses;
	}

	after() : staticinitialization(pt.iscte.dcti.umlviewer.tester.model..*) {
		appClasses.add(thisJoinPoint.getSignature().getDeclaringType());
	}

	after(Object o): execution(* pt.iscte.dcti.umlviewer.tester.model.*.* (..)) && target(o) {
		if(recording) {
			MethodSignature sig = (MethodSignature) thisJoinPoint.getSignature();
			Class<?> clazz = sig.getDeclaringType();

			if(fragmentClasses.get(clazz) == null) {
				fragmentClasses.put(clazz, new LinkedList<String>());
			}

			fragmentClasses.get(clazz).add(sig.getMethod().getName());

			//Class Inheritance
			Class<?> superClass = clazz.getSuperclass();
			if(superClass != null && appClasses.contains(superClass) && !fragmentClasses.containsKey(superClass)) {
				fragmentClasses.put(superClass, null);
			}

			Class<?>[] interfaces = clazz.getInterfaces();
			for(Class<?> aux: interfaces) {
				if(appClasses.contains(aux) && !fragmentClasses.containsKey(aux)) {
					fragmentClasses.put(aux, null);
				}
			}

			//Class Fields
			for(Field field: clazz.getDeclaredFields()) {
				if(field.isSynthetic()) continue;

				Class<?> fieldType = field.getType();

				if(Collection.class.isAssignableFrom(fieldType)) {
					Type genericFieldType = field.getGenericType();

					if(!ParameterizedType.class.isAssignableFrom(genericFieldType.getClass())) continue;

					Type aux = ((ParameterizedType)genericFieldType).getActualTypeArguments()[0];

					if(!Class.class.isAssignableFrom(aux.getClass())) continue;

					Class<?> actualFieldType = (Class<?>)aux;

					if(actualFieldType.isArray()) continue;

					if(appClasses.contains(actualFieldType) && !fragmentClasses.containsKey(actualFieldType)) {
						fragmentClasses.put(actualFieldType, null);
					}
				}

				else if(fieldType.isArray()) {
					Class<?> componentFieldType = fieldType.getComponentType();

					if(componentFieldType.isArray()) continue;

					if(appClasses.contains(componentFieldType) && !fragmentClasses.containsKey(componentFieldType)) {
						fragmentClasses.put(componentFieldType, null);
					}
				}

				else {
					if(appClasses.contains(fieldType) && !fragmentClasses.containsKey(fieldType)) {
						fragmentClasses.put(fieldType, null);
					}
				}
			}

			//Method Parameters
			Class<?>[] parameterTypes = sig.getMethod().getParameterTypes();
			for(Class<?> parameterType: parameterTypes) {
				if(appClasses.contains(parameterType) && !fragmentClasses.containsKey(parameterType)) {
					fragmentClasses.put(parameterType, null);
				}
			}

			//Method Return Type
			Class<?> returnType = sig.getMethod().getReturnType();
			if(appClasses.contains(returnType) && !fragmentClasses.containsKey(returnType)) {
				fragmentClasses.put(returnType, null);
			}
		}
	}

	private class Server extends Thread {

		private ServerSocket serverSocket;

		public void run() {
			try {
				serverSocket = new ServerSocket(DEFAULT_PORT);

				while(true) {
					Socket socket = serverSocket.accept();

					new RequestHandler(socket).start();
				}
			}
			catch(IOException e) { e.printStackTrace(); }
			finally {
				if(serverSocket != null) {
					try {
						serverSocket.close();
					} catch(IOException e) { e.printStackTrace(); }
				}
			}
		}

	}

	private class RequestHandler extends Thread {

		private Socket socket;
		private ObjectInputStream inputStream;
		private ObjectOutputStream outputStream;

		private RequestHandler(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			try {
				inputStream = new ObjectInputStream(socket.getInputStream());
				outputStream = new ObjectOutputStream(socket.getOutputStream());

				handleRequest();
			}
			catch (ClassNotFoundException e) { e.printStackTrace(); }
			catch (IOException e) { e.printStackTrace(); }
			finally {
				if(outputStream != null) {
					try {
						outputStream.close();
					} catch (IOException e) { e.printStackTrace(); }
				}
				if(inputStream != null) {
					try {
						inputStream.close();
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
				outputStream.writeObject(status);

				Integer request = (Integer)inputStream.readObject();
				if(status.intValue() == 0 && request.intValue() == 1) {
					startRecord();

					status = getCurrentStatus();
					outputStream.writeObject(status);
				}
				else if(status.intValue() == 1 && request.intValue() == 0) {
					endRecord();

					Map<String, ClassDataTransferObject> data = ClassesToBytes.getClassBytes(getFragmentClasses());
					outputStream.writeObject(data);
				}
				else if(request.intValue() == -1) {
					//END SESSION REQUEST
					outputStream.writeObject(new Integer(-1));
				}
				else {
					//OUT_OF_SYNC -> Ignore request
					outputStream.writeObject(new Integer(-1));
				}
			}
		}

	}

}