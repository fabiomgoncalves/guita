package pt.iscte.dcti.umlviewer.view;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.IContainer;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;
//import org.osgi.framework.Bundle;
//import org.osgi.framework.BundleContext;
//import org.osgi.framework.FrameworkUtil;
import pt.iscte.dcti.umlviewer.service.Service;

public class UMLViewer extends ViewPart implements Service {
	
	private static final boolean SHOW_REPORTS = true;
	
	private final Color CLASS_COLOR = new Color(null, 255, 255, 206);
	private final Font CLASS_FONT = new Font(null, "Arial", 12, SWT.BOLD);
	private final Image CLASS_IMG = new Image(Display.getDefault(), UMLClassFigure.class.getResourceAsStream("images/class_obj.gif"));
	private final Image PUBLIC_IMG = new Image(Display.getDefault(), UMLClassFigure.class.getResourceAsStream("images/methpub_obj.gif"));
	private final Image PRIVATE_IMG = new Image(Display.getDefault(), UMLClassFigure.class.getResourceAsStream("images/field_private_obj.gif"));

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
		graph.setConnectionStyle(ZestStyles.CONNECTIONS_DIRECTED);
		graph.setLayoutAlgorithm(new SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
		addNode(createClassFigure("Waiting"));
	}

	public void setFocus() {
		graph.setFocus();
	}

	/*public void showFragment(Set<Class<?>> classes, Set<Method> methods) {
		graph.getDisplay().syncExec(new Agent(classes));
	}*/
	
	public void showFragment(Set<Class<?>> classes, Set<Method> methods) {
		Collection<IFigure> figures = new LinkedList<IFigure>();
		for(Class<?> clazz: classes) {
			figures.add(createClassFigure(clazz.getSimpleName()));
		}
		graph.getDisplay().syncExec(new Agent(figures));
	}

	/*private void addNode(String str) {
		System.out.println("ENTROU");
		new UMLNode(graph, SWT.NONE, createClassFigure(str));
		System.out.println("VAI SAIR");
	}*/
	
	private void addNode(IFigure figure) {
		if(SHOW_REPORTS) {
			System.out.println("TRYING TO ADD FIGURE TO GRAPH");
		}
		
		new UMLNode(graph, SWT.NONE, figure);
		
		if(SHOW_REPORTS) {
			System.out.println("LEAVING ADD METHOD");
		}
	}
	
	private IFigure createClassFigure(String className) {
		if(SHOW_REPORTS) {
			System.out.println("CREATING FIGURE FOR CLASS: " + className);
		}
		Label label = new Label(className, CLASS_IMG);
		label.setFont(CLASS_FONT);
		
		UMLClassFigure classFigure = new UMLClassFigure(CLASS_COLOR, label);
		
		Label attribute1 = new Label("attr1", PRIVATE_IMG);
		Label attribute2 = new Label("attr2", PRIVATE_IMG);
		Label method1 = new Label("method1", PUBLIC_IMG);
		Label method2 = new Label("method2", PUBLIC_IMG);
		
		classFigure.getAttributesCompartment().add(attribute1);
		classFigure.getAttributesCompartment().add(attribute2);
		classFigure.getMethodsCompartment().add(method1);
		classFigure.getMethodsCompartment().add(method2);
		classFigure.setSize(-1, -1);
		
		return classFigure;
	}

	/*private class Agent implements Runnable {

		private Set<Class<?>> classes;

		private Agent(Set<Class<?>> classes) {
			this.classes = classes;
		}

		public void run() {
			for(Class<?> clazz: classes) {
				addNode(clazz.getSimpleName());
				System.out.println("CLASS: " + clazz.getSimpleName() + " -> " + clazz.getName());
			}
		}

	}*/
	
	private class Agent implements Runnable {

		private Collection<IFigure> figures;

		private Agent(Collection<IFigure> classes) {
			this.figures = classes;
		}

		public void run() {
			for(IFigure figure: figures) {
				addNode(figure);
			}
		}

	}
	
	class UMLNode extends GraphNode {
		
		IFigure customFigure = null;

		public UMLNode(IContainer graphModel, int style, IFigure figure) {
			super(graphModel, style, figure);
		}

		protected IFigure createFigureForModel() {
			return (IFigure) this.getData();
		}

	}

}