package org.eclipselabs.guita.ipreviews.handler;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class MethodInvocationVisitor extends ASTVisitor {
	
	MethodInvocation node;
	
	@Override
	public boolean visit(MethodInvocation node) {
		if(this.node == null)
			this.node = node;
		return super.visit(node);
	}
	
	public MethodInvocation getNode() {
		return node;
	}
}
