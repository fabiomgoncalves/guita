package pt.iscte.dcti.umlviewer.view;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
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

	//GREPCODE
	//A classe deverá ter o nome completo (com o caminho),
	//e os métodos têm de ter return type e parametros de entrada.
	
	//Queue com as classes recentemente adicionadas, a qual será consultada para efectuar as ligações.
	//ou
	//Adiciona uma classe, e cria as novas ligações. Irá proceder assim classe a classe.
	
	//Filtrar métodos públicos/protected/private? Possívelmente, não incluir o private.

	private static final boolean SHOW_REPORTS = true;

	private static UMLViewer instance;

	private Composite composite;
	private Graph graph;

	private Map<Class<?>, UMLNode> nodes;

	public UMLViewer() {
		instance = this;
		nodes = new HashMap<Class<?>, UMLNode>();
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
	}

	public void setFocus() {
		graph.setFocus();
	}

	public void showFragment(Map<Class<?>, Collection<Method>> fragment_classes) {
		graph.getDisplay().syncExec(new Agent(fragment_classes));
	}
	
	public void clear() {
		if(SHOW_REPORTS) {
			System.out.println("SERVICE CLEAR");
		}
	}

	private boolean existsNode(Class<?> clazz) {
		return nodes.containsKey(clazz);
	}

	private void createNode(Class<?> clazz) {
		if(SHOW_REPORTS) {
			System.out.println("CREATING NODE FOR CLASS: " + clazz.getSimpleName());
		}

		UMLClassFigure figure = new UMLClassFigure(clazz.getSimpleName());
		UMLNode node = new UMLNode(graph, SWT.NONE, figure);
		nodes.put(clazz, node);

		if(SHOW_REPORTS) {
			System.out.println("NODE CREATED FOR CLASS: " + clazz.getSimpleName());
		}
	}

	private void addMethodsToNode(Class<?> clazz, Collection<Method> class_methods) {
		if(SHOW_REPORTS) {
			System.out.println("ADDING METHODS TO: " + clazz.getSimpleName());
			System.out.println("METHODS TO ADD: " + class_methods.toString());
		}

		UMLNode node = nodes.get(clazz);
		UMLClassFigure figure = (UMLClassFigure)node.getNodeFigure();
		figure.addMethods(class_methods);
	}

	//Problema de ligações duplicadas, quando a clazz tem uma referencia para si propria, em que irá ficar duplicado
	private void setConnections(Class<?> clazz) {
		//#2
		//Quem liga a si
		for(Entry<Class<?>, UMLNode> entry: nodes.entrySet()) {
			Class<?> tmp = entry.getKey();
			for(Field field: tmp.getDeclaredFields()) {
				if(!field.isSynthetic()) {
					if(field.getType() == clazz) {
						new GraphConnection(graph, SWT.NONE, nodes.get(tmp), nodes.get(clazz));
					}
				}
			}
			
			//A quem se liga
			for(Field field: clazz.getDeclaredFields()) {
				if(!field.isSynthetic()) {
					if(nodes.containsKey(field.getType())) {
						new GraphConnection(graph, SWT.NONE, nodes.get(clazz), nodes.get(field.getType()));
					}
				}
			}
			
			//Quem descende de si
			for(Class<?> c: nodes.keySet()) {
				if(c != clazz) {
					if(clazz.isAssignableFrom(c)) {
						new GraphConnection(graph, SWT.NONE, nodes.get(c), nodes.get(clazz));
					}
				}
			}
			
			//De quem descende
			for(Class<?> c: nodes.keySet()) {
				if(c != clazz) {
					if(c.isAssignableFrom(clazz)) {
						new GraphConnection(graph, SWT.NONE, nodes.get(clazz), nodes.get(c));
					}
				}
			}
		}
	}
	
	//#2 - EM FALTA: Colecções/Arrays (agregação e composição); Subclasses e superclasses

	private void setLayout() {
		graph.setLayoutAlgorithm(new SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
	}

	private class Agent implements Runnable {

		Map<Class<?>, Collection<Method>> fragment_classes;

		private Agent(Map<Class<?>, Collection<Method>> fragment_classes) {
			this.fragment_classes = fragment_classes;
		}

		public void run() {
			for(Entry<Class<?>, Collection<Method>> entry: fragment_classes.entrySet()) {
				if(entry.getValue() != null) {
					if(!existsNode(entry.getKey())) {
						createNode(entry.getKey());
					}
					addMethodsToNode(entry.getKey(), entry.getValue());
					setConnections(entry.getKey());
				}
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