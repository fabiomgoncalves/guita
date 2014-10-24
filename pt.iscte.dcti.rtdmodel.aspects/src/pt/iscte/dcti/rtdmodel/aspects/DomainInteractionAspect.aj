package pt.iscte.dcti.rtdmodel.aspects;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.reflect.MethodSignature;
import org.eclipselabs.jmodeldiagram.model.Association;
import org.eclipselabs.jmodeldiagram.model.JClass;
import org.eclipselabs.jmodeldiagram.model.JInterface;
import org.eclipselabs.jmodeldiagram.model.JModel;
import org.eclipselabs.jmodeldiagram.model.JOperation;
import org.eclipselabs.jmodeldiagram.model.JType;


public abstract aspect DomainInteractionAspect {

	private static final int DEFAULT_PORT = 8081;

//	private Set<Class<?>> appClasses;
	private boolean recording;

	private JModel fragment;
	private Map<JType, Class<?>> mapTypes;

	private static Server server;
	private int socketPort;

	public DomainInteractionAspect(int socketPort) {
		this.socketPort = socketPort;
//		appClasses = new HashSet<Class<?>>();
		recording = false;
		System.out.println("DIA aspect CONST");	
	}

	public DomainInteractionAspect() {
		this(DEFAULT_PORT);
	}


	private Integer getCurrentStatus() {
		return recording ? new Integer(1) : new Integer(0);
	}

	private void startRecord() {
		fragment = new JModel();
		mapTypes = new HashMap<JType, Class<?>>();
		recording = true;
	}

	private void endRecord() {
		recording = false;
	}


	protected abstract pointcut scope();

	private static boolean initialized() {
		return server != null;
	}

	after() : scope() && staticinitialization(*) && if(!initialized()) {
//		appClasses.add(thisJoinPoint.getSignature().getDeclaringType());
//		System.out.println("STATIC: " + thisJoinPoint.getSignature().getDeclaringType().getName() + " " +
//				thisJoinPoint.getSourceLocation() + " " + Thread.currentThread().getId());
		if(server == null) {
			System.out.println("no SERVER - " + thisJoinPoint.getSourceLocation());

			server = new Server();
			server.start();
			System.out.println("aspect SERVER started");
		}
	}

	after(Object o) : scope() && execution(public !static * *.* (..)) && target(o) {
		//synchronized(this) {
//			if(server == null) {
//				System.out.println("no SERVER - " + thisJoinPoint.getSourceLocation());
//
//				server = new Server();
//				server.start();
//				System.out.println("aspect SERVER started");
//			}
		//}
		
		if(recording) {
			System.out.println(thisJoinPoint.getSignature());
			
			MethodSignature sig = (MethodSignature) thisJoinPoint.getSignature();
			Class<?> clazz = sig.getDeclaringType();

//			if(clazz.isSynthetic() || clazz.isAnonymousClass())
//				return;

			JType owner = null;

			if(!fragment.hasType(clazz.getName())) {
				owner = handleNewClass(clazz);
			}

			JType t = fragment.getType(clazz.getName());
			JOperation op = new JOperation(t, sig.getMethod().getName());
			int line = thisJoinPoint.getSourceLocation().getLine();
			op.setProperty("LINE", Integer.toString(line));

			
			//Class<?>[] interfaces = clazz.getInterfaces();
			//			for(Class<?> aux: interfaces) {
			//				if(appClasses.contains(aux) && !fragmentClasses.containsKey(aux)) {
			//					fragmentClasses.put(aux, null);
			//				}
			//			}



			//Method Parameters
			//Class<?>[] parameterTypes = sig.getMethod().getParameterTypes();
			//			for(Class<?> parameterType: parameterTypes) {
			//				if(appClasses.contains(parameterType) && !fragmentClasses.containsKey(parameterType)) {
			//					fragmentClasses.put(parameterType, null);
			//				}
			//			}

			//Method Return Type
			//Class<?> returnType = sig.getMethod().getReturnType();
			//			if(appClasses.contains(returnType) && !fragmentClasses.containsKey(returnType)) {
			//				fragmentClasses.put(returnType, null);
			//			}
		}
	}

	private JType handleNewClass(Class<?> clazz) {
		JType owner;
		String name = clazz.getName();
		owner = clazz.isInterface() ? new JInterface(name) : new JClass(name);
		fragment.addType(owner);
		mapTypes.put(owner, clazz);
		//		if(!clazz.isInterface())
		//			handleFields(clazz, owner);

		return owner;
	}

	private void handleInheritanceRelations() {
		for(Class<?> c : mapTypes.values()) {
			if(!c.isInterface() && !c.getSuperclass().equals(Object.class) && fragment.hasType(c.getSuperclass().getName())) {
				JClass derived = (JClass) fragment.getType(c.getName());
				JClass base = (JClass) fragment.getType(c.getSuperclass().getName());
				derived.setSuperclass(base);

			}
		}
	}

	private void handleAssociations() {
		for(Class<?> c : mapTypes.values()) {
			if(c.isInterface())
				continue;

			for(Method method : c.getMethods()) {
				Class<?> retType = method.getReturnType();
				if(Collection.class.isAssignableFrom(retType)) {	
					Type returnType = method.getGenericReturnType();
					if(returnType instanceof ParameterizedType) {
						Type genericType = ((ParameterizedType)returnType).getActualTypeArguments()[0];
						Class<?> targetType = (Class<?>) genericType;
						if(fragment.hasType(targetType.getName())) {
							JType source = fragment.getType(c.getName());
							JType target = fragment.getType(targetType.getName());
							new Association((JClass) source, target, false);
						}
					}
				}
				else if(fragment.hasType(retType.getName())) {
					JType source = fragment.getType(c.getName());
					JType target = fragment.getType(retType.getName());
					new Association((JClass) source, target, true);
				}


			}
		}
	}

	private void handleFields(Class<?> clazz, JType owner) {
		for(Field field: clazz.getDeclaredFields()) {
			if(field.isSynthetic()) continue;

			Class<?> fieldType = field.getType();

			Class<?> relTarget = null;
			boolean unary = false;

			if(Collection.class.isAssignableFrom(fieldType)) {
				Type genericFieldType = field.getGenericType();

				if(!ParameterizedType.class.isAssignableFrom(genericFieldType.getClass())) continue;

				Type aux = ((ParameterizedType)genericFieldType).getActualTypeArguments()[0];

				if(!Class.class.isAssignableFrom(aux.getClass())) continue;

				if(((Class<?>)aux).isArray()) continue;

				relTarget = (Class<?>) aux;
			}

			else if(fieldType.isArray()) {
				Class<?> componentFieldType = fieldType.getComponentType();

				if(componentFieldType.isArray()) continue;

				relTarget = componentFieldType;
			}

//			else if(appClasses.contains(fieldType)){
//				relTarget = fieldType;
//				unary = true;
//			}

			if(owner instanceof JClass && relTarget != null) {
				JType target = relTarget.isInterface() ? new JInterface(relTarget.getName()) : new JClass(relTarget.getName());

				fragment.addType(target);
				new Association((JClass) owner, target, unary);
			}
		}
	}



	private class Server extends Thread {

		private ServerSocket serverSocket;

		public void run() {
			try {
				serverSocket = new ServerSocket(socketPort);

				while(true) {
					Socket socket = serverSocket.accept();

					new RequestHandler(socket).start();
					System.out.println("NEWRECORD");
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
			synchronized(DomainInteractionAspect.this) {
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
					handleInheritanceRelations();
					handleAssociations();
					outputStream.writeObject(fragment);
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