package org.eclipselabs.guita.ipreviews.handler;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;

public class VariableDeclarationStatementVisitor extends ASTVisitor {
	
	ClassInstanceCreation node;

	@Override
	public boolean visit(ClassInstanceCreation node) {
		if(this.node == null)
			this.node = node;
		return super.visit(node);
	}

	public ClassInstanceCreation getNode() {
		return node;
	}
}
