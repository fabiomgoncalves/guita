package org.eclipselabs.guita.ipreviews.handler;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.InfixExpression;

public class InfixVisitor extends ASTVisitor {

	InfixExpression node;

	@Override
	public boolean visit(InfixExpression node) {
		if(this.node == null){
			this.node = node;
		}
		return super.visit(node);
	}
	
	public InfixExpression getNode() {
		return node;
	}

}
