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
package pt.iscte.dcti.umlviewer.view;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

public class UMLClassFigure extends Figure {

	private static final boolean SHOW_REPORTS = false;

	private static final Color CLASS_COLOR = new Color(null, 255, 255, 206);
	private static final Font CLASS_FONT = new Font(null, "Arial", 12, SWT.BOLD);
	private static final Font ABSTRACT_CLASS_FONT = new Font(null, "Arial", 12, SWT.BOLD | SWT.ITALIC);
	private static final Font INTERFACE_DESC_FONT = new Font(null, "Arial", 12, SWT.NONE);
	private static final Font METHOD_FONT = new Font(null, "Arial", 10, SWT.NONE);
	private static final Font ABSTRACT_METHOD_FONT = new Font(null, "Arial", 10, SWT.ITALIC);
	private static final Color TOOLTIP_COLOR = new Color(null, 255, 255, 206);
	private static final Font TOOLTIP_FONT = new Font(null, "Arial", 11, SWT.NONE);

	private CompartmentFigure methodsCompartment;

	private Class<?> clazz;
	private Set<Method> methods;

	protected UMLClassFigure(Class<?> clazz) {
		ToolbarLayout layout = new ToolbarLayout();
		setLayoutManager(layout);

		setBorder(new LineBorder(ColorConstants.black, 1));
		setBackgroundColor(CLASS_COLOR);
		setOpaque(true);

		addNameLabel(clazz);

		methodsCompartment = new CompartmentFigure();
		add(methodsCompartment);

		setSize();

		this.clazz = clazz;
		methods = new HashSet<Method>();
	}

	private void setSize() {
		setSize(-1, -1);
	}

	private CompartmentFigure getMethodsCompartment() {
		return this.methodsCompartment;
	}

	public Class<?> getClazz() {
		return this.clazz;
	}

	public Set<Method> getMethods() {
		return this.methods;
	}

	//------------------------------------------------------
	//------------------------------------------------------
	//					NAME SECTION
	//------------------------------------------------------
	//------------------------------------------------------
	private void addNameLabel(Class<?> clazz) {
		Font font = null;

		if(Modifier.isInterface(clazz.getModifiers())) { //Interface
			Label interfaceLabel = createLabel("<<interface>>", INTERFACE_DESC_FONT);
			add(interfaceLabel);
			font = CLASS_FONT;

			if(SHOW_REPORTS) {
				System.out.println(clazz.getSimpleName() + " IS INTERFACE");
			}
		}
		else if (Modifier.isAbstract(clazz.getModifiers())) { //Abstract
			font = ABSTRACT_CLASS_FONT;

			if(SHOW_REPORTS) {
				System.out.println(clazz.getSimpleName() + " IS ABSTRACT");
			}
		}
		else { //Class
			font = CLASS_FONT;

			if(SHOW_REPORTS) {
				System.out.println(clazz.getSimpleName() + " IS CLASS");
			}
		}

		Label nameLabel = createLabel(clazz.getSimpleName(), font);
		Label toolTipLabel = createToolTipLabel(clazz.getCanonicalName());
		nameLabel.setToolTip(toolTipLabel);
		add(nameLabel);
	}
	//######################################################
	//######################################################

	//------------------------------------------------------
	//------------------------------------------------------
	//					METHODS SECTION
	//------------------------------------------------------
	//------------------------------------------------------
	public void addMethods(Collection<Method> methodsToAdd) {
		for(Method method: methodsToAdd) {

			if(methods.contains(method)) continue;

			addMethodLabel(method);
			methods.add(method);

			if(SHOW_REPORTS) {
				System.out.println("METHOD " + method.getName() + " ADDED TO " + clazz.getSimpleName());
			}
		}

		//getMethodsCompartment().mymethod();
		//setSize();
	}

	private void addMethodLabel(Method method) {
		String aux = method.getName() + "()";

		if(Modifier.isProtected(method.getModifiers())) { //Protected
			aux = "# " + aux;
		}
		else if(Modifier.isPrivate(method.getModifiers())) { //Private
			aux = "- " + aux;
		}
		else { //Public
			aux = "+ " + aux;
		}

		Font font = null;

		if(Modifier.isAbstract(method.getModifiers())) { //Abstract
			font = ABSTRACT_METHOD_FONT;
		}
		else { //Method
			font = METHOD_FONT;
		}

		Label methodLabel = createLabel(aux, font);

		String methodDesc = getMethodDescription(method);
		Label toolTipLabel = createToolTipLabel(methodDesc);

		methodLabel.setToolTip(toolTipLabel);

		getMethodsCompartment().add(methodLabel);
	}

	private String getMethodDescription(Method method) {
		StringBuilder builder = new StringBuilder();

		builder.append(method.getName()).append("(");

		if(method.getParameterTypes().length > 0) {

			for(Class<?> parameterType: method.getParameterTypes()) {
				builder.append(parameterType.getCanonicalName()).append(", ");
			}

			builder.delete((builder.length()-2), builder.length());
		}

		builder.append(") : ");
		builder.append(method.getReturnType().getCanonicalName());

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