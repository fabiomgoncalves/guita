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


public aspect Tracer {
	public static final int PORT_IN = 5656;

	private ServerSocket serverSocket;
	private int portOut;

	private static boolean rec;
	private int serialNumber;
	private HashSet<MemberAccess> sent;

	protected pointcut scope() : !within(Tracer);

	public Tracer() {
//		System.out.println("HI from TRACER!");	
		try {
			serverSocket = new ServerSocket(PORT_IN);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		rec = false;
		sent = new HashSet<MemberAccess>();
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

					Boolean activate = (Boolean) ois.readObject();  
					Integer port = (Integer) ois.readObject();					
					ois.close();
					is.close();
					sock.close();

					if(rec && activate)  {
						System.err.println("Cannot start new recording");
					}
					else if(!rec && !activate) {
						System.err.println("Not recording");
					}
					else {
						rec = activate.booleanValue();
						portOut = port;

//						System.out.println("REQUEST: " + activate + "  " + port);
						if(rec) {
							serialNumber = 1;
							sent.clear();
						}
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}			
			}
		}
	}


	after() :
		if(rec) && get(* *) && scope() {
			Field field = ((FieldSignature) thisJoinPoint.getSignature()).getField();
			SourceLocation loc = thisJoinPointStaticPart.getSourceLocation();		
			send(new FieldAccess(field, Type.READ, loc.getLine()));
		}

	after() :
		if(rec) && set(* *) && scope() {
			Field field = ((FieldSignature) thisJoinPoint.getSignature()).getField();
			SourceLocation loc = thisJoinPointStaticPart.getSourceLocation();		
			send(new FieldAccess(field, Type.WRITE, loc.getLine()));
		}

	before() :
		if(rec) && execution(* * (..)) && scope() {

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
				clientSocket = new Socket("localhost", portOut);	
				OutputStream os = clientSocket.getOutputStream();  
				ObjectOutputStream oos = new ObjectOutputStream(os);  
				oos.writeObject(access);
				oos.close();
				os.close();
				clientSocket.close();
			} 
			catch (IOException ex) {
				System.err.println("Accept failed: " + portOut);
			}
			sent.add(access);
			serialNumber++;
		}
	}
}
