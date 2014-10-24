/*******************************************************************************
 * Copyright 2005-2007, CHISEL Group, University of Victoria, Victoria, BC,
 * Canada. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: The Chisel Group, University of Victoria
 ******************************************************************************/
//MODIFIED
package org.eclipselabs.jmodeldiagram.view;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipselabs.jmodeldiagram.DiagramListener;
import org.eclipselabs.jmodeldiagram.model.JClass;
import org.eclipselabs.jmodeldiagram.model.JOperation;
import org.eclipselabs.jmodeldiagram.model.JType;

public class UMLClassFigure extends Figure {

	private static final Color CLASS_COLOR = new Color(null, 255, 255, 255);
	private static final Font CLASS_FONT = new Font(null, "Arial", 12, SWT.BOLD);
	private static final Font ABSTRACT_CLASS_FONT = new Font(null, "Arial", 12, SWT.BOLD | SWT.ITALIC);
	private static final Font INTERFACE_DESC_FONT = new Font(null, "Arial", 12, SWT.NONE);
	private static final Font METHOD_FONT = new Font(null, "Arial", 10, SWT.NONE);
	private static final Font ABSTRACT_METHOD_FONT = new Font(null, "Arial", 10, SWT.ITALIC);
	private static final Color TOOLTIP_COLOR = new Color(null, 255, 255, 206);
	private static final Font TOOLTIP_FONT = new Font(null, "Arial", 11, SWT.NONE);

	private CompartmentFigure methodsCompartment;

	private JType type;
	private Set<JOperation> operations;
	
	private Map<Label, JOperation> operationLabels;
	

	protected UMLClassFigure(JType type) {
		ToolbarLayout layout = new ToolbarLayout();
		setLayoutManager(layout);

		setBorder(new LineBorder(ColorConstants.black, 1));
		setBackgroundColor(CLASS_COLOR);
		setOpaque(true);

		addNameLabel(type);

		methodsCompartment = new CompartmentFigure();
		add(methodsCompartment);

		setSize();

		this.type = type;
		operations = new HashSet<JOperation>();
		operationLabels = new HashMap<Label, JOperation>();
	}

	public JType getJType() {
		return type;
	}

	
	public void select() {
		setBackgroundColor(TOOLTIP_COLOR);
	}
	
	public void unselect() {
		setBackgroundColor(CLASS_COLOR);
	}
	
	private void setSize() {
		setSize(-1, -1);
	}

	private CompartmentFigure getMethodsCompartment() {
		return this.methodsCompartment;
	}

	JType getType() {
		return type;
	}

	JOperation getOperation(Label label) {
		return operationLabels.get(label);
	}
	
	public Set<JOperation> getMethods() {
		return operations;
	}

	//------------------------------------------------------
	//------------------------------------------------------
	//					NAME SECTION
	//------------------------------------------------------
	//------------------------------------------------------
	private void addNameLabel(final JType type) {
		Font font = null;

		if(type.isInterface()) { //Interface
			Label interfaceLabel = createLabel("<<interface>>", INTERFACE_DESC_FONT);
			add(interfaceLabel);
			font = CLASS_FONT;
		}
		else if (type.isClass() && ((JClass) type).isAbstract()) { //Abstract
			font = ABSTRACT_CLASS_FONT;
		}
		else { //Class
			font = CLASS_FONT;
		}

		Label nameLabel = createLabel(type.getName(), font);
		Label toolTipLabel = createToolTipLabel(type.getQualifiedName());
		nameLabel.setBorder(new MarginBorder(3,10,3,10));
		nameLabel.setToolTip(toolTipLabel);
		nameLabel.setBackgroundColor(new Color(null, 255, 0, 0));
		add(nameLabel);
	}
	
	
	public void addOperations(Iterable<JOperation> operations) {
		for(JOperation o: operations) {
			if(!this.operations.contains(o)) {
				this.operations.add(o);
				addMethodLabel(o);
			}
		}
	}

	
	private void addMethodLabel(final JOperation operation) {
		String text = operation.getVisibility().symbol() + " " + operation.getName() + "()";

		Font font = null;

		if(operation.isAbstract()) { //Abstract
			font = ABSTRACT_METHOD_FONT;
		}
		else { //Method
			font = METHOD_FONT;
		}

		Label methodLabel = createLabel(text, font);
		methodLabel.setBorder(new MarginBorder(3));
		operationLabels.put(methodLabel, operation);
		
		String methodDesc = getMethodDescription(operation);
		Label toolTipLabel = createToolTipLabel(methodDesc);

		methodLabel.setToolTip(toolTipLabel);
//		methodLabel.addMouseListener(new MouseListener() {
//
//			@Override
//			public void mouseReleased(MouseEvent me) { }
//
//			@Override
//			public void mousePressed(MouseEvent me) { }
//
//			@Override
//			public void mouseDoubleClicked(MouseEvent me) {
//				for(DiagramListener h : listeners)
//					h.operationEvent(operation, Event.DOUBLE_CLICK);
//			}
//		});
		getMethodsCompartment().add(methodLabel);
	}

	private String getMethodDescription(JOperation operation) {
		StringBuilder builder = new StringBuilder();

		builder.append(operation.getName()).append("(TODO");

//		if(method.getParameterTypes().length > 0) {
//
//			for(Class<?> parameterType: method.getParameterTypes()) {
//				builder.append(parameterType.getCanonicalName()).append(", ");
//			}
//
//			builder.delete((builder.length()-2), builder.length());
//		}
//
//		builder.append(") : ");
//		builder.append(method.getReturnType().getCanonicalName());

		return builder.toString();
	}
	//######################################################
	//######################################################

	//------------------------------------------------------
	//------------------------------------------------------
	//					COMMON METHODS
	//------------------------------------------------------
	//------------------------------------------------------
	private Label createLabel(String text, Font font) {
		Label label = new Label(text);
		label.setFont(font);
		return label;
	}

	private Label createToolTipLabel(String text) {
		Label toolTipLabel = createLabel(text, TOOLTIP_FONT);
		toolTipLabel.setBackgroundColor(TOOLTIP_COLOR);
		toolTipLabel.setOpaque(true);
		return toolTipLabel;
	}
	//######################################################
	//######################################################

	
}