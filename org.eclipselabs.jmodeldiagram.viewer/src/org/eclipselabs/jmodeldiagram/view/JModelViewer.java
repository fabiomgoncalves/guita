package org.eclipselabs.jmodeldiagram.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionEndpointLocator;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.IContainer;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.DirectedGraphLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.HorizontalLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.RadialLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.VerticalLayoutAlgorithm;
import org.eclipselabs.jmodeldiagram.DiagramListener;
import org.eclipselabs.jmodeldiagram.DiagramListener.Event;
import org.eclipselabs.jmodeldiagram.model.Association;
import org.eclipselabs.jmodeldiagram.model.JClass;
import org.eclipselabs.jmodeldiagram.model.JInterface;
import org.eclipselabs.jmodeldiagram.model.JModel;
import org.eclipselabs.jmodeldiagram.model.JOperation;
import org.eclipselabs.jmodeldiagram.model.JType;

public class JModelViewer extends ViewPart {

	private static JModelViewer instance;

	public static JModelViewer getInstance() {
		return instance;
	}

	private Composite composite;
	private Graph graph;

	private LayoutMode layoutMode;

	private Map<JType, UMLNode> nodesMapper;


	private HashSet<DiagramListener> listeners = new HashSet<DiagramListener>();

	public void addClickHandler(DiagramListener handler) {
		listeners.add(handler);
	}

	public void removeClickHandler(DiagramListener handler) {
		listeners.remove(handler);
	}
	
	public JModelViewer() {
		instance = this;
		nodesMapper = new HashMap<JType, UMLNode>();
		layoutMode = LayoutMode.Spring;
	}

	public void createPartControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());
		graph = new Graph(composite, SWT.NONE);
		graph.setBackground(new Color(null, 250,250,250));
		createPopupMenu();

