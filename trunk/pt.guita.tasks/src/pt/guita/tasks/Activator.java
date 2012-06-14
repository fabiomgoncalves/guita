package pt.guita.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionContext;
import org.eclipse.mylyn.context.core.IInteractionContextManager;
import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;
import org.eclipse.mylyn.monitor.core.InteractionEvent;
import org.eclipse.mylyn.monitor.core.InteractionEvent.Kind;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import pt.guita.tasks.common.AnonymousClassMethodAccess;
import pt.guita.tasks.common.FieldAccess;
import pt.guita.tasks.common.MemberAccess;
import pt.guita.tasks.common.MethodAccess;
import pt.guita.tasks.tracer.Tracer;


/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator, IStructuredSelection {

	// The plug-in ID
	public static final String PLUGIN_ID = "pt.guita.tasks"; //$NON-NLS-1$

	private ServerSocket serverSocket = null;

	private List<Execution> execution = new ArrayList<Execution>();
	
	private static class Execution {
		IMethod method;
		int line;
		
		public Execution(IMethod method, int line) {
			this.method = method;
			this.line = line;
		}
		
		public boolean equals(Object obj) {
			return 
			obj instanceof Execution &&
			method.equals(((Execution) obj).method);
		}
		
		public int hashCode() {
			return method.hashCode();
		}
		
		@Override
		public String toString() {			
			return method.getElementName() + " - " + line;
		}
	}
	
	private int selection = -1;
	
	private static Activator _instance;

	private boolean rec;
	
	public Activator() {
		_instance = this;
		rec = true;
		System.out.println("ACTIVATED");
	}
	
	public void startRecording() {
		rec = true;
		selection = -1;
		execution.clear();
		IInteractionContextManager contextManager = ContextCore.getContextManager();
		contextManager.setContextCapturePaused(false);
		Socket clientSocket = null;
		try {
			clientSocket = new Socket("localhost", Tracer.PORT_IN);	
			OutputStream os = clientSocket.getOutputStream();  
			ObjectOutputStream oos = new ObjectOutputStream(os);  
			oos.writeObject(new Boolean(true));
			oos.writeObject(new Integer(6767));
			oos.close();
			os.close();
			clientSocket.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void stopRecording() {
		rec = false;		
		IInteractionContextManager contextManager = ContextCore.getContextManager();
		contextManager.setContextCapturePaused(true);
		Socket clientSocket = null;
		try {
			clientSocket = new Socket("localhost", Tracer.PORT_IN);	
			OutputStream os = clientSocket.getOutputStream();  
			ObjectOutputStream oos = new ObjectOutputStream(os);  
			oos.writeObject(new Boolean(false));
			oos.writeObject(new Integer(6767));
			oos.close();
			os.close();
			clientSocket.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Activator getInstance() {
		return _instance;
	}
	
	public void goToFirst() {
		if(!execution.isEmpty())
			selection = 0;
		else
			selection = -1;
	}

	
	public void next() {
		if(execution.isEmpty())
			selection = -1;
		else
			selection = (selection + 1) % execution.size();
	}
	
	public void updateSelection(Object obj) {
		for(Execution e : execution)
			if(e.method.equals(obj))
				selection = execution.indexOf(obj);
	}
	
	private void addMethod(IMethod method, int line) {
		Execution e = new Execution(method, line);
		if(!execution.contains(e)) {
			execution.add(e);
			System.out.println("ADD: " + e.method);
		}
	}
	
	public void clearContext() {		
		IInteractionContextManager contextManager = ContextCore.getContextManager();
		IInteractionContext activeContext = contextManager.getActiveContext();		
		contextManager.setContextCapturePaused(false);
		activeContext.delete(activeContext.getAllElements());		
		contextManager.setContextCapturePaused(true);
		execution.clear();
		selection = -1;
	}
	
	public void start(BundleContext context) throws Exception {
		try {
			serverSocket = new ServerSocket(6767);
		} 
		catch (IOException e) {
			System.err.println("Could not listen on port: 6767");
		}

		new Listener().start();
//		System.out.println("STARTED");
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

					if(rec)
						handleTraceRequest(access);					
				}
				catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}			
		}

		@SuppressWarnings("restriction")
		private void handleTraceRequest(MemberAccess access) {
			String className = access.className;
			int lastDot = className.lastIndexOf('.');
			String file = className.replace('.', '/').substring(lastDot+1) + ".java";
			System.out.println("TRACE: " + access + " : " + file);
						
			IInteractionContextManager contextManager = ContextCore.getContextManager();
			IInteractionContext activeContext = contextManager.getActiveContext();			
			
			IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
			for(IProject proj : wsRoot.getProjects()) {
				try {
					if(proj.isOpen() && proj.hasNature(JavaCore.NATURE_ID)) {
						IJavaProject javaProj = (IJavaProject) proj.getNature(JavaCore.NATURE_ID);
						handlePackages(access, file, activeContext, javaProj);
					}
				}
				catch (CoreException e) {
					System.err.println("PROBLEM: " + access.memberName);
					e.printStackTrace();
				}					
			}
		}

		private void handlePackages(MemberAccess access, String file,
				IInteractionContext activeContext, IJavaProject javaProj)
		throws JavaModelException {
			
			for(IPackageFragment pack : javaProj.getPackageFragments()) {
				if (pack.getKind() == IPackageFragmentRoot.K_SOURCE){
					ICompilationUnit[] compilationUnits = pack.getCompilationUnits();
					for (ICompilationUnit iCompilationUnit : compilationUnits) {									
						if(iCompilationUnit.getElementName().equals(file)) {	

							for (IType type : iCompilationUnit.getAllTypes()) {
								//								if(access instanceof FieldAccess) {
								//									handleFields(access, activeContext, type);
								//								}

								if(access instanceof AnonymousClassMethodAccess) {
									handleInnerClass((AnonymousClassMethodAccess) access, activeContext, type, access.line);
								}	
								else if(access instanceof MethodAccess) {										
									handleMethods(access.memberName, activeContext, type, access.line);										
								}
								else if(access instanceof FieldAccess) {
									handleFields(access, activeContext, type);
								}
							}
						}
					}
				}
			}
		}

		private  IType findType(IType type, int goal) throws JavaModelException {
			int index = 0;
			IMethod[] methods = type.getMethods();
			for(IMethod m : methods) {
				int c = countInner(m);
				if(index + c >= goal) {
					return m.getType("", goal-index);
				}
				index += c;
			}
			return null;
		}

		private int countInner(IMethod m) {
			int n = 0;
			IType t = null;
			do {
				t = m.getType("", n+1);	
				n++;							
			}
			while(t.exists());
				
			return n;
		}

		private void handleInnerClass(AnonymousClassMethodAccess access, IInteractionContext activeContext, IType type, int line) 
		throws JavaModelException { 			

			type = findType(type, access.anonymousLevels[0]);

			//			for(int i : access.anonymousLevels)
			//				type = type.getType("", i);

			handleMethods(access.memberName, activeContext, type, line);
		}

		private void handleMethods(String memberName, IInteractionContext activeContext, IType type, int line)
		throws JavaModelException {
			for (IMethod method : type.getMethods()) {				
				if(method.getElementName().equals(memberName)) {
					ContextCorePlugin.getContextManager()
					.processInteractionEvent(
							method,
							Kind.PROPAGATION, 
							InteractionEvent.ID_UNKNOWN, 
							activeContext);
					
					addMethod(method, line);
					
					if(execution.size() == 1)
						selection = 0;
					
					return;
				}
			}
		}

		
		private void handleFields(MemberAccess access,
				IInteractionContext activeContext, IType type)
		throws JavaModelException {
			for(IField field : type.getFields()) {
				System.out.println("Field: " + field);
				if(field.getElementName().equals(access.memberName)) {
					ContextCorePlugin.getContextManager()
					.processInteractionEvent(
							field,
							Kind.PROPAGATION, 
							InteractionEvent.ID_UNKNOWN, 
							activeContext);
					return;
				}
			}
		}		
	}

	private IFile f;

	private IFile locate(final String className) {
		IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();

		f = null;
		try {
			wsRoot.accept(new IResourceVisitor() { 
				public boolean visit(IResource resource) { 
					if (resource.getType() != IResource.FILE)
						return true; 

					String[] path = className.substring(0,className.lastIndexOf('.')).split("\\.");
					IContainer c = resource.getParent();

					for(int i = path.length - 1; i >= 0; i--) {
						if(c == null || !c.getName().equals(path[i]))
							return false;

						c = c.getParent();
					}
					int lastDot = className.lastIndexOf('.');
					String file = className.replace('.', '/').substring(lastDot+1) + ".java";

					if (resource.getName().equals(file)) {
						f = (IFile) resource;
					}
					return false; 
				} 
			});
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		return f;
	}

	public void stop(BundleContext context) throws Exception {


	}

	@Override
	public boolean isEmpty() {
		return selection == -1;
	}

	@Override
	public Object getFirstElement() {
		return selection == -1 ? null : execution.get(selection).method;
	}
	
	public int getElementLine() {
		return execution.get(selection).line;
	}
	
	@Override
	public Iterator iterator() {
		return Arrays.asList(getFirstElement()).iterator();
	}

	@Override
	public int size() {
		return isEmpty() ? 0 : 1;
	}

	@Override
	public Object[] toArray() {
		return new Object[] { getFirstElement() };
	}

	@Override
	public List toList() {
		return Arrays.asList(getFirstElement());
	}

	

}
