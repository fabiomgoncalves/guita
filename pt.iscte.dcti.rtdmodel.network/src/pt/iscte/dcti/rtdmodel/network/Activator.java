package pt.iscte.dcti.rtdmodel.network;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipselabs.jmodeldiagram.DiagramListener;
import org.eclipselabs.jmodeldiagram.DiagramListener.Event;
import org.eclipselabs.jmodeldiagram.JModelDiagram;
import org.eclipselabs.jmodeldiagram.model.JOperation;
import org.eclipselabs.jmodeldiagram.model.JType;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {
	private static final int DEFAULT_PORT = 8081;

	private static Activator instance;

	public static Activator getInstance() {
		return instance;
	}

	private int socketPort = DEFAULT_PORT;

	private DiagramListener listener = new DiagramListener.Adapter() {
		@Override
		public void operationEvent(JOperation op, Event event) {
			IType type = findType(op.getOwner().getQualifiedName());
			if(type != null) {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					ITextEditor editor = (ITextEditor) IDE.openEditor(page, (IFile) type.getResource());
					for(IMethod im : type.getMethods()) {
						if(im.getElementName().equals(op.getName())) {
							ISourceRange range =  im.getNameRange();
							editor.selectAndReveal(range.getOffset(), range.getLength());
						}
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}

		private IType findType(String qualifiedName) {
			IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();

			for(IProject p : workspace.getProjects()) {
				try {
					if(p.getNature(JavaCore.NATURE_ID) != null) {
						IJavaProject proj = JavaCore.create(p);
						IType t = proj.findType(qualifiedName);
						if(t != null)
							return t;
					}
				}
				catch (CoreException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		public void classEvent(JType type, Event event) {
			IType t = findType(type.getQualifiedName());
			if(type != null) {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				ITextEditor editor;
				try {
					editor = (ITextEditor) IDE.openEditor(page, (IFile) t.getResource());
					ISourceRange range =  t.getNameRange();
					editor.selectAndReveal(range.getOffset(), range.getLength());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	};

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;
		JModelDiagram.addListener(new DiagramListener.EventFilter(listener, Event.DOUBLE_CLICK));
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		instance = null;
	}

	public void setSocketPort(int port) {
		socketPort = port;
	}

	public int getSocketPort() {
		return socketPort;
	}
}
