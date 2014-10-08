package pt.iscte.dcti.umlviewer.view;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionEndpointLocator;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;
import org.eclipse.draw2d.geometry.PointList;
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

import pt.iscte.dcti.umlviewer.service.ClickHandler;
import pt.iscte.dcti.umlviewer.service.Service;

public class UMLViewer extends ViewPart implements Service {

	//---------------------------------------------
	//Notas de disserta��o:
	//Ideia da proxy (em alternativa aos aspectos).
	//Justificar o uso dos aspectos.
	//FALAR PROF: Limita��o quando se trata de DAO e Entity em projetos WEB. Seria necess�rio melhorar o prot�tipo.
	//DAO � usada para acesso aos dados, e � nela que s�o chamados os m�todos.
	//Entity a.k.a. Model s�o as data transfer objects, e apenas servem para passar os dados entre as camadas.
	//O ideal seria combinar os m�todos invocados na DAO com as rela��es que as Entidades t�m entre si.
	//Exemplos: Solution e Portal. No Solution seria poss�vel, mas no Portal n�o.
	//---------------------------------------------

	//---------------------------------------------
	//Refer�ncias:
	//GREPCODE
	//http://www.ibm.com/developerworks/rational/library/content/RationalEdge/sep04/bell/
	//http://help.eclipse.org/juno/index.jsp --> API do ZEST apenas dispon�vel a partir do kepler
	//http://www.eclipse.org/articles/Article-GEF-Draw2d/GEF-Draw2d.html
	//http://git.eclipse.org/c/gef/org.eclipse.gef.git/tree/org.eclipse.zest.tests/src/org/eclipse/zest/tests/swt
	//http://www.vogella.com/tutorials/EclipseZest/article.html
	//---------------------------------------------

	//---------------------------------------------
	//Notas da aplica��o:
	//BUG: Duas liga��es distintas mas para a mesma classe ficam sobrepostas. (Curve?)
	//BUG: N�o desenha correctamente uma liga��o para si mesmo (node). (VER GraphSnippet10, GraphSnippet11, 12, e 13) (DESATIVADO)
	//BUG: Se mover uma classe, esta n�o ir� adicionar novos m�todos.
	//---------------------------------------------

	//------------------------------------------------------
	//------------------------------------------------------
	//					SINGLETON
	//------------------------------------------------------
	//------------------------------------------------------
	private static UMLViewer instance;

	public static UMLViewer getInstance() {
		return instance;
	}
	//######################################################
	//######################################################

	private static final boolean SHOW_REPORTS = false;

	private Composite composite;
	private Graph graph;

	private Map<Class<?>, UMLNode> nodesMapper;

	public UMLViewer() {
		instance = this;
		nodesMapper = new HashMap<Class<?>, UMLNode>();
	}

