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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class UMLClassFigure extends Figure {
	
	private static final Color CLASS_COLOR = new Color(null, 255, 255, 206);
	private static final Font CLASS_FONT = new Font(null, "Arial", 12, SWT.BOLD);
	private static final Image CLASS_IMG = new Image(Display.getDefault(), UMLClassFigure.class.getResourceAsStream("images/class_obj.gif"));
	private static final Image PUBLIC_IMG = new Image(Display.getDefault(), UMLClassFigure.class.getResourceAsStream("images/methpub_obj.gif"));
	private static final Image PRIVATE_IMG = new Image(Display.getDefault(), UMLClassFigure.class.getResourceAsStream("images/field_private_obj.gif"));

	private CompartmentFigure attributeFigure = new CompartmentFigure();
	private CompartmentFigure methodFigure = new CompartmentFigure();
	
	protected UMLClassFigure(String className) {
		ToolbarLayout layout = new ToolbarLayout();
		setLayoutManager(layout);
		setBorder(new LineBorder(ColorConstants.black, 1));
		setBackgroundColor(CLASS_COLOR);
		setOpaque(true);
		
		Label name_label = new Label(className, CLASS_IMG);
		name_label.setFont(CLASS_FONT);
		
		add(name_label);
		add(attributeFigure);
		add(methodFigure);
		
		Label attribute1 = new Label("attribute", PRIVATE_IMG);
		Label method1 = new Label("method()", PUBLIC_IMG);
		getAttributesCompartment().add(attribute1);
		getMethodsCompartment().add(method1);
		setSize(-1, -1);
	}

	private CompartmentFigure getAttributesCompartment() {
		return attributeFigure;
	}

	private CompartmentFigure getMethodsCompartment() {
		return methodFigure;
	}
}
