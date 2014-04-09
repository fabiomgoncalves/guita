package org.eclipselabs.guita.ipreviews.handler;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;

public class ArrayAccessVisitor extends ASTVisitor{
	private ArrayAccess node;
	
	@Override
	public boolean visit(ArrayAccess node) {
		if(this.node == null)
			this.node = node;
		return super.visit(node);
	}
	
	public ArrayAccess getNode() {
		return node;
	}
}
