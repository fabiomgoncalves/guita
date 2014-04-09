package org.eclipselabs.guita.ipreviews.handler;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;

public class ArrayInitVisitor extends ASTVisitor {
	ArrayCreation node;
	@Override
	public boolean visit(ArrayCreation node) {
		System.out.println(node.dimensions());
		this.node = node;
		return super.visit(node);
	}
	
	@Override
	public boolean visit(ArrayInitializer node) {
		return super.visit(node);
	}

	public ArrayCreation getNode() {
		return node;
	}
}