//		graph.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				System.out.println("selected " + e);
//			}
//		});
		
		graph.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				handleEvent(e, Event.SELECT);
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				handleEvent(e, Event.DOUBLE_CLICK);
			}

			private void handleEvent(MouseEvent e, Event event) {
				IFigure fig = graph.getFigureAt(e.x, e.y);
				if(fig instanceof UMLClassFigure) {
					UMLClassFigure classFig = (UMLClassFigure) fig;
					IFigure underFig = classFig.findFigureAt(e.x, e.y);
					if(underFig instanceof Label) {
						JOperation op = classFig.getOperation((Label) underFig);
						if(op != null) {
							for(DiagramListener l : listeners)
								l.operationEvent(op, event);
						}
						else {
							JType type = classFig.getJType();
							for(DiagramListener l : listeners)
								l.classEvent(type, event);
						}
					}
				}
			}
		});
	}

	private void createPopupMenu() {
		Menu menu = new Menu(graph);
		MenuItem layoutItem = new MenuItem(menu, SWT.CASCADE);
		layoutItem.setText("Layout");
		Menu layoutMenu = new Menu(layoutItem);
		for(LayoutMode mode : LayoutMode.values())
			mode.createMenuItem(layoutMenu, graph, this);
		layoutItem.setMenu(layoutMenu);

		graph.setMenu(menu);
	}

	public void setFocus() {
		graph.setFocus();
	}

	public void displayModel(JModel model, boolean incremental) {
		if(!incremental)
			clear();

		if(model.hasDescription())
			graph.setToolTipText(model.getDescription());

		graph.getDisplay().syncExec(new ShowFragmentAgent(model)); //Agent will call showFragmentAction
	}

	private void showFragmentAction(Iterable<JType> types) {
		Set<JType> newTypes = new HashSet<JType>(); //A set with all added classes

		for(JType type : types) {
			UMLNode node = nodesMapper.get(type);

			if(node == null) {
				node = createNode(type);
				newTypes.add(type);
			}
			((UMLClassFigure)node.getNodeFigure()).addOperations(type);
		}

		setConnections(nodesMapper.keySet(), newTypes);

		layoutMode.layout(graph);
	}




	private UMLNode createNode(JType type) {
		UMLClassFigure figure = new UMLClassFigure(type);
		UMLNode node = new UMLNode(graph, SWT.NONE, figure);
		nodesMapper.put(type, node);
		return node;
	}


	private void setConnections(Iterable<JType> previousTypes, Collection<JType> newTypes) {
		for(JType prev : previousTypes) { //Sets connections between the previous classes and the added classes
			if(prev.isClass()) {
				setInheritanceConnectionsClass((JClass) prev, newTypes);
				setAssociations((JClass) prev, newTypes);
			}
			else
				setInheritanceConnectionsInterface((JInterface) prev, newTypes);
		}

		for(JType neww : newTypes) { //Sets connections between the added classes and all the classes
			if(neww.isClass()) {
				setInheritanceConnectionsClass((JClass) neww, nodesMapper.keySet());
				setAssociations((JClass) neww, nodesMapper.keySet());
			}
			else
				setInheritanceConnectionsInterface((JInterface) neww, nodesMapper.keySet());
		}



	}

	private void setInheritanceConnectionsClass(JClass jc, Collection<JType> set) {
		JClass superClass = jc.getSuperclass(); //extends
		if(superClass != null && set.contains(superClass))
			createExtendsConnection(jc, superClass);

		for(JInterface ji : jc.getInterfaces()) {
			if(set.contains(ji))
				createImplementsConnection(jc, ji);
		}
	}

	private void setInheritanceConnectionsInterface(JInterface ji, Collection<JType> set) {
		for(JInterface i : ji.getSuperInterfaces()) {
			if(set.contains(i))
				createExtendsConnection(ji, i);
		}
	}

	private void setAssociations(JClass jc, Collection<JType> set) {
		for(Association a : jc.getAssociations()) {
			if(a.isUnary())
				createSimpleConnection(jc, a.getTarget(), a.getName());
			else
				createMultipleConnection(jc, a.getTarget(), a.getName());
		}
	}

	private void createExtendsConnection(JType source, JType target) {
		GraphConnection connection = new GraphConnection(graph, ZestStyles.NONE, nodesMapper.get(source), nodesMapper.get(target));
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

	private void createImplementsConnection(JType source, JType target) {
		GraphConnection connection = new GraphConnection(graph, ZestStyles.NONE, nodesMapper.get(source), nodesMapper.get(target));
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

	

	private void createSimpleConnection(JClass source, JType target, String name) {
		GraphConnection connection = new GraphConnection(graph, ZestStyles.NONE, nodesMapper.get(source), nodesMapper.get(target));
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
		Label targetMultiplicityLabel = new Label(name);

		connectionFigure.add(targetMultiplicityLabel, targetEndpointLocator);
	}

	private void createMultipleConnection(JClass source, JType target, String name) {
		GraphConnection connection = new GraphConnection(graph, ZestStyles.NONE, nodesMapper.get(source), nodesMapper.get(target));
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
		Label targetMultiplicityLabel = new Label(name);

		connectionFigure.add(targetMultiplicityLabel, targetEndpointLocator);
	}

	public void clear() {
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
			GraphNode node = (GraphNode)nodes[i];
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

		private Iterable<JType> types;

		private ShowFragmentAgent(Iterable<JType> types) {
			this.types = types;
		}

		public void run() {
			showFragmentAction(types);
		}

	}

	private class ClearAgent implements Runnable {

		public void run() {
			clearAction();
		}

	}

	private class UMLNode extends GraphNode {

		private UMLClassFigure figure;

		public UMLNode(IContainer graphModel, int style, UMLClassFigure figure) {
			super(graphModel, style, figure);
			this.figure = figure;
		}

		protected IFigure createFigureForModel() {
			return (IFigure) this.getData();
		}
		@Override
		public void highlight() {
			figure.select();
		}


		@Override
		public void unhighlight() {
			figure.unselect();
		}

	}

	









	private enum LayoutMode {
		Spring {
			@Override
			LayoutAlgorithm layoutAlgorithm() {

				return new SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
			}
		},
		Radial {
			@Override
			LayoutAlgorithm layoutAlgorithm() {
				return new RadialLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
			}
		},
		DirectedGraph {
			@Override
			LayoutAlgorithm layoutAlgorithm() {
				return new DirectedGraphLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
			}
		},
		HorizontalLayout {
			@Override
			LayoutAlgorithm layoutAlgorithm() {
				return new HorizontalLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
			}
		},
		VerticalLayout {
			@Override
			LayoutAlgorithm layoutAlgorithm() {
				return new VerticalLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
			}
		},
		TreeLayout {
			@Override
			LayoutAlgorithm layoutAlgorithm() {
				return new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
			}
		};

		abstract LayoutAlgorithm layoutAlgorithm();

		private MenuItem item;

		void createMenuItem(Menu parent, final Graph graph, final JModelViewer viewer) {
			item = new MenuItem(parent, SWT.CHECK);
			item.setText(name());
			if(ordinal() == 0)
				item.setSelection(true);
			item.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					viewer.layoutMode = LayoutMode.this;
					graph.setLayoutAlgorithm(layoutAlgorithm(), true);
					for(LayoutMode mode : values())
						if(mode.item != item) {
							mode.item.setSelection(false);
						}
				}
			});
		}

		void layout(Graph graph) {
			graph.setLayoutAlgorithm(layoutAlgorithm(), true);
		}
	}









}