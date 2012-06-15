package pt.guita.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import pt.guita.tasks.common.MemberAccess;


/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator {

	public static final String PLUGIN_ID = "pt.guita.tasks";

	
	private static Activator _instance;

	private Recorder recorder;
	private ServerSocket serverSocket = null;
	
	public Activator() {
		_instance = this;
		recorder = new Recorder();
	}
	
	public static Activator getInstance() {
		if(_instance == null)
			throw new IllegalStateException("Not activated");
		
		return _instance;
	}
	
	Recorder getRecorder() {
		return recorder;
	}
		
	public void start(BundleContext context) throws Exception {
		try {
			serverSocket = new ServerSocket(6767);
		} 
		catch (IOException e) {
			System.err.println("Could not listen on port: 6767");
		}

		new Listener().start();
	}
	

	public void stop(BundleContext context) throws Exception {
		

	}

	public class Listener extends Thread {
		@Override
		public void run() {
			Socket sock;
			while(true) {
				try {
					sock = serverSocket.accept();
					InputStream is = sock.getInputStream();  
					ObjectInputStream ois = new ObjectInputStream(is);  
					MemberAccess access = (MemberAccess) ois.readObject();  
					ois.close();
					is.close();
					sock.close();

					if(recorder.isRecOn())
						recorder.handleTraceRequest(access);					
				}
				catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}			
		}
	}
		

//	private IFile f;
//
//	private IFile locate(final String className) {
//		IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
//
//		f = null;
//		try {
//			wsRoot.accept(new IResourceVisitor() { 
//				public boolean visit(IResource resource) { 
//					if (resource.getType() != IResource.FILE)
//						return true; 
//
//					String[] path = className.substring(0,className.lastIndexOf('.')).split("\\.");
//					IContainer c = resource.getParent();
//
//					for(int i = path.length - 1; i >= 0; i--) {
//						if(c == null || !c.getName().equals(path[i]))
//							return false;
//
//						c = c.getParent();
//					}
//					int lastDot = className.lastIndexOf('.');
//					String file = className.replace('.', '/').substring(lastDot+1) + ".java";
//
//					if (resource.getName().equals(file)) {
//						f = (IFile) resource;
//					}
//					return false; 
//				} 
//			});
//		} catch (CoreException e1) {
//			e1.printStackTrace();
//		}
//		return f;
//	}
	
}
