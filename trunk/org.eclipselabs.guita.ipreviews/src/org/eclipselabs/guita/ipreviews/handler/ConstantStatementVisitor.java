package org.eclipselabs.guita.ipreviews.handler;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.eclipselabs.guita.ipreviews.regex.Regex;

public class ConstantStatementVisitor extends ASTVisitor {
	
	QualifiedName object = null;
	HashMap<String, Object> variables = new HashMap<String, Object>();
	HashMap<String, Widget> widgets = new HashMap<String, Widget>();
	HashMap<String, Control> controls = new java.util.HashMap<String, Control>();
	HashMap<String, Class<?>> variables_classes = new HashMap<String, Class<?>>();
	HashMap<String, Class<?>> widgets_classes = new HashMap<String, Class<?>>();
	HashMap<String, Class<?>> controls_classes = new HashMap<String, Class<?>>();

	@Override
	public boolean visit(QualifiedName node) {
		object = node;
		return super.visit(node);
	}
	
	public QualifiedName getQualifiedName() {
		return object;
	}
}
