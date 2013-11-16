package pt.iscte.dcti.umlviewer.view;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.IContainer;
import org.eclipse.zest.core.widgets.ZestStyles;

import pt.iscte.dcti.umlviewer.service.Service_v2;

public class UMLViewer_v2 extends ViewPart implements Service_v2 {
	
	private static UMLViewer_v2 instance = null;
	
	public static UMLViewer_v2 getInstance() {
		return instance;
	}
	
	private Composite composite;
	private Graph graph;
	
	public UMLViewer_v2() {
		instance = this;
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
