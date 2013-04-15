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
	
	public static final String ID = "pt.iscte.dcti.umlviewer.view.UMLViewer";
	
	//Guardar os nodes em exibição no grafo. Mapeá-los através de uma String com o nome da classe.
	
	private static final boolean SHOW_REPORTS = true;

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
		addNode(createFigure("WAITING"));
		setLayout();
	}

	public void setFocus() {
		graph.setFocus();
	}
	
	public void showFragment(Set<Class<?>> classes, Set<Method> methods) {
		Collection<IFigure> figures = new LinkedList<IFigure>();
		for(Class<?> clazz: classes) {
			figures.add(createFigure(clazz.getSimpleName()));
		}
		graph.getDisplay().syncExec(new Agent(figures));
	}
	
	private IFigure createFigure(String className) {
		if(SHOW_REPORTS) {
			System.out.println("CREATING FIGURE FOR CLASS: " + className);
		}
		
		IFigure figure = new UMLClassFigure(className);
		return figure;
	}
	
	private void addNode(IFigure figure) {
		if(SHOW_REPORTS) {
			System.out.println("TRYING TO ADD FIGURE TO GRAPH");
		}
		
		new UMLNode(graph, SWT.NONE, figure);
		
		if(SHOW_REPORTS) {
			System.out.println("LEAVING ADD METHOD");
		}
	}
	
	private void setLayout() {
		graph.setLayoutAlgorithm(new SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
	}
	
	private class Agent implements Runnable {

		private Collection<IFigure> figures;

		private Agent(Collection<IFigure> classes) {
			this.figures = classes;
		}

		public void run() {
			for(IFigure figure: figures) {
				addNode(figure);
			}
			setLayout();
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