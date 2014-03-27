package org.eclipselabs.guita.ipreviews.handler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;

public class NewStatementVisitor extends ASTVisitor{

	public List<ClassInstanceCreation> new_argument_classes = new ArrayList<ClassInstanceCreation>();


	@Override
	public boolean visit(ClassInstanceCreation node) {
		new_argument_classes.add(node);
		return super.visit(node);
	}

	public List<ClassInstanceCreation> getNew_argument_classes() {
		return new_argument_classes;
	}
}
