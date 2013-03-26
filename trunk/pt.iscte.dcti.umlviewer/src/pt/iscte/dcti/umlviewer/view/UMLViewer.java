package pt.iscte.dcti.umlviewer.view;

import java.lang.reflect.Method;
import java.util.Set;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;
//import org.osgi.framework.Bundle;
//import org.osgi.framework.BundleContext;
//import org.osgi.framework.FrameworkUtil;
import pt.iscte.dcti.umlviewer.service.Service;

public class UMLViewer extends ViewPart implements Service {

	private static UMLViewer instance;

	private Composite composite;
	private Graph graph;

	public UMLViewer() {
		instance = this;
		//Bundle bundle = FrameworkUtil.getBundle(this.getClass());
		//BundleContext context = bundle.getBundleContext();
		//context.registerService(Service.class.getName(), this, null);
	}

	public static UMLViewer getInstance() {
		return instance;
	}

	public void createPartControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());
		graph = new Graph(composite, SWT.NONE);
		graph.setLayoutAlgorithm(new SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
		new GraphNode(graph, SWT.NONE, "WAITING FOR A CALL");
	}

	public void setFocus() {
		graph.setFocus();
	}

	public void showFragment(Set<Class<?>> classes, Set<Method> methods) {
		graph.getDisplay().syncExec(new Agent(classes));
	}

	private void addNode(String str) {
		new GraphNode(graph, SWT.NONE, str);
	}

	private class Agent implements Runnable {

		private Set<Class<?>> classes;

		private Agent(Set<Class<?>> classes) {
			this.classes = classes;
		}

		public void run() {
			for(Class<?> clazz: classes) {
				addNode(clazz.getName());
			}
		}

	}

}