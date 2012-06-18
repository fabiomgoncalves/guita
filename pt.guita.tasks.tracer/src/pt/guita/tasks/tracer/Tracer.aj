package pt.guita.tasks.tracer;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

import org.aspectj.lang.reflect.FieldSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.reflect.SourceLocation;

import pt.guita.tasks.common.AnonymousClassMethodAccess;
import pt.guita.tasks.common.FieldAccess;
import pt.guita.tasks.common.MemberAccess;
import pt.guita.tasks.common.MethodAccess;
import pt.guita.tasks.common.FieldAccess.Type;
import pt.guita.tasks.common.TraceRequest;


public aspect Tracer {
	public static final int PORT_IN = 5656;

	private ServerSocket serverSocket;

	private static TraceRequest currentRequest;

	private int serialNumber;
	private HashSet<MemberAccess> sent;

	protected pointcut scope() : !within(Tracer);

	public Tracer() {
		System.out.println("HI from TRACER!");	
		try {
			serverSocket = new ServerSocket(PORT_IN);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		sent = new HashSet<MemberAccess>();
		currentRequest = new TraceRequest();
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

					TraceRequest request = (TraceRequest) ois.readObject();
					
//					Boolean activate = (Boolean) ois.readObject();  
//					Integer port = (Integer) ois.readObject();
					
					ois.close();
					is.close();
					sock.close();

					if(currentRequest.activate() && request.activate())  {
						System.err.println("Cannot start new recording");
					}
					else if(!currentRequest.activate() && !request.activate()) {
						System.err.println("Not recording");
					}
					else {
						currentRequest = request;
//						System.out.println("REQUEST: " + activate + "  " + port);
						if(currentRequest.activate()) {
							serialNumber = 1;
							sent.clear();
						}
					}
				}
				catch(Exception e) {
					System.err.println("prob");
					e.printStackTrace();
					
				}			
			}
		}
	}


	after() :
		if(currentRequest.activate() && currentRequest.traceFieldReads) && get(* *) && scope() {
			Field field = ((FieldSignature) thisJoinPoint.getSignature()).getField();
			SourceLocation loc = thisJoinPointStaticPart.getSourceLocation();		
			send(new FieldAccess(field, Type.READ, loc.getLine()));
		}

	after() :
		if(currentRequest.activate() && currentRequest.traceFieldWrites) && set(* *) && scope() {
			Field field = ((FieldSignature) thisJoinPoint.getSignature()).getField();
			SourceLocation loc = thisJoinPointStaticPart.getSourceLocation();		
			send(new FieldAccess(field, Type.WRITE, loc.getLine()));
		}

	before() :
		if(currentRequest.activate()) && execution(* * (..)) && scope() {

			MethodSignature sig = (MethodSignature) thisJoinPoint.getSignature();
			Method method = sig.getMethod();
			SourceLocation loc = thisJoinPointStaticPart.getSourceLocation();
			Object obj = thisJoinPoint.getThis();
			Class<?> clazz = obj == null ? method.getDeclaringClass() : obj.getClass();

			MethodAccess access = null;
			if(clazz.isAnonymousClass()) {
				access = new AnonymousClassMethodAccess(method, clazz, serialNumber, loc.getLine());
			}
			else {
				access = new MethodAccess(method, clazz, serialNumber, loc.getLine());
			}

			send(access);			
		}


	private void send(MemberAccess access) {
		if(!sent.contains(access)) {
//			System.out.println("SEND: " + access);
			Socket clientSocket = null;
			try {
				clientSocket = new Socket("localhost", currentRequest.port);	
				OutputStream os = clientSocket.getOutputStream();  
				ObjectOutputStream oos = new ObjectOutputStream(os);  
				oos.writeObject(access);
				oos.close();
				os.close();
				clientSocket.close();
			} 
			catch (IOException ex) {
				System.err.println("Accept failed: " + currentRequest.port);
			}
			sent.add(access);
			serialNumber++;
		}
	}
}