	public void createPartControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());
		graph = new Graph(composite, SWT.NONE);
	}

	public void setFocus() {
		graph.setFocus();
	}

	//------------------------------------------------------
	//------------------------------------------------------
	//				SERVICE SHOWFRAGMENT
	//------------------------------------------------------
	//------------------------------------------------------
	public void showFragment(Map<Class<?>, Collection<Method>> fragmentClasses) {
		if(SHOW_REPORTS) {
			System.out.println("SERVICE SHOWFRAGMENT");
		}

		graph.getDisplay().syncExec(new ShowFragmentAgent(fragmentClasses)); //Agent will call showFragmentAction
	}

	private void showFragmentAction(Map<Class<?>, Collection<Method>> fragmentClasses) {
		Set<Class<?>> previousClasses = getCurrentClasses(); //A set with all the classes before start adding
		Set<Class<?>> addedClasses = new HashSet<Class<?>>(); //A set with all added classes

		for(Entry<Class<?>, Collection<Method>> entry: fragmentClasses.entrySet()) {

			if(entry.getValue() == null || entry.getValue().isEmpty()) continue; //If the class has no methods to show

			if(!existsNode(entry.getKey())) {
				createNode(entry.getKey());
				addedClasses.add(entry.getKey());
			}
			addMethodsToNode(entry.getKey(), entry.getValue());

		}

		setConnections(previousClasses, addedClasses);
		setLayout();
	}

	private Set<Class<?>> getCurrentClasses() {
		Set<Class<?>> currentClasses = new HashSet<Class<?>>();
		currentClasses.addAll(nodesMapper.keySet());

		return currentClasses;
	}

	private boolean existsNode(Class<?> clazz) {
		return nodesMapper.containsKey(clazz);
	}

	private void createNode(Class<?> clazz) {
		UMLClassFigure figure = new UMLClassFigure(clazz, listeners);
		UMLNode node = new UMLNode(graph, SWT.NONE, figure);
		nodesMapper.put(clazz, node);

		if(SHOW_REPORTS) {
			System.out.println("NODE CREATED FOR CLASS: " + clazz.getSimpleName());
		}
	}

	private void addMethodsToNode(Class<?> clazz, Collection<Method> methodsToAdd) {
		UMLNode node = nodesMapper.get(clazz);
		UMLClassFigure nodeFigure = (UMLClassFigure)node.getNodeFigure();
		nodeFigure.addMethods(methodsToAdd);

		if(SHOW_REPORTS) {
			System.out.println("ADDED METHODS TO CLASS: " + clazz.getSimpleName() + " -> " + methodsToAdd.toString());
		}
	}

	private void setConnections(Set<Class<?>> previousClasses, Set<Class<?>> addedClasses) {
		for(Class<?> clazz: previousClasses) { //Sets connections between the previous classes and the added classes
			setClassConnections(clazz, addedClasses);
		}

		for(Class<?> clazz: addedClasses) { //Sets connections between the added classes and all the classes
			setClassConnections(clazz, nodesMapper.keySet());
		}
	}

	private void setClassConnections(Class<?> clazz, Set<Class<?>> clazzSet) {
		setInheritanceConnections(clazz, clazzSet);
		setAssociationConnections(clazz, clazzSet);
	}

	private void setInheritanceConnections(Class<?> clazz, Set<Class<?>> clazzSet) {
		Class<?> superClass = clazz.getSuperclass(); //extends
		if(superClass != null && clazzSet.contains(superClass)) {
			createExtendsConnection(clazz, superClass);
		}

		for(Class<?> temp: clazz.getInterfaces()) { //implements
			if(clazzSet.contains(temp)) {
				if(clazz.isInterface()) { //An interface can extend many interfaces, but those are obtained with getInterfaces method 
					createExtendsConnection(clazz, temp);
				}
				else {
					createImplementsConnection(clazz, temp);
				}
			}
		}
	}

	private void createExtendsConnection(Class<?> sourceClass, Class<?> destinationClass) {
		GraphConnection connection = new GraphConnection(graph, ZestStyles.NONE, nodesMapper.get(sourceClass), nodesMapper.get(destinationClass));
		connection.setLineColor(ColorConstants.black);
		connection.setHighlightColor(ColorConstants.darkGreen);

		if(!PolylineConnection.class.isAssignableFrom(connection.getConnectionFigure().getClass())) return;

		PolylineConnection connectionFigure = (PolylineConnection)connection.getConnectionFigure();

		PointList decorationPointList = new PointList();
		decorationPointList.addPoint(-2,2);
		decorationPointList.addPoint(0,0);
		decorationPointList.addPoint(-2,-2);

		PolygonDecoration decoration = new PolygonDecoration();
		decoration.setBackgroundColor(ColorConstants.white);
		decoration.setTemplate(decorationPointList);

		connectionFigure.setTargetDecoration(decoration);
	}

	private void createImplementsConnection(Class<?> sourceClass, Class<?> destinationClass) {
		GraphConnection connection = new GraphConnection(graph, ZestStyles.NONE, nodesMapper.get(sourceClass), nodesMapper.get(destinationClass));
		connection.setLineColor(ColorConstants.black);
		connection.setHighlightColor(ColorConstants.darkGreen);

		if(!PolylineConnection.class.isAssignableFrom(connection.getConnectionFigure().getClass())) return;

		PolylineConnection connectionFigure = (PolylineConnection)connection.getConnectionFigure();

		PointList decorationPointList = new PointList();
		decorationPointList.addPoint(-2,2);
		decorationPointList.addPoint(0,0);
		decorationPointList.addPoint(-2,-2);

		PolygonDecoration decoration = new PolygonDecoration();
		decoration.setBackgroundColor(ColorConstants.white);
		decoration.setTemplate(decorationPointList);

		connectionFigure.setTargetDecoration(decoration);

		connectionFigure.setLineStyle(Graphics.LINE_DASH);
	}

	private void setAssociationConnections(Class<?> clazz, Set<Class<?>> clazzSet) {
		for(Field field: clazz.getDeclaredFields()) {

			if(field.isSynthetic()) continue;

			Class<?> fieldType = field.getType();

			if(Collection.class.isAssignableFrom(fieldType)) { //Collection field
				Type genericFieldType = field.getGenericType();

				if(!ParameterizedType.class.isAssignableFrom(genericFieldType.getClass())) continue; //If the collection does not have a parametrized type

				Type aux = ((ParameterizedType)genericFieldType).getActualTypeArguments()[0];

				if(!Class.class.isAssignableFrom(aux.getClass())) continue; //If the parametrized type is not a class type, i.e., Collection<Collection<Type>>

				Class<?> actualFieldType = (Class<?>)aux;

				if(actualFieldType.isArray()) continue; //If the parametrized type is an array

				if(clazzSet.contains(actualFieldType)) {
					createMultipleConnection(clazz, actualFieldType, field);
				}
			}
			else if(fieldType.isArray()) { //Array field
				Class<?> componentFieldType = fieldType.getComponentType();

				if(componentFieldType.isArray()) continue; //If the component type of the array is another array

				if(clazzSet.contains(componentFieldType)) {
					createMultipleConnection(clazz, componentFieldType, field);
				}
			}
			else { //Class field
				if(clazzSet.contains(fieldType)) {
					createSimpleConnection(clazz, fieldType, field);
				}
			}

		}
	}

	private void createSimpleConnection(Class<?> sourceClass, Class<?> destinationClass, Field sourceField) {
		//if(sourceClass.equals(destinationClass)) return;

		GraphConnection connection = new GraphConnection(graph, ZestStyles.NONE, nodesMapper.get(sourceClass), nodesMapper.get(destinationClass));
		connection.setLineColor(ColorConstants.black);
		connection.setHighlightColor(ColorConstants.darkGreen);

		if(!PolylineConnection.class.isAssignableFrom(connection.getConnectionFigure().getClass())) return;

		PolylineConnection connectionFigure = (PolylineConnection)connection.getConnectionFigure();

		PointList decorationPointList = new PointList();
		decorationPointList.addPoint(-2,2);
		decorationPointList.addPoint(0,0);
		decorationPointList.addPoint(-2,-2);

		PolylineDecoration decoration = new PolylineDecoration();
		decoration.setTemplate(decorationPointList);

		connectionFigure.setTargetDecoration(decoration);

		ConnectionEndpointLocator targetEndpointLocator = new ConnectionEndpointLocator(connectionFigure, true);
		targetEndpointLocator.setVDistance(15);
		Label targetMultiplicityLabel = new Label(sourceField.getName());

		connectionFigure.add(targetMultiplicityLabel, targetEndpointLocator);
	}

	private void createMultipleConnection(Class<?> sourceClass, Class<?> destinationClass, Field sourceField) {
		//if(sourceClass.equals(destinationClass)) return;

		GraphConnection connection = new GraphConnection(graph, ZestStyles.NONE, nodesMapper.get(sourceClass), nodesMapper.get(destinationClass));
		connection.setLineColor(ColorConstants.black);
		connection.setHighlightColor(ColorConstants.red);

		if(!PolylineConnection.class.isAssignableFrom(connection.getConnectionFigure().getClass())) return;

		PolylineConnection connectionFigure = (PolylineConnection)connection.getConnectionFigure();

		PointList decorationPointList = new PointList();
		decorationPointList.addPoint(0,0);
		decorationPointList.addPoint(-2,2);
		decorationPointList.addPoint(-4,0);
		decorationPointList.addPoint(-2,-2);

		PolygonDecoration decoration = new PolygonDecoration();
		decoration.setBackgroundColor(ColorConstants.white);
		decoration.setTemplate(decorationPointList);

		connectionFigure.setSourceDecoration(decoration);

		ConnectionEndpointLocator targetEndpointLocator = new ConnectionEndpointLocator(connectionFigure, true);
		targetEndpointLocator.setVDistance(15);
		Label targetMultiplicityLabel = new Label(sourceField.getName());

		connectionFigure.add(targetMultiplicityLabel, targetEndpointLocator);
	}

	private void setLayout() {
		graph.setLayoutAlgorithm(new SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
	}
	//######################################################
	//######################################################

	//------------------------------------------------------
	//------------------------------------------------------
	//					SERVICE CLEAR
	//------------------------------------------------------
	//------------------------------------------------------
	public void clear() {
		if(SHOW_REPORTS) {
			System.out.println("SERVICE CLEAR");
		}

		graph.getDisplay().syncExec(new ClearAgent()); //Agent will call clearAction
	}

	private void clearAction() {
		Object[] connections = graph.getConnections().toArray();
		for(int i = 0; i < connections.length; i++) { //Clear connections
			GraphConnection conn = (GraphConnection)connections[i];
			conn.dispose();
		}

		Object[] nodes = graph.getNodes().toArray();
		for(int i = 0; i < nodes.length; i++) { //Clear nodes
			UMLNode node = (UMLNode)nodes[i];
			node.dispose();
		}

		nodesMapper.clear(); //Clear mapper
	}
	//######################################################
	//######################################################

	//------------------------------------------------------
	//------------------------------------------------------
	//					INNER CLASSES
	//------------------------------------------------------
	//------------------------------------------------------
	private class ShowFragmentAgent implements Runnable {

		private Map<Class<?>, Collection<Method>> fragment_classes;

		private ShowFragmentAgent(Map<Class<?>, Collection<Method>> fragment_classes) {
			this.fragment_classes = fragment_classes;
		}

		public void run() {
			showFragmentAction(fragment_classes);
		}

	}

	private class ClearAgent implements Runnable {

		public void run() {
			clearAction();
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
	//######################################################
	//######################################################


	private HashSet<ClickHandler> listeners = new HashSet<ClickHandler>();

	public void addClickHandler(ClickHandler handler) {
		listeners.add(handler);
	}

	public void removeClickHandler(ClickHandler handler) {
		listeners.remove(handler);
	}




}